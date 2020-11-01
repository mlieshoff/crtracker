package crtracker.checks;

import static java.lang.Math.max;
import static java.util.Arrays.asList;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.mili.utils.sql.service.ServiceFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import crtracker.Config;
import crtracker.Utils;
import crtracker.job.AbstractJob;
import crtracker.persistency.Role;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;
import crtracker.service.FtpService;
import crtracker.service.MessageService;
import lombok.Data;

public class WebsiteGenerator extends AbstractJob {

  private static final DecimalFormat format = new DecimalFormat("#,###,###,##0");

  private final Config config;

  private final FtpService ftpService = ServiceFactory.getService(FtpService.class);

  private final MessageService messageService = ServiceFactory.getService(MessageService.class);

  private final MeasureDao measureDao = new MeasureDao();

  private final String clanTag;

  public WebsiteGenerator(Config config) {
    this.config = config;
    clanTag = config.getConfig().getProperty("crtracker.clan.tag");
  }

  @Override
  protected void runIntern() throws Exception {
    Session session = config.createSession();
    Transaction transaction = session.beginTransaction();
    try {
      TextMeasure members = measureDao.getCurrentTextMeasure(session, CrTrackerTypes.CLAN_MEMBERS, clanTag);
      Map<String, String> tag2Name = resolveMemberTags(session, asList(members.getValue().split(",")));
      List<HighscoreEntry> model = new ArrayList<>();
      List<HighscoreEntry> tournamentModel = new ArrayList<>();
      Pair<DateTime, DateTime> calendarWeek = Utils.getCalendarWeekFromTo(new Date());
      for (Map.Entry<String, String> entry : tag2Name.entrySet()) {
        StringMeasure idMeasure = measureDao.getCurrentStringMeasure(session, CrTrackerTypes.ID, entry.getKey());
        NumberMeasure
            donationMeasure =
            measureDao
                .getLastNumberMeasure(session, CrTrackerTypes.MEMBER_DONATIONS, entry.getKey(), calendarWeek.getLeft(),
                    calendarWeek.getRight());
        NumberMeasure
            fameMeasure =
            measureDao.getLastNumberMeasure(session, CrTrackerTypes.MEMBER_RIVER_WARS_FAME, entry.getKey(),
                calendarWeek.getLeft(), calendarWeek.getRight());
        NumberMeasure
            repairPointsMeasure =
            measureDao.getLastNumberMeasure(session, CrTrackerTypes.MEMBER_RIVER_WARS_SHIP_REPAIRPOINTS, entry.getKey(),
                calendarWeek.getLeft(), calendarWeek.getRight());
        NumberMeasure
            roleMeasure =
            measureDao.getCurrentNumberMeasure(session, CrTrackerTypes.MEMBER_ROLE, entry.getKey());
        NumberMeasure
            tournamentMeasure =
            measureDao.getCurrentNumberMeasure(session, CrTrackerTypes.INTERN_TOURNAMENT, entry.getKey());

        Date joiningDate = idMeasure.getMeasureId().getModifiedAt();

        Role role = Role.MEMBER;
        if (roleMeasure != null) {
          role = Role.forCode(roleMeasure.getValue().intValue());
        }

        long donations = 0;
        if (donationMeasure != null) {
          donations = donationMeasure.getValue();
        }

        long fame = 0;
        if (fameMeasure != null) {
          fame = fameMeasure.getValue();
        }

        long repairPoints = 0;
        if (repairPointsMeasure != null) {
          repairPoints = repairPointsMeasure.getValue();
        }

        long tournamentCrowns = tournamentMeasure != null ? tournamentMeasure.getValue() : 0;

        model.add(
            new HighscoreEntry(entry.getKey(), entry.getValue(), donations, fame, repairPoints, role, joiningDate));
        tournamentModel
            .add(new HighscoreEntry(entry.getKey(), entry.getValue(), tournamentCrowns, 0, 0, role, joiningDate));
      }
      model.sort((o1, o2) -> Long.compare(o2.getSortNumber(), o1.getSortNumber()));
      rankThem(model);

      tournamentModel.sort((o1, o2) -> Long.compare(o2.getDonations(), o1.getDonations()));
      rankThem(tournamentModel);

      String website = generateSite(model, tournamentModel);
      ftpService.upload(
          config.getConfig().getProperty("ftp.server.url"),
          Integer.valueOf(config.getConfig().getProperty("ftp.server.port")),
          config.getCredentials().getProperty("ftp.server.username"),
          config.getCredentials().getProperty("ftp.server.password"),
          config.getConfig().getProperty("ftp.server.folder") + "/index.html",
          website
      );
      transaction.commit();
      messageService.sendAlert(config, "Website generated and uploaded.");
    } catch (Exception e) {
      e.printStackTrace();
      if (transaction != null) {
        transaction.rollback();
      }
      messageService.sendAlert(config, "Error while generate website: " + e.getMessage());
    } finally {
      session.close();
    }
  }

  public void rankThem(List<HighscoreEntry> list) {
    int visual = 1;
    HighscoreEntry oldUserData = null;
    for (HighscoreEntry userData : list) {
      if (oldUserData != null && oldUserData.getSortNumber() != userData.getSortNumber()) {
        visual++;
      }
      userData.setRank(visual);
      oldUserData = userData;
    }
  }

  @Data
  public static class HighscoreEntry {
    private final String memberTag;
    private final String memberName;
    private final long donations;
    private final long fame;
    private final long repairPoints;
    private final Role role;
    private final Date joiningDate;
    private int rank;

    long getSortNumber() {
      return max(max(donations, fame), repairPoints);
    }
  }

  private String generateSite(List<HighscoreEntry> model, List<HighscoreEntry> tournamentModel) {
    StringBuilder s1 = new StringBuilder();
    s1.append("<table class=\"table table-inverse table-striped\">");
    s1.append("<thead>");
    s1.append("<tr>");
    s1.append("<th>");
    s1.append("#");
    s1.append("</th>");
    s1.append("<th>");
    s1.append("Nick");
    s1.append("</th>");
    s1.append("<th>");
    s1.append("Kronen");
    s1.append("</th>");
    s1.append("</tr>");
    s1.append("</thead>");
    s1.append("<tbody>");
    for (HighscoreEntry highscoreEntry : tournamentModel) {
      s1.append("<tr>");
      s1.append("<th scope=\"row\">");
      s1.append(highscoreEntry.getRank());
      s1.append(".</th>");
      s1.append("<td>");
      s1.append(String.format("<a class=\"h4\" href=\"https://spy.deckshop.pro/player/%s\">%s</a>",
          highscoreEntry.getMemberTag().replace("#", ""), highscoreEntry.getMemberName()));
      s1.append("</td>");
      s1.append("<td>");
      s1.append(format.format(highscoreEntry.getDonations()));
      s1.append("</td>");
      s1.append("</tr>");
    }
    s1.append("</tbody>");
    s1.append("</table>");

    StringBuilder s = new StringBuilder();
    s.append("<table class=\"table table-inverse table-striped\">");
    s.append("<thead>");
    s.append("<tr>");
    s.append("<th>");
    s.append("#");
    s.append("</th>");
    s.append("<th>");
    s.append("Nick");
    s.append("</th>");
    s.append("<th>");
    s.append("Spenden");
    s.append("</th>");
    s.append("<th>");
    s.append("Ruhm");
    s.append("</th>");
    s.append("<th>");
    s.append("Reparatur");
    s.append("</th>");
    s.append("<th>");
    s.append("Action");
    s.append("</th>");
    s.append("</tr>");
    s.append("</thead>");
    s.append("<tbody>");
    for (HighscoreEntry highscoreEntry : model) {
      s.append("<tr>");
      s.append("<th scope=\"row\">");
      s.append(highscoreEntry.getRank());
      s.append(".</th>");
      s.append("<td>");
      s.append(String.format("<a class=\"h4\" href=\"https://spy.deckshop.pro/player/%s\">%s</a>",
          highscoreEntry.getMemberTag().replace("#", ""), highscoreEntry.getMemberName()));
      s.append("</td>");
      s.append("<td>");
      s.append(format.format(highscoreEntry.getDonations()));
      s.append("</td>");
      s.append("<td>");
      s.append(format.format(highscoreEntry.getFame()));
      s.append("</td>");
      s.append("<td>");
      s.append(format.format(highscoreEntry.getRepairPoints()));
      s.append("</td>");
      s.append("<td>");
      s.append(action(highscoreEntry));
      s.append("</td>");
      s.append("</tr>");
    }
    s.append("</tbody>");
    s.append("</table>");

    String template = "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "  <head>\n" +
        "    <!-- Required meta tags -->\n" +
        "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
        "\n" +
        "    <!-- Bootstrap CSS -->\n" +
        "    <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css\" integrity=\"sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M\" crossorigin=\"anonymous\">\n"
        +
        "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://s.unicode-table.com/css/font-awesome.min.css?fe256\" /><link href=\"https://fonts.googleapis.com/css?family=Cuprum:400,700\" rel=\"stylesheet\">\n"
        +
        "  </head>\n" +
        "  <body>\n" +
        "    <h1 align=\"center\">Royal Card Forces</h1>\n" +
        "        <div class=\"container bg-dark text-muted text-center pt-3 pb-4\">\n" +
        "    <h2 align=\"center\">Willkommen in unserem Clash Royal Clan!</h2>\n" +
        "    <h3 align=\"center\">Melde Dich an zum <a href=\"http://gg.gg/rcfdiscord\">Chat</a>!</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Völlig unkompliziert und anonym, per Discord, und ideal zur Kommunikation.\n" +
        "        </p>\n" +
        "    <h3 align=\"center\">Was ist das wöchentliche Ziel?</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Von Sonntag bis Sonntag: Spende mindestens 400 Karten ODER erreiche im Clankrieg in der Summe von Ruhm- und Reparaturpunkten mindestens 500 Punkte.\n"
        +
        "        </p>\n" +
        "    <h3 align=\"center\">Wie werde ich Ältester?</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Erfülle das wöchentliche Ziel.\n" +
        "        </p>\n" +
        "    <h3 align=\"center\">Wie werde ich Vize?</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Sei lange aktiv im Clan, mache mit, bringe Dich im Discord mit ein!\n" +
        "        </p>\n" +
        "    <h3 align=\"center\">Wann startet der Clankrieg?</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Der Clankrieg läuft immer. Repariere oder kämpfe so oft es Dir möglich ist.\n" +
        "        </p>\n" +
        "    <h3 align=\"center\">Warum wurde ich gekickt?</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Entweder bist Du unangenehn im Chat aufgefallen, oder Du bist Mitglied und hast das wöchentliche Ziel nicht erreicht, oder Du warst mehr als 5 Tage offline!\n"
        +
        "        </p>\n" +
        "    <h3 align=\"center\">Warum wurde ich degradiert?</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Entweder bist Du unangenehn im Chat aufgefallen oder Du hast das wöchentliche Ziel nicht erreicht.\n"
        +
        "        </p>\n" +
        "    <h3 align=\"center\">Wer darf kicken?</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Bei uns dürfen nur die Vize kicken! Eine Ausnahme besteht, wenn kein Vize da ist und jemand ausklinkt im Chat.\n"
        +
        "        </p>\n" +
        "    <h3 align=\"center\">Was ist die Clan-Liga?</h3>\n" +
        "        <p class=\"small\">\n" +
        "            Sammle Kronen im Solo-Testspiel gegen Deine Mates, um in der Clan-Liga aufzusteigen.\n" +
        "        </p>\n" +
        "        </div>\n" +
        "    <h3 align=\"center\">Clan-Liga</h3>\n" +
        "%s" +
        "    <h3 align=\"center\">Wochen-Highscore vom (%s)</h3>\n" +
        "%s" +
        "<hr/><p/>%s" +
        "\n" +
        "    <!-- Optional JavaScript -->\n" +
        "    <!-- jQuery first, then Popper.js, then Bootstrap JS -->\n" +
        "    <script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\" integrity=\"sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN\" crossorigin=\"anonymous\"></script>\n"
        +
        "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.11.0/umd/popper.min.js\" integrity=\"sha384-b/U6ypiBEHpOf/4+1nzFpr53nxSS+GLCkfwBdFNTxtclqqenISfwAzpKaMNFNmj4\" crossorigin=\"anonymous\"></script>\n"
        +
        "    <script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/js/bootstrap.min.js\" integrity=\"sha384-h0AbiXch4ZDo7tp9hKZ4TsHbi047NrKGLO3SEJAg45jXxnGIfYzk4Si90RDIqNm1\" crossorigin=\"anonymous\"></script>\n"
        +
        "  </body>\n" +
        "</html>";
    String footer = "<footer class=\"bg-dark text-muted text-center pt-3 pb-4\">\n" +
        "        <div class=\"container\">\n" +
        "\n" +
        "\n" +
        "            <p class=\"mb-3 mt-3\">\n" +
        "                Made with passion and ♥ by micah" +
        "            </p>\n" +
        "\n" +
        "            <p class=\"small\">\n" +
        "                This content is not affiliated with, endorsed, sponsored, or specifically approved by Supercell and Supercell is not responsible for it.\n"
        +
        "                <br class=\"d-none d-lg-block\">\n" +
        "                For more information see <a href=\"http://www.supercell.com/fan-content-policy\" target=\"_blank\" rel=\"nofollow\" class=\"text-white\">Supercell’s Fan Content Policy</a>.\n"
        +
        "            </p>\n" +
        "        </div>\n" +
        "    </footer>";
    return String.format(template, s1, new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), s, footer);
  }

  private String action(HighscoreEntry highscoreEntry) {
    boolean kick = true;
    Date oneWeekBefore = new DateTime().minusDays(7).toDate();
    Date joiningDate = highscoreEntry.getJoiningDate();
    String action = "<FONT COLOR=\"#00FF00\">WEEKLY COMPLETED</FONT>";
    if (!joiningDate.after(oneWeekBefore)) {
      long cwPoints = highscoreEntry.getFame() + highscoreEntry.getRepairPoints();
      boolean promotion = false;
      if (cwPoints >= 500 || highscoreEntry.getDonations() >= 400) {
        kick = false;
        promotion = true;
      }
      if (kick) {
        if (highscoreEntry.getRole() == Role.MEMBER) {
          return "<FONT COLOR=\"#FF0000\">KICK</FONT>";
        } else {
          return "<FONT COLOR=\"#FF0000\">DEGRADE</FONT>";
        }
      } else {
        if (promotion && highscoreEntry.getRole() == Role.MEMBER) {
          return "<FONT COLOR=\"#00FF00\">PROMOTE</FONT>";
        }
      }
    } else {
      return "<FONT COLOR=\"#00FF00\">*PROSPECT*</FONT>";
    }
    return action;
  }

  @Override
  public long getTimeout() {
    return 60000 * 60;
  }

  private Map<String, String> resolveMemberTags(Session session, Collection<String> memberTags) {
    Map<String, String> tag2Name = new TreeMap<>();
    for (String memberTag : memberTags) {
      StringMeasure name = measureDao.getCurrentStringMeasure(session, CrTrackerTypes.MEMBER_NICK, memberTag);
      tag2Name.put(memberTag, name.getValue());
    }
    return tag2Name;
  }

}

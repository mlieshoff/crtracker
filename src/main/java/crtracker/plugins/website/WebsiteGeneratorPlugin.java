package crtracker.plugins.website;

import static java.lang.Math.max;
import static java.util.Arrays.asList;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import crtracker.persistency.Role;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugins.messaging.AlertPluginEvent;
import crtracker.service.FtpService;
import crtracker.util.CipherUtil;
import lombok.Data;

@Service
public class WebsiteGeneratorPlugin extends AbstractPlugin {

  private static final DecimalFormat format = new DecimalFormat("#,###,###,##0");

  @Autowired private FtpService ftpService;

  @Autowired private MeasureDao measureDao;

  @Scheduled(initialDelay = 60000, fixedDelay = 60000 * 60)
  public void run() {
    super.run();
  }

  @Override
  public void runIntern(Session session) throws Exception {
    TextMeasure members =
        measureDao.getCurrentTextMeasure(
            session, CrTrackerTypes.CLAN_MEMBERS, configurationService.getClanTag());
    Map<String, String> tag2Name =
        resolveMemberTags(session, asList(members.getValue().split(",")));
    List<HighscoreEntry> weeklyModel = new ArrayList<>();
    List<HighscoreEntry> tournamentModel = new ArrayList<>();
    List<HighscoreEntry> warModel = new ArrayList<>();
    List<HighscoreEntry> goblinRoadModel = new ArrayList<>();
    Pair<DateTime, DateTime> calendarWeek = CipherUtil.getCalendarWeekFromTo(new Date());
    for (Map.Entry<String, String> entry : tag2Name.entrySet()) {
      StringMeasure idMeasure =
          measureDao.getCurrentStringMeasure(session, CrTrackerTypes.ID, entry.getKey());
      NumberMeasure donationMeasure =
          measureDao.getLastNumberMeasure(
              session,
              CrTrackerTypes.MEMBER_DONATIONS,
              entry.getKey(),
              calendarWeek.getLeft(),
              calendarWeek.getRight());
      NumberMeasure fameMeasure =
          measureDao.getLastNumberMeasure(
              session,
              CrTrackerTypes.MEMBER_RIVER_WARS_FAME,
              entry.getKey(),
              calendarWeek.getLeft(),
              calendarWeek.getRight());
      NumberMeasure repairPointsMeasure =
          measureDao.getLastNumberMeasure(
              session,
              CrTrackerTypes.MEMBER_RIVER_WARS_SHIP_REPAIRPOINTS,
              entry.getKey(),
              calendarWeek.getLeft(),
              calendarWeek.getRight());
      NumberMeasure roleMeasure =
          measureDao.getCurrentNumberMeasure(session, CrTrackerTypes.MEMBER_ROLE, entry.getKey());
      NumberMeasure tournamentMeasure =
          measureDao.getCurrentNumberMeasure(
              session, CrTrackerTypes.INTERN_TOURNAMENT, entry.getKey());
      NumberMeasure warMeasure =
          measureDao.getCurrentNumberMeasure(
              session, CrTrackerTypes.MEMBER_LAST_10_RIVER_WARS_FAME, entry.getKey());
      NumberMeasure goblinRoadCurrentTrophiesMeasure =
          measureDao.getCurrentNumberMeasure(
              session, CrTrackerTypes.GOBLIN_ROAD_CURRENT_TROPHIES, entry.getKey());

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

      long warScore = warMeasure != null ? warMeasure.getValue() : 0;

      long goblinCurrentTrophies =
          goblinRoadCurrentTrophiesMeasure != null
              ? goblinRoadCurrentTrophiesMeasure.getValue()
              : 0;

      weeklyModel.add(
          new HighscoreEntry(
              entry.getKey(), entry.getValue(), donations, fame, repairPoints, role, joiningDate));
      tournamentModel.add(
          new HighscoreEntry(
              entry.getKey(), entry.getValue(), tournamentCrowns, 0, 0, role, joiningDate));
      warModel.add(
          new HighscoreEntry(entry.getKey(), entry.getValue(), warScore, 0, 0, role, joiningDate));
      goblinRoadModel.add(
          new HighscoreEntry(
              entry.getKey(), entry.getValue(), goblinCurrentTrophies, 0, 0, role, joiningDate));
    }

    weeklyModel.sort((o1, o2) -> Long.compare(o2.getGreatestValue(), o1.getGreatestValue()));
    rankThem(session, CrTrackerTypes.HIGHSCORE_RANKING_WEEKLY, weeklyModel);

    tournamentModel.sort((o1, o2) -> Long.compare(o2.getValue1(), o1.getValue1()));
    rankThem(session, CrTrackerTypes.HIGHSCORE_RANKING_TOURNAMENT, tournamentModel);

    warModel.sort((o1, o2) -> Long.compare(o2.getValue1(), o1.getValue1()));
    rankThem(session, CrTrackerTypes.HIGHSCORE_RANKING_WAR, warModel);

    goblinRoadModel.sort((o1, o2) -> Long.compare(o2.getValue1(), o1.getValue1()));
    rankThem(session, CrTrackerTypes.HIGHSCORE_RANKING_GOBLIN_ROAD_CURRENT, goblinRoadModel);

    String website = generateSite(weeklyModel, tournamentModel, warModel, goblinRoadModel);

    ftpService.upload(
        configurationService.getConfig().getProperty("ftp.server.url"),
        Integer.parseInt(configurationService.getConfig().getProperty("ftp.server.port")),
        configurationService.getCredentials().getProperty("ftp.server.username"),
        configurationService.getCredentials().getProperty("ftp.server.password"),
        configurationService.getConfig().getProperty("ftp.server.folder") + "/index.html",
        website);
    eventBus.fire(new AlertPluginEvent("Website generated and uploaded."));
  }

  public void rankThem(Session session, CrTrackerTypes crTrackerTypes, List<HighscoreEntry> list) {
    Map<String, Long> oldRankings =
        loadRanks(
            session,
            crTrackerTypes,
            list.stream()
                .map(highscoreEntry -> highscoreEntry.memberTag)
                .collect(Collectors.toList()));
    int visual = 1;
    HighscoreEntry oldUserData = null;
    for (HighscoreEntry userData : list) {
      if (oldUserData != null && oldUserData.getGreatestValue() != userData.getGreatestValue()) {
        visual++;
      }
      userData.setRank(visual);
      Long oldRank = oldRankings.get(userData.memberTag);
      if (oldRank != null) {
        if (oldRank < userData.getRank()) {
          userData.setRankChange(-1);
        } else if (oldRank > userData.getRank()) {
          userData.setRankChange(1);
        }
      }
      oldUserData = userData;
    }
    saveRanks(session, crTrackerTypes, list);
  }

  private Map<String, Long> loadRanks(
          Session session, CrTrackerTypes crTrackerTypes, List<String> playerTags) {
    Map<String, Long> map = new HashMap<>();
    for (String playerTag : playerTags) {
      NumberMeasure measureRank =
              measureDao.getCurrentNumberMeasure(session, crTrackerTypes, playerTag);
      if (measureRank != null) {
        map.put(playerTag, measureRank.getValue());
      }
    }
    return map;
  }

  private void saveRanks(
          Session session, CrTrackerTypes crTrackerTypes, List<HighscoreEntry> model) {
    for (HighscoreEntry highscoreEntry : model) {
      measureDao.updateNumberMeasure(
              session, highscoreEntry.memberTag, crTrackerTypes.getCode(), highscoreEntry.rank);
    }
  }

  @Data
  public static class HighscoreEntry {
    private final String memberTag;
    private final String memberName;
    private final long value1;
    private final long value2;
    private final long value3;
    private final Role role;
    private final Date joiningDate;
    private int rank;
    private int rankChange;

    long getGreatestValue() {
      return max(max(value1, value2), value3);
    }
  }

  private String generateSite(
      List<HighscoreEntry> weeklyModel,
      List<HighscoreEntry> tournamentModel,
      List<HighscoreEntry> warModel,
      List<HighscoreEntry> goblinRoadModel) {
    String template =
        "<!DOCTYPE html>\n"
            + "<html lang=\"en\">\n"
            + "  <head>\n"
            + "    <!-- Required meta tags -->\n"
            + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
            + "    <meta charset=\"UTF-8\">\n"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n"
            + "\n"
            + "    <!-- Bootstrap CSS -->\n"
            + "    <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css\" integrity=\"sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M\" crossorigin=\"anonymous\">\n"
            + "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://s.unicode-table.com/css/font-awesome.min.css?fe256\" /><link href=\"https://fonts.googleapis.com/css?family=Cuprum:400,700\" rel=\"stylesheet\">\n"
            + "  </head>\n"
            + "  <body>\n"
            + "    <h1 align=\"center\">Royal Card Forces</h1>\n"
            + "        <div class=\"container bg-dark text-muted text-center pt-3 pb-4\">\n"
            + "    <h2 align=\"center\">Willkommen in unserem Clash Royal Clan!</h2>\n"
            + "    <h3 align=\"center\">Melde Dich an zum <a href=\"https://gg.gg/rcfdiscord\">Chat</a>!</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Völlig unkompliziert und anonym, per Discord, und ideal zur Kommunikation.\n"
            + "        </p>\n"
            + "    <h3 align=\"center\">Wir sind ein Kriegsclan!</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Das bedeutet der Fokus liegt auf bei uns auf Beteiligung am Clankrieg.\n"
            + "        </p>\n"
            + "    <h3 align=\"center\">Achte auf Deine Schiffsverteidigung!</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Starte jeden Krieg mit 3 Verteidigungsdecks!\n"
            + "        </p>\n"
            + "    <h3 align=\"center\">Repariere das Schiff!</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Qualmt und brennt das Schiff, dann repariere!\n"
            + "        </p>\n"
            + "    <h3 align=\"center\">Wie werde ich Ältester?</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Erreiche mindestens 1200 Punkte im Clankrieg.\n"
            + "        </p>\n"
            + "    <h3 align=\"center\">Wie werde ich Vize?</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Sei lange aktiv im Clan, mache mit, bringe Dich im Discord mit ein!\n"
            + "        </p>\n"
            + "    <h3 align=\"center\">Warum wurde ich degradiert/gekickt?</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Entweder bist Du unangenehn im Chat aufgefallen, oder Du hast Dich nicht am Krieg beteiligt, oder Du warst mehr als 5 Tage offline.\n"
            + "        </p>\n"
            + "    <h3 align=\"center\">Wer darf kicken?</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Bei uns dürfen nur die Vize kicken! Eine Ausnahme besteht, wenn kein Vize da ist und jemand ausklinkt im Chat.\n"
            + "        </p>\n"
            + "    <h3 align=\"center\">Was ist die Clan-Liga?</h3>\n"
            + "        <p class=\"small\">\n"
            + "            Sammle Kronen im Solo-Testspiel gegen Deine Mates, um in der Clan-Liga aufzusteigen.\n"
            + "        </p>\n"
            + "        </div>\n"
            + "    <h3 align=\"center\">Aktuelle Goblin Road</h3>\n"
            + "%s"
            + "    <h3 align=\"center\">Clan-Liga</h3>\n"
            + "%s"
            + "    <h3 align=\"center\">Wochen-Highscore vom (%s)</h3>\n"
            + "%s"
            + "    <h3 align=\"center\">Kriegs-Highscore</h3>\n"
            + "%s"
            + "<hr/><p/>%s"
            + "\n"
            + "    <!-- Optional JavaScript -->\n"
            + "    <!-- jQuery first, then Popper.js, then Bootstrap JS -->\n"
            + "    <script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\" integrity=\"sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN\" crossorigin=\"anonymous\"></script>\n"
            + "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.11.0/umd/popper.min.js\" integrity=\"sha384-b/U6ypiBEHpOf/4+1nzFpr53nxSS+GLCkfwBdFNTxtclqqenISfwAzpKaMNFNmj4\" crossorigin=\"anonymous\"></script>\n"
            + "    <script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/js/bootstrap.min.js\" integrity=\"sha384-h0AbiXch4ZDo7tp9hKZ4TsHbi047NrKGLO3SEJAg45jXxnGIfYzk4Si90RDIqNm1\" crossorigin=\"anonymous\"></script>\n"
            + "  </body>\n"
            + "</html>";
    return String.format(
        template,
        generateOneValueTable("Troph&auml;en", goblinRoadModel),
        generateOneValueTable("Kronen", tournamentModel),
        new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()),
        generateWeeklyTable(weeklyModel),
        generateOneValueTable("Kriegsruhm", warModel),
        generateFooter());
  }

  private String generateFooter() {
    return "<footer class=\"bg-dark text-muted text-center pt-3 pb-4\">\n"
        + "        <div class=\"container\">\n"
        + "\n"
        + "\n"
        + "            <p class=\"mb-3 mt-3\">\n"
        + "                Made with passion and ♥ by micah"
        + "            </p>\n"
        + "\n"
        + "            <p class=\"small\">\n"
        + "                This content is not affiliated with, endorsed, sponsored, or specifically approved by Supercell and Supercell is not responsible for it.\n"
        + "                <br class=\"d-none d-lg-block\">\n"
        + "                For more information see <a href=\"https://www.supercell.com/fan-content-policy\" target=\"_blank\" rel=\"nofollow\" class=\"text-white\">Supercell’s Fan Content Policy</a>.\n"
        + "            </p>\n"
        + "        </div>\n"
        + "    </footer>";
  }

  private String generateOneValueTable(String valueName, List<HighscoreEntry> model) {
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
    s.append(valueName);
    s.append("</th>");
    s.append("</tr>");
    s.append("</thead>");
    s.append("<tbody>");
    for (HighscoreEntry highscoreEntry : model) {
      s.append("<tr>");
      s.append("<th scope=\"row\">");
      s.append(highscoreEntry.getRank());
      s.append(".");
      s.append(getRankingSign(highscoreEntry));
      s.append("</th>");
      s.append("<td>");
      s.append(
          String.format(
              "<a class=\"h4\" href=\"https://spy.deckshop.pro/player/%s\">%s</a>",
              highscoreEntry.getMemberTag().replace("#", ""), highscoreEntry.getMemberName()));
      s.append("</td>");
      s.append("<td>");
      s.append(format.format(highscoreEntry.getValue1()));
      s.append("</td>");
      s.append("</tr>");
    }
    s.append("</tbody>");
    s.append("</table>");
    return s.toString();
  }

  private String generateWeeklyTable(List<HighscoreEntry> weeklyModel) {
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
    for (HighscoreEntry highscoreEntry : weeklyModel) {
      s.append("<tr>");
      s.append("<th scope=\"row\">");
      s.append(highscoreEntry.getRank());
      s.append(".");
      s.append(getRankingSign(highscoreEntry));
      s.append("</th>");
      s.append("<td>");
      s.append(
          String.format(
              "<a class=\"h4\" href=\"https://spy.deckshop.pro/player/%s\">%s</a>",
              highscoreEntry.getMemberTag().replace("#", ""), highscoreEntry.getMemberName()));
      s.append("</td>");
      s.append("<td>");
      s.append(format.format(highscoreEntry.getValue1()));
      s.append("</td>");
      s.append("<td>");
      s.append(format.format(highscoreEntry.getValue2()));
      s.append("</td>");
      s.append("<td>");
      s.append(format.format(highscoreEntry.getValue3()));
      s.append("</td>");
      s.append("<td>");
      s.append(action(highscoreEntry));
      s.append("</td>");
      s.append("</tr>");
    }
    s.append("</tbody>");
    s.append("</table>");
    return s.toString();
  }

  private String getRankingSign(HighscoreEntry highscoreEntry) {
    if (highscoreEntry.getRankChange() > 0) {
      return "<FONT COLOR=\"#00FF00\">&uarr;</FONT>";
    } else if (highscoreEntry.getRankChange() < 0) {
      return "<FONT COLOR=\"#FF0000\">&darr;</FONT>";
    }
    return "";
  }

  private String action(HighscoreEntry highscoreEntry) {
    boolean kick = true;
    Date oneWeekBefore = new DateTime().minusDays(7).toDate();
    Date joiningDate = highscoreEntry.getJoiningDate();
    String action = "<FONT COLOR=\"#00FF00\">WEEKLY COMPLETED</FONT>";
    if (joiningDate.after(oneWeekBefore)) {
      return "<FONT COLOR=\"#00FF00\">*PROSPECT*</FONT>";
    } else {
      long cwPoints = highscoreEntry.getValue2() + highscoreEntry.getValue3();
      boolean promotion = false;
      if (cwPoints >= 1200) {
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
    }
    return action;
  }

  private Map<String, String> resolveMemberTags(Session session, Collection<String> memberTags) {
    Map<String, String> tag2Name = new TreeMap<>();
    for (String memberTag : memberTags) {
      StringMeasure name =
          measureDao.getCurrentStringMeasure(session, CrTrackerTypes.MEMBER_NICK, memberTag);
      tag2Name.put(memberTag, name.getValue());
    }
    return tag2Name;
  }
}

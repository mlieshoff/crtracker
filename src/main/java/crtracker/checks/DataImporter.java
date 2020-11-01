package crtracker.checks;

import static java.util.Arrays.asList;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mili.utils.sql.service.ServiceFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import crtracker.Config;
import crtracker.api.ApiWrapper;
import crtracker.job.AbstractJob;
import crtracker.persistency.Role;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;
import crtracker.persistency.model.TextMeasure;
import crtracker.service.MessageService;
import jcrapi2.model.ClanMember;
import jcrapi2.model.CurrentClanRiverRaceClanParticipant;
import jcrapi2.model.PlayerBattleLog;
import jcrapi2.response.GetClanResponse;
import jcrapi2.response.GetCurrentClanRiverRaceResponse;
import jcrapi2.response.GetPlayerBattleLogResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataImporter extends AbstractJob {

  private final static SimpleDateFormat BATTLE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

  private final MessageService messageService = ServiceFactory.getService(MessageService.class);

  private final Config config;

  private final ApiWrapper apiWrapper;

  private final String clanTag;

  private final MeasureDao measureDao = new MeasureDao();

  public DataImporter(Config config) throws Exception {
    this.config = config;
    clanTag = config.getConfig().getProperty("crtracker.clan.tag");
    apiWrapper = config.createApiWrapper();
  }

  @Override
  public long getTimeout() {
    return 60000;
  }

  @Override
  protected void runIntern() throws Exception {
    Session session = config.createSession();
    Transaction transaction = session.beginTransaction();
    try {
      doImport(session);
      transaction.commit();
    } catch (Exception e) {
      log.error("Error while importing data", e);
      if (transaction != null) {
        transaction.rollback();
      }
      messageService.sendAlert(config, "Error while importing data: " + e.getMessage());
    } finally {
      session.close();
    }
  }

  private void doImport(Session session) {
    importClan(session);
  }

  private void importClan(Session session) {
    importClan(session, apiWrapper.getClanData(clanTag));
    importRiverRace(session, apiWrapper.getCurrentClanRiverRace(clanTag));
  }

  private void importClan(Session session, GetClanResponse getClanResponse) {
    TextMeasure oldMembers = measureDao
        .updateTextMeasure(session, getClanResponse.getTag(), CrTrackerTypes.CLAN_MEMBERS.getCode(),
            getClanMemberTags(getClanResponse));
    importMembers(session, getClanResponse);
    if (oldMembers != null) {
      Set<String> old = new TreeSet<>();
      old.addAll(asList(oldMembers.getValue().split(",")));

      Set<String> current = new TreeSet<>();
      current.addAll(asList(getClanMemberTags(getClanResponse).split(",")));

      if (!current.equals(old)) {
        Collection<String> newMembers = CollectionUtils.subtract(current, old);
        Collection<String> leftMembers = CollectionUtils.subtract(old, current);
        if (newMembers.size() > 0) {
          messageService.sendWelcome(config,
              "Willkommen im Clan! Bitte begrüßt die folgenden Neuankömmlinge im Clan-Chat:\n"
                  + StringUtils.join(resolveMemberTags(session, newMembers), "\n"));
        }
        if (leftMembers.size() > 0) {
          messageService.sendAlert(config, "Folgende Member haben den Clan verlassen:\n" + StringUtils
              .join(resolveMemberTags(session, leftMembers), "\n"));
        }
      }
    }
  }

  private Collection<String> resolveMemberTags(Session session, Collection<String> memberTags) {
    Collection<String> names = new TreeSet<>();
    for (String memberTag : memberTags) {
      log.info("try to resolve tag: {}", memberTag);
      StringMeasure name = measureDao.getCurrentStringMeasure(session, CrTrackerTypes.MEMBER_NICK, memberTag);
      if (name != null) {
        names.add(name.getValue());
      } else {
        names.add(memberTag + " (Springer)");
        log.warn("something weird while resolving name for tag: " + memberTag);
      }
    }
    return names;
  }

  private void importMembers(Session session, GetClanResponse getClanResponse) {
    for (ClanMember clanMember : getClanResponse.getMemberList()) {
      measureDao.updateNumberMeasure(session, clanMember.getTag(), CrTrackerTypes.MEMBER_DONATIONS.getCode(),
          clanMember.getDonations());
      measureDao.updateNumberMeasure(session, clanMember.getTag(), CrTrackerTypes.MEMBER_ROLE.getCode(),
          Role.forName(clanMember.getRole()).getCode());
      measureDao.updateStringMeasure(session, clanMember.getTag(), CrTrackerTypes.MEMBER_NICK.getCode(),
          clanMember.getName());
      importBattles(session, getClanResponse, clanMember);
    }
  }

  private static String getClanMemberTags(GetClanResponse getClanResponse) {
    Set<String> clanMemberTags = new TreeSet<>();
    for (ClanMember clanMember : getClanResponse.getMemberList()) {
      clanMemberTags.add(clanMember.getTag());
    }
    return StringUtils.join(clanMemberTags, ",");
  }

  private void importBattles(Session session, GetClanResponse getClanResponse, ClanMember clanMember) {
    String playerTag = clanMember.getTag();
    NumberMeasure lastLigaBattleIdMeasure = measureDao
        .getCurrentNumberMeasure(session, CrTrackerTypes.MEMBER_LAST_TIME_LIGA_BATTLE, playerTag);
    long lastLigaBattleTimeMillis = lastLigaBattleIdMeasure != null ? lastLigaBattleIdMeasure.getValue() : 0;
    NumberMeasure lastBattleIdMeasure = measureDao
        .getCurrentNumberMeasure(session, CrTrackerTypes.MEMBER_LAST_TIME_BATTLE, playerTag);
    long lastBattleTimeMillis = lastBattleIdMeasure != null ? lastBattleIdMeasure.getValue() : 0;
    GetPlayerBattleLogResponse getPlayerBattleLogResponse = apiWrapper.getBattleLogFor(playerTag);
    for (PlayerBattleLog playerBattleLog : getPlayerBattleLogResponse) {
      try {
        Date battleTime = BATTLE_TIME_FORMAT.parse(playerBattleLog.getBattleTime());
        long battleTimeMillis = battleTime.getTime();
        if ("clanmate".equalsIgnoreCase(playerBattleLog.getType())) {
          if (battleTimeMillis > lastLigaBattleTimeMillis) {
            lastLigaBattleTimeMillis = battleTimeMillis;
            int player1Crowns = playerBattleLog.getTeam().get(0).getCrowns();
            int player2Crowns = playerBattleLog.getOpponent().get(0).getCrowns();
            NumberMeasure
                ratingMeasure =
                measureDao.getCurrentNumberMeasure(session, CrTrackerTypes.INTERN_TOURNAMENT, playerTag);
            long rating = ratingMeasure != null ? ratingMeasure.getValue() : 0;
            if (player1Crowns > player2Crowns) {
              rating += player1Crowns;
            } else if (player1Crowns < player2Crowns) {
              rating += player2Crowns;
            } else {
              rating += player1Crowns;
            }
            measureDao.updateNumberMeasure(session, playerTag, CrTrackerTypes.INTERN_TOURNAMENT.getCode(),
                rating);
            measureDao.updateNumberMeasure(session, playerTag,
                CrTrackerTypes.MEMBER_LAST_TIME_LIGA_BATTLE.getCode(), lastLigaBattleTimeMillis);
            messageService.sendLiga(config, getClanResponse, playerBattleLog);
          }
        } else {
          if (battleTimeMillis > lastBattleTimeMillis) {
            lastBattleTimeMillis = battleTimeMillis;
            measureDao.updateNumberMeasure(session, playerTag, CrTrackerTypes.MEMBER_LAST_TIME_BATTLE.getCode(),
                lastBattleTimeMillis);
            messageService.sendLiveTicker(config, getClanResponse, playerBattleLog);
          }
        }
      } catch (ParseException e) {
        log.warn("error while parsing battle time", e);
      }
    }
  }

  private void importRiverRace(Session session, GetCurrentClanRiverRaceResponse getCurrentClanRiverRaceResponse) {
    for (CurrentClanRiverRaceClanParticipant currentClanRiverRaceClanParticipant : getCurrentClanRiverRaceResponse
        .getClan().getParticipants()) {
      String playerTag = currentClanRiverRaceClanParticipant.getTag();
      int fame = currentClanRiverRaceClanParticipant.getFame();
      int repairPoints = currentClanRiverRaceClanParticipant.getRepairPoints();
      measureDao.updateNumberMeasure(session, playerTag, CrTrackerTypes.MEMBER_RIVER_WARS_FAME.getCode(), fame);
      measureDao.updateNumberMeasure(session, playerTag, CrTrackerTypes.MEMBER_RIVER_WARS_SHIP_REPAIRPOINTS.getCode(),
          repairPoints);
    }
  }

}

package crtracker.plugins.liga;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.NumberMeasure;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import crtracker.plugins.battle.ClanMateBattlePluginEvent;
import jcrapi2.api.intern.players.battlelog.LogEntry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LigaPlugin extends AbstractPlugin<ClanMateBattlePluginEvent> {

  private static final SimpleDateFormat BATTLE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

  @Autowired
  private MeasureDao measureDao;

  @Override
  public void onPluginEvent(Session session, ClanMateBattlePluginEvent clanMateBattlePluginEvent) {
    String playerTag = clanMateBattlePluginEvent.getPlayerTag();
    NumberMeasure
        lastLigaBattleIdMeasure =
        measureDao.getCurrentNumberMeasure(session, CrTrackerTypes.MEMBER_LAST_TIME_LIGA_BATTLE, playerTag);
    long lastLigaBattleTimeMillis = lastLigaBattleIdMeasure != null ? lastLigaBattleIdMeasure.getValue() : 0;
    for (LogEntry logEntry : clanMateBattlePluginEvent.getLigaBattles()) {
      try {
        Date battleTime = BATTLE_TIME_FORMAT.parse(logEntry.getBattleTime());
        long battleTimeMillis = battleTime.getTime();
        if (battleTimeMillis > lastLigaBattleTimeMillis) {
          lastLigaBattleTimeMillis = battleTimeMillis;
          int player1Crowns = logEntry.getTeam().get(0).getCrowns();
          int player2Crowns = logEntry.getOpponent().get(0).getCrowns();
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
          pluginManager.fire(new LigaMessagePluginEvent(logEntry));
        }
      } catch (ParseException e) {
        log.warn("error while parsing battle time", e);
      }
    }
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof ClanMateBattlePluginEvent;
  }

}

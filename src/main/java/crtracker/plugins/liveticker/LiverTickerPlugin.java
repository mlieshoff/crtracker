package crtracker.plugins.liveticker;

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
import crtracker.plugins.battle.PvpBattlePluginEvent;
import jcrapi2.model.PlayerBattleLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LiverTickerPlugin extends AbstractPlugin<PvpBattlePluginEvent> {

  private static final SimpleDateFormat BATTLE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

  @Autowired
  private MeasureDao measureDao;

  @Override
  public void onPluginEvent(Session session, PvpBattlePluginEvent liverTickerPluginEvent) {
    String playerTag = liverTickerPluginEvent.getPlayerTag();
    NumberMeasure lastBattleIdMeasure = measureDao
        .getCurrentNumberMeasure(session, CrTrackerTypes.MEMBER_LAST_TIME_BATTLE, playerTag);
    long lastBattleTimeMillis = lastBattleIdMeasure != null ? lastBattleIdMeasure.getValue() : 0;
    for (PlayerBattleLog playerBattleLog : liverTickerPluginEvent.getLigaBattles()) {
      try {
        Date battleTime = BATTLE_TIME_FORMAT.parse(playerBattleLog.getBattleTime());
        long battleTimeMillis = battleTime.getTime();
        if (battleTimeMillis > lastBattleTimeMillis) {
          lastBattleTimeMillis = battleTimeMillis;
          measureDao.updateNumberMeasure(session, playerTag, CrTrackerTypes.MEMBER_LAST_TIME_BATTLE.getCode(),
              lastBattleTimeMillis);
          pluginManager.fire(new LiveTickerMessagePluginEvent(playerBattleLog));
        }
      } catch (ParseException e) {
        log.warn("error while parsing battle time", e);
      }
    }
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof PvpBattlePluginEvent;
  }

}

package crtracker.plugins.progress;

import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.players.info.GoblinRoad;
import jcrapi2.api.intern.players.info.Progress;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlayerProgressPlugin extends AbstractPlugin<PlayerProgressPluginEvent> {

  @Autowired private MeasureDao measureDao;

  @Override
  public void onPluginEvent(Session session, PlayerProgressPluginEvent playerProgressPluginEvent) {
    String playerTag = playerProgressPluginEvent.getPlayerTag();
    Progress progress = playerProgressPluginEvent.getProgress();
    GoblinRoad goblinRoad = progress.getGoblinRoad();
    int bestTrophies = goblinRoad.getBestTrophies();
    int currentTrophies = goblinRoad.getTrophies();
    measureDao.updateNumberMeasure(
        session, playerTag, CrTrackerTypes.GOBLIN_ROAD_BEST_TROPHIES.getCode(), bestTrophies);
    measureDao.updateNumberMeasure(
        session, playerTag, CrTrackerTypes.GOBLIN_ROAD_CURRENT_TROPHIES.getCode(), currentTrophies);
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof PlayerProgressPluginEvent;
  }
}

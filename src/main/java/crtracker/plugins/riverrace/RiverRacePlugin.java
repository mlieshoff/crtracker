package crtracker.plugins.riverrace;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.currentriverrace.CurrentRiverRaceResponse;
import jcrapi2.api.intern.clans.currentriverrace.Participant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RiverRacePlugin extends AbstractPlugin<RiverRacePluginEvent> {

  @Autowired
  private MeasureDao measureDao;

  @Override
  public void onPluginEvent(Session session, RiverRacePluginEvent riverRacePluginEvent) {
    CurrentRiverRaceResponse
        getCurrentClanRiverRaceResponse =
        riverRacePluginEvent.getCurrentRiverRaceResponse();
    for (Participant currentClanRiverRaceClanParticipant : getCurrentClanRiverRaceResponse.getClan()
        .getParticipants()) {
      String playerTag = currentClanRiverRaceClanParticipant.getTag();
      int fame = currentClanRiverRaceClanParticipant.getFame();
      int repairPoints = currentClanRiverRaceClanParticipant.getRepairPoints();
      measureDao.updateNumberMeasure(session, playerTag, CrTrackerTypes.MEMBER_RIVER_WARS_FAME.getCode(), fame);
      measureDao.updateNumberMeasure(session, playerTag, CrTrackerTypes.MEMBER_RIVER_WARS_SHIP_REPAIRPOINTS.getCode(),
          repairPoints);
    }
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof RiverRacePluginEvent;
  }

}

package crtracker.plugins.api;

import crtracker.plugins.progress.PlayerProgressPluginEvent;
import jcrapi2.api.intern.players.info.PlayerResponse;
import org.hibernate.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import crtracker.integration.ApiWrapper;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugins.battle.BattlePluginEvent;
import crtracker.plugins.fluctuation.FluctuationPluginEvent;
import crtracker.plugins.member.ClanMemberPluginEvent;
import crtracker.plugins.riverrace.RiverRaceLogPluginEvent;
import crtracker.plugins.riverrace.RiverRacePluginEvent;
import jcrapi2.api.intern.clans.info.ClanResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataPlugin extends AbstractPlugin<DataPluginEvent> {

  private ApiWrapper apiWrapper;

  @Scheduled(initialDelay = 60000, fixedDelay = 60000)
  public void run() {
    super.run();
  }

  @Override
  public void runIntern(Session session) {
    String clanTag = configurationService.getClanTag();
    apiWrapper = configurationService.createApiWrapper();
    ClanResponse clanResponse = apiWrapper.getClanData(clanTag);
    clanResponse
        .getMemberList()
        .forEach(
            member -> {
              String memberTag = member.getTag();
              eventBus.fire(new ClanMemberPluginEvent(member));
              eventBus.fire(new BattlePluginEvent(member, apiWrapper.getBattleLogFor(memberTag)));
              PlayerResponse playerResponse = apiWrapper.getPlayer(memberTag);
              eventBus.fire(
                  new PlayerProgressPluginEvent(
                      playerResponse.getTag(), playerResponse.getProgress()));
            });
    eventBus.fire(new RiverRacePluginEvent(apiWrapper.getCurrentClanRiverRace(clanTag)));
    eventBus.fire(new RiverRaceLogPluginEvent(apiWrapper.getRiverRaceLog(clanTag)));
    eventBus.fire(new FluctuationPluginEvent(clanResponse.getMemberList()));
  }
}

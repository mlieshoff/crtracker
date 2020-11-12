package crtracker.plugins.api;

import org.hibernate.Session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import crtracker.integration.ApiWrapper;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugins.battle.BattlePluginEvent;
import crtracker.plugins.clan.ClanPluginEvent;
import crtracker.plugins.member.ClanMemberPluginEvent;
import crtracker.plugins.riverrace.RiverRacePluginEvent;
import jcrapi2.model.ClanMember;
import jcrapi2.response.GetClanResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataPlugin extends AbstractPlugin {

  private ApiWrapper apiWrapper;

  @Scheduled(initialDelay = 60000, fixedDelay = 60000)
  public void run() {
    super.run();
  }

  @Override
  public void runIntern(Session session) {
    String clanTag = configurationService.getClanTag();
    apiWrapper = configurationService.createApiWrapper();
    GetClanResponse getClanResponse = apiWrapper.getClanData(clanTag);
    pluginManager.fire(new ClanPluginEvent(getClanResponse));
    for (ClanMember clanMember : getClanResponse.getMemberList()) {
      pluginManager.fire(new ClanMemberPluginEvent(clanMember));
      pluginManager.fire(new BattlePluginEvent(clanMember, apiWrapper.getBattleLogFor(clanMember.getTag())));
    }
    pluginManager.fire(new RiverRacePluginEvent(apiWrapper.getCurrentClanRiverRace(clanTag)));
  }

}

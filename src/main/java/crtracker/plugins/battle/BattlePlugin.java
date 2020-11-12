package crtracker.plugins.battle;

import org.hibernate.Session;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import jcrapi2.model.ClanMember;
import jcrapi2.model.PlayerBattleLog;
import jcrapi2.response.GetPlayerBattleLogResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BattlePlugin extends AbstractPlugin<BattlePluginEvent> {

  @Override
  public void onPluginEvent(Session session, BattlePluginEvent battlePluginEvent) {
    ClanMember clanMember = battlePluginEvent.getClanMember();
    String playerTag = clanMember.getTag();
    List<PlayerBattleLog> clanMateBattles = new ArrayList<>();
    List<PlayerBattleLog> pvpBattles = new ArrayList<>();
    GetPlayerBattleLogResponse getPlayerBattleLogResponse = battlePluginEvent.getGetPlayerBattleLogResponse();
    for (PlayerBattleLog playerBattleLog : getPlayerBattleLogResponse) {
      if ("clanmate".equalsIgnoreCase(playerBattleLog.getType())) {
        clanMateBattles.add(playerBattleLog);
      } else {
        pvpBattles.add(playerBattleLog);
      }
    }
    if (!clanMateBattles.isEmpty()) {
      pluginManager.fire(new ClanMateBattlePluginEvent(playerTag, clanMateBattles));
    }
    if (!pvpBattles.isEmpty()) {
      pluginManager.fire(new PvpBattlePluginEvent(playerTag, pvpBattles));
    }
  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return pluginEvent instanceof BattlePluginEvent;
  }

}

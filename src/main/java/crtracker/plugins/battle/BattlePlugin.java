package crtracker.plugins.battle;

import org.hibernate.Session;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.info.Member;
import jcrapi2.api.intern.players.battlelog.BattleLogResponse;
import jcrapi2.api.intern.players.battlelog.LogEntry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BattlePlugin extends AbstractPlugin<BattlePluginEvent> {

  @Override
  public void onPluginEvent(Session session, BattlePluginEvent battlePluginEvent) {
    Member member = battlePluginEvent.getMember();
    String playerTag = member.getTag();
    List<LogEntry> clanMateBattles = new ArrayList<>();
    List<LogEntry> pvpBattles = new ArrayList<>();
    BattleLogResponse getPlayerBattleLogResponse = battlePluginEvent.getBattleLogResponse();
    for (LogEntry logEntry : getPlayerBattleLogResponse) {
      if ("clanmate".equalsIgnoreCase(logEntry.getType())) {
        clanMateBattles.add(logEntry);
      } else {
        pvpBattles.add(logEntry);
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

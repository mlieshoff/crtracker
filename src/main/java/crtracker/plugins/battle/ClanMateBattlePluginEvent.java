package crtracker.plugins.battle;

import java.util.List;
import crtracker.plugin.PluginEvent;
import jcrapi2.model.PlayerBattleLog;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClanMateBattlePluginEvent implements PluginEvent {

  private final String playerTag;

  private final List<PlayerBattleLog> ligaBattles;

}

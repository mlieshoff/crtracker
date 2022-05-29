package crtracker.plugins.battle;

import java.util.List;
import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.players.battlelog.LogEntry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PvpBattlePluginEvent implements PluginEvent {

  private final String playerTag;

  private final List<LogEntry> ligaBattles;

}

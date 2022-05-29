package crtracker.plugins.battle;

import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.info.Member;
import jcrapi2.api.intern.players.battlelog.BattleLogResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BattlePluginEvent implements PluginEvent {

  private final Member member;

  private final BattleLogResponse battleLogResponse;

}

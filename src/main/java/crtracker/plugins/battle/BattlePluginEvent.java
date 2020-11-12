package crtracker.plugins.battle;

import crtracker.plugin.PluginEvent;
import jcrapi2.model.ClanMember;
import jcrapi2.response.GetPlayerBattleLogResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BattlePluginEvent implements PluginEvent {

  private final ClanMember clanMember;

  private final GetPlayerBattleLogResponse getPlayerBattleLogResponse;

}

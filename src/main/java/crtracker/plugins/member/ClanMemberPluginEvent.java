package crtracker.plugins.member;

import crtracker.plugin.PluginEvent;
import jcrapi2.model.ClanMember;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClanMemberPluginEvent implements PluginEvent {

  private final ClanMember clanMember;

}

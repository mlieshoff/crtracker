package crtracker.plugins.member;

import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.info.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClanMemberPluginEvent implements PluginEvent {

  private final Member member;

}

package crtracker.plugins.fluctuation;

import java.util.List;
import crtracker.plugin.PluginEvent;
import jcrapi2.model.ClanMember;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FluctuationPluginEvent implements PluginEvent {

  private final List<ClanMember> clanMembers;

}

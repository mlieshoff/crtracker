package crtracker.plugins.fluctuation;

import java.util.List;
import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.info.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FluctuationPluginEvent implements PluginEvent {

  private final List<Member> members;

}

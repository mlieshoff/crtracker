package crtracker.plugins.riverrace;

import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.riverracelog.RiverRaceLogResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RiverRaceLogPluginEvent implements PluginEvent {

  private final RiverRaceLogResponse riverRaceLogResponse;

}

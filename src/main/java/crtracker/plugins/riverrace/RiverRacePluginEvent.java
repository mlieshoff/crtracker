package crtracker.plugins.riverrace;

import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.currentriverrace.CurrentRiverRaceResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RiverRacePluginEvent implements PluginEvent {

  private final CurrentRiverRaceResponse currentRiverRaceResponse;

}

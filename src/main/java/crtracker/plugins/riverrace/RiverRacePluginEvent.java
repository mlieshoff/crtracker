package crtracker.plugins.riverrace;

import crtracker.plugin.PluginEvent;
import jcrapi2.response.GetCurrentClanRiverRaceResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RiverRacePluginEvent implements PluginEvent {

  private final GetCurrentClanRiverRaceResponse getCurrentClanRiverRaceResponse;

}

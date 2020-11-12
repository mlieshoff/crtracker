package crtracker.plugins.clan;

import crtracker.plugin.PluginEvent;
import jcrapi2.response.GetClanResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClanPluginEvent implements PluginEvent {

  private final GetClanResponse getClanResponse;

}

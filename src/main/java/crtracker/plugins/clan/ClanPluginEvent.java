package crtracker.plugins.clan;

import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.clans.info.ClanResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClanPluginEvent implements PluginEvent {

  private final ClanResponse clanResponse;

}

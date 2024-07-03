package crtracker.plugins.progress;

import crtracker.plugin.PluginEvent;
import jcrapi2.api.intern.players.info.Progress;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlayerProgressPluginEvent implements PluginEvent {

  private final String playerTag;

  private final Progress progress;

}

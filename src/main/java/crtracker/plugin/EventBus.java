package crtracker.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EventBus {

  @Autowired
  private Set<? extends PluginEventListener<?>> pluginEventListeners;

  public <T extends PluginEvent> void fire(T pluginEvent) {
    boolean consumed = false;
    for (PluginEventListener pluginEventListener : pluginEventListeners) {
      if (pluginEventListener.canHandlePluginEvent(pluginEvent)) {
        String pluginName = pluginEventListener.getClass().getSimpleName();
        String pluginEventName = pluginEvent.getClass().getSimpleName();
        try {
          log.info("plugin {} tries to handle event {}.", pluginName, pluginEventName);
          pluginEventListener.onPluginEvent(PluginContext.get(), pluginEvent);
          log.info("plugin {} handled event {}.", pluginName, pluginEventName);
          consumed = true;
        } catch (Exception e) {
          log.error("plugin {} throws exception while handling event {}.", pluginName, pluginEventName);
        }
      }
    }
    if (!consumed) {
      log.warn("event {} was not consumed.", pluginEvent.getClass().getSimpleName());
    }
  }

}

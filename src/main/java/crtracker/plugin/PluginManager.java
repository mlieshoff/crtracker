package crtracker.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PluginManager {

  @Autowired
  private Map<Class<? extends Plugin<?>>, Plugin<?>> plugins;

  public void onStart() {
    plugins.values().forEach(Plugin::onStart);
  }

  @PreDestroy
  public void onStop() {
    plugins.values().forEach(Plugin::onStop);
  }

  public void fire(PluginEvent pluginEvent) {
    boolean consumed = false;
    for (Plugin plugin : plugins.values()) {
      if (plugin.canHandlePluginEvent(pluginEvent)) {
        String pluginName = plugin.getClass().getSimpleName();
        String pluginEventName = pluginEvent.getClass().getSimpleName();
        try {
          log.info("plugin {} tries to handle event {}.", pluginName, pluginEventName);
          plugin.onPluginEvent(PluginContext.get(), pluginEvent);
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

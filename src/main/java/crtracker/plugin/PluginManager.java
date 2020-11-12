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
        consumed = true;
        log.info("plugin {} handles event {}.", plugin.getClass().getSimpleName(),
            pluginEvent.getClass().getSimpleName());
        plugin.onPluginEvent(PluginContext.get(), pluginEvent);
      }
    }
    if (!consumed) {
      log.warn("event {} was not consumed.", pluginEvent.getClass().getSimpleName());
    }
  }

}

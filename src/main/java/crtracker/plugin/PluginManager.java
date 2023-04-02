package crtracker.plugin;

import org.mili.utils.sql.service.MigrationService;
import org.mili.utils.sql.service.ServiceException;
import org.mili.utils.sql.service.ServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import crtracker.service.ConfigurationService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PluginManager {

  private static final String[] ARGS = {"../server_credentials/conf/crtracker/key",
      "../server/apps/crtracker/conf/config.properties",
      "../server_credentials/conf/crtracker/encrypted/test_credentials.properties",
      "/tmp/status.txt",
      "-Dtest=true"
  };

  public static final AtomicReference<String[]> ARGS_HOLDER = new AtomicReference<>();

  @Autowired
  private Map<Class<? extends Plugin<?>>, Plugin<?>> plugins;

  @Autowired
  private EventBus eventBus;

  @Autowired
  private ConfigurationService configurationService;

  @EventListener(ApplicationReadyEvent.class)
  public void onStart() throws Exception {
    String[] args = ARGS_HOLDER.get();
    if (args == null) {
      args = ARGS;
      ARGS_HOLDER.set(args);
    }
    configurationService.initialize(args[0], args[1], args[2]);
    try {
      ServiceFactory.getService(MigrationService.class).migrate(false, PluginManager.class);
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
//    eventBus.fire(new AlertPluginEvent("CrTracker starting..."));
    plugins.values().forEach(Plugin::onStart);
  }

  @PreDestroy
  public void onStop() {
    plugins.values().forEach(Plugin::onStop);
//    eventBus.fire(new AlertPluginEvent("CrTracker stopping..."));
  }

}

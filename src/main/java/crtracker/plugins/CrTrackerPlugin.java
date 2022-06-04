package crtracker.plugins;

import org.hibernate.Session;
import org.mili.utils.sql.service.MigrationService;
import org.mili.utils.sql.service.ServiceFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.stereotype.Component;

import crtracker.plugin.AbstractPlugin;
import crtracker.plugins.messaging.AlertPluginEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CrTrackerPlugin extends AbstractPlugin implements ApplicationRunner {

  private static final String[] ARGS = {"../server_credentials/conf/crtracker/key",
      "../server/apps/crtracker/conf/config.properties",
      "../server_credentials/conf/crtracker/encrypted/test_credentials.properties",
      "/tmp/status.txt",
      "-Dtest=true"
  };

  @Override
  public void stopIntern(Session session) {
    pluginManager.fire(new AlertPluginEvent("CrTracker stopping..."));
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (args.getSourceArgs().length == 0) {
      args = new DefaultApplicationArguments(ARGS);
    }
    String[] sourceArgs = args.getSourceArgs();
    configurationService.initialize(sourceArgs[0], sourceArgs[1], sourceArgs[2]);
    pluginManager.onStart();
    run();
  }

  @Override
  public void runIntern(Session session) throws Exception {
    pluginManager.fire(new AlertPluginEvent("CrTracker starting..."));
    ServiceFactory.getService(MigrationService.class).migrate(false, CrTrackerPlugin.class);
  }

}

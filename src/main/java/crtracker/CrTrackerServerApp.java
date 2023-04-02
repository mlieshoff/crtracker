package crtracker;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import crtracker.plugin.PluginManager;
import crtracker.service.ConfigurationService;

@EnableScheduling
@SpringBootApplication
public class CrTrackerServerApp {

  public static void main(String[] args) {
    if (new File(args[3]).exists()) {
      if (!ConfigurationService.TEST) {
        throw new IllegalStateException("CrTracker is already running!");
      }
    }
    SpringApplicationBuilder app = new SpringApplicationBuilder(CrTrackerServerApp.class)
        .web(WebApplicationType.NONE);
    app.build().addListeners(new ApplicationPidFileWriter(args[3]));
    PluginManager.ARGS_HOLDER.set(args);
    app.run(args);
  }

}
package crtracker;

import com.google.common.collect.ImmutableMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import crtracker.plugin.Plugin;

@Configuration
@ComponentScan(basePackages = "crtracker")
public class ApplicationConfig {

  @Bean
  public Map<Class<? extends Plugin>, Plugin> plugins(List<Plugin> pluginList) {
    ImmutableMap.Builder<Class<? extends Plugin>, Plugin> builder = ImmutableMap.builder();
    pluginList.forEach(plugin -> builder.put(plugin.getClass(), plugin));
    return builder.build();
  }

}


package crtracker.plugin;

import org.hibernate.Session;

public interface Plugin<T extends PluginEvent> {

  void onStart();

  void run();

  void onPluginEvent(Session session, T pluginEvent);

  boolean canHandlePluginEvent(PluginEvent pluginEvent);

  boolean isActive();

  void onStop();

}

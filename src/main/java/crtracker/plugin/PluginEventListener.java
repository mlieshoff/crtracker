package crtracker.plugin;

import org.hibernate.Session;

public interface PluginEventListener<T> {

  void onPluginEvent(Session session, T pluginEvent);

  boolean canHandlePluginEvent(PluginEvent pluginEvent);

}

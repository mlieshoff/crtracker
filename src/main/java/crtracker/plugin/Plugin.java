package crtracker.plugin;

public interface Plugin<T extends PluginEvent> {

  void onStart();

  void run();

  boolean isActive();

  void onStop();

}

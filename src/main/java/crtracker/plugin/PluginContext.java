package crtracker.plugin;

import org.hibernate.Session;

public class PluginContext {

  private static final ThreadLocal<Session> holder = new ThreadLocal<>();

  public static void bind(Session session) {
    Session foundSession = holder.get();
    if (foundSession == null) {
      holder.set(session);
    } else {
      throw new IllegalStateException("session already bound!");
    }
  }

  public static void unbind() {
    Session foundSession = holder.get();
    if (foundSession == null) {
      throw new IllegalStateException("no session to unbound!");
    }
    holder.remove();
  }

  public static Session get() {
    Session foundSession = holder.get();
    if (foundSession == null) {
      throw new IllegalStateException("no session bounded!");
    }
    return holder.get();
  }

}

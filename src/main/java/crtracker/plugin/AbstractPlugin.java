package crtracker.plugin;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import crtracker.plugins.messaging.AlertPluginEvent;
import crtracker.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractPlugin<T extends PluginEvent> implements Plugin<T> {

  @Autowired
  protected PluginManager pluginManager;

  @Autowired
  protected ConfigurationService configurationService;

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public void onStart() {
    String pluginClassname = getClass().getName();
    log.info("{}: start onStart.", pluginClassname);
    doInTransaction(session -> startIntern(session));
    log.info("{}: stop onStart.", pluginClassname);
  }

  @Override
  public void onStop() {
    String pluginClassname = getClass().getName();
    log.info("{}: start onStop.", pluginClassname);
    doInTransaction(session -> stopIntern(session));
    log.info("{}: stop onStop.", pluginClassname);
  }

  @Override
  public void run() {
    String pluginClassname = getClass().getName();
    log.info("{}: start run.", pluginClassname);
    doInTransaction(session -> runIntern(session));
    log.info("{}: stop run.", pluginClassname);
  }

  private void doInTransaction(Action action) {
    String pluginClassname = getClass().getName();
    Session session = configurationService.createSession();
    Transaction transaction = session.beginTransaction();
    PluginContext.bind(session);
    try {
      action.execute(session);
      transaction.commit();
    } catch (Exception e) {
      pluginManager.fire(new AlertPluginEvent("Error occured: " + pluginClassname + " -> " + e.getMessage()));
      log.error("{}: exception occured", pluginClassname, e);
      if (transaction != null) {
        transaction.rollback();
      }
    } finally {
      session.close();
      PluginContext.unbind();
    }
  }

  public void startIntern(Session session) throws Exception {

  }

  public void runIntern(Session session) throws Exception {

  }

  public void stopIntern(Session session) throws Exception {

  }

  @Override
  public void onPluginEvent(Session session, T pluginEvent) {

  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return false;
  }

}

package crtracker.plugin;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import crtracker.plugins.messaging.AlertPluginEvent;
import crtracker.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractPlugin<T extends PluginEvent> implements Plugin<T>, PluginEventListener<T> {

  @Autowired
  protected EventBus eventBus;

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
    doInTransaction(this::startIntern);
    log.info("{}: stop onStart.", pluginClassname);
  }

  @Override
  public void onStop() {
    String pluginClassname = getClass().getName();
    log.info("{}: start onStop.", pluginClassname);
    doInTransaction(this::stopIntern);
    log.info("{}: stop onStop.", pluginClassname);
  }

  @Override
  public void run() {
    String pluginClassname = getClass().getName();
    log.info("{}: start run.", pluginClassname);
    doInTransaction(this::runIntern);
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
      eventBus.fire(new AlertPluginEvent("Error occured: " + pluginClassname + " -> " + e.getMessage()));
      log.error("{}: exception occured", pluginClassname, e);
      if (transaction != null) {
        transaction.rollback();
      }
    } finally {
      session.close();
      PluginContext.unbind();
    }
  }

  public void startIntern(Session session) {

  }

  public void runIntern(Session session) throws Exception {

  }

  public void stopIntern(Session session) {

  }

  @Override
  public void onPluginEvent(Session session, T pluginEvent) {

  }

  @Override
  public boolean canHandlePluginEvent(PluginEvent pluginEvent) {
    return false;
  }

}

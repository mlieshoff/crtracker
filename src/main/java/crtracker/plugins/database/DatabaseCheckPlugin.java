package crtracker.plugins.database;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import crtracker.persistency.dao.MeasureDao;
import crtracker.plugin.AbstractPlugin;
import crtracker.plugins.messaging.AlertPluginEvent;

@Service
public class DatabaseCheckPlugin extends AbstractPlugin {

  @Autowired
  private MeasureDao measureDao;

  @Override
  @Scheduled(initialDelay = 60000, fixedDelay = 60000 * 15)
  public void run() {
    super.run();
  }

  @Override
  public void runIntern(Session session) {
    StringBuilder s = new StringBuilder();
    s.append("* Number of rows:\n");
    s.append("    string measures: ");
    s.append(measureDao.getCountStringMeasures(session));
    s.append("\n");
    s.append("    number measures: ");
    s.append(measureDao.getCountNumberMeasures(session));
    s.append("\n");
    s.append("    decimal measures: ");
    s.append(measureDao.getCountDecimalMeasures(session));
    s.append("\n");
    s.append("    text measures: ");
    s.append(measureDao.getCountTextMeasures(session));
    s.append("\n");
    eventBus.fire(new AlertPluginEvent(s.toString()));
  }

}

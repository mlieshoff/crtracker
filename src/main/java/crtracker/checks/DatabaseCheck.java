package crtracker.checks;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mili.utils.sql.service.ServiceFactory;

import crtracker.Config;
import crtracker.job.AbstractJob;
import crtracker.persistency.dao.MeasureDao;
import crtracker.service.MessageService;

public class DatabaseCheck extends AbstractJob {

    private final Config config;

    private final MessageService messageService = ServiceFactory.getService(MessageService.class);

    private final MeasureDao measureDao = new MeasureDao();

    public DatabaseCheck(Config config) {
        this.config = config;
    }

    @Override
    protected void runIntern() throws Exception {
        Session session = config.createSession();
        Transaction transaction = session.beginTransaction();
        try {
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
            messageService.sendAlert(config, s.toString());
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null) {
                transaction.rollback();
            }
            messageService.sendAlert(config, "Error while database check data: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    @Override
    public long getTimeout() {
        return 60000 * 15;
    }

}

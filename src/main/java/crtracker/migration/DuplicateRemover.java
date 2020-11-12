package crtracker.migration;

import static java.util.Arrays.asList;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import crtracker.service.ConfigurationService;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.TextMeasure;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DuplicateRemover {

    private final MeasureDao measureDao = new MeasureDao();

    public void run(ConfigurationService configurationService, String clanTag) {
        Session session = configurationService.createSession();
        Transaction transaction = session.beginTransaction();
        try {
            List<TextMeasure> lastTextMeasures = measureDao.getLastTextMeasures(session, CrTrackerTypes.CLAN_MEMBERS, clanTag, 1000);
            String old = null;
            for (int i = lastTextMeasures.size() - 1; i >= 0; i --) {
                TextMeasure textMeasure = lastTextMeasures.get(i);
                if (old != null && compare(textMeasure.getValue(), old)) {
                    session.delete(textMeasure);
                }
                old = textMeasure.getValue();
            }
            transaction.commit();
        } catch (Exception e) {
            log.error("Error while importing data", e);
            if (transaction != null) {
                transaction.rollback();
            }
        } finally {
            session.close();
        }
    }

    private boolean compare(String current, String old) {
        Set<String> oldSet = new TreeSet<>();
        oldSet.addAll(asList(old.split(",")));
        Set<String> currentSet = new TreeSet<>();
        currentSet.addAll(asList(current.split(",")));
        return oldSet.equals(currentSet);
    }

}

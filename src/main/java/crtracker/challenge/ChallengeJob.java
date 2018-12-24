package crtracker.challenge;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.mili.utils.sql.service.ServiceFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import crtracker.Config;
import crtracker.CrTracker;
import crtracker.Utils;
import crtracker.checks.WebsiteGenerator;
import crtracker.job.AbstractJob;
import crtracker.persistency.Role;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;
import crtracker.service.MessageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChallengeJob extends AbstractJob {

    private final MessageService messageService = ServiceFactory.getService(MessageService.class);

    private final Config config;

    private final String clanTag;

    private final MeasureDao measureDao = new MeasureDao();

    private final ChallengeDao challengeDao = new ChallengeDao();

    public ChallengeJob(Config config) throws Exception {
        this.config = config;
        clanTag = config.getConfig().getProperty("crtracker.clan.tag");
    }

    @Override public long getTimeout() {
        return 60000;
    }

    @Override
    protected void runIntern() throws Exception {
        Session session = config.createSession();
        Transaction transaction = session.beginTransaction();
        try {
            Map<Long, ChallengeDefinition> challengeDefinitions = challengeDao.getActiveChallengeDefinitions(session);
            if (MapUtils.isNotEmpty(challengeDefinitions)) {
                DateTime now = new DateTime();
                stopAndComputeChallengesIfNeeded(session, challengeDefinitions, now);
                startNewChallengesIfNeeded(session, challengeDefinitions, now);
            }
            transaction.commit();
        } catch (Exception e) {
            log.error("Error while challenge job", e);
            if (transaction != null) {
                transaction.rollback();
            }
            messageService.sendAlert(config, "Error while challenge job: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void stopAndComputeChallengesIfNeeded(Session session, Map<Long, ChallengeDefinition> challengeDefinitions,
            DateTime now) {
        List<RunningChallenge> runningChallenges = challengeDao.getRunningChallenges(session);
        if (isNotEmpty(runningChallenges)) {
            for (RunningChallenge runningChallenge : runningChallenges) {
                ChallengeDefinition challengeDefinition = challengeDefinitions.get(runningChallenge.getChallengeId());
                ImmutablePair<DateTime, DateTime> activationRange = new ImmutablePair<>(new DateTime(runningChallenge.getStart()), new DateTime(runningChallenge.getEnd()));
                if (!Utils.isInRange(now, activationRange)) {
                    runningChallenge.setChallengeStatus(ChallengeStatus.ENDED.getCode());
                    session.merge(runningChallenge);
                    summarizeChallenge(session, challengeDefinition, runningChallenge, activationRange);
                }
            }
        }
    }

    private void summarizeChallenge(Session session, ChallengeDefinition challengeDefinition,
            RunningChallenge runningChallenge, ImmutablePair<DateTime, DateTime> activationRange) {
        if ("DONATIONS".equalsIgnoreCase(challengeDefinition.getObjectives())) {
            List<SummarizeNumberEntry> summarizeNumberEntries = new DonationChallengeHandler().summarize(session, clanTag, challengeDefinition, runningChallenge, activationRange);
            // get top x

            // save results
            for (SummarizeNumberEntry summarizeNumberEntry : summarizeNumberEntries) {
                String id = String.format("%s:%s", runningChallenge.getUuid(), summarizeNumberEntry.getMemberTag());
                measureDao.updateNumberMeasure(session, id, CrTrackerTypes.CHALLENGE.getCode(), summarizeNumberEntry.getValue());
            }
        }
    }

    private void startNewChallengesIfNeeded(Session session, Map<Long, ChallengeDefinition> challengeDefinitions,
            DateTime now) {
        for (Map.Entry<Long, ChallengeDefinition> entry : challengeDefinitions.entrySet()) {
            long challengeId = entry.getKey();
            ChallengeDefinition challengeDefinition = entry.getValue();
            Pair<DateTime, DateTime> activationRange = challengeDefinition.getActivationRange(now);
            if (Utils.isInRange(now, activationRange)) {
                RunningChallenge runningChallenge = new RunningChallenge(
                        UUID.randomUUID().toString(),
                        challengeId,
                        ChallengeStatus.RUNNING.getCode(),
                        activationRange.getLeft().toDate(),
                        activationRange.getRight().toDate()
                );
                session.save(runningChallenge);
            }
        }
    }

}

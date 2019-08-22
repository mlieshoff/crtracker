package crtracker.challenge;

import static crtracker.Utils.isInRange;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.mili.utils.sql.service.ServiceFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import crtracker.Config;
import crtracker.job.AbstractJob;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
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

    @Override
    public long getTimeout() {
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
                List<RunningChallenge> runningChallenges = challengeDao.getRunningChallenges(session);
                stopAndComputeChallengesIfNeeded(session, runningChallenges, challengeDefinitions, now);
                startNewChallengesIfNeeded(session, runningChallenges, challengeDefinitions, now);
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

    private void stopAndComputeChallengesIfNeeded(Session session, List<RunningChallenge> runningChallenges,
            Map<Long, ChallengeDefinition> challengeDefinitions, DateTime now) {
        if (isNotEmpty(runningChallenges)) {
            for (RunningChallenge runningChallenge : runningChallenges) {
                ChallengeDefinition challengeDefinition = challengeDefinitions.get(runningChallenge.getChallengeId());
                ImmutablePair<DateTime, DateTime> activationRange = new ImmutablePair<>(
                        new DateTime(runningChallenge.getStart()), new DateTime(runningChallenge.getEnd()));
                if (!isInRange(now, activationRange)) {
                    runningChallenge.setChallengeStatus(ChallengeStatus.ENDED.getCode());
                    session.merge(runningChallenge);
                    summarizeChallenge(session, challengeDefinition, runningChallenge, activationRange);
                }
            }
        }
    }

    private void summarizeChallenge(Session session, ChallengeDefinition challengeDefinition,
            RunningChallenge runningChallenge, ImmutablePair<DateTime, DateTime> activationRange) {
        List<SummarizeNumberEntry> summarizeNumberEntries = new BasicChallengeHandler()
                .summarize(session, clanTag, challengeDefinition, runningChallenge, activationRange);
        for (SummarizeNumberEntry summarizeNumberEntry : summarizeNumberEntries) {
            String id = String.format("%s:%s", runningChallenge.getUuid(), summarizeNumberEntry.getMemberTag());
            measureDao.updateNumberMeasure(session, id, CrTrackerTypes.CHALLENGE.getCode(),
                    summarizeNumberEntry.getValue());
        }
    }

    private void startNewChallengesIfNeeded(Session session, List<RunningChallenge> runningChallenges,
            Map<Long, ChallengeDefinition> challengeDefinitions, DateTime now) {
        Set<Long> runningChallengeIds = runningChallenges.stream()
                .map(runningChallenge -> runningChallenge.getChallengeId()).collect(Collectors.toSet());
        for (Map.Entry<Long, ChallengeDefinition> entry : challengeDefinitions.entrySet()) {
            long challengeId = entry.getKey();
            ChallengeDefinition challengeDefinition = entry.getValue();
            Pair<DateTime, DateTime> activationRange = challengeDefinition.getActivationRange(now);
            if (!runningChallengeIds.contains(challengeId) && isInRange(now, activationRange)) {
                RunningChallenge runningChallenge = new RunningChallenge(randomUUID().toString(), challengeId,
                        ChallengeStatus.RUNNING.getCode(), activationRange.getLeft().toDate(),
                        activationRange.getRight().toDate());
                session.save(runningChallenge);
            }
        }
    }

}

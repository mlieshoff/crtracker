package crtracker.plugins.challenge;

import static crtracker.util.CipherUtil.isInRange;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.plugin.AbstractPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChallengePlugin extends AbstractPlugin {

  @Autowired
  private MeasureDao measureDao;

  @Autowired
  private ChallengeDao challengeDao;

  @Scheduled(initialDelay = 60000, fixedDelay = 60000)
  public void run() {
    super.run();
  }

  @Override
  public void runIntern(Session session) throws Exception {
    String clanTag = configurationService.getClanTag();
    Map<Long, ChallengeDefinition> challengeDefinitions = challengeDao.getActiveChallengeDefinitions(session);
    if (MapUtils.isNotEmpty(challengeDefinitions)) {
      DateTime now = new DateTime();
      List<RunningChallenge> runningChallenges = challengeDao.getRunningChallenges(session);
      stopAndComputeChallengesIfNeeded(clanTag, session, runningChallenges, challengeDefinitions, now);
      startNewChallengesIfNeeded(session, runningChallenges, challengeDefinitions, now);
    }
  }

  private void stopAndComputeChallengesIfNeeded(String clanTag, Session session,
                                                List<RunningChallenge> runningChallenges,
                                                Map<Long, ChallengeDefinition> challengeDefinitions, DateTime now) {
    if (isNotEmpty(runningChallenges)) {
      for (RunningChallenge runningChallenge : runningChallenges) {
        ChallengeDefinition challengeDefinition = challengeDefinitions.get(runningChallenge.getChallengeId());
        ImmutablePair<DateTime, DateTime> activationRange = new ImmutablePair<>(
            new DateTime(runningChallenge.getStart()), new DateTime(runningChallenge.getEnd()));
        if (!isInRange(now, activationRange)) {
          runningChallenge.setChallengeStatus(ChallengeStatus.ENDED.getCode());
          session.merge(runningChallenge);
          summarizeChallenge(clanTag, session, challengeDefinition, runningChallenge, activationRange);
        }
      }
    }
  }

  private void summarizeChallenge(String clanTag, Session session, ChallengeDefinition challengeDefinition,
                                  RunningChallenge runningChallenge,
                                  ImmutablePair<DateTime, DateTime> activationRange) {
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

package crtracker.challenge;

import static java.util.Arrays.asList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import crtracker.Utils;
import crtracker.persistency.dao.MeasureDao;
import crtracker.persistency.model.CrTrackerTypes;
import crtracker.persistency.model.NumberMeasure;
import crtracker.persistency.model.StringMeasure;

public class DonationChallengeHandler {

    private final MeasureDao measureDao = new MeasureDao();

    public List<SummarizeNumberEntry> summarize(Session session, String clanTag, ChallengeDefinition challengeDefinition, RunningChallenge runningChallenge,
            ImmutablePair<DateTime, DateTime> activationRange) {
        StringMeasure members = measureDao.getCurrentStringMeasure(session, CrTrackerTypes.CLAN_MEMBERS, clanTag);
        List<SummarizeNumberEntry> model = new ArrayList<>();
        for (String memberTag : asList(members.getValue().split(","))) {
            NumberMeasure donationMeasure = measureDao.getLastNumberMeasure(session, CrTrackerTypes.MEMBER_DONATIONS, memberTag, activationRange.getLeft(), activationRange.getRight());
            long donations = 0;
            if (donationMeasure != null) {
                donations = donationMeasure.getValue();
            }
            model.add(new SummarizeNumberEntry(memberTag, donations));
        }
        model.sort(new Comparator<SummarizeNumberEntry>() {
            @Override
            public int compare(SummarizeNumberEntry o1, SummarizeNumberEntry o2) {
                if (challengeDefinition.getChallengeSummaryType() == ChallengeSummaryType.TOP.getCode()) {
                    return Long.compare(o2.getValue(), o1.getValue());
                } else {
                    return Long.compare(o1.getValue(), o2.getValue());
                }
           }
        });
        rankThem(model);
        for (Iterator<SummarizeNumberEntry> iterator = model.iterator(); iterator.hasNext(); ) {
            SummarizeNumberEntry summarizeNumberEntry = iterator.next();
            if (summarizeNumberEntry.getRank() > challengeDefinition.getChallengeSummaryNumber()) {
                iterator.remove();
            }
        }
        return model;
    }

    private void rankThem(List<SummarizeNumberEntry> list) {
        int visual = 1;
        SummarizeNumberEntry oldUserData = null;
        for (SummarizeNumberEntry userData : list) {
            if (oldUserData != null && oldUserData.getValue() != userData.getValue()) {
                visual ++;
            }
            userData.setRank(visual);
            oldUserData = userData;
        }
    }

}

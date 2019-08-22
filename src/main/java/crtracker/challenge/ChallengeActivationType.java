package crtracker.challenge;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;

import java.util.Map;
import crtracker.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChallengeActivationType {

    WEEKLY_NATURAL((byte) 1) {
        @Override public Pair<DateTime, DateTime> getActivationRange(DateTime now) {
            Pair<DateTime, DateTime> calendarWeek = Utils.getCalendarWeekFromTo(now);
            DateTime start = calendarWeek.getLeft();
            DateTime end = calendarWeek.getRight();
            return new ImmutablePair<>(start, end);
        }
    },
    WEEKLY_FLUENT((byte) 2) {
        @Override public Pair<DateTime, DateTime> getActivationRange(DateTime now) {
            return new ImmutablePair<>(now, now.plusDays(7));
        }
    };

    private static final Map<Byte, ChallengeActivationType> LOOKUP;

    static {
        ImmutableMap.Builder<Byte, ChallengeActivationType> builder = ImmutableMap.builder();
        for (ChallengeActivationType challengeActivationType : values()) {
            builder.put(challengeActivationType.code, challengeActivationType);
        }
        LOOKUP = builder.build();
    }

    @Getter
    private final byte code;

    public static ChallengeActivationType fromCode(byte challengeActivationType) {
        return LOOKUP.get(challengeActivationType);
    }

    public abstract Pair<DateTime, DateTime> getActivationRange(DateTime now);

}

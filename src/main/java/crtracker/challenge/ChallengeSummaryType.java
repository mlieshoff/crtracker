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
public enum ChallengeSummaryType {

    TOP((byte) 1),
    BOTTOM((byte) 2);

    private static final Map<Byte, ChallengeSummaryType> LOOKUP;

    static {
        ImmutableMap.Builder<Byte, ChallengeSummaryType> builder = ImmutableMap.builder();
        for (ChallengeSummaryType challengeSummaryType : values()) {
            builder.put(challengeSummaryType.code, challengeSummaryType);
        }
        LOOKUP = builder.build();
    }

    @Getter
    private final byte code;

    public static ChallengeSummaryType fromCode(byte challengeSummaryType) {
        return LOOKUP.get(challengeSummaryType);
    }

}

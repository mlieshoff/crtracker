package crtracker.challenge;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChallengeStatus {

    ENDED((byte) 1),
    RUNNING((byte) 2);

    private static final Map<Byte, ChallengeStatus> LOOKUP;

    static {
        ImmutableMap.Builder<Byte, ChallengeStatus> builder = ImmutableMap.builder();
        for (ChallengeStatus challengeStatus : values()) {
            builder.put(challengeStatus.code, challengeStatus);
        }
        LOOKUP = builder.build();
    }

    @Getter
    private final byte code;

    public static ChallengeStatus fromCode(byte challengeStatus) {
        return LOOKUP.get(challengeStatus);
    }

}

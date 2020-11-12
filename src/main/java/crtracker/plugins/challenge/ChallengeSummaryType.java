package crtracker.plugins.challenge;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
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

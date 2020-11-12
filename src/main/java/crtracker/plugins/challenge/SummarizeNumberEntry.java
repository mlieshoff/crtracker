package crtracker.plugins.challenge;

import lombok.Data;

@Data
public class SummarizeNumberEntry {

  private final String memberTag;

  private final long value;

  private int rank;

}

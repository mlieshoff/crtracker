package crtracker.plugins.challenge;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Table(name = "challenge_states")
@Entity
public class RunningChallenge {

  @Id
  private String uuid;

  @Column(name = "challenge_id")
  private long challengeId;

  @Column(name = "challenge_status")
  private byte challengeStatus;

  @Column(name = "start")
  private Date start;

  @Column(name = "end")
  private Date end;

  public RunningChallenge() {
  }

}

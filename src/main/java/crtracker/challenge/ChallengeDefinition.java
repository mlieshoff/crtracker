package crtracker.challenge;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Table(name = "challenge_definitions")
@Entity
public class ChallengeDefinition {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "active")
    private boolean active;

    @Column(name = "challenge_activation_type")
    private byte challengeActivationType;

    @Column(name = "challenge_summary_type")
    private byte challengeSummaryType;

    @Column(name = "challenge_summary_number")
    private byte challengeSummaryNumber;

    @Column(name = "objectives")
    private String objectives;

    public ChallengeDefinition() {
    }

    public Pair<DateTime, DateTime> getActivationRange(DateTime now) {
        return ChallengeActivationType.fromCode(challengeActivationType).getActivationRange(now);
    }

}

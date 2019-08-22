package crtracker.api;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlayerBattleLogData {

    private final List<PlayerBattleLogDataEntry> entries = new ArrayList<>();

    @Getter
    @RequiredArgsConstructor
    public static class PlayerBattleLogDataEntry {

        private final String id;
        private final String type;

        private final int player1Crowns;
        private final int player2Crowns;

        private final String player1Tag;
        private final String player2Tag;

        private final String player1Name;
        private final String player2Name;

    }

}

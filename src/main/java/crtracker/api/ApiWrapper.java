package crtracker.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ApiWrapper {

    private final String officialApiUrl;
    private final String officialApiToken;
    private final String royalApiUrl;
    private final String royalApiToken;

    public ClanData getClanData(String clanTag) {
        try {
            log.info("try get clan data from official api...");
            return new OfficialApi(officialApiUrl, officialApiToken).getClanData(clanTag);
        } catch (Exception e) {
            log.error("catched exception while getting clan data[{}]...", royalApiUrl, e);
            throw new IllegalStateException(e);
        }
    }

    public PlayerBattleLogData getBattleLogFor(String playerTag) {
        try {
            log.info("try get battle log data from official api...");
            return new OfficialApi(officialApiUrl, officialApiToken).getPlayerBattleLogData(playerTag);
        } catch (Exception e) {
            log.error("catched exception while getting battle log [{}]...", royalApiUrl, e);
            throw new IllegalStateException(e);
        }
    }

}

package crtracker.integration;

import crtracker.integration.clashroyale.OfficialApi;
import jcrapi2.response.GetClanResponse;
import jcrapi2.response.GetCurrentClanRiverRaceResponse;
import jcrapi2.response.GetPlayerBattleLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ApiWrapper {

  private final String proxyApiUrl;
  private final String proxyApiToken;
  private final String officialApiUrl;
  private final String officialApiToken;

  public GetClanResponse getClanData(String clanTag) {
    try {
      log.info("try get clan data from official api...");
      return new OfficialApi(proxyApiUrl, proxyApiToken).getClanData(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting clan data[{}]...", officialApiUrl, e);
      return new OfficialApi(officialApiUrl, officialApiToken).getClanData(clanTag);
    }
  }

  public GetPlayerBattleLogResponse getBattleLogFor(String playerTag) {
    try {
      log.info("try get battle log data from official api...");
      return new OfficialApi(proxyApiUrl, proxyApiToken).getPlayerBattleLogData(playerTag);
    } catch (Exception e) {
      log.error("catched exception while getting battle log [{}]...", officialApiToken, e);
      return new OfficialApi(officialApiUrl, officialApiToken).getPlayerBattleLogData(playerTag);
    }
  }

  public GetCurrentClanRiverRaceResponse getCurrentClanRiverRace(String clanTag) {
    try {
      log.info("try get battle log data from official api...");
      return new OfficialApi(proxyApiUrl, proxyApiToken).getCurrentClanRiverRace(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting battle log [{}]...", officialApiToken, e);
      return new OfficialApi(officialApiUrl, officialApiToken).getCurrentClanRiverRace(clanTag);
    }
  }

}

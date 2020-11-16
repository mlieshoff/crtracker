package crtracker.integration;

import crtracker.integration.clashroyale.OfficialApi;
import jcrapi2.response.GetClanResponse;
import jcrapi2.response.GetCurrentClanRiverRaceResponse;
import jcrapi2.response.GetPlayerBattleLogResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiWrapper {

  private final String proxyApiUrl;
  private final String proxyApiToken;
  private final String officialApiUrl;
  private final String officialApiToken;

  private final OfficialApi officialApi;
  private final OfficialApi proxiedApi;

  public ApiWrapper(String proxyApiUrl, String proxyApiToken, String officialApiUrl, String officialApiToken) {
    this.proxyApiUrl = proxyApiUrl;
    this.proxyApiToken = proxyApiToken;
    this.officialApiUrl = officialApiUrl;
    this.officialApiToken = officialApiToken;
    proxiedApi = new OfficialApi(proxyApiUrl, proxyApiToken);
    officialApi = new OfficialApi(officialApiUrl, officialApiToken);
  }

  public GetClanResponse getClanData(String clanTag) {
    try {
      log.info("try get clan data from official api...");
      return proxiedApi.getClanData(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting clan data[{}]...", officialApiUrl, e);
      return officialApi.getClanData(clanTag);
    }
  }

  public GetPlayerBattleLogResponse getBattleLogFor(String playerTag) {
    try {
      log.info("try get battle log data from official api...");
      return proxiedApi.getPlayerBattleLogData(playerTag);
    } catch (Exception e) {
      log.error("catched exception while getting battle log [{}]...", officialApiToken, e);
      return officialApi.getPlayerBattleLogData(playerTag);
    }
  }

  public GetCurrentClanRiverRaceResponse getCurrentClanRiverRace(String clanTag) {
    try {
      log.info("try get battle log data from official api...");
      return proxiedApi.getCurrentClanRiverRace(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting battle log [{}]...", officialApiToken, e);
      return officialApi.getCurrentClanRiverRace(clanTag);
    }
  }

}

package crtracker.integration;

import crtracker.integration.clashroyale.OfficialApi;
import jcrapi2.api.intern.clans.currentriverrace.CurrentRiverRaceResponse;
import jcrapi2.api.intern.clans.info.ClanResponse;
import jcrapi2.api.intern.players.battlelog.BattleLogResponse;
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

  public ClanResponse getClanData(String clanTag) {
    try {
      log.info("try get clan data from official api...");
      return proxiedApi.getClanData(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting clan data[{}]...", officialApiUrl, e);
      try {
        return officialApi.getClanData(clanTag);
      } catch (Exception ex) {
        log.error("exception while getting clan data[{}]...", officialApiUrl, e);
        throw new IllegalStateException(e);
      }
    }
  }

  public BattleLogResponse getBattleLogFor(String playerTag) {
    try {
      log.info("try get battle log data from official api...");
      return proxiedApi.getPlayerBattleLogData(playerTag);
    } catch (Exception e) {
      log.error("catched exception while getting battle log [{}]...", officialApiToken, e);
      try {
        return officialApi.getPlayerBattleLogData(playerTag);
      } catch (Exception ex) {
        log.error("exception while getting battle log [{}]...", officialApiToken, e);
        throw new IllegalStateException(e);
      }
    }
  }

  public CurrentRiverRaceResponse getCurrentClanRiverRace(String clanTag) {
    try {
      log.info("try get battle log data from official api...");
      return proxiedApi.getCurrentClanRiverRace(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting current river race [{}]...", officialApiToken, e);
      try {
        return officialApi.getCurrentClanRiverRace(clanTag);
      } catch (Exception ex) {
        log.error("exception while getting current river race [{}]...", officialApiToken, e);
        throw new IllegalStateException(e);
      }
    }
  }

}

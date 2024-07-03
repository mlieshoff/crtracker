package crtracker.integration;

import crtracker.integration.clashroyale.OfficialApi;
import jcrapi2.api.intern.clans.currentriverrace.CurrentRiverRaceResponse;
import jcrapi2.api.intern.clans.info.ClanResponse;
import jcrapi2.api.intern.clans.riverracelog.RiverRaceLogResponse;
import jcrapi2.api.intern.players.battlelog.BattleLogResponse;
import jcrapi2.api.intern.players.info.PlayerResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiWrapper {

  private final String proxyApiUrl;
  private final String officialApiUrl;

  private final OfficialApi officialApi;
  private final OfficialApi proxiedApi;

  public ApiWrapper(String proxyApiUrl, String proxyApiToken, String officialApiUrl, String officialApiToken) {
    this.proxyApiUrl = proxyApiUrl;
    this.officialApiUrl = officialApiUrl;
    proxiedApi = new OfficialApi(proxyApiUrl, proxyApiToken);
    officialApi = new OfficialApi(officialApiUrl, officialApiToken);
  }

  public ClanResponse getClanData(String clanTag) {
    try {
      log.info("try get clan data from official api...");
      return proxiedApi.getClanData(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting clan data[{}]...", proxyApiUrl, e);
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
      log.error("catched exception while getting battle log [{}]...", proxyApiUrl, e);
      try {
        return officialApi.getPlayerBattleLogData(playerTag);
      } catch (Exception ex) {
        log.error("exception while getting battle log [{}]...", officialApiUrl, e);
        throw new IllegalStateException(e);
      }
    }
  }

  public CurrentRiverRaceResponse getCurrentClanRiverRace(String clanTag) {
    try {
      log.info("try get river race data from official api...");
      return proxiedApi.getCurrentClanRiverRace(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting current river race [{}]...", proxyApiUrl, e);
      try {
        return officialApi.getCurrentClanRiverRace(clanTag);
      } catch (Exception ex) {
        log.error("exception while getting current river race [{}]...", officialApiUrl, e);
        throw new IllegalStateException(e);
      }
    }
  }

  public RiverRaceLogResponse getRiverRaceLog(String clanTag) {
    try {
      log.info("try get river race log data from official api...");
      return proxiedApi.getRiverRaceLog(clanTag);
    } catch (Exception e) {
      log.error("catched exception while getting current river race log [{}]...", proxyApiUrl, e);
      try {
        return officialApi.getRiverRaceLog(clanTag);
      } catch (Exception ex) {
        log.error("exception while getting river race log [{}]...", officialApiUrl, e);
        throw new IllegalStateException(e);
      }
    }
  }

  public PlayerResponse getPlayer(String playerTag) {
    try {
      log.info("try get player data from official api...");
      return proxiedApi.getPlayer(playerTag);
    } catch (Exception e) {
      log.error("catched exception while getting player [{}]...", proxyApiUrl, e);
      try {
        return officialApi.getPlayer(playerTag);
      } catch (Exception ex) {
        log.error("exception while getting player [{}]...", officialApiUrl, e);
        throw new IllegalStateException(e);
      }
    }
  }

}

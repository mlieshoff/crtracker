package crtracker.integration.clashroyale;

import jcrapi2.Api;
import jcrapi2.request.GetClanRequest;
import jcrapi2.request.GetCurrentClanRiverRaceRequest;
import jcrapi2.request.GetPlayerBattleLogRequest;
import jcrapi2.response.GetClanResponse;
import jcrapi2.response.GetCurrentClanRiverRaceResponse;
import jcrapi2.response.GetPlayerBattleLogResponse;

public class OfficialApi {

  private final Api api;

  public OfficialApi(String url, String token) {
    api = new Api(url, token);
  }

  public GetClanResponse getClanData(String clanTag) {
    return api.getClan(GetClanRequest.builder(clanTag).build());
  }

  public GetPlayerBattleLogResponse getPlayerBattleLogData(String playerTag) {
    return api.getPlayerBattleLog(GetPlayerBattleLogRequest.builder(playerTag).build());
  }

  public GetCurrentClanRiverRaceResponse getCurrentClanRiverRace(String clanTag) {
    return api.getCurrentClanRiverRace(GetCurrentClanRiverRaceRequest.builder(clanTag).build());
  }

}

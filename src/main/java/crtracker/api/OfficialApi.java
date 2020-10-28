package crtracker.api;

import jcrapi2.Api;
import jcrapi2.request.GetClanRequest;
import jcrapi2.request.GetPlayerBattleLogRequest;
import jcrapi2.response.GetClanResponse;
import jcrapi2.response.GetPlayerBattleLogResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OfficialApi {

  private final String url;
  private final String token;

  private Api api;

  public GetClanResponse getClanData(String clanTag) {
    api = new Api(url, token);
    return api.getClan(GetClanRequest.builder(clanTag).build());
  }

  public GetPlayerBattleLogResponse getPlayerBattleLogData(String playerTag) {
    api = new Api(url, token);
    return api.getPlayerBattleLog(GetPlayerBattleLogRequest.builder(playerTag).build());
  }

}

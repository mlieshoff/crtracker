package crtracker.integration.clashroyale;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jcrapi2.JCrApi;
import jcrapi2.api.intern.clans.ClanApi;
import jcrapi2.api.intern.clans.currentriverrace.CurrentRiverRaceRequest;
import jcrapi2.api.intern.clans.currentriverrace.CurrentRiverRaceResponse;
import jcrapi2.api.intern.clans.info.ClanRequest;
import jcrapi2.api.intern.clans.info.ClanResponse;
import jcrapi2.api.intern.clans.riverracelog.RiverRaceLogRequest;
import jcrapi2.api.intern.clans.riverracelog.RiverRaceLogResponse;
import jcrapi2.api.intern.players.PlayerApi;
import jcrapi2.api.intern.players.battlelog.BattleLogRequest;
import jcrapi2.api.intern.players.battlelog.BattleLogResponse;
import jcrapi2.api.intern.players.info.PlayerRequest;
import jcrapi2.api.intern.players.info.PlayerResponse;
import supercell.api.wrapper.essentials.connector.StandardConnector;

public class OfficialApi {

  private final ClanApi clanApi;
  private final PlayerApi playerApi;

  public OfficialApi(String url, String token) {
    JCrApi jCrApi = new JCrApi(url, token, new StandardConnector());
    clanApi = jCrApi.getApi(ClanApi.class);
    playerApi = jCrApi.getApi(PlayerApi.class);
  }

  public ClanResponse getClanData(String clanTag) throws ExecutionException, InterruptedException, TimeoutException {
    return clanApi.findByTag(ClanRequest.builder(clanTag).build()).get(10, TimeUnit.SECONDS);
  }

  public BattleLogResponse getPlayerBattleLogData(String playerTag)
      throws ExecutionException, InterruptedException, TimeoutException {
    return playerApi.getBattleLog(BattleLogRequest.builder(playerTag).build()).get(10, TimeUnit.SECONDS);
  }

  public CurrentRiverRaceResponse getCurrentClanRiverRace(String clanTag)
      throws ExecutionException, InterruptedException, TimeoutException {
    return clanApi.getCurrentRiverRace(CurrentRiverRaceRequest.builder(clanTag).build()).get(10, TimeUnit.SECONDS);
  }

  public RiverRaceLogResponse getRiverRaceLog(String clanTag)
      throws ExecutionException, InterruptedException, TimeoutException {
    return clanApi.getRiverRaceLog(RiverRaceLogRequest.builder(clanTag).build()).get(10, TimeUnit.SECONDS);
  }

  public PlayerResponse getPlayer(String playerTag)
      throws ExecutionException, InterruptedException, TimeoutException {
    return playerApi.findByTag(PlayerRequest.builder(playerTag).build()).get(10, TimeUnit.SECONDS);
  }

}

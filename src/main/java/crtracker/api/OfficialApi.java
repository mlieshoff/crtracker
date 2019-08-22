package crtracker.api;

import jcrapi2.Api;
import jcrapi2.model.ClanMember;
import jcrapi2.model.PlayerBattleLog;
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

    public ClanData getClanData(String clanTag) {
        api = new Api(url, token);
        GetClanResponse getClanResponse = api.getClan(GetClanRequest.builder(clanTag).build());
        ClanData clanData = new ClanData(getClanResponse.getTag());
        for (ClanMember clanMember : getClanResponse.getMemberList()) {
            // TODO api bug string donations
            ClanDataMember clanDataMember = new ClanDataMember(clanMember.getTag(), clanMember.getName(),
                    clanMember.getRole(), Integer.valueOf(clanMember.getDonations()));
            clanData.getClanDataMembers().add(clanDataMember);
        }
        return clanData;
    }

    public PlayerBattleLogData getPlayerBattleLogData(String playerTag) {
        api = new Api(url, token);
        GetPlayerBattleLogResponse getPlayerBattleLogResponse = api
                .getPlayerBattleLog(GetPlayerBattleLogRequest.builder(playerTag).build());
        PlayerBattleLogData playerBattleLogData = new PlayerBattleLogData();
        for (PlayerBattleLog playerBattleLog : getPlayerBattleLogResponse) {
            PlayerBattleLogData.PlayerBattleLogDataEntry playerBattleLogDataEntry = new PlayerBattleLogData.PlayerBattleLogDataEntry(
                    playerBattleLog.getBattleTime(),
                    playerBattleLog.getType(),
                    playerBattleLog.getTeam().get(0).getCrowns(),
                    playerBattleLog.getOpponent().get(0).getCrowns(),
                    playerBattleLog.getTeam().get(0).getTag(),
                    playerBattleLog.getOpponent().get(0).getTag(),
                    playerBattleLog.getTeam().get(0).getName(),
                    playerBattleLog.getOpponent().get(0).getName()
            );
            playerBattleLogData.getEntries().add(playerBattleLogDataEntry);
        }
        return playerBattleLogData;
    }

}

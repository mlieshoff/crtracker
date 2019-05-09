package crtracker.api;

import jcrapi2.Api;
import jcrapi2.model.ClanMember;
import jcrapi2.request.GetClanRequest;
import jcrapi2.response.GetClanResponse;
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
            ClanDataMember clanDataMember = new ClanDataMember(clanMember.getTag(), clanMember.getName(), clanMember.getRole(), Integer.valueOf(clanMember.getDonations()));
            clanData.getClanDataMembers().add(clanDataMember);
        }
        return clanData;
    }

}

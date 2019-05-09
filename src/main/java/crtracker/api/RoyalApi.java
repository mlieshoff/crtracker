package crtracker.api;

import jcrapi.Api;
import jcrapi.model.Clan;
import jcrapi.model.Member;
import jcrapi.request.ClanRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoyalApi {

    private final String url;
    private final String token;

    private Api api;

    public ClanData getClanData(String clanTag) {
        api = new Api(url, token);
        Clan clan = api.getClan(ClanRequest.builder(removeRouteIfNeeded(clanTag)).build());
        ClanData clanData = new ClanData(addRouteIfNeeded(clan.getTag()));
        for (Member member : clan.getMembers()) {
            ClanDataMember clanDataMember = new ClanDataMember(addRouteIfNeeded(member.getTag()), member.getName(), member.getRole(), member.getDonations());
            clanData.getClanDataMembers().add(clanDataMember);
        }
        return clanData;
    }

    private String addRouteIfNeeded(String tag) {
        if (!tag.startsWith("#")) {
            return "#" + tag;
        }
        return tag;
    }

    private String removeRouteIfNeeded(String tag) {
        if (tag.startsWith("#")) {
            return tag.substring(1);
        }
        return tag;
    }

}

package crtracker.api;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClanData {

    private final String tag;

    private final List<ClanDataMember> clanDataMembers = new ArrayList<>();

}

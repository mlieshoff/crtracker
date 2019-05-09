package crtracker.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClanDataMember {

    private final String tag;
    private final String name;
    private final String role;

    private final int donations;

}

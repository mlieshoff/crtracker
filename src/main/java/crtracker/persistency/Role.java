package crtracker.persistency;

import com.google.common.collect.ImmutableMap;

/**
 * @author Michael Lieshoff
 */
public enum Role {

    LEADER(2),
    COLEADER(4),
    ELDER(3),
    MEMBER(1);

    private static final ImmutableMap<Integer, Role> LOOKUP_BY_CODE;
    private static final ImmutableMap<String, Role> LOOKUP_BY_NAME;

    static {
        ImmutableMap.Builder<Integer, Role> builder = ImmutableMap.<Integer, Role>builder();
        ImmutableMap.Builder<String, Role> builder2 = ImmutableMap.<String, Role>builder();
        for (Role role : values()) {
            builder.put(role.getCode(), role);
            builder2.put(role.name(), role);
        }
        LOOKUP_BY_CODE = builder.build();
        LOOKUP_BY_NAME = builder2.build();
    }

    private final int code;

    Role(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Role forCode(int role) {
        return LOOKUP_BY_CODE.get(role);
    }
    
    public static Role forName(String name) {
        return LOOKUP_BY_NAME.get(name.toUpperCase());
    }

}

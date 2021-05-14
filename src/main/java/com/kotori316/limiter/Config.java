package com.kotori316.limiter;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final Config INSTANCE = new Config();
    private ForgeConfigSpec.IntValue permission;

    public static Config getInstance() {
        return INSTANCE;
    }

    private Config() {
    }

    public ForgeConfigSpec setup(ForgeConfigSpec.Builder builder) {
        permission = builder.comment("The permission level required to execute command. 2 requires cheat enabled.")
            .defineInRange("lms.permission_level", 2, 0, 4);
        return builder.build();
    }

    public int getPermission() {
        return permission.get();
    }
}

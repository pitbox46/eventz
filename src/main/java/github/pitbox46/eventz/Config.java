package github.pitbox46.eventz;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final String CATEGORY_GENERAL = "general";

    public static ForgeConfigSpec SERVER_CONFIG;

    public static ForgeConfigSpec.IntValue LOWER_COOLDOWN;
    public static ForgeConfigSpec.IntValue UPPER_COOLDOWN;

    static {
        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

        SERVER_BUILDER.comment("General Settings").push(CATEGORY_GENERAL);

        LOWER_COOLDOWN = SERVER_BUILDER.comment("Lower bound on the cooldown between events in minutes")
                .defineInRange("lower_cooldown", 60, 0, Integer.MAX_VALUE);
        UPPER_COOLDOWN = SERVER_BUILDER.comment("Upper bound on the cooldown between events in minutes")
                .defineInRange("upper_cooldown", 120, 0, Integer.MAX_VALUE);

        SERVER_BUILDER.pop();
        SERVER_CONFIG = SERVER_BUILDER.build();
    }
}

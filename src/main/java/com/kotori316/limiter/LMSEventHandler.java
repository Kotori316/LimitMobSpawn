package com.kotori316.limiter;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;

@Mod.EventBusSubscriber(modid = LimitMobSpawn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LMSEventHandler {
    public static final Marker LMS_MARKER = MarkerManager.getMarker("LMS");

    /**
     * The last guard to prevent mob spawning.
     * This method is for initial spawning of Structures and other spawning.
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // MISCs(item frame, ender pearl, potion cloud, etc.) should be allowed to be spawned.
        // Boss monsters(Ender Dragon, Wither) should be spawned.
        Entity entity = event.getEntity();
        if (entity.getType().getCategory() == MobCategory.MISC || !entity.canChangeDimensions())
            return;
        LazyOptional<LMSHandler> maybeHandler = event.getLevel().getCapability(Caps.getLmsCapability());

        if (LMSHandler.getCombinedForce(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn1 -> spawn1.test(event.getLevel(), entity.blockPosition(), entity.getType(), null)) ||
            LMSHandler.getCombinedDefault(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn -> spawn.test(event.getLevel(), entity.blockPosition(), entity.getType(), null)))
            return; // SKIP
        if (LMSHandler.getCombinedDeny(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn1 -> spawn1.test(event.getLevel(), entity.blockPosition(), entity.getType(), null))) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMS_MARKER, "onEntityJoinWorld denied spawning of {}.", entity);
            event.setCanceled(true);
        }
    }
}

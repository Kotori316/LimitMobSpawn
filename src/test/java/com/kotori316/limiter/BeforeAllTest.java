package com.kotori316.limiter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import cpw.mods.modlauncher.Launcher;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class BeforeAllTest {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    static void beforeAll() {
        BeforeAllTest.initialize();
    }

    public static synchronized void initialize() {
        if (!INITIALIZED.getAndSet(true)) {
            SharedConstants.tryDetectVersion();
            // initLoader();
            changeDist();
            assertEquals(Dist.CLIENT, FMLEnvironment.dist);
            Bootstrap.bootStrap();
        }
    }

    private static void changeDist() {
        try {
            Field dist = FMLLoader.class.getDeclaredField("dist");
            dist.setAccessible(true);
            dist.set(null, Dist.CLIENT);
        } catch (Exception e) {
            fail(e);
        }
    }

    private static void initLoader() {
        try {
            Constructor<Launcher> constructor = Launcher.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Exception e) {
            fail(e);
        }
    }

    protected static void testCycle(TestSpawn limit) {
        assertAll(
            () -> assertEquals(limit, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(JsonOps.INSTANCE, limit.to(JsonOps.INSTANCE)))),
            () -> assertEquals(limit, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(JsonOps.COMPRESSED, limit.to(JsonOps.COMPRESSED)))),
            () -> assertEquals(limit, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(NbtOps.INSTANCE, limit.to(NbtOps.INSTANCE))))
        );
    }

}

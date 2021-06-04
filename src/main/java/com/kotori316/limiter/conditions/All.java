package com.kotori316.limiter.conditions;

import java.util.Collections;
import java.util.Set;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.TestSpawn;

public class All implements TestSpawn {
    private static final All INSTANCE = new All();
    public static final TestSpawn.Serializer<All> SERIALIZER = new Serializer();

    public static All getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable SpawnReason reason) {
        return true;
    }

    @Override
    public String toString() {
        return "All Mobs";
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return toString();
    }

    private static class Serializer extends TestSpawn.Serializer<All> {
        @Override
        public String getType() {
            return "all";
        }

        @Override
        public <T> All from(Dynamic<T> dynamic) {
            return getInstance();
        }

        @Override
        public <T> T to(TestSpawn a, DynamicOps<T> ops) {
            return ops.emptyMap();
        }
        @Override
        public Set<String> propertyKeys() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, ISuggestionProvider provider) {
            return Collections.emptySet();
        }
    }
}

package com.kotori316.limiter.conditions;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
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
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<All> {
        @Override
        public String getType() {
            return "all";
        }

        @Override
        public All fromJson(JsonObject object) {
            return getInstance();
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            return new JsonObject();
        }
    }
}

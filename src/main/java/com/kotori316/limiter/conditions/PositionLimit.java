package com.kotori316.limiter.conditions;

import java.util.Objects;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class PositionLimit implements TestSpawn {
    public static final TestSpawn.Serializer<PositionLimit> SERIALIZER = new Serializer();
    private final int minX, maxX, minY, maxY, minZ, maxZ;

    public PositionLimit(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public PositionLimit(BlockPos pos1, BlockPos pos2) {
        minX = Math.min(pos1.getX(), pos2.getX());
        maxX = Math.max(pos1.getX(), pos2.getX());
        minY = Math.min(pos1.getY(), pos2.getY());
        maxY = Math.max(pos1.getY(), pos2.getY());
        minZ = Math.min(pos1.getZ(), pos2.getZ());
        maxZ = Math.max(pos1.getZ(), pos2.getZ());
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable SpawnReason reason) {
        return minX <= pos.getX() && pos.getX() < maxX &&
            minY <= pos.getY() && pos.getY() < maxY &&
            minZ <= pos.getZ() && pos.getZ() < maxZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PositionLimit that = (PositionLimit) o;
        return minX == that.minX && maxX == that.maxX && minY == that.minY && maxY == that.maxY && minZ == that.minZ && maxZ == that.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, maxX, minY, maxY, minZ, maxZ);
    }

    @Override
    public String toString() {
        return "PositionLimit{" +
            '(' + minX + ", " + minY + ", " + minZ + ") -> (" + maxX + ", " + maxY + ", " + maxZ + ')' +
            '}';
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<PositionLimit> {
        @Override
        public String getType() {
            return "dimension";
        }

        @Override
        public PositionLimit fromJson(JsonObject object) {
            return BlockPos.CODEC.decode(JsonOps.INSTANCE, object.get("pos1")).flatMap(pos1 ->
                BlockPos.CODEC.decode(JsonOps.INSTANCE, object.get("pos2")).map(pos2 ->
                    new PositionLimit(pos1.getFirst(), pos2.getFirst())))
                .getOrThrow(true, s -> LimitMobSpawn.LOGGER.error("Erred when loading PositionLimit, {}", s));
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            PositionLimit l = (PositionLimit) t;
            JsonObject object = new JsonObject();
            BlockPos.CODEC.encodeStart(JsonOps.INSTANCE, new BlockPos(l.minX, l.minY, l.minZ)).result().ifPresent(
                pos1 -> object.add("pos1", pos1)
            );
            BlockPos.CODEC.encodeStart(JsonOps.INSTANCE, new BlockPos(l.maxX, l.maxY, l.maxZ)).result().ifPresent(
                pos2 -> object.add("pos2", pos2)
            );
            return object;
        }
    }
}

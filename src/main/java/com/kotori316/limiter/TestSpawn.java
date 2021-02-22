package com.kotori316.limiter;

import java.util.Objects;

import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public interface TestSpawn {

    boolean test(EntitySpawnPlacementRegistry.PlacementType placeType,
                 IWorldReader worldIn,
                 BlockPos pos,
                 EntityType<?> entityTypeIn);

    default TestSpawn and(TestSpawn other) {
        return new And(this, other);
    }

    default TestSpawn not() {
        return new Not(this);
    }

    class And implements TestSpawn {
        private final TestSpawn t1, t2;

        public And(TestSpawn t1, TestSpawn t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        @Override
        public boolean test(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn) {
            return t1.test(placeType, worldIn, pos, entityTypeIn) && t2.test(placeType, worldIn, pos, entityTypeIn);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            And and = (And) o;
            return Objects.equals(t1, and.t1) && Objects.equals(t2, and.t2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(t1, t2);
        }

        @Override
        public String toString() {
            return "And{" +
                "t1=" + t1 +
                ", t2=" + t2 +
                '}';
        }
    }

    class Not implements TestSpawn {
        private final TestSpawn t1;

        public Not(TestSpawn t1) {
            this.t1 = t1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Not not = (Not) o;
            return Objects.equals(t1, not.t1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(t1);
        }

        @Override
        public String toString() {
            return "Not{" +
                "t1=" + t1 +
                '}';
        }

        @Override
        public boolean test(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn) {
            return !t1.test(placeType, worldIn, pos, entityTypeIn);
        }

        @Override
        public TestSpawn not() {
            return t1;
        }
    }

    class DimensionLimit implements TestSpawn {
        private final RegistryKey<World> type;

        public DimensionLimit(RegistryKey<World> type) {
            this.type = type;
        }

        @Override
        public boolean test(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn) {
            RegistryKey<World> type;
            if (worldIn instanceof World) {
                World world = (World) worldIn;
                type = world.getDimensionKey();
            } else {
                type = World.OVERWORLD;
            }
            return this.type == type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DimensionLimit that = (DimensionLimit) o;
            return Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }

        @Override
        public String toString() {
            return "DimensionLimit{" + "type=" + type + '}';
        }
    }

    class EntityLimit implements TestSpawn {
        private final EntityType<?> type;

        public EntityLimit(EntityType<?> type) {
            this.type = type;
        }

        @Override
        public boolean test(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn) {
            return this.type.equals(entityTypeIn);
        }

        @Override
        public String toString() {
            return "EntityLimit{" +
                "type=" + type + '(' + type.getRegistryName() + ')' +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntityLimit that = (EntityLimit) o;
            return Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type);
        }
    }
}

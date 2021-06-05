package com.kotori316.limiter.conditions;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;

import com.kotori316.limiter.TestSpawn;

public class BiomeLimit implements TestSpawn {
    public static final TestSpawn.Serializer<BiomeLimit> SERIALIZER = new BiomeSerializer();
    @Nonnull
    private final RegistryKey<Biome> biomeRegistryKey;

    public BiomeLimit(@Nonnull RegistryKey<Biome> biomeRegistryKey) {
        this.biomeRegistryKey = biomeRegistryKey;
    }

    public BiomeLimit(@Nonnull ResourceLocation biome) {
        this(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, biome));
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable SpawnReason reason) {
        if (worldIn instanceof IWorldReader) {
            IWorldReader worldReader = (IWorldReader) worldIn;
            Biome biome = worldReader.getBiome(pos);
            return test(biome);
        }
        return false;
    }

    public boolean test(Biome biome) {
        return biomeRegistryKey.getLocation().equals(biome.getRegistryName());
    }

    @Override
    public String toString() {
        return "BiomeLimit{" +
            "biome=" + biomeRegistryKey +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiomeLimit that = (BiomeLimit) o;
        return Objects.equals(biomeRegistryKey, that.biomeRegistryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(biomeRegistryKey);
    }

    @Override
    public Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return "biome " + biomeRegistryKey.getLocation();
    }

    private static final class BiomeSerializer extends StringLimitSerializer<BiomeLimit, RegistryKey<Biome>> {
        @Override
        public String getType() {
            return "biome";
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, @Nullable ISuggestionProvider provider) {
            if (provider == null || !property.equals(saveKey()))
                return Collections.emptySet();
            return provider.func_241861_q()
                .getRegistry(Registry.BIOME_KEY)
                .keySet()
                .stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());
        }

        @Override
        public RegistryKey<Biome> fromString(String s) {
            return RegistryKey.getOrCreateKey(Registry.BIOME_KEY, new ResourceLocation(s));
        }

        @Override
        public String valueToString(RegistryKey<Biome> biomeRegistryKey) {
            return biomeRegistryKey.getLocation().toString();
        }

        @Override
        public String saveKey() {
            return "biome";
        }

        @Override
        public BiomeLimit instance(RegistryKey<Biome> biomeRegistryKey) {
            return new BiomeLimit(biomeRegistryKey);
        }

        @Override
        public RegistryKey<Biome> getter(BiomeLimit dimensionLimit) {
            return dimensionLimit.biomeRegistryKey;
        }
    }
}

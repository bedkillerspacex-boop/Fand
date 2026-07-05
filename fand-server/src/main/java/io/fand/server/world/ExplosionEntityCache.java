package io.fand.server.world;

import java.util.List;
import java.util.Objects;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Per-level cache for explosion entity queries.
 * <p>
 * Caches the result of {@code level.getEntities(source, aabb)} keyed by the
 * exact query bounds and excluded source entity. This avoids repeated expensive
 * section traversals when many explosions issue the same entity query in one tick.
 */
public final class ExplosionEntityCache {

    private final Object2ObjectMap<QueryKey, List<Entity>> cache = new Object2ObjectOpenHashMap<>();

    public List<Entity> getEntities(net.minecraft.server.level.ServerLevel level,
                                    Vec3 center,
                                    double doubleRadius,
                                    @Nullable Entity source) {
        int x0 = Mth.floor(center.x - doubleRadius - 1.0);
        int x1 = Mth.floor(center.x + doubleRadius + 1.0);
        int y0 = Mth.floor(center.y - doubleRadius - 1.0);
        int y1 = Mth.floor(center.y + doubleRadius + 1.0);
        int z0 = Mth.floor(center.z - doubleRadius - 1.0);
        int z1 = Mth.floor(center.z + doubleRadius + 1.0);
        var queryKey = new QueryKey(x0, x1, y0, y1, z0, z1, source);
        return cache.computeIfAbsent(queryKey, k -> {
            AABB aabb = new AABB(x0, y0, z0, x1, y1, z1);
            return level.getEntities(source, aabb);
        });
    }

    public void clear() {
        cache.clear();
    }

    private record QueryKey(
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ,
            @Nullable Entity source
    ) {

        @Override
        public boolean equals(Object other) {
            return other instanceof QueryKey that
                    && minX == that.minX
                    && maxX == that.maxX
                    && minY == that.minY
                    && maxY == that.maxY
                    && minZ == that.minZ
                    && maxZ == that.maxZ
                    && source == that.source;
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hash(minX, maxX, minY, maxY, minZ, maxZ) + System.identityHashCode(source);
        }
    }
}

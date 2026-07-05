package io.fand.api.world;

/**
 * Immutable chunk coordinate pair.
 */
public record ChunkPos(int x, int z) {

    public static ChunkPos of(int x, int z) {
        return new ChunkPos(x, z);
    }

    public static ChunkPos containing(int blockX, int blockZ) {
        return new ChunkPos(Math.floorDiv(blockX, 16), Math.floorDiv(blockZ, 16));
    }

    public static ChunkPos containing(Location location) {
        java.util.Objects.requireNonNull(location, "location");
        return containing(location.blockX(), location.blockZ());
    }

    public int minBlockX() {
        return x << 4;
    }

    public int minBlockZ() {
        return z << 4;
    }

    public int maxBlockX() {
        return minBlockX() + 15;
    }

    public int maxBlockZ() {
        return minBlockZ() + 15;
    }

    public long distanceSquared(ChunkPos other) {
        java.util.Objects.requireNonNull(other, "other");
        long dx = (long) x - other.x;
        long dz = (long) z - other.z;
        return dx * dx + dz * dz;
    }

    public int chebyshevDistance(ChunkPos other) {
        java.util.Objects.requireNonNull(other, "other");
        long dx = Math.abs((long) x - other.x);
        long dz = Math.abs((long) z - other.z);
        long distance = Math.max(dx, dz);
        return distance > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) distance;
    }

    public ChunkRegion regionAround(int radius) {
        return ChunkRegion.around(this, radius);
    }
}

package io.fand.server.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

final class ExplosionEntityCacheTest {

    @Test
    void sameQueryReusesEntityList() {
        var level = mock(ServerLevel.class);
        List<Entity> entities = List.of();
        when(level.getEntities(isNull(), any(AABB.class))).thenReturn(entities);
        var cache = new ExplosionEntityCache();

        var first = cache.getEntities(level, new Vec3(10.2, 64.5, -3.7), 8.0D, null);
        var second = cache.getEntities(level, new Vec3(10.2, 64.5, -3.7), 8.0D, null);

        assertThat(first).isSameAs(entities);
        assertThat(second).isSameAs(entities);
        verify(level).getEntities(isNull(), any(AABB.class));
        verifyNoMoreInteractions(level);
    }

    @Test
    void differentRadiusUsesDifferentQueryBounds() {
        var level = mock(ServerLevel.class);
        List<Entity> narrow = List.of();
        List<Entity> wide = List.of(mock(Entity.class));
        when(level.getEntities(isNull(), any(AABB.class))).thenReturn(narrow, wide);
        var cache = new ExplosionEntityCache();
        var center = new Vec3(10.2, 64.5, -3.7);

        var first = cache.getEntities(level, center, 4.0D, null);
        var second = cache.getEntities(level, center, 8.0D, null);

        assertThat(first).isSameAs(narrow);
        assertThat(second).isSameAs(wide);
        var bounds = ArgumentCaptor.forClass(AABB.class);
        verify(level).getEntities(isNull(), bounds.capture());
        verify(level).getEntities(isNull(), bounds.capture());
        assertThat(bounds.getAllValues()).containsExactly(
                new AABB(5.0D, 59.0D, -9.0D, 15.0D, 69.0D, 5.0D),
                new AABB(1.0D, 55.0D, -13.0D, 19.0D, 73.0D, 9.0D));
    }

    @Test
    void differentSourceEntityUsesDifferentQuery() {
        var level = mock(ServerLevel.class);
        var firstSource = mock(Entity.class);
        var secondSource = mock(Entity.class);
        when(level.getEntities(any(Entity.class), any(AABB.class))).thenReturn(List.of(), List.of());
        var cache = new ExplosionEntityCache();
        var center = new Vec3(10.2, 64.5, -3.7);

        cache.getEntities(level, center, 4.0D, firstSource);
        cache.getEntities(level, center, 4.0D, secondSource);

        verify(level).getEntities(same(firstSource), any(AABB.class));
        verify(level).getEntities(same(secondSource), any(AABB.class));
    }
}

package io.fand.server.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import io.fand.api.structure.CustomStructureSet;
import io.fand.api.structure.StructureFormat;
import io.fand.api.structure.StructureGenerationPlacement;
import io.fand.api.structure.StructurePlacement;
import io.fand.api.structure.StructureProjection;
import io.fand.api.structure.StructureRegistration;
import io.fand.api.structure.StructureService;
import io.fand.api.structure.StructureSetEntry;
import io.fand.api.structure.StructureTemplate;
import io.fand.api.structure.StructureVolume;
import io.fand.api.world.Location;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

final class PluginStructureServiceTest {

    @Test
    void preservesMinecraftStructureReferencesWhileScopingPluginOwnedKeys() {
        var delegate = new FakeStructureService();
        var service = new PluginStructureService(delegate, new PluginResourceTracker(), "demo");
        var set = new CustomStructureSet(
                Key.key("external:overworld_features"),
                List.of(
                        new StructureSetEntry(Key.key("minecraft:village_plains"), 1),
                        new StructureSetEntry(Key.key("external:watchtower"), 2)),
                StructureGenerationPlacement.randomSpread(24, 8, 12345));

        var registration = service.registerStructureSet(set);
        service.locate(Key.key("minecraft:village_plains"), null, 8);

        assertThat(registration.key()).isEqualTo(Key.key("demo:overworld_features"));
        assertThat(delegate.registeredStructureSet(Key.key("demo:overworld_features")).orElseThrow().structures())
                .extracting(StructureSetEntry::structure)
                .containsExactly(Key.key("minecraft:village_plains"), Key.key("demo:watchtower"));
        assertThat(delegate.lastLocatedStructure).isEqualTo(Key.key("minecraft:village_plains"));
    }

    private static final class FakeStructureService implements StructureService {

        private final Map<Key, CustomStructureSet> structureSets = new LinkedHashMap<>();
        private Key lastLocatedStructure;

        @Override
        public Optional<CustomStructureSet> registeredStructureSet(Key key) {
            return Optional.ofNullable(structureSets.get(key));
        }

        @Override
        public StructureRegistration registerStructureSet(CustomStructureSet structureSet) {
            structureSets.put(structureSet.key(), structureSet);
            return new FakeStructureRegistration(structureSet.key(), () -> structureSets.remove(structureSet.key()));
        }

        @Override
        public Optional<StructureTemplate> template(Key key) {
            return Optional.empty();
        }

        @Override
        public CompletableFuture<Boolean> save(Key key, StructureVolume volume) {
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public CompletableFuture<Optional<StructureProjection>> exportTemplate(Key key, StructureFormat format) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        @Override
        public CompletableFuture<Boolean> importTemplate(Key key, StructureProjection projection) {
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public CompletableFuture<Optional<StructureProjection>> load(Path path, StructureFormat format) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        @Override
        public CompletableFuture<Boolean> save(Key key, Path path, StructureFormat format) {
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public CompletableFuture<Boolean> place(Key key, Location origin, StructurePlacement placement) {
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public CompletableFuture<Optional<Location>> locate(Key structure, Location origin, int radius) {
            lastLocatedStructure = structure;
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    private static final class FakeStructureRegistration implements StructureRegistration {

        private final Key key;
        private final Runnable unregister;
        private boolean active = true;

        private FakeStructureRegistration(Key key, Runnable unregister) {
            this.key = key;
            this.unregister = unregister;
        }

        @Override
        public Key key() {
            return key;
        }

        @Override
        public boolean active() {
            return active;
        }

        @Override
        public void unregister() {
            if (active) {
                active = false;
                unregister.run();
            }
        }
    }
}

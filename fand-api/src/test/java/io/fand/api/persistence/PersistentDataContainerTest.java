package io.fand.api.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

final class PersistentDataContainerTest {

    @Test
    void valuesAccessorDoesNotExposeMutableState() {
        var key = Key.key("fand:owner");
        var container = PersistentDataContainer.EMPTY.withString(key, "alice");

        container.values().addProperty(key.asString(), "mallory");

        assertThat(container.getString(key)).contains("alice");
    }

    @Test
    void constructorDefensivelyCopiesValues() {
        var key = Key.key("fand:owner");
        var values = new JsonObject();
        values.addProperty(key.asString(), "alice");

        var container = new PersistentDataContainer(values);
        values.addProperty(key.asString(), "mallory");

        assertThat(container.getString(key)).contains("alice");
    }
}

package io.fand.server.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

final class ServerThreadingTest {

    @Test
    void callBlockingRethrowsTaskFailureWithoutCompletionWrapper() {
        var failure = new IllegalArgumentException("boom");
        var future = new CompletableFuture<Void>();
        future.completeExceptionally(failure);

        assertThatThrownBy(() -> ServerThreading.joinBlocking(future)).isSameAs(failure);
    }
}

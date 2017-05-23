package io.muon.jflow.core;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@FunctionalInterface
public interface SerializableFunction<I, O> extends Serializable, Function<I, O> {

    default AsyncSerializableFunction<I, O> toAsync() {
        return i -> CompletableFuture.completedFuture(apply(i));
    }
}

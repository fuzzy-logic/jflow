package io.muon.jflow.core;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@FunctionalInterface
public interface AsyncSerializableFunction<I, O> extends Serializable, Function<I, CompletableFuture<O>> {
    static <I, O> AsyncSerializableFunction<I, O> of(Function<I, CompletableFuture<O>> f) {
        return f::apply;
    }

    default <O2> AsyncSerializableFunction<I, O2> andThenAsync(AsyncSerializableFunction<O, O2> f) {
        return i -> apply(i).thenCompose(f);
    }
}

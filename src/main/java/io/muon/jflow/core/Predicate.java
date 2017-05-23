package io.muon.jflow.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by gawain on 19/05/2017.
 */
public interface Predicate<I> extends Serializable, AsyncSerializableFunction<I, Boolean> {

    static <I> Predicate<I> sync(String name, SerializableFunction<I, Boolean> function) {
        return async(name, function.toAsync());
    }

    static <I> Predicate<I> async(String name, AsyncSerializableFunction<I, Boolean> function) {
        return new Predicate<I>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public CompletableFuture<Boolean> apply(I i) {
                return function.apply(i);
            }
        };
    }

    default CompletableFuture<Boolean> check(I result) {
        return apply(result);
    }

    String getName();
}

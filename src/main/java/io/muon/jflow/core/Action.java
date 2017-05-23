package io.muon.jflow.core;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Created by gawain on 19/05/2017.
 */
public interface Action<I, O> extends Serializable, AsyncSerializableFunction<I, O> {

    static <I, O> Action<I, O> sync(String name, final SerializableFunction<I, O> behaviour) {
        return async(name, behaviour.toAsync());
    }

    static <I, O> Action<I, O> async(String name, final AsyncSerializableFunction<I, O> behaviour) {
        return new Action<I, O>() {

            @Override
            public CompletableFuture<O> apply(I nextStepInput) {
                return behaviour.apply(nextStepInput);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    default CompletableFuture<O> run(I nextStepInput) {
        return apply(nextStepInput);
    }

    String getName();

}

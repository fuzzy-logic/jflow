package io.muon.jflow.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by gawain on 19/05/2017.
 */
public interface Flow<I, O> {

    static <I, O> Terminal<I, O> of(Action<I, O> action) {
        return new Terminal<I, O>(action);
    }

    static <I, O> Branch<I, O> branch(Predicate<I> predicate, Flow<I, O> flowIfTrue, Flow<I, O> flowIfFalse) {
        return new Branch<>(predicate, flowIfTrue, flowIfFalse);
    }

    CompletableFuture<O> run(I input);
    void describe(FlowDescription description);

    default String describe() {
        return new FlowDescription().describe(this::describe).toString();
    }

    final class Terminal<I, O> implements Flow<I, O> {

        private final Action<I, O> action;

        private Terminal(Action<I, O> action) {
            this.action = action;
        }

        @Override
        public CompletableFuture<O> run(I input) {
            return action.run(input);
        }

        @Override
        public void describe(FlowDescription description) {
            description.append(action.getName());
        }

        public <N> Pair<I, O, N> add(Action<O, N> nextFlow) {
            return add(of(nextFlow));
        }

        public <N> Pair<I, O, N> add(Flow<O, N> nextFlow) {
            return new Pair<>(of(action), nextFlow);
        }

        public Branch<I, O> branch(Predicate<I> predicate, Flow<I, O> flowOnBranch) {
            return new Branch<>(predicate, flowOnBranch, this);
        }
    }

    final class Pair<I, O, N> implements Flow<I, N> {
        private final Flow<I, O> firstFlow;
        private final Flow<O, N> nextFlow;

        private Pair(Flow<I, O> firstFlow, Flow<O, N> nextFlow) {
            this.firstFlow = firstFlow;
            this.nextFlow = nextFlow;
        }

        @Override
        public CompletableFuture<N> run(I input) {
            return firstFlow.run(input).thenCompose(nextFlow::run);
        }

        @Override
        public void describe(FlowDescription description) {
            description.append("Pair:")
                    .indent()
                        .newline().append("First: ").describe(firstFlow::describe)
                        .newline().append("Then: ").describe(nextFlow::describe)
                    .outdent().newline();
        }

        public <N2> Sequence<I, N2> add(Flow<N, N2> next) {
            return new Sequence<I, N2>(firstFlow, Collections.singletonList(nextFlow), next);
        }

        public Pair<I, O, N> branch(Predicate<O> predicate, Flow<O, N> flowOnBranch) {
            return new Pair<>(firstFlow, new Branch<>(predicate, flowOnBranch, nextFlow));
        }

        public Pair<I, O, N> branch(Predicate<O> predicate, Action<O, N> actionOnBranch) {
            return branch(predicate, of(actionOnBranch));
        }
    }

    final class Sequence<I, O> implements Flow<I, O> {

        private final Flow firstFlow;
        private final List<Flow> intermediateFlows;
        private final Flow lastflow;

        public Sequence(Flow<I, ?> firstFlow, List<Flow> intermediateFlows, Flow<?, O> lastflow) {
            this.firstFlow = firstFlow;
            this.intermediateFlows = intermediateFlows;
            this.lastflow = lastflow;
        }

        @Override
        public CompletableFuture<O> run(I input) {
            AsyncSerializableFunction<I, O> asyncSerializableFunction = (AsyncSerializableFunction<I, O>)
                    intermediateFlows.stream().map(flow -> (AsyncSerializableFunction) AsyncSerializableFunction.of(flow::run))
                    .reduce(
                            AsyncSerializableFunction.of(firstFlow::run),
                            (f1, f2) -> f1.andThenAsync(f2))
                    .andThenAsync(lastflow::run);
            return asyncSerializableFunction.apply(input);
        }

        public <N2> Sequence<I, N2> add(Flow<O, N2> next) {
            List<Flow> intermediate = Stream.concat(intermediateFlows.stream(), Stream.of(lastflow)).collect(Collectors.toList());
            return new Sequence<I, N2>(firstFlow, intermediate, next);
        }

        @Override
        public void describe(FlowDescription description) {
            description.append("Sequence:").indent()
                    .newline().append("1: ").describe(firstFlow::describe);

            int index = 2;
            Iterator<Flow> flowIterator = intermediateFlows.iterator();
            while (flowIterator.hasNext()) {
                description.newline().append(Integer.toString(index)).append(": ").describe(flowIterator.next()::describe);
                index ++;
            }
            description.newline().append(Integer.toString(index)).append(": ").describe(lastflow::describe);
            description.outdent().newline();
        }
    }

    final class Branch<I, O> implements Flow<I, O> {

        private final Predicate<I> predicate;
        private final Flow<I, O> flowIfTrue;
        private final Flow<I, O> flowIfFalse;

        private Branch(Predicate<I> predicate, Flow<I, O> flowIfTrue, Flow<I, O> flowIfFalse) {
            this.predicate = predicate;
            this.flowIfTrue = flowIfTrue;
            this.flowIfFalse = flowIfFalse;
        }

        @Override
        public CompletableFuture<O> run(I input) {
            return predicate.check(input).thenCompose(matches -> matches
                        ? flowIfTrue.run(input)
                        : flowIfFalse.run(input));
        }

        public Branch<I, O> add(Predicate<I> predicate, Flow<I, O> flowOnBranch) {
            return new Branch<>(predicate, flowOnBranch, this);
        }

        @Override
        public void describe(FlowDescription description) {
            description.append("Branch on ").append(predicate.getName()).indent()
                .newline().append("If true: ").describe(flowIfTrue::describe)
                .newline().append("If false: ").describe(flowIfFalse::describe)
            .outdent().newline();
        }
    }


}

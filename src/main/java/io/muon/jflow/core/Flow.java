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

        public <N> Pair<I, O, N> add(Action<O, N> nextStep) {
            return add(of(nextStep));
        }

        public <N> Pair<I, O, N> add(Flow<O, N> nextStep) {
            return new Pair<>(of(action), nextStep);
        }

        public Branch<I, O> branch(Predicate<I> predicate, Flow<I, O> stepOnBranch) {
            return new Branch<>(predicate, stepOnBranch, this);
        }
    }

    final class Pair<I, O, N> implements Flow<I, N> {
        private final Flow<I, O> firstStep;
        private final Flow<O, N> nextStep;

        private Pair(Flow<I, O> firstStep, Flow<O, N> nextStep) {
            this.firstStep = firstStep;
            this.nextStep = nextStep;
        }

        @Override
        public CompletableFuture<N> run(I input) {
            return firstStep.run(input).thenCompose(nextStep::run);
        }

        @Override
        public void describe(FlowDescription description) {
            description.append("Pair:")
                    .indent()
                        .newline().append("First: ").describe(firstStep::describe)
                        .newline().append("Then: ").describe(nextStep::describe)
                    .outdent().newline();
        }

        public <N2> Sequence<I, N2> add(Flow<N, N2> next) {
            return new Sequence<I, N2>(firstStep, Collections.singletonList(nextStep), next);
        }

        public Pair<I, O, N> branch(Predicate<O> predicate, Flow<O, N> stepOnBranch) {
            return new Pair<>(firstStep, new Branch<>(predicate, stepOnBranch, nextStep));
        }

        public Pair<I, O, N> branch(Predicate<O> predicate, Action<O, N> actionOnBranch) {
            return branch(predicate, of(actionOnBranch));
        }
    }

    final class Sequence<I, O> implements Flow<I, O> {

        private final Flow firstStep;
        private final List<Flow> intermediateSteps;
        private final Flow lastStep;

        public Sequence(Flow<I, ?> firstStep, List<Flow> intermediateSteps, Flow<?, O> lastStep) {
            this.firstStep = firstStep;
            this.intermediateSteps = intermediateSteps;
            this.lastStep = lastStep;
        }

        @Override
        public CompletableFuture<O> run(I input) {
            AsyncSerializableFunction<I, O> asyncSerializableFunction = (AsyncSerializableFunction<I, O>)
                    intermediateSteps.stream().map(step -> (AsyncSerializableFunction) AsyncSerializableFunction.of(step::run))
                    .reduce(
                            AsyncSerializableFunction.of(firstStep::run),
                            (f1, f2) -> f1.andThenAsync(f2))
                    .andThenAsync(lastStep::run);
            return asyncSerializableFunction.apply(input);
        }

        public <N2> Sequence<I, N2> add(Flow<O, N2> next) {
            List<Flow> intermediate = Stream.concat(intermediateSteps.stream(), Stream.of(lastStep)).collect(Collectors.toList());
            return new Sequence<I, N2>(firstStep, intermediate, next);
        }

        @Override
        public void describe(FlowDescription description) {
            description.append("Sequence:").indent()
                    .newline().append("1: ").describe(firstStep::describe);

            int index = 2;
            Iterator<Flow> stepIterator = intermediateSteps.iterator();
            while (stepIterator.hasNext()) {
                description.newline().append(Integer.toString(index)).append(": ").describe(stepIterator.next()::describe);
                index ++;
            }
            description.newline().append(Integer.toString(index)).append(": ").describe(lastStep::describe);
            description.outdent().newline();
        }
    }

    final class Branch<I, O> implements Flow<I, O> {

        private final Predicate<I> predicate;
        private final Flow<I, O> stepIfTrue;
        private final Flow<I, O> stepIfFalse;

        private Branch(Predicate<I> predicate, Flow<I, O> stepIfTrue, Flow<I, O> stepIfFalse) {
            this.predicate = predicate;
            this.stepIfTrue = stepIfTrue;
            this.stepIfFalse = stepIfFalse;
        }

        @Override
        public CompletableFuture<O> run(I input) {
            return predicate.check(input).thenCompose(matches -> matches
                        ? stepIfTrue.run(input)
                        : stepIfFalse.run(input));
        }

        public Branch<I, O> add(Predicate<I> predicate, Flow<I, O> stepOnBranch) {
            return new Branch<>(predicate, stepOnBranch, this);
        }

        @Override
        public void describe(FlowDescription description) {
            description.append("Branch on ").append(predicate.getName()).indent()
                .newline().append("If true: ").describe(stepIfTrue::describe)
                .newline().append("If false: ").describe(stepIfFalse::describe)
            .outdent().newline();
        }
    }


}

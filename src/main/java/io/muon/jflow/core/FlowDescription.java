package io.muon.jflow.core;

import java.util.function.Consumer;
import java.util.stream.IntStream;

public class FlowDescription {

    private final StringBuilder stringBuilder = new StringBuilder();
    private int indentLevel = 0;

    public FlowDescription append(String text) {
        stringBuilder.append(text);
        return this;
    }

    public FlowDescription describe(Consumer<FlowDescription> selfDescribing) {
        selfDescribing.accept(this);
        return this;
    }

    public FlowDescription indent() {
        indentLevel++;
        return this;
    }

    public FlowDescription outdent() {
        indentLevel--;
        return this;
    }

    public FlowDescription newline() {
        append("\n");
        IntStream.range(0, indentLevel).forEach(i -> append("\t"));
        return this;
    }

    public String toString() {
        return stringBuilder.toString();
    }
}

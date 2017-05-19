package io.muon.jflow.core;

/**
 * Created by gawain on 19/05/2017.
 */

public class JFlow {

    Step firstStep;
    Step lastStep;
    Object lastResult;





    public JFlow firstStep(Action action) {
        Step step  = new Step(action);
        firstStep = step;
        lastStep = step;
        return this;
    }

    public JFlow addStep(Action action) {
        Step step  = new Step(action);
        firstStep.addStep(step);
        lastStep = step;
        return this;
    }

    public JFlow addStep(Action action, Predicate predicate) {
        firstStep = new Step(action);
        return this;
    }

    public Object enter(Object initialInput) {
        if (firstStep == null) {
            throw new RuntimeException("cannot enter flow with any steps set");
        }
        boolean running = true;
        Step nextStep = firstStep;
        Object input = initialInput;
        Object result = null;
        while(running) {
            result = nextStep.run(input);
            nextStep = nextStep.getNextStep();
            if (nextStep == null) running = false;
        }
        return result;
    }
}

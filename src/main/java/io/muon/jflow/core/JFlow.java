package io.muon.jflow.core;

/**
 * Created by gawain on 19/05/2017.
 */

public class JFlow {

    Step firstStep;
    Step lastStep;
    Object lastResult;





    public JFlow firstStep(Action action) {
        Step step  = new Step(action.getName(), action);
        firstStep = step;
        lastStep = step;
        return this;
    }

    public JFlow addStep(String id, Action action, Predicate predicate) {
        Step step  = new Step(action.getName(), action);
        Step addToStep = getStepById(id, firstStep);
        addToStep.addBranchStep(predicate, step);
        lastStep = step;
        return this;
    }

    public JFlow addDefaultStep(String id, Action action) {
        Step step  = new Step(action.getName(), action);
        Step addToStep = getStepById(id, firstStep);
        addToStep.addDefaultStep(step);
        lastStep = step;
        return this;
    }

    public Step getStepById(String id, Step step) {
        Step matchingStep = null;
        if (step.getName() == id) {
            matchingStep = step;
        }
        if (matchingStep != null) return matchingStep;

        for (Step s : step.subSteps()) {
            Step match = getStepById(id, s);
            if (match != null) {
                matchingStep = match;
                break;
            }
        }

        return matchingStep;
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
            Step currentStep = nextStep;
            System.out.println("jflow current step == " + nextStep);
            result = nextStep.run(input);
            System.out.println("jflow result == " + result);
            nextStep = currentStep.getNextStep();
            System.out.println("jflow next step == " + nextStep);
            if (nextStep == null) running = false;
        }
        return result;
    }
}

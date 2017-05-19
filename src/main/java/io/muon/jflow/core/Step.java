package io.muon.jflow.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gawain on 19/05/2017.
 */
public class Step {

    Action action;
    Step firstStep;
    Object result;
    Step errorStep = null;
    boolean branchedStep = false;
    Map<Predicate, Step> steps = new HashMap<Predicate, Step>();

    Step(Action step) {
            this.action = step;
    }

    public Step getNextStep() {
        if (firstStep != null && steps.size() == 0) return firstStep;
        if (action == null || result == null) throw new RuntimeException("step must have action set and be run to generate result before next step can be determined");
        Iterator it = steps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Predicate, Step> pair = (Map.Entry)it.next();
            Predicate predicate = pair.getKey();
            Step step = pair.getValue();
            boolean predicateMatch = predicate.check(result);
            if (predicateMatch) return step;

        }
        if (errorStep == null) throw new RuntimeException("no matching predicate for branch and no errorsteo set");
        return errorStep;
    }

    public Object run(Object input) {
        result = action.run(input);
        return this;
    }

    public Object getResult() {
        return this.result;
    }

    public void addStep(Step branchStep) {
        // there is only a single non predicated step
        if (branchedStep || steps.size() > 1) throw new RuntimeException("can only use addStep() with no predicate if it the first and only step with no predicate");
        firstStep = new Step(action);
    }

    public void addStep(Predicate predicate, Step branchStep) {
        if ( ! branchedStep && firstStep != null && steps.size() == 0) throw new RuntimeException("cannot add branch step with predicate if it the first has no predicate");
        branchedStep = true;
        if (steps.size() == 0) firstStep = new Step(action);
        steps.put(predicate, branchStep);
    }

    public void addErrorStep(Step step) {
        this.errorStep = step;
    }

}

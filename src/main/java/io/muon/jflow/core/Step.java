package io.muon.jflow.core;

import javax.swing.*;
import java.util.*;

/**
 * Created by gawain on 19/05/2017.
 */
public class Step {

    Action action;
    Step defaultStep;
    Object result;
    Step errorStep = null;
    String name;
    Map<Predicate, Step> steps = new HashMap<Predicate, Step>();

    Step(String name, Action step) {
            this.action = step;
            this.name = name;
    }

    public Collection<Step> subSteps() {
        List<Step> steps = new ArrayList<Step>(this.steps.size() + 1);
        steps.add(defaultStep);
        steps.addAll(this.steps.values());
        return steps;
    }

    public Step getNextStep() {
        if (defaultStep != null && steps.size() == 0) return defaultStep;
        if (action == null || result == null) throw new RuntimeException("step must have action set and be run to generate result before next step can be determined");
        Step returnStep = defaultStep;
        Iterator it = steps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Predicate, Step> pair = (Map.Entry)it.next();
            Predicate predicate = pair.getKey();
            Step step = pair.getValue();
            System.out.println("For step '" + this.name +  "' testing predicate '" + predicate.getName() + "'");
            boolean predicateMatch = predicate.check(result);
            if (predicateMatch) {
                System.out.println("For step '" + this.name +  "' predicate match! '" + predicate.getName() + "'");
                System.out.println("For step '" + this.name +  "' next step == '" + step.getName() + "'");
                returnStep = step;
            }
        }
        //System.out.println("returning step '" + returnStep.getName() + "'");
        return returnStep;
    }

    public Object run(Object input) {
        result = action.run(input);
        return result;
    }

    public Object getResult() {
        return this.result;
    }

    public void addDefaultStep(Step branchStep) {
        defaultStep = branchStep;
    }

    public boolean hasSingleStep() {
        return (defaultStep != null && steps.size() < 1);
    }

    public void addBranchStep(Predicate predicate, Step branchStep) {
        steps.put(predicate, branchStep);
    }

    public void addErrorStep(Step step) {
        this.errorStep = step;
    }

    @Override
    public String toString() {
        return name;
    }


    public String getName() {
        return name;
    }
}

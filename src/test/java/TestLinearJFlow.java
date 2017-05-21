import io.muon.jflow.core.JFlow;
import io.muon.jflow.core.Action;


import static org.junit.Assert.assertEquals;

import io.muon.jflow.core.Predicate;
import org.junit.Test;

/**
 * Created by gawain on 19/05/2017.
 */
public class TestLinearJFlow {


    @Test
    public void testJflow() {

        // Actions and predicates
        Action startStep = new StartStep();
        Action helloStep = new HelloStep();
        Action goAwayStep = new GoawayStep();
        Predicate isWinterPredicate = new IsWinterPredicate();

        // Create the desired flow and branches
        JFlow flow = new JFlow();
        flow.firstStep(startStep);
        flow.addStep(startStep.getName(), goAwayStep, isWinterPredicate);
        flow.addDefaultStep(startStep.getName(), helloStep);

        Object result1 = flow.enter("winter");
        assertEquals("Please go away, winter",  result1 );

        Object result2 = flow.enter("summer");
        assertEquals("Hello, I love summer",  result2 );
    }



    // Action and predicate implementations
    private class StartStep implements Action {
        public Object run(Object input) {
            return input;
        }

        public String getName() {
            return "start step";
        }
    }

    private class HelloStep implements Action {
        public Object run(Object input) {
            return "Hello, I love " + input;
        }
        public String getName() {
            return "default: hello step";
        }
    }

    private class GoawayStep implements Action {
        public Object run(Object input) {
                return "Please go away, " + input;
        }
        public String getName() {
            return "go away step";
        }
    }

    private class IsWinterPredicate implements Predicate {
        public boolean check(Object result) {
            return result.toString().contains("winter");
        }
        public String getName() {
            return "crap weather test";
        }
    }
}

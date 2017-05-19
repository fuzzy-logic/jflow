import io.muon.jflow.core.JFlow;
import io.muon.jflow.core.Action;


import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Created by gawain on 19/05/2017.
 */
public class TestLinearJFlow {


    @Test
    public void testJflow() {
        JFlow flow = new JFlow();

        Action step1 = new HelloStep();
        Action step2 = new GoodbyeStep();
        Action step3 = new GoawayStep();

        flow.firstStep(step1);
        flow.addStep(step2);
        flow.addStep(step3);
        Object result = flow.enter("gawain");

        assertEquals("hello, goodbye, gawain. Please go away.",  result );
    }

    private class HelloStep implements Action {
        public Object run(Object input) {
            return "hello, " + input;
        }
    }

    private class GoodbyeStep implements Action {
        public Object run(Object input) {
            String[] csv = ((String) input).split(",");
            return csv[0] + ", goodbye," + csv[1];
        }
    }

    private class GoawayStep implements Action {
        public Object run(Object input) {

            return input + ". Please go away.";
        }
    }
}

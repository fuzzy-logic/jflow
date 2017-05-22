import io.muon.jflow.core.Flow;
import io.muon.jflow.core.Action;


import static org.junit.Assert.assertEquals;

import io.muon.jflow.core.Predicate;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by gawain on 19/05/2017.
 */
public class TestLinearJFlow {


    @Test
    public void testJflow() throws ExecutionException, InterruptedException {
        // Create the desired flow and branches
        Action<String, String> startStep = Action.sync("start step", i -> i);
        Action<String, String> emphasise = Action.sync("emphasise", i -> i + "!");
        Action<String, String> sayComeAgainAnother = Action.sync("rainy step", i -> i + " - rain, rain, go away!");
        Action<String, String> sayHello = Action.sync("hello step", i -> "Hello, I love " + i);
        Predicate<String> isRainy = Predicate.sync("rainy test", i -> i.contains("rainy"));
        Predicate<String> isWinter = Predicate.sync("winter test", i -> i.contains("winter"));
        Action<String, String> sayGoAway = Action.sync("go away step", i -> "Please go away, " + i);

        Flow<String, String> flow = Flow.of(startStep)
                .add(Flow.of(sayHello).add(emphasise).branch(isRainy, sayComeAgainAnother))
                .branch(isWinter, sayGoAway);

        System.out.println(flow.describe());

        String result1 = flow.run("winter").get();
        assertEquals("Please go away, winter",  result1 );

        String result2 = flow.run("summer").get();
        assertEquals("Hello, I love summer!",  result2 );

        String result3 = flow.run("rainy summer").get();
        assertEquals("Hello, I love rainy summer - rain, rain, go away!",  result3);
    }

}

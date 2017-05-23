# jflow

### why ANOTHER workflow solution?

I agree, but after spending hours evulating just a small fraction of solutions I found two common themes:

1.) Over engineering GUI driven workflow for non developers
2.) Developers still actually have to do the tricky stuff and now the APIs are corrupted by the compromises of point 1

### WHat is JFLOW


In any software system, epsecially a microservices one, at somepoint you're going to have to orchestrate a bunch of calls, transform some data, and make some decisions as to what steps to perform next depending on the returned data. This is the bread and butter of day to day coders, bascially, if-this-then-that for the enterprise. All to often the code decends in a typical state of lazy anarchy (think mad max) rather than ordered civility (err, does that actually exist yet?).

This is where jflow was imaculately conceived from.

JFlow is a lightweight simple api to compose actions as easily decoupled steps in a wider logically branching workflow just for developers. There's no gui driven workflows, there's no non technical interface. It's programmed for programmers.

# Example

```
Action<String, String> startStep = Action.sync("start step", i -> i);
        Action<String, String> nextStep = Action.sync("next step", i -> i);
        Action<String, String> emphasise = Action.sync("emphasise", i -> i + "!");
        Action<String, String> sayComeAgainAnother = Action.sync("rainy step", i -> i + " - rain, rain, go away!");
        Action<String, String> sayHello = Action.sync("hello step", i -> "Hello, I love " + i);
        Predicate<String> isRainy = Predicate.sync("rainy test", i -> i.contains("rainy"));
        Predicate<String> isWinter = Predicate.sync("winter test", i -> i.contains("winter"));
        Action<String, String> sayGoAway = Action.sync("go away step", i -> "Please go away, " + i);

        Flow<String, String> flow = Flow.of(startStep)
                .add(nextStep)
                .add(Flow.branch(isWinter,
                        Flow.of(sayGoAway),
                        Flow.of(sayHello)
                                .add(Flow.branch(isRainy,
                                        Flow.of(sayComeAgainAnother),
                                        Flow.of(emphasise)))));

        System.out.println(flow.describe());

        String result1 = flow.run("winter").get();
        assertEquals("Please go away, winter",  result1 );

        String result2 = flow.run("summer").get();
        assertEquals("Hello, I love summer!",  result2 );

        String result3 = flow.run("rainy summer").get();
        assertEquals("Hello, I love rainy summer - rain, rain, go away!",  result3);
```

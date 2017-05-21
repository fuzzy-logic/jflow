package io.muon.jflow.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gawain on 19/05/2017.
 */
public interface Predicate {

    public boolean check(Object result);

    public String getName();
}

package io.muon.jflow.core;

/**
 * Created by gawain on 19/05/2017.
 */
public interface Action {

    Object run(Object nextStepInput);
}

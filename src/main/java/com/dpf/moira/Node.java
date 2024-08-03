package com.dpf.moira;

import java.lang.reflect.ParameterizedType;

public abstract class Node<S, E extends Enum<E>> {

    abstract public E execute(Scenario<S> scenario);

    final Class<S> getScenarioClass() {
        @SuppressWarnings("unchecked")
        Class<S> scenarioClass = (Class<S>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return scenarioClass;
    }

}

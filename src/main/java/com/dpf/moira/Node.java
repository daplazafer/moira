package com.dpf.moira;

import com.dpf.moira.entity.Context;

import java.lang.reflect.ParameterizedType;

public abstract class Node<C, E extends Enum<E>> {

    abstract public E execute(Context<C> context);

    final Class<C> getContextClass() {
        @SuppressWarnings("unchecked")
        Class<C> contextClass = (Class<C>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return contextClass;
    }

}

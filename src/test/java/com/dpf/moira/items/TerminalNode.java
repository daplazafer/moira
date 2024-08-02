package com.dpf.moira.items;

import com.dpf.moira.Decision;
import com.dpf.moira.Node;

import static com.dpf.moira.items.TerminalNode.Result;

@Decision(id = "end", description = "Finishing execution")
public class TerminalNode extends Node<CarContext, Result> {

    public enum Result {
    }

    @Override
    public Result decide(CarContext context) {
        System.out.println("Fin de la prueba");
        return null;
    }
}

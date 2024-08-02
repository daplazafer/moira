package com.dpf.moira.items;

import com.dpf.moira.Decision;
import com.dpf.moira.Node;

import static com.dpf.moira.items.IsCarRunningNode.Result;
import static com.dpf.moira.items.IsCarRunningNode.Result.NO;
import static com.dpf.moira.items.IsCarRunningNode.Result.YES;

@Decision(id = "isCarRunning", description = "Is the car running?")
public class IsCarRunningNode extends Node<CarContext, Result> {

    public enum Result {
        YES, NO;
    }

    @Override
    public Result decide(CarContext context) {
        System.out.println("Comprobando velocidad");
        if (context.speed() > 0) {
            return YES;
        }
        return NO;
    }
}

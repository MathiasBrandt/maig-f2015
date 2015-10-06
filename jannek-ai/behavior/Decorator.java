/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.itu.jegk.behavior;

/**
 *
 * @author Jannek
 */
public class Decorator extends BehaviorNode {

    public enum Type {
        Inverter,
        Succeeder,
        Repeater,
        RepeatUntilFail
    }
    
    private final Type type;
    private final BehaviorNode child;

    public Decorator(Type type, BehaviorNode child) {
        this.type = type;
        this.child = child;
    }
    
    @Override
    public BehaviorNode Activate() {
        if (done)
            return null;
        
        return child;
    }
    
    @Override
    public void Return(boolean value) {
        switch (type) {
            case Inverter:
                result = !value;
                done = true;
                break;
            case Succeeder:
                result = true;
                done = true;
                break;
            case Repeater:
                // yes nothing
                break;
            case RepeatUntilFail:
                result = true;
                done = !value;
                break;
        }
    }
}

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
public class Composite extends BehaviorNode {

    public enum Type {
        Sequence,
        Selector,
        RandomSequence,
        RandomSelector
    }
    
    private final Type type;
    private final BehaviorNode[] children;
    
    private int index;
    private final boolean terminate;
    
    public Composite(Type type, BehaviorNode ... children)
    {
        this.type = type;
        this.children = children;
        
        // selector should terminate on true
        // sequence should terminate on false
        terminate = type == Type.Selector || type == Type.RandomSelector;
    }
    
    @Override
    public void Init() {
        // shuffle if random
        if (type == Type.RandomSelector || type == Type.RandomSequence)
        {
            for (int i = 0; i < children.length; i++) {
                BehaviorNode swap = children[i];
                int rand = (int)(Math.random() * children.length);
                children[i] = children[rand];
                children[rand] = swap;
            }
        }
        
        index = -1;
        done = false;
    }

    @Override
    public BehaviorNode Activate() {
        if (done)
            return null;
        
        index++;
        if (index >= children.length)
        {
            // on end: sequence is true, selector is false
            result = !terminate;
            return null;
        }
        
        return children[index];
    }

    @Override
    public void Return(boolean value) {
        
        // selector should instantly return true on success
        // sequence should instantly return false on error
        if (value == terminate)
        {
            result = value;
            done = true;
        }
    }
    
}

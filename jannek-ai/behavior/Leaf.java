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
public abstract class Leaf extends BehaviorNode {
    
    @Override
    public BehaviorNode Activate() {
        result = LeafAction();
        return null;
    }
    
    /**
     * The actual game action or test.
     * @return result of action/test.
     */
    public abstract boolean LeafAction();
}

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
public abstract class BehaviorNode {
    protected boolean result = false;
    protected boolean done = false;
    /**
     * Used to setup initial values for nodes. i.e. shuffle random sequence,
     * reset index for sequences and selectors. Or simply just set done to
     * false.
     */
    public void Init() { done = false; }
    /**
     * Activate a single iteration of the node. In many cases there are only
     * one, but for composites and repeaters, there can be several.
     * @return child to go deeper, this to stay at node, null to return to parent.
     */
    public abstract BehaviorNode Activate();
    /**
     * The result of the node. Only use when Activate returns null.
     * @return The result.
     */
    public boolean Result() { return result; }
    /**
     * Called on parent node when returning from a child.
     * @param value Result of last visited child
     */
    public void Return(boolean value) { }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.itu.jegk.behavior;

import java.util.Stack;

/**
 * Base execution of behavior trees. The actual tree is implemented in the
 * provided BehaviorNode.
 * @author Jannek
 */
public class BehaviorTree {
    
    private final Stack<BehaviorNode> tree;
    private final BehaviorNode first;
    
    public BehaviorTree(BehaviorNode first)
    {
        tree = new Stack<>();
        this.first = first;
        tree.push(first);
        first.Init();
    }
    
    public void Reset()
    {
        tree.clear();
        tree.push(first);
        first.Init();
    }
    
    /**
     * Execute one step in the tree. I choose this approach over recursion, to
     * better control and stop the flow of the tree.
     * @return Does the tree have more steps (false when finished)
     */
    public boolean Execute()
    {
        if (tree.empty())
            return false;
        
        BehaviorNode current = tree.peek();
        BehaviorNode next = current.Activate();
        if (next == null)
        {
            tree.pop();
            if (tree.empty())
                return false;
            tree.peek().Return(current.Result());
        }
        else if (next != current)
        {
            tree.add(next);
            next.Init();
        }
        return true;
    }
}

package mfli.behaviortree;

import java.util.ArrayList;
import java.util.Collections;

public class RandomSelectorNode extends Node {
	Node[] sequence;
	ArrayList<Integer> executionOrder;
	
	public RandomSelectorNode(Node... sequence) {
		this.sequence = sequence;
		
		executionOrder = new ArrayList<Integer>(sequence.length);
		for(int i = 0; i < executionOrder.size(); i++) {
			executionOrder.add(i);
		}
		Collections.shuffle(executionOrder);
	}

	@Override
	public boolean execute() {
		for(int i = 0; i < executionOrder.size(); i++) {
			int index = executionOrder.get(i);
			boolean result = sequence[index].execute();
			
			if(result) {
				return true;
			}
		}
		
		return false;
	}

}

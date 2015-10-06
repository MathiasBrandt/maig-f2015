package mfli.behaviortree;

public class SelectorNode extends Node {
	Node[] sequence;
	
	public SelectorNode(Node... sequence) {
		this.sequence = sequence;
	}

	@Override
	public boolean execute() {
		for(int i = 0; i < sequence.length; i++) {
			boolean result = sequence[i].execute();
			
			if(result) {
				return true;
			}
		}
		
		return false;
	}

}

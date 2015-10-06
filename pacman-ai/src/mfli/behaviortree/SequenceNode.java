package mfli.behaviortree;

public class SequenceNode extends Node {
	Node[] sequence;

	public SequenceNode(Node... sequence) {
		this.sequence = sequence;
	}
	
	@Override
	public boolean execute() {
		for(Node node : sequence) {
			boolean result = node.execute();
			
			if(!result) {
				return false;
			}
		}
		
		return true;
	}
}

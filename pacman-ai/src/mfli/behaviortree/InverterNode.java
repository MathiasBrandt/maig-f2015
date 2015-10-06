package mfli.behaviortree;

public class InverterNode extends Node {
	Node child;
	
	public InverterNode(Node child) {
		this.child = child;
	}

	@Override
	public boolean execute() {
		return !child.execute();
	}
}

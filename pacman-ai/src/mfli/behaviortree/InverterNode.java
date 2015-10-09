/**
 * Mathias Flink Brandt
 * mfli@itu.dk
 */

package mfli.behaviortree;

public class InverterNode extends Node {
	Node child;
	
	public InverterNode(Node child) {
		this.child = child;
	}

	/**
	 * Evaluates the child and returns the opposite of the child's result
	 * @return Returns true if the child returned false, and vice versa.
	 */
	@Override
	public boolean execute() {
		return !child.execute();
	}
}

/**
 * Mathias Flink Brandt
 * mfli@itu.dk
 */

package mfli.behaviortree;

public class SequenceNode extends Node {
	Node[] sequence;

	public SequenceNode(Node... sequence) {
		this.sequence = sequence;
	}
	
	/**
	 * Execute each child in sequence.
	 * @return Returns true if all children evaluate to true. Returns false immediately if a child evaluates to false.
	 */
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

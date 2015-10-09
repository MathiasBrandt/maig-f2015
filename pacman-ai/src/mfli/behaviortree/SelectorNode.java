/**
 * Mathias Flink Brandt
 * mfli@itu.dk
 */

package mfli.behaviortree;

public class SelectorNode extends Node {
	Node[] sequence;
	
	public SelectorNode(Node... sequence) {
		this.sequence = sequence;
	}

	/**
	 * Execute each child in sequence.
	 * @return Returns true immediately if a child evaluates to true. Returns false if all children evaluate to false.
	 */
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

/**
 * Mathias Flink Brandt
 * mfli@itu.dk
 */

package mfli.behaviortree;

public abstract class Node {
	
	/**
	 * Execute the node
	 * @return The result of executing the node
	 */
	public abstract boolean execute();
}

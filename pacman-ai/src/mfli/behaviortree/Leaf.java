package mfli.behaviortree;

import java.util.function.BooleanSupplier;

public class Leaf extends Node {
	BooleanSupplier action;

	public Leaf(BooleanSupplier action) {
		this.action = action;
	}

	@Override
	public boolean execute() {
		return action.getAsBoolean();
	}
}

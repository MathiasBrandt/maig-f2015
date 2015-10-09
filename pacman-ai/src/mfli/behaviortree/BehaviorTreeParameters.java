package mfli.behaviortree;

public class BehaviorTreeParameters {
	public int distance_flee;
	public int distance_attack;
	public int distance_eat_power_pill;
	
	public BehaviorTreeParameters() {
	}
	
	public BehaviorTreeParameters(int... values) {
		this.distance_flee = values[0];
		this.distance_attack = values[1];
		this.distance_eat_power_pill = values[2];
	}
}

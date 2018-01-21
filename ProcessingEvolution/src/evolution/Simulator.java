package evolution;

import java.util.ArrayList;

import evolution.Evolution3WEB.Creature;
import evolution.Evolution3WEB.Muscle;
import evolution.Evolution3WEB.Node;

public class Simulator {

	public Simulator(Creature thisCreature) {
		setTo(thisCreature);
	}

	protected ArrayList<Node> n = new ArrayList<Node>();
	protected ArrayList<Muscle> m = new ArrayList<Muscle>();
	protected int id;
	private int timer;
	private float cTimer;

	public void setTo(Creature thisCreature) {
		n.clear();
		m.clear();
		for (int i = 0; i < thisCreature.n.size(); i++) {
			n.add(thisCreature.n.get(i).copyNode());
		}
		for (int i = 0; i < thisCreature.m.size(); i++) {
			m.add(thisCreature.m.get(i).copyMuscle());
		}
		id = thisCreature.id;
		timer = 0;
		cTimer = thisCreature.creatureTimer;
	}
	
	void simulate(int stepcount) {
		for (int s = 0; s < stepcount; s++) {
			simulateStep();
			timer++;
		}

	}

	void simulateStep() {
		float target;
		for (int i = 0; i < m.size(); i++) {
			Muscle mi = m.get(i);
			mi.thruPeriod = ((timer / cTimer) / mi.period) % 1;
			if ((mi.thruPeriod <= mi.extendTime && mi.extendTime <= mi.contractTime)
					|| (mi.contractTime <= mi.thruPeriod && mi.thruPeriod <= mi.extendTime)
					|| (mi.extendTime <= mi.contractTime && mi.contractTime <= mi.thruPeriod)) {
				target = mi.contractLength;
				mi.contracted = true;
			} else {
				target = mi.extendLength;
				mi.contracted = false;
			}
			mi.applyForce(i, target);
		}
		for (int i = 0; i < n.size(); i++) {
			Node ni = n.get(i);
			ni.applyForces(i);
			ni.applyGravity(i);
			ni.hitWalls(i);
		}
	}

	float getAverage() {
		float average = 0;
		for (int i = 0; i < n.size(); i++) {
			Node ni = n.get(i);
			average += ni.x;
		}
		average = average / n.size();
		return average;
	}

}

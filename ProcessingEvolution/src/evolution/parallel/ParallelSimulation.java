package evolution.parallel;

import java.util.List;

import evolution.Creature;
import evolution.Rectangle;

public class ParallelSimulation {

    /**
     * @param cs Array of creatures. Their Distance will get updated.
     * @param rects Obstacles for the creatures.
     */
    public static void simulateFitness(Creature[] cs, final List<? extends Rectangle> rects) {
        for (int i = 0; i < 1000; i++) {
            Creature simulator = cs[i].copyCreature(0);
            int timer = 0;
            for (int s = 0; s < 900; s++) {
                simulator.simulate(timer, rects);
                timer++;
            }
            float midDistance = simulator.getAverage();
            cs[i].setDistance(midDistance * 0.2f);
        }
    }

}

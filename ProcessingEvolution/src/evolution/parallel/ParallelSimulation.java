package evolution.parallel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import evolution.Creature;
import evolution.Rectangle;

public class ParallelSimulation {

    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(8);

    private static class Simulate implements Callable<Object> {

        /**
         * The creature that is simulated. After simulation its fitness is updated.
         */
        private Creature c;

        /**
         * The set of obstacles.
         */
        private Iterable<? extends Rectangle> rects;

        public Simulate(Creature c, Iterable<? extends Rectangle> rects) {
            this.c = c;
            this.rects = rects;
        }

        /**
         * Do the simulation and store its results in the creature directly.
         * 
         * @return null
         */
        @Override
        public Object call() {
            Creature runningCreature = c.copyCreature(0);
            int timer = 0;
            for (int s = 0; s < 900; s++) {
                runningCreature.simulate(timer, rects);
                timer++;
            }
            float midDistance = runningCreature.getAverage();
            c.setFitness(midDistance * 0.2f);
            return null;
        }
    }

    /**
     * @param cs
     *            Array of creatures. Their Distance will get updated.
     * @param rects
     *            Obstacles for the creatures.
     */
    public static void simulateFitness(Creature[] cs, final List<? extends Rectangle> rects) {
        ArrayList<Callable<Object>> simulators = new ArrayList<Callable<Object>>(cs.length);
        for (int i = 0; i < cs.length; i++) {
            if (!cs[i].hasBeenTested()) {
                simulators.add(new Simulate(cs[i], rects));
            }
        }
        try {
            threadPoolExecutor.invokeAll(simulators);
        } catch (InterruptedException e) {
            // Our threads have been interrupted. What shall we do? 
            // Let's abort
            throw new RuntimeException("ThreadPool got interrupted. This is an unexpected error.", e);
        }
    }

}

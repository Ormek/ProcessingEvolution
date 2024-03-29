package evolution.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.my.performance.PerfRecorder;
import evolution.Creature;
import evolution.Rectangle;

/**
 * This class can update the distance of a set of creatures. The simulation runs in parallel with a fixed number of threads.
 * @author Oliver Meyer
 *
 */
public class ParallelSimulation {

    private static final int THREAD_COUNT = 8;
    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(THREAD_COUNT);

    /** Private constructor because no instance of ParallelSimulation is needed **/
    private ParallelSimulation() {};

    private static class SimulateSetOfCreatures implements Callable<Object> {
        private Iterable<Creature> creatures;
        private Iterable<? extends Rectangle> rects;

        public SimulateSetOfCreatures(Iterable<Creature> set, Iterable<? extends Rectangle> rects) {
            this.creatures = set;
            this.rects = rects;
        }

        /**
         * Do the simulation and store its results in the creature directly.
         * 
         * @return null
         */
        @Override
        public Object call() {
            for (Creature c : creatures) {

                Creature runningCreature = c.copyCreature(0);
                int timer = 0;
                for (int s = 0; s < 900; s++) {
                    runningCreature.simulate(timer, rects);
                    timer++;
                }
                float midDistance = runningCreature.getAverage();
                c.setFitness(midDistance * 0.2f);
            }
            return null;
        }
    }

    private static class SimulateSingleCreature implements Callable<Object> {

        /**
         * The creature that is simulated. After simulation its fitness is updated.
         */
        private Creature c;

        /**
         * The set of obstacles.
         */
        private Iterable<? extends Rectangle> rects;

        public SimulateSingleCreature(Creature c, Iterable<? extends Rectangle> rects) {
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
    private static void simulateFitnessSingle(Creature[] cs, final List<? extends Rectangle> rects) {
        ArrayList<Callable<Object>> simulators = new ArrayList<Callable<Object>>(cs.length);
        for (int i = 0; i < cs.length; i++) {
            if (!cs[i].hasBeenTested()) {
                simulators.add(new SimulateSingleCreature(cs[i], rects));
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
    
    private static void simulateFitnessSingle(Stream<? extends Creature> cs, final List<? extends Rectangle> rects) {
        List<Callable<Object>> simulators =  cs.filter(c->!c.hasBeenTested())
                .map(c->new SimulateSingleCreature(c, rects))
                .collect(Collectors.toList());
        try {
            threadPoolExecutor.invokeAll(simulators);
        } catch (InterruptedException e) {
            // Our threads have been interrupted. What shall we do? 
            // Let's abort
            throw new RuntimeException("ThreadPool got interrupted. This is an unexpected error.", e);
        }
    }

    /**
     * Simulate a set of creatures using multiple creatures per runnable. By having multiple creatures per simulator less runnables are involved resulting in less thread switching.
     * @param cs
     *            Array of creatures. Their Distance will get updated.
     * @param rects
     *            Obstacles for the creatures.
     */
    private static void simulateFitnessMultiple(Creature[] cs, final List<? extends Rectangle> rects) {
        
        ArrayList<ArrayList<Creature>> creatureSets = new ArrayList<ArrayList<Creature>>(THREAD_COUNT);
        // init with empty Sets
        final int expectedGroupSize = cs.length/2/THREAD_COUNT; // THREAD COUNT Groups, and have is untested
        for (int i = 0; i<THREAD_COUNT; i++) {
            creatureSets.add(new ArrayList<Creature>(expectedGroupSize));
        }
        int currentSet= 0;
        for (Creature creature : cs) {
            if (!creature.hasBeenTested()) {
                creatureSets.get(currentSet++%THREAD_COUNT).add(creature);
            }
            
        }
        
        // Create a MultiSimulator per set
        ArrayList<Callable<Object>> simulators = new ArrayList<Callable<Object>>(creatureSets.size());
        for (ArrayList<Creature> arrayList : creatureSets) {
            simulators.add(new SimulateSetOfCreatures(arrayList, rects));
        }
        try {
            threadPoolExecutor.invokeAll(simulators);
        } catch (InterruptedException e) {
            // Our threads have been interrupted. What shall we do? 
            // Let's abort
            throw new RuntimeException("ThreadPool got interrupted. This is an unexpected error.", e);
        }
    }
    
    static PerfRecorder p = new PerfRecorder();
    static {
        p.setLabel("Just the simulation took: ");
    }
    
    /**
     * @param cs
     *            Array of creatures. Their Distance will get updated.
     * @param rects
     *            Obstacles for the creatures.
     */
    public static void simulateFitness(Creature[] cs, final List<? extends Rectangle> rects) {
        p.startTiming();
        simulateFitnessSingle(cs, rects);
        p.stopTiming();
    }
    
    public static void simulateFitness(Stream<? extends Creature> cs, final List<? extends Rectangle> rects) {
        p.startTiming();
        simulateFitnessSingle(cs, rects);
        p.stopTiming();
    }
    
}

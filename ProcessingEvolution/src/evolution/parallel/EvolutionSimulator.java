/**
 * 
 */
package evolution.parallel;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.my.performance.PerfRecorder;
import evolution.Creature;
import evolution.Rectangle;
import evolution.Result;

/**
 * <p>
 * Simulate the evolution of creatures in a static environment. Input is a set of creatures and an environment. This
 * class will spawn threads to evolve the creatures, through adaptation to the environment. Class does not care about
 * creature ids.
 * </p>
 * 
 * Its main loop does the following:
 * 
 * <pre>
 * 1. Kill weak creatures and spawn new ones.
 * 2. Evaluate generation.
 * 3. Queue result
 * 4. Increase gen count, abort if gen limit reached.
 * </pre>
 * 
 * Results are objects of {@link Result}. Each results is queued into a {@link BlockingQueue} provided at creation time.
 * 
 * @author Oliver Meyer
 *
 */
public class EvolutionSimulator implements Runnable {

    private static final long SEED = 123;
    private ArrayList<Creature> population = new ArrayList<Creature>();
    private ArrayList<Rectangle> rects;
    final private int CREATURE_COUNT;
    final PerfRecorder p = new PerfRecorder();

    // Current generation
    private int generation;

    /**
     * Simulator pushes results of simulation into resultBuffer.
     */
    private BlockingQueue<Result> resultBuffer;

    /**
     * Number of generations to run until we stop. Setting this to 0 will stop the simulation.
     */
    private int genToDo;

    /**
     * The thread running this simulation or null, if it is not running.
     */
    private Thread backgroundThread;
    /**
     * If true the first loop will skip natural selection and and regrow; it will start with evaluation the given
     * population. If false, all iterations will be similar.
     * 
     */
    private boolean skipFirstKill;

    /**
     * Create an EvolutionSimulator with a given set of creatures and environment. It will simulate exactly one
     * generation, skipping the first kill
     * 
     * @param creatures
     *            Stream of Creatures that make up the population. It must supply an even number of creatures.
     * @param rectangles
     *            Stream of Rectangles that make up the environment for the creatures.
     * @param resultBuffer
     *            Buffer to receive results of evolution. Main loop will block if the buffer is full.
     * 
     * @throws IllegalArgumentException
     *             if an uneven number, or no creatures are supplied.
     * 
     */
    public EvolutionSimulator(Stream<Creature> creatures, Stream<Rectangle> rectangles,
            BlockingQueue<Result> resultBuffer) {
        this(creatures, rectangles, resultBuffer, 1, true);
    }

    /**
     * Create an EvolutionSimulator with a given set of creatures and environment, that will simulate a given number of
     * generations and then terminate.
     * 
     * @param creatures
     *            Stream of Creatures that make up the population. It must supply an even number of creatures.
     * @param rectangles
     *            Stream of Rectangles that make up the environment for the creatures.
     * @param resultBuffer
     *            Buffer to receive results of evolution. Main loop will block if the buffer is full.
     * @param genToDo
     *            Number of generations to simulate. Must be greater than 0
     * @param skipFirstKill
     *            If true the first loop will skip natural selection and and regrow; it will start with evaluation the
     *            given population. If false, all iterations will be similar.
     * 
     * @throws IllegalArgumentException
     *             if an uneven number, or no creatures are supplied.
     * @throws IllegalArgumentException
     *             if genToDo is not greater than 0
     * 
     * 
     */
    public EvolutionSimulator(Stream<Creature> creatures, Stream<Rectangle> rectangles,
            BlockingQueue<Result> resultBuffer, int genToDo, boolean skipFirstKill) {
        if (!(genToDo > 0)) {
            throw new IllegalArgumentException("genToDo must be greater than 0, but is " + genToDo);
        }
        creatures.forEach(c -> population.add(c.copyCreature(0)));
        rects = rectangles.collect(Collectors.toCollection(() -> new ArrayList<Rectangle>()));
        CREATURE_COUNT = population.size();
        if (CREATURE_COUNT % 2 == 1 || CREATURE_COUNT == 0) {
            throw new IllegalArgumentException("EvolutionSimulator requires an even number of creatures.");
        }
        p.setLabel("Simulating 100 generations took: ");
        this.genToDo = genToDo;
        this.resultBuffer = resultBuffer;
        this.skipFirstKill = skipFirstKill;
    }

    /**
     * Start the evolution of the creatures.
     **/
    @Override
    public void run() {
        mainLoop();
    }

    /**
     * 
     * <pre>
     * 1. Kill weak creatures and spawn new ones.
     * 2. Evaluate generation.
     * 3. Queue result
     * 4. Increase gen count, abort if gen limit reached.
     * </pre>
     * 
     */
    private void mainLoop() {
        p.startTiming();
        try {
            while (!Thread.interrupted() && generation <= genToDo) {
                if (!skipFirstKill) {
                    // Selection, that is let half the population die
                    naturalSelection();

                    // Regrow, that is mutate the survivors
                    regrow();
                } else {
                    skipFirstKill = false;
                }

                // Update the fitness of the creatures, that is, make them experience the environment
                ParallelSimulation.simulateFitness(population.stream(), rects);

                // Sort them
                sortPopulation();

                // Increase generation count. The sorted population p is gen n+1
                generation++;

                logPerformance();

                // Update
                reportProgress();
            }
        } catch (InterruptedException e) {
            // Stop because we got interrupted
            genToDo = 0;
        }
        backgroundThread = null; // We are actually done.
    }

    private void logPerformance() {
        if (generation % 100 == 0) {
            p.setLabel(statusMessage() + "It took: ");
            p.recordIteration();
        }
        if (generation % 10 == 0) {
            System.out.println("Gen: " + generation + ", Queue at: " + resultBuffer.size());
        }
    }

    private float r() {
        return (float) Math.pow(random(-1, 1), 19.0);
    }

    private void regrow() {
        for (int i = 0; i < CREATURE_COUNT / 2; i++) {
            // Creatures are stored in pairs (x, 999-x). If x is dead, then 999-x is alive and vice versa.
            // The dead creature will be replaced by a modified version of its alive counterpart.
            int liveIndex, deadIndex;
            if (!population.get(i).isAlive()) { // i is dead
                liveIndex = CREATURE_COUNT - 1 - i;
                deadIndex = i;
            } else {
                liveIndex = i;
                deadIndex = CREATURE_COUNT - 1 - i;
            }
            Creature liveCreature = population.get(liveIndex);
            Creature offspring = liveCreature.modified(0, () -> r(), (x, y) -> random(x, y));
            population.set(deadIndex, offspring); // mutated
        }
    }

    private Random internalRandom = new Random(SEED);

    private float random(float low, float high) {
        float value;
        final float diff = high - low;
        if (diff == 0) {
            return low;
        }
        do {
            value = internalRandom.nextFloat() * diff;
        } while (value == diff);
        return value + low;
    }

    private void naturalSelection() {

        final double increment = 1.0 / CREATURE_COUNT;
        double f = 0;

        for (int fastIndex = 0; fastIndex < CREATURE_COUNT / 2; fastIndex++) {
            double rand = (Math.pow(random(-1.0f, 1.0f), 3) + 1) / 2; // cube function
            boolean slowOneDies = (f <= rand);
            int victimsIndex;
            if (slowOneDies) {
                victimsIndex = CREATURE_COUNT - fastIndex - 1;
            } else {
                victimsIndex = fastIndex;
            }
            Creature ck = population.get(victimsIndex);
            ck.die();
            f += increment;
        }
    }

    private void sortPopulation() {
        // Sort in descending order, so switch first and second in comparator
        population.sort((first, second) -> Float.compare(second.getFitness(), first.getFitness()));
    }

    private void reportProgress() throws InterruptedException {
        // Clone population
        ArrayList<Creature> clonedPop = new ArrayList<Creature>(population.size());
        for (Creature current : population) {
            clonedPop.add(current.clone());
        }
        Result result = new Result(generation, clonedPop);
        resultBuffer.put(result);
    }

    private String statusMessage() {
        final float best = population.get(0).getFitness();
        final float worst = population.get(population.size() - 1).getFitness();
        final float median = population.get(population.size() / 2).getFitness();
        return "Generation: " + generation + " Best: " + best + " Median: " + median + " Worst: " + worst;
    }

    /**
     * Ensure this Simulator is running. If no thread exists to run this simulator, create one. Otherwise, do nothing.
     * You should be able to restart a simulation. It will continue with the next generation.
     */
    public void start() {
        if (backgroundThread == null) {
            backgroundThread = new Thread(this, "EvolutionSimulator");
            backgroundThread.start();
        } else {
            // we are already there. Do nothing
        }
    }

    /**
     * Make this Simulation stop eventually. May simulate one more generation and then stop.
     */
    public void finish() {
        genToDo = 0;
    }

}

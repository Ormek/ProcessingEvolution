/**
 * 
 */
package evolution.parallel;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.my.performance.PerfRecorder;
import evolution.Creature;
import evolution.Rectangle;

/**
 * Simulate the evolution of creatures in a static environment. Input is a set of creatures and an environment. This
 * class will spawn threads to evolve the creatures, through adaptation to the environment. Class does not care about creature ids.
 * 
 * @author Oliver Meyer
 *
 */
public class EvolutionSimulator implements Runnable {

    private static final long SEED = 37;
    private ArrayList<Creature> population = new ArrayList<Creature>();
    private ArrayList<Rectangle> rects;
    final private int CREATURE_COUNT;
    final PerfRecorder p = new PerfRecorder();

    // Flag request to stop running.
    volatile private boolean requestStop = false;

    // Flag request to receive a progress update.
    volatile private boolean requestUpdate = true;

    // Current generation
    private int generation;

    /**
     * Create an EvolutionSimulator with a given set of creatures and environment.
     * 
     * @param creatures
     *            Stream of Creatures that make up the population. It must supply an even number of creatures.
     * @param rectangles
     *            Stream of Rectangles that make up the environment for the creatures.
     * 
     * @throws IllegalArgumentException
     *             if an uneven number, or no creatures are supplied.
     * 
     */
    public EvolutionSimulator(Stream<Creature> creatures, Stream<Rectangle> rectangles) {
        creatures.forEach(c -> population.add(c.copyCreature(0)));
        rects = rectangles.collect(Collectors.toCollection(() -> new ArrayList<Rectangle>()));
        CREATURE_COUNT = population.size();
        if (CREATURE_COUNT % 2 == 1 || CREATURE_COUNT == 0) {
            throw new IllegalArgumentException("EvolutionSimulator requires an even number of creatures.");
        }
            p.setLabel("Simulating 100 generations took: ");

    }
    


    /**
     * Start the evolution of the creatures.
     **/
    @Override
    public void run() {
        p.startTiming();
        mainLoop();
    }



    private void mainLoop() {
        while (true) {
            // Update the fitness of the creatures, that is, make them experience the environment
            ParallelSimulation.simulateFitness(population.stream(), rects);
            generation++;
            
            logPerformance();
            
            // Sort them
            sortPopulation();
            
            // Update -- if requested
            if (requestUpdate) {
                reportProgress();
            }
            // Stop -- if requested
            if (requestStop) {
                break;
            }
            
            // Selection, that is let half the population die
            naturalSelection();
            
            // Regrow, that is mutate the survivors
            regrow();
        }
    }



    private void logPerformance() {
        if (generation %100 == 0) {
            p.setLabel(statusMessage()+"It took: ");
            p.recordIteration();
        }
    }
    
    private float r() {
        return (float)Math.pow(random(-1, 1), 19.0);
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
            Creature offspring = liveCreature.modified(0, () -> r(),
                    (x, y) -> random(x, y));
            population.set(deadIndex, offspring); // mutated
        }
    }

    private Random internalRandom = new Random(SEED);

    private float random(float low, float high) {
        float value;
        final float diff = high - low;
        if (diff==0) {
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

    private void reportProgress() {
        System.out.println(statusMessage());
        //requestUpdate = false;
    }
    
    private String statusMessage() {
        final float best = population.get(0).getFitness();
        final float worst = population.get(population.size() - 1).getFitness();
        final float median = population.get(population.size() / 2).getFitness();
        return "Generation: " + generation + " Best: " + best + " Median: " + median + " Worst: " + worst;
    }

}
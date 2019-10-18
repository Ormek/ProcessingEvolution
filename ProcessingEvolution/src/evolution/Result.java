package evolution;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Result of creature evolution = the next generation. Please note that who ever own the Result object owns all objects
 * referenced by it. Result producers should provide a copy for the Result object, whereas result consumers should read
 * only.
 * 
 * @author Oliver Meyer
 *
 */
public class Result {

    /**
     * The creatures that make up this generation
     */
    private ArrayList<Creature> population;

    /**
     * Number of this generation relativ to the EvolutionSimulator life. The first generation simulated by any Simulator
     * is always 1.
     */
    private int currentGeneration;

    /**
     * Create a new result object, using the population and generation provided.
     * 
     * @param generation
     * @param population
     */
    public Result(int generation, ArrayList<Creature> population) {
        this.currentGeneration = generation;
        this.population = population;
    }

    public ArrayList<Creature> getPopulation() {
        return population;
    }
    
    public int getGeneration() {
        return currentGeneration;
    }

}

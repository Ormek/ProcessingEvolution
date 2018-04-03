package evolution;

import java.util.ArrayList;

/**
 * Result of creature evolution. Apart from the generation itself, it holds additional data used for statistics and display.
 * 
 * @author Oliver Meyer
 *
 */
public class Result {
    
    /**
     * The creatures that make up this generation
     */
    ArrayList<Creature> population;
    
    /**
     * Number of this generation relativ to the EvolutionSimulator life. The first generation simulated by any Simulator is always 1.
     */
    int currentGeneration;
    
    Integer histogram[];

}

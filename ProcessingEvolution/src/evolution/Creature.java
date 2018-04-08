package evolution;

import java.util.ArrayList;

import de.my.function.FloatBinaryOperation;
import de.my.function.FloatSupplier;
import processing.core.PApplet;

import static evolution.Evolution3WEB.*;

public class Creature implements Cloneable {
    private ArrayList<Node> n;
    private ArrayList<Muscle> m;
    private float fitness;
    final int id;
    private boolean alive;
    float creatureTimer;
    float mutability;

    /**
     * true iff the fitness of this creature as already been determined. There is no need to simulate again (if the
     * environment did not change!)
     */
    private boolean tested;

    private Creature(int tid, ArrayList<Node> tn, ArrayList<Muscle> tm,  boolean talive, float tct,
            float tmut) {
        id = tid;
        m = tm;
        n = tn;
        alive = talive;
        creatureTimer = tct;
        mutability = tmut;
    }

    /**
     * Create a new creature with the given data. The creature is alive and untested.
     * 
     */
    public Creature(int tid, ArrayList<Node> tn, ArrayList<Muscle> tm, float tct, float tmut) {
        this(tid, tn, tm,  true, tct, tmut);
    }

    /**
     * Creates a new random creature with the given id. The creature is "stable" and at the origin. 
     * @param id id to use for the new creature
     * @param random returns random number in the range [a, b)
     * @return newly created creature in a stable condition.
     */
    public static Creature createRandomCreature(int id, FloatBinaryOperation random) {
        ArrayList<Node> n;
        ArrayList<Muscle> m;
        // Determine number of nodes and muscles this creature will have
        int nodeNum = PApplet.parseInt(random.applyAsFloat(3, 6));
        n = new ArrayList<Node>(nodeNum);

        int muscleNum = PApplet.parseInt(random.applyAsFloat(nodeNum - 1, nodeNum * 3 - 6));
        m = new ArrayList<Muscle>(muscleNum);
        // Create Nodes
        for (int i = 0; i < nodeNum; i++) {
            // always use node size 0.4, used to be random(0.1,1), random(0,1)
            n.add(new Node(random.applyAsFloat(-1, 1), random.applyAsFloat(-1, 1), 0, 0,
                    random.applyAsFloat(MINIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE),
                    random.applyAsFloat(MINIMUM_NODE_FRICTION, MAXIMUM_NODE_FRICTION)));
        }
        // Create Muscles
        for (int i = 0; i < muscleNum; i++) {
            int tc1;
            int tc2;
            if (i < nodeNum - 1) {
                tc1 = i;
                tc2 = i + 1;
            } else {
                tc1 = PApplet.parseInt(random.applyAsFloat(0, nodeNum));
                tc2 = tc1;
                while (tc2 == tc1) {
                    tc2 = PApplet.parseInt(random.applyAsFloat(0, nodeNum));
                }
            }
            float rlength1 = random.applyAsFloat(0.5f, 1.5f);
            float rlength2 = random.applyAsFloat(0.5f, 1.5f);
            float rtime1 = random.applyAsFloat(0, 1);
            float rtime2 = random.applyAsFloat(0, 1);
            m.add(new Muscle(PApplet.parseInt(random.applyAsFloat(1, 3)), tc1, tc2, rtime1, rtime2,
                    min(rlength1, rlength2), max(rlength1, rlength2), isItContracted(rtime1, rtime2),
                    random.applyAsFloat(0.02f, 0.08f)));
        }
        // Create the creature based on these nodes and muscles
        float heartbeat = random.applyAsFloat(40, 80);
        Creature createdCreature = new Creature(id, new ArrayList<Node>(n), new ArrayList<Muscle>(m), heartbeat,
                1.0f);

        // That lone nodes and overlapping muscles are still considered when we come to a stable configuration seems weird. 

        // Make the created Nodes and muscles come to equilibrium 
        createdCreature.toStableConfiguration();
        // Move the creature to the center and to the floor
        createdCreature.adjustToCenter();

        createdCreature.checkForOverlap();
        createdCreature.checkForLoneNodes((x, y) -> random.applyAsFloat(x, y));

        return createdCreature;

    }

    /**
     * Returns a modified copy of this creature.
     * 
     * @param id
     *            id for the new creature to use.
     * @param r
     *            some random function
     * @param random
     *            another random function
     * @return
     */
    public Creature modified(int id, FloatSupplier r, FloatBinaryOperation random) {
        Creature modifiedCreature = new Creature(id, new ArrayList<Node>(0), new ArrayList<Muscle>(0), true,
                creatureTimer + r.getAsFloat() * 16 * mutability * Evolution3WEB.MUTABILITY_FACTOR,
                Evolution3WEB.min(mutability * Evolution3WEB.MUTABILITY_FACTOR * random.applyAsFloat(0.8f, 1.25f), 2));
        for (int i = 0; i < n.size(); i++) {
            modifiedCreature.n.add(n.get(i).modifyNode(mutability, r));
        }
        for (int i = 0; i < m.size(); i++) {
            modifiedCreature.m.add(m.get(i).modifyMuscle(n.size(), mutability, r, random));
        }
        if (random.applyAsFloat(0, 1) < 0.04f * mutability * Evolution3WEB.MUTABILITY_FACTOR || n.size() <= 2) {
            // Add a node
            modifiedCreature.addRandomNode(random);
        }
        if (random.applyAsFloat(0, 1) < 0.04f * mutability * Evolution3WEB.MUTABILITY_FACTOR) {
            // Add a muscle
            modifiedCreature.addRandomMuscle(-1, -1, random);
        }
        if (random.applyAsFloat(0, 1) < 0.04f * mutability * Evolution3WEB.MUTABILITY_FACTOR
                && modifiedCreature.n.size() >= 4) {
            // Remove a node
            modifiedCreature.removeRandomNode(random);
        }
        if (random.applyAsFloat(0, 1) < 0.04f * mutability * Evolution3WEB.MUTABILITY_FACTOR
                && modifiedCreature.m.size() >= 2) {
            // Remove a muscle
            modifiedCreature.removeRandomMuscle(random);
        }
        modifiedCreature.checkForOverlap();
        modifiedCreature.checkForLoneNodes(random);
        modifiedCreature.normalize();
        return modifiedCreature;
    }

    public void checkForOverlap() {
        ArrayList<Integer> bads = new ArrayList<Integer>();
        for (int i = 0; i < m.size(); i++) {
            for (int j = i + 1; j < m.size(); j++) {
                if (m.get(i).c1 == m.get(j).c1 && m.get(i).c2 == m.get(j).c2) {
                    bads.add(i);
                } else if (m.get(i).c1 == m.get(j).c2 && m.get(i).c2 == m.get(j).c1) {
                    bads.add(i);
                } else if (m.get(i).c1 == m.get(i).c2) {
                    bads.add(i);
                }
            }
        }
        for (int i = bads.size() - 1; i >= 0; i--) {
            int b = bads.get(i) + 0;
            if (b < m.size()) {
                m.remove(b);
            }
        }
        tested = false;
    }

    public void checkForLoneNodes(FloatBinaryOperation random) {
        if (n.size() >= 3) {
            for (int i = 0; i < n.size(); i++) {
                int connections = 0;
                int connectedTo = -1;
                for (int j = 0; j < m.size(); j++) {
                    if (m.get(j).c1 == i) {
                        connections++;
                        connectedTo = m.get(j).c2;
                    } else if (m.get(j).c2 == i) {
                        connections++;
                        connectedTo = m.get(j).c1;
                    }
                }
                if (connections <= 1) {
                    int newConnectionNode = Evolution3WEB.floor(random.applyAsFloat(0, n.size()));
                    while (newConnectionNode == i || newConnectionNode == connectedTo) {
                        newConnectionNode = Evolution3WEB.floor(random.applyAsFloat(0, n.size()));
                    }
                    addRandomMuscle(i, newConnectionNode, random);
                }
            }
        }
        tested = false;
    }

    private void addRandomNode(FloatBinaryOperation random) {
        int parentNode = Evolution3WEB.floor(random.applyAsFloat(0, n.size()));
        float ang1 = random.applyAsFloat(0, 2 * Evolution3WEB.PI);
        float distance = Evolution3WEB.sqrt(random.applyAsFloat(0, 1));
        float x = n.get(parentNode).x + Evolution3WEB.cos(ang1) * 0.5f * distance;
        float y = n.get(parentNode).y + Evolution3WEB.sin(ang1) * 0.5f * distance;
        n.add(new Node(x, y, 0, 0,
                random.applyAsFloat(Evolution3WEB.MINIMUM_NODE_SIZE, Evolution3WEB.MAXIMUM_NODE_SIZE),
                random.applyAsFloat(Evolution3WEB.MINIMUM_NODE_FRICTION, Evolution3WEB.MAXIMUM_NODE_FRICTION))); // random(0.1,1),random(0,1)
        int nextClosestNode = 0;
        float record = 100000;
        for (int i = 0; i < n.size() - 1; i++) {
            if (i != parentNode) {
                float dx = n.get(i).x - x;
                float dy = n.get(i).y - y;
                if (Evolution3WEB.sqrt(dx * dx + dy * dy) < record) {
                    record = Evolution3WEB.sqrt(dx * dx + dy * dy);
                    nextClosestNode = i;
                }
            }
        }
        addRandomMuscle(parentNode, n.size() - 1, random);
        addRandomMuscle(nextClosestNode, n.size() - 1, random);
    }

    public void addRandomMuscle(int tc1, int tc2, FloatBinaryOperation random) {
        if (tc1 == -1) {
            tc1 = PApplet.parseInt(random.applyAsFloat(0, n.size()));
            tc2 = tc1;
            while (tc2 == tc1 && n.size() >= 2) {
                tc2 = PApplet.parseInt(random.applyAsFloat(0, n.size()));
            }
        }
        float rlength1 = random.applyAsFloat(0.5f, 1.5f);
        float rlength2 = random.applyAsFloat(0.5f, 1.5f);
        float rtime1 = random.applyAsFloat(0, 1);
        float rtime2 = random.applyAsFloat(0, 1);
        if (tc1 != -1) {
            float distance = Evolution3WEB.dist(n.get(tc1).x, n.get(tc1).y, n.get(tc2).x, n.get(tc2).y);
            float ratio = random.applyAsFloat(0.01f, 0.2f);
            rlength1 = distance * (1 - ratio);
            rlength2 = distance * (1 + ratio);
        }
        m.add(new Muscle(PApplet.parseInt(random.applyAsFloat(1, 3)), tc1, tc2, rtime1, rtime2,
                Evolution3WEB.min(rlength1, rlength2), Evolution3WEB.max(rlength1, rlength2),
                Evolution3WEB.isItContracted(rtime1, rtime2), random.applyAsFloat(0.02f, 0.08f)));
    }

    public void removeRandomNode(FloatBinaryOperation random) {
        int choice = Evolution3WEB.floor(random.applyAsFloat(0, n.size()));
        n.remove(choice);
        int i = 0;
        while (i < m.size()) {
            if (m.get(i).c1 == choice || m.get(i).c2 == choice) {
                m.remove(i);
            } else {
                i++;
            }
        }
        for (int j = 0; j < m.size(); j++) {
            if (m.get(j).c1 >= choice) {
                m.get(j).c1--;
            }
            if (m.get(j).c2 >= choice) {
                m.get(j).c2--;
            }
        }
    }

    public void removeRandomMuscle(FloatBinaryOperation random) {
        int choice = Evolution3WEB.floor(random.applyAsFloat(0, m.size()));
        m.remove(choice);
    }

    /**
     * @param newID
     *            id of the new creature or -1 to copy the id as well
     * @return a copy if this, possibly with a new id.
     */
    public Creature copyCreature(int newID) {
        ArrayList<Node> n2 = new ArrayList<Node>(n.size());
        ArrayList<Muscle> m2 = new ArrayList<Muscle>(m.size());
        for (int i = 0; i < n.size(); i++) {
            n2.add(n.get(i).copyNode());
        }
        for (int i = 0; i < m.size(); i++) {
            m2.add(m.get(i).copyMuscle());
        }
        if (newID == -1) {
            newID = id;
        }
        Creature copy = new Creature(newID, n2, m2, isAlive(), creatureTimer, mutability);
        copy.fitness = this.fitness;
        copy.tested = this.tested;
        return copy;
    }
    
    @Override
    public Creature clone() {
        return copyCreature(-1);
    }

    /**
     * Set the fitness of the creature. The fitness of the creature is defined by the environment. The creature does
     * store its fitness, but does not determine it.
     * 
     * @param f
     *            new fitness of the creature.
     */
    public void setFitness(float f) {
        fitness = f;
        tested = true;
    }

    /**
     * Getter for the fitness of the creature.
     * 
     * @return the what the environment determined as the fitness of the creature.
     */
    public float getFitness() {
        return fitness;
    }

    /**
     * @return true iff this creature has been given its fitness.
     */
    public boolean hasBeenTested() {
        return tested;
    }

    public float getAverage() {
        float sum = 0;
        for (int i = 0; i < n.size(); i++) {
            Node ni = n.get(i);
            sum += ni.x;
        }
        return sum / n.size();
    }

    public void normalize() {
        toStableConfiguration();
        adjustToCenter();
    }

    public void toStableConfiguration() {
        for (int j = 0; j < 200; j++) {
            for (int i = 0; i < m.size(); i++) {
                float target;
                Muscle mi = m.get(i);
                if (mi.contracted) {
                    target = mi.contractLength;
                } else {
                    target = mi.extendLength;
                }
                mi.applyForce(n, target);
            }
            for (int i = 0; i < n.size(); i++) {
                Node ni = n.get(i);
                ni.applyForces();
            }
        }
        for (int i = 0; i < n.size(); i++) {
            Node ni = n.get(i);
            ni.vx = 0;
            ni.vy = 0;
        }
    }

    private void adjustToCenter() {
        float avx = 0;
        float lowY = -1000;
        for (int i = 0; i < n.size(); i++) {
            Node ni = n.get(i);
            avx += ni.x;
            if (ni.y + ni.m / 2 > lowY) {
                lowY = ni.y + ni.m / 2;
            }
        }
        avx /= n.size();
        for (int i = 0; i < n.size(); i++) {
            Node ni = n.get(i);
            ni.x -= avx;
            ni.y -= lowY;
        }
        tested = false;
    }

    public void simulate(int timer, Iterable<? extends Rectangle> rects) {
        for (int i = 0; i < m.size(); i++) {
            Muscle mi = m.get(i);

            float target2;
            target2 = mi.calculateTargetLength(timer, creatureTimer);
            mi.applyForce(n, target2);
        }
        for (int i = 0; i < n.size(); i++) {
            Node ni = n.get(i);
            ni.applyForces();
            ni.applyGravity();
            ni.hitWalls(rects);
        }
    }

    public void copyNodes(ArrayList<Node> n2) {
        for (int i = 0; i < n.size(); i++) {
            n2.add(n.get(i).copyNode());
        }

    }

    /**
     * Species are identified by node count and muscle count.
     * 
     * @return Species identifier
     */
    public int getSpecies() {
        return (n.size() % 10) * 10 + (m.size() % 10);
    }

    public void drawCreatureWhole(float x, float y, int toImage, Evolution3WEB canvas) {
        for (int i = 0; i < m.size(); i++) {
            Muscle mi = m.get(i);
            canvas.drawMuscle(mi, n.get(mi.c1), n.get(mi.c2), x, y, toImage);
        }
        for (int i = 0; i < n.size(); i++) {
            canvas.drawNode(n.get(i), x, y, toImage);
        }
    }

    public void copyMuscles(ArrayList<Muscle> m2) {
        for (int i = 0; i < m.size(); i++) {
            m2.add(m.get(i).copyMuscle());
        }

    }

    /**
     * @return the alive
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * @param alive
     *            the alive to set
     */
    public void die() {
        this.alive = false;
    }
}
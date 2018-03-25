package evolution;

import java.util.ArrayList;
import java.util.Arrays;

import de.my.performance.PerfRecorder;
import evolution.parallel.EvolutionSimulator;
import evolution.parallel.ParallelSimulation;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.event.MouseEvent;

import static evolution.Evolution3WEB.MenuStates.*;

public class Evolution3WEB extends PApplet {

    // Number of creature in one generation. Please note, that the program is not fit to handle anything but 1000 here, yet.
    private static final int CREATURE_COUNT = 1000;

    enum MenuStates {
        MENU_0_TITLE_PAGE, MENU_1_SHOW_STATS, MENU_2_CREATE_INITIAL_POPULATION, MENU_3_RESET_GEN, MENU_4_SELECT_OR_SIMULATE, MENU_5_SIMULATE_SINGLE_RUNNING, MENU_6_SORT_UPDATE_STATS, MENU_7_SHOW_RESULTS, MENU_8_SHOW_SORTING, MENU_9_SHOW_SORTED_CREATURES, MENU_10_KILL_CREATURES, MENU_11_SHOW_DEAD, MENU_12_REPRODUCE, MENU_13_SHOW_NEW_GENERATION
    };

    // These are the easy-to-edit variables.
    final boolean USE_RANDOM_SEED = true;
    // determines whether random factors will be determined by the preset seed.
    // If this is false, the program will run differently every time. If it's
    // true, it will run exactly the same.
    final int SEED = 38;
    final float WINDOW_SIZE = 1.0f; // window size multiplier. If it's 1, the
                                    // size is 1280x720.
                                    // The seed that determines all random factors in the simulation. Same seed
                                    // = same simulation results,
                                    // different seed = different simulation results. Make sure USE_RANDOM_SEED
                                    // is true for this.
    final float SORT_ANIMATION_SPEED = 5.0f; // Determines speed of sorting
                                             // animation. Higher number is
                                             // faster.
    public final static float MINIMUM_NODE_SIZE = 0.4f; // Note: all units are 20 cm.
    // Meaning, a value of 1
    // equates to a 20 cm node.
    public final static float MAXIMUM_NODE_SIZE = 0.4f;
    public final static float MINIMUM_NODE_FRICTION = 0.0f;
    public final static float MAXIMUM_NODE_FRICTION = 1.0f;
    public final static float GRAVITY = 0.005f; // higher = more friction.
    public final static float AIR_FRICTION = 0.95f; // The lower the number, the more
    // friction. 1 = no friction.
    // Above 1 = chaos.
    public final static float MUTABILITY_FACTOR = 1.0f; // How fast the creatures
    // mutate. 1 is normal.

    public final static boolean haveGround = true; // true if the ground exists, false
    // if no ground.

    // Add rectangular obstacles by filling up this array of rectangles. The
    // parameters are x1, y1, x2, y2, specifying
    // two opposite vertices. NOTE: The units are 20 cm, so 1 = 20 cm, and 5 = 1
    // m.
    // ALSO NOTE: y-values increase as you go down. So 3 is in the air, and -3
    // is in the ground. 0 is the surface.
    final Rectangle[] RECTANGLES = {
            //Example hurdles 
            //            new Rectangle(2, -0.4, 7, 1), new Rectangle(4, -0.8f, 9, 1), new Rectangle(6, -1.2, 11, 1),
            //            new Rectangle(8, -1.6, 13, 1), new Rectangle(10, -2, 15, 1), new Rectangle(12, -2.4f, 17, 1),
            //            new Rectangle(14, -2.8, 19, 1), new Rectangle(16, -3.2, 21, 1), new Rectangle(18, -3.6f, 23, 1),
            //            new Rectangle(20, -4.0, 25, 1)

    };

    float histMinValue = -1; // histogram information
    float histMaxValue = 8;
    int histBarsPerMeter = 10;

    // Okay, that's all the easy to edit stuff.

    PFont font;
    ArrayList<Float[]> percentile = new ArrayList<Float[]>(0);
    ArrayList<Integer[]> barCounts = new ArrayList<Integer[]>(0);
    ArrayList<Integer[]> speciesCounts = new ArrayList<Integer[]>(0);
    ArrayList<Integer> topSpeciesCounts = new ArrayList<Integer>(0);
    ArrayList<Creature> creatureDatabase = new ArrayList<Creature>(0);
    ArrayList<Rectangle> rects = new ArrayList<Rectangle>(0);
    PGraphics graphImage;
    PGraphics screenImage;
    PGraphics popUpImage;
    PGraphics segBarImage;
    // 0 = 100th percentile
    // 1 = 90th percentile
    // ...
    // 8 = 20th percentile
    // 9 = 10th percentile
    // 10 = 9th percentile
    // 11 = 8th percentile
    // ...
    // 19 = 0th percentile
    int minBar = PApplet.parseInt(histMinValue * histBarsPerMeter);
    int maxBar = PApplet.parseInt(histMaxValue * histBarsPerMeter);
    int barLen = maxBar - minBar;
    int gensToDo = 0;
    float cTimer = 60;

    int windowWidth = 1280;
    int windowHeight = 720;
    int timer = 0;
    float cam = 0;
    int frames = 60;
    MenuStates menu = MENU_0_TITLE_PAGE;
    int gen = -1;
    float sliderX = 1170;
    int genSelected = 0;
    boolean drag = false;
    boolean justGotBack = false;
    int creaturesTested = 0;
    int fontSize = 0;
    int[] fontSizes = { 50, 36, 25, 20, 16, 14, 11, 9 };
    int statusWindow = -4;
    int overallTimer = 0;
    boolean miniSimulation = false;
    int creatureWatching = 0;
    int simulationTimer = 0;
    int[] creaturesInPosition = new int[CREATURE_COUNT];

    float camzoom = 0.015f;

    float average;
    int speed;
    int id;
    boolean stepbystep;
    boolean stepbystepslow;
    boolean slowDies;
    int timeShow;
    int[] p;

    public Evolution3WEB() {
        super();
        if (CREATURE_COUNT == 1000) {
            p = new int[] { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500, 600, 700, 800, 900, 910,
                    920, 930, 940, 950, 960, 970, 980, 990, 999 };
        } else {
            p = new int[29];
            p[1] = 0;
            for (int i = 1; i < p.length; i++) {
                p[i] = CREATURE_COUNT / p.length * i;
            }
            p[p.length] = CREATURE_COUNT - 1;
        }
    }

    public float inter(int a, int b, float offset) {
        return PApplet.parseFloat(a) + (PApplet.parseFloat(b) - PApplet.parseFloat(a)) * offset;
    }

    public float r() {
        return pow(random(-1, 1), 19);
    }

    public void drawGround(int toImage) {
        if (toImage == 0) {
            noStroke();
            fill(0, 130, 0);
            if (haveGround)
                rect(0, windowHeight * 0.8f, windowWidth, windowHeight * 0.2f);
            for (int i = 0; i < rects.size(); i++) {
                Rectangle r = rects.get(i);
                rect(r.x1 / camzoom - cam / camzoom + windowWidth / 2, r.y1 / camzoom + windowHeight * 0.8f,
                        (r.x2 - r.x1) / camzoom, (r.y2 - r.y1) / camzoom);
            }
        } else if (toImage == 2) {
            popUpImage.noStroke();
            popUpImage.fill(0, 130, 0);
            if (haveGround)
                popUpImage.rect(0, 360, 450, 90);
            float ww = 450;
            float wh = 450;
            for (int i = 0; i < rects.size(); i++) {
                Rectangle r = rects.get(i);
                popUpImage.rect(r.x1 / camzoom - cam / camzoom + ww / 2, r.y1 / camzoom + wh * 0.8f,
                        (r.x2 - r.x1) / camzoom, (r.y2 - r.y1) / camzoom);
            }
        }
    }

    public void drawNode(Node ni, float x, float y, int toImage) {
        int c = color(512 - PApplet.parseInt(ni.f * 512), 0, 0);
        if (ni.f <= 0.5f) {
            c = color(255, 255 - PApplet.parseInt(ni.f * 512), 255 - PApplet.parseInt(ni.f * 512));
        }
        if (toImage == 0) {
            fill(c);
            noStroke();
            ellipse(ni.x / camzoom + x, ni.y / camzoom + y, ni.m / camzoom, ni.m / camzoom);
        } else if (toImage == 1) {
            screenImage.fill(c);
            screenImage.noStroke();
            screenImage.ellipse(ni.x / camzoom + x, ni.y / camzoom + y, ni.m / camzoom, ni.m / camzoom);
        } else if (toImage == 2) {
            popUpImage.fill(c);
            popUpImage.noStroke();
            popUpImage.ellipse(ni.x / camzoom + x, ni.y / camzoom + y, ni.m / camzoom, ni.m / camzoom);
        }
    }

    public void drawMuscle(Muscle mi, Node ni1, Node ni2, float x, float y, int toImage) {
        boolean c = mi.contracted;
        float w = 0.1f / camzoom;
        if (c) {
            w = 0.2f / camzoom;
        }
        if (toImage == 0) {
            strokeWeight(w);
            stroke(70, 35, 0, mi.rigidity * 3000);
            line(ni1.x / camzoom + x, ni1.y / camzoom + y, ni2.x / camzoom + x, ni2.y / camzoom + y);
        } else if (toImage == 1) {
            screenImage.strokeWeight(w);
            screenImage.stroke(70, 35, 0, mi.rigidity * 3000);
            screenImage.line(ni1.x / camzoom + x, ni1.y / camzoom + y, ni2.x / camzoom + x, ni2.y / camzoom + y);
        } else if (toImage == 2) {
            popUpImage.strokeWeight(w);
            popUpImage.stroke(70, 35, 0, mi.rigidity * 3000);
            popUpImage.line(ni1.x / camzoom + x, ni1.y / camzoom + y, ni2.x / camzoom + x, ni2.y / camzoom + y);
        }
    }

    public void drawPosts(int toImage) {
        if (toImage == 0) {
            noStroke();
            for (int i = PApplet.parseInt((-cam * camzoom - windowWidth / 2) / 5) - 1; i <= PApplet
                    .parseInt((-cam * camzoom + windowWidth / 2) / 5) + 1; i++) {
                fill(255);
                rect(windowWidth / 2 + (i * 5 - cam - 0.1f) / camzoom, windowHeight * 0.8f - 3 / camzoom,
                        0.2f / camzoom, 3 / camzoom);
                rect(windowWidth / 2 + (i * 5 - cam - 1) / camzoom, windowHeight * 0.8f - 3 / camzoom, 2 / camzoom,
                        1 / camzoom);
                fill(120);
                text(i + " m", windowWidth / 2 + (i * 5 - cam) / camzoom, windowHeight * 0.8f - 2.17f / camzoom);
            }
        } else if (toImage == 2) {
            popUpImage.textAlign(CENTER);
            popUpImage.textFont(font, 0.96f / camzoom);
            popUpImage.noStroke();
            float w = 450;
            float h = 450;
            for (int i = PApplet.parseInt((-cam * camzoom - w / 2) / 5) - 1; i <= PApplet
                    .parseInt((-cam * camzoom + w / 2) / 5) + 1; i++) {
                popUpImage.fill(255);
                popUpImage.rect(w / 2 + (i * 5 - cam - 0.1f) / camzoom, h * 0.8f - 3 / camzoom, 0.2f / camzoom,
                        3 / camzoom);
                popUpImage.rect(w / 2 + (i * 5 - cam - 1) / camzoom, h * 0.8f - 3 / camzoom, 2 / camzoom, 1 / camzoom);
                popUpImage.fill(120);
                popUpImage.text(i + " m", w / 2 + (i * 5 - cam) / camzoom, h * 0.8f - 2.17f / camzoom);
            }
        }
    }

    public void drawArrow(float x) {
        noStroke();
        fill(120, 0, 255);
        rect(windowWidth / 2 + (x - cam - 1.7f) / camzoom, windowHeight * 0.8f - 4.8f / camzoom, 3.4f / camzoom,
                1.1f / camzoom);
        beginShape();
        vertex(windowWidth / 2 + (x - cam) / camzoom, windowHeight * 0.8f - 3.2f / camzoom);
        vertex(windowWidth / 2 + (x - cam - 0.5f) / camzoom, windowHeight * 0.8f - 3.7f / camzoom);
        vertex(windowWidth / 2 + (x - cam + 0.5f) / camzoom, windowHeight * 0.8f - 3.7f / camzoom);
        endShape(CLOSE);
        fill(255);
        text((PApplet.parseFloat(round(x * 2)) / 10) + " m", windowWidth / 2 + (x - cam) / camzoom,
                windowHeight * 0.8f - 3.91f / camzoom);
    }

    public void drawGraphImage() {
        image(graphImage, 50, 180, 650, 380);
        image(segBarImage, 50, 580, 650, 100);
        if (gen >= 1) {
            stroke(0, 160, 0, 255);
            strokeWeight(3);
            float genWidth = PApplet.parseFloat(610) / gen;
            float lineX = 90 + genSelected * genWidth;
            line(lineX, 180, lineX, 500 + 180);
            Integer[] s = speciesCounts.get(genSelected);
            textAlign(LEFT);
            textFont(font, 12);
            noStroke();
            for (int i = 1; i < 101; i++) {
                int c = s[i] - s[i - 1];
                if (c >= 25) {
                    float y = ((s[i] + s[i - 1]) / 2) / 1000.0f * 100 + 573;
                    if (i - 1 == topSpeciesCounts.get(genSelected)) {
                        stroke(0);
                        strokeWeight(2);
                    } else {
                        noStroke();
                    }
                    fill(255, 255, 255);
                    rect(lineX + 10, y, 50, 14);
                    colorMode(HSB, 1.0f);
                    fill(getColor(i - 1, true));
                    text("S" + floor((i - 1) / 10) + "" + ((i - 1) % 10) + ": " + c, lineX + 11, y + 12);
                    colorMode(RGB, 255);
                }
            }
            noStroke();
        }
    }

    public int getColor(int i, boolean adjust) {
        colorMode(HSB, 1.0f);
        float col = (i * 1.618034f) % 1;
        if (i == 46) {
            col = 0.083333f;
        } else if (i == 44) {
            col = 0.1666666f;
        } else if (i == 57) {
            col = 0.5f;
        }
        float light = 1.0f;
        if (abs(col - 0.333f) <= 0.18f && adjust) {
            light = 0.7f;
        }
        return color(col, 1.0f, light);
    }

    public void drawGraph(int graphWidth, int graphHeight) {
        drawLines(60, PApplet.parseInt(graphHeight * 0.05f), graphWidth - 60, PApplet.parseInt(graphHeight * 0.9f));
        if (gen >= 1) {
            drawSegBars(60, 0, graphWidth - 60, 150);
        }
    }

    public void drawLines(int x, int y, int graphWidth, int graphHeight) {
        graphImage.beginDraw();
        graphImage.smooth();
        graphImage.background(220);
        if (gen >= 1) {
            float gh = PApplet.parseFloat(graphHeight);
            float genWidth = PApplet.parseFloat(graphWidth) / gen;
            float best = extreme(1);
            float worst = extreme(-1);
            float meterHeight = PApplet.parseFloat(graphHeight) / (best - worst);
            float zero = (best / (best - worst)) * gh;
            float unit = setUnit(best, worst);
            graphImage.stroke(150);
            graphImage.strokeWeight(2);
            graphImage.fill(150);
            graphImage.textFont(font, 18);
            graphImage.textAlign(RIGHT);
            for (float i = ceil((worst - (best - worst) / 18.0f) / unit) * unit; i < best
                    + (best - worst) / 18.0f; i += unit) {
                float lineY = y - i * meterHeight + zero;
                graphImage.line(x, lineY, graphWidth + x, lineY);
                graphImage.text(showUnit(i, unit) + " m", x - 5, lineY + 4);
            }
            graphImage.stroke(0);
            for (int i = 0; i < 29; i++) {
                int k;
                if (i == 28) {
                    k = 14;
                } else if (i < 14) {
                    k = i;
                } else {
                    k = i + 1;
                }
                if (k == 14) {
                    graphImage.stroke(255, 0, 0, 255);
                    graphImage.strokeWeight(5);
                } else {
                    stroke(0);
                    if (k == 0 || k == 28 || (k >= 10 && k <= 18)) {
                        graphImage.strokeWeight(3);
                    } else {
                        graphImage.strokeWeight(1);
                    }
                }
                for (int j = 0; j < gen; j++) {
                    graphImage.line(x + j * genWidth, (-percentile.get(j)[k]) * meterHeight + zero + y,
                            x + (j + 1) * genWidth, (-percentile.get(j + 1)[k]) * meterHeight + zero + y);
                }
            }
        }
        graphImage.endDraw();
    }

    public void drawSegBars(int x, int y, int graphWidth, int graphHeight) {
        segBarImage.beginDraw();
        segBarImage.smooth();
        segBarImage.noStroke();
        segBarImage.colorMode(HSB, 1);
        segBarImage.background(0, 0, 0.5f);
        float genWidth = PApplet.parseFloat(graphWidth) / gen;
        int gensPerBar = floor(gen / 500) + 1;
        for (int i = 0; i < gen; i += gensPerBar) {
            int i2 = min(i + gensPerBar, gen);
            float barX1 = x + i * genWidth;
            float barX2 = x + i2 * genWidth;
            for (int j = 0; j < 100; j++) {
                segBarImage.fill(getColor(j, false));
                segBarImage.beginShape();
                segBarImage.vertex(barX1, y + speciesCounts.get(i)[j] / 1000.0f * graphHeight);
                segBarImage.vertex(barX1, y + speciesCounts.get(i)[j + 1] / 1000.0f * graphHeight);
                segBarImage.vertex(barX2, y + speciesCounts.get(i2)[j + 1] / 1000.0f * graphHeight);
                segBarImage.vertex(barX2, y + speciesCounts.get(i2)[j] / 1000.0f * graphHeight);
                segBarImage.endShape();
            }
        }
        segBarImage.endDraw();
        colorMode(RGB, 255);
    }

    public float extreme(float sign) {
        float record = -sign;
        for (int i = 0; i < gen; i++) {
            float toTest = percentile.get(i + 1)[PApplet.parseInt(14 - sign * 14)];
            if (toTest * sign > record * sign) {
                record = toTest;
            }
        }
        return record;
    }

    public float setUnit(float best, float worst) {
        float unit2 = 3 * log(best - worst) / log(10) - 3.3f;
        if ((unit2 + 100) % 3 < 1) {
            return pow(10, PApplet.parseInt(unit2 / 3));
        } else if ((unit2 + 100) % 3 < 2) {
            return pow(10, PApplet.parseInt((unit2 - 1) / 3)) * 2;
        } else {
            return pow(10, PApplet.parseInt((unit2 - 2) / 3)) * 5;
        }
    }

    public String showUnit(float i, float unit) {
        if (unit < 1) {
            return nf(i, 0, 2) + "";
        } else {
            return PApplet.parseInt(i) + "";
        }
    }

    public ArrayList<Creature> quickSort(ArrayList<Creature> c) {
        if (c.size() <= 1) {
            return c;
        } else {
            ArrayList<Creature> less = new ArrayList<Creature>();
            ArrayList<Creature> more = new ArrayList<Creature>();
            ArrayList<Creature> equal = new ArrayList<Creature>();
            Creature c0 = c.get(0);
            equal.add(c0);
            for (int i = 1; i < c.size(); i++) {
                Creature ci = c.get(i);
                if (ci.getFitness() == c0.getFitness()) {
                    equal.add(ci);
                } else if (ci.getFitness() < c0.getFitness()) {
                    less.add(ci);
                } else {
                    more.add(ci);
                }
            }
            ArrayList<Creature> total = new ArrayList<Creature>();
            total.addAll(quickSort(more));
            total.addAll(equal);
            total.addAll(quickSort(less));
            return total;
        }
    }

    public static boolean isItContracted(float rtime1, float rtime2) {
        if (rtime1 <= rtime2) {
            return true;
        } else {
            return false;
        }
    }

    public void toStableConfiguration(int nodeNum, int muscleNum) {
        for (int j = 0; j < 200; j++) {
            for (int i = 0; i < muscleNum; i++) {
                float target;
                Muscle mi = m.get(i);
                if (mi.contracted) {
                    target = mi.contractLength;
                } else {
                    target = mi.extendLength;
                }
                mi.applyForce(n, target);
            }
            for (int i = 0; i < nodeNum; i++) {
                Node ni = n.get(i);
                ni.applyForces();
            }
        }
        for (int i = 0; i < nodeNum; i++) {
            Node ni = n.get(i);
            ni.vx = 0;
            ni.vy = 0;
        }
    }

    public void adjustToCenter(int nodeNum) {
        float avx = 0;
        float lowY = -1000;
        for (int i = 0; i < nodeNum; i++) {
            Node ni = n.get(i);
            avx += ni.x;
            if (ni.y + ni.m / 2 > lowY) {
                lowY = ni.y + ni.m / 2;
            }
        }
        avx /= nodeNum;
        for (int i = 0; i < nodeNum; i++) {
            Node ni = n.get(i);
            ni.x -= avx;
            ni.y -= lowY;
        }
    }

    public void setGlobalVariables(Creature thisCreature) {
        n.clear();
        m.clear();
        thisCreature.copyNodes(n);
        thisCreature.copyMuscles(m);
        id = thisCreature.id;
        timer = 0;
        camzoom = 0.01f;
        cam = 0;
        cTimer = thisCreature.creatureTimer;
        simulationTimer = 0;
    }

    public void simulate() {
        for (int i = 0; i < m.size(); i++) {
            Muscle mi = m.get(i);

            float target2;
            target2 = mi.calculateTargetLength(timer, cTimer);
            mi.applyForce(n, target2);
        }
        for (int i = 0; i < n.size(); i++) {
            Node ni = n.get(i);
            ni.applyForces();
            ni.applyGravity();
            ni.hitWalls(rects);
        }
    }

    public void setAverage() {
        average = 0;
        for (int i = 0; i < n.size(); i++) {
            Node ni = n.get(i);
            average += ni.x;
        }
        average = average / n.size();
    }

    ArrayList<Node> n = new ArrayList<Node>();
    ArrayList<Muscle> m = new ArrayList<Muscle>();
    Creature[] c = new Creature[CREATURE_COUNT];
    ArrayList<Creature> c2 = new ArrayList<Creature>();

    public void mouseWheel(MouseEvent event) {
        int delta = event.getCount();
        if (menu == MENU_5_SIMULATE_SINGLE_RUNNING) {
            if (delta == -1) {
                camzoom *= 0.9090909f;
                if (camzoom < 0.006f) {
                    camzoom = 0.006f;
                }
                textFont(font, 0.96f / camzoom);
            } else if (delta == 1) {
                camzoom *= 1.1f;
                if (camzoom > 0.1f) {
                    camzoom = 0.1f;
                }
                textFont(font, 0.96f / camzoom);
            }
        }
    }

    public void mousePressed() {
        if (gensToDo >= 1) {
            gensToDo = 0;
        }
        float mX = mouseX / WINDOW_SIZE;
        float mY = mouseY / WINDOW_SIZE;
        if (menu == MENU_1_SHOW_STATS && gen >= 1 && abs(mY - 365) <= 25 && abs(mX - sliderX - 25) <= 25) {
            drag = true;
        }
    }

    public void openMiniSimulation() {
        simulationTimer = 0;
        if (gensToDo == 0) {
            miniSimulation = true;
            int id;
            Creature cj;
            if (statusWindow <= -1) {
                cj = creatureDatabase.get((genSelected - 1) * 3 + statusWindow + 3);
                id = cj.id;
            } else {
                id = statusWindow;
                cj = c2.get(id);
            }
            setGlobalVariables(cj);
            creatureWatching = id;
        }
    }

    public void setMenu(MenuStates m) {
        menu = m;
        if (m == MENU_1_SHOW_STATS) {
            drawGraph(975, 570);
        }
    }

    public void startASAP() {
        setMenu(MENU_4_SELECT_OR_SIMULATE);
        creaturesTested = 0;
        stepbystep = false;
        stepbystepslow = false;
    }

    public void mouseReleased() {
        drag = false;
        miniSimulation = false;
        float mX = mouseX / WINDOW_SIZE;
        float mY = mouseY / WINDOW_SIZE;
        if (menu == MENU_0_TITLE_PAGE && abs(mX - windowWidth / 2) <= 200 && abs(mY - 400) <= 100) {
            setMenu(MENU_1_SHOW_STATS);
        } else if (menu == MENU_1_SHOW_STATS && gen == -1 && abs(mX - 120) <= 100 && abs(mY - 300) <= 50) {
            setMenu(MENU_2_CREATE_INITIAL_POPULATION);
        } else if (menu == MENU_1_SHOW_STATS && gen >= 0 && abs(mX - 990) <= 230) {
            if (abs(mY - 40) <= 20) {
                // Do 1 step-by-step generation
                setMenu(MENU_4_SELECT_OR_SIMULATE);
                creaturesTested = 0;
                stepbystep = true;
                stepbystepslow = true;
            }
            if (abs(mY - 90) <= 20) {
                // Do 1 quick generation
                setMenu(MENU_4_SELECT_OR_SIMULATE);
                creaturesTested = 0;
                stepbystep = true;
                stepbystepslow = false;
            }
            if (abs(mY - 140) <= 20) {
                // Do ASAP
                if (mX < 990) {
                    // Do 1 gen ASAP.
                    gensToDo = 1;
                } else {
                    // Do gens ALAP.
                    gensToDo = 1000000000;
                }
                setMenu(MENU_4_SELECT_OR_SIMULATE);
                creaturesTested = 0;
                stepbystep = false;
                stepbystepslow = false;
            }
        } else if (menu == MENU_3_RESET_GEN && abs(mX - 1030) <= 130 && abs(mY - 684) <= 20) {
            gen = 0;
            setMenu(MENU_1_SHOW_STATS);
        } else if (menu == MENU_7_SHOW_RESULTS && abs(mX - 1030) <= 130 && abs(mY - 684) <= 20) {
            setMenu(MENU_8_SHOW_SORTING);
        } else if (menu == MENU_9_SHOW_SORTED_CREATURES && abs(mX - 1030) <= 130 && abs(mY - 690) <= 20) {
            setMenu(MENU_10_KILL_CREATURES);
        } else if (menu == MENU_11_SHOW_DEAD && abs(mX - 1130) <= 80 && abs(mY - 690) <= 20) {
            setMenu(MENU_12_REPRODUCE);
        } else if (menu == MENU_13_SHOW_NEW_GENERATION && abs(mX - 1130) <= 80 && abs(mY - 690) <= 20) {
            setMenu(MENU_1_SHOW_STATS);
        }
    }

    public void drawScreenImage(int stage) {
        screenImage.beginDraw();
        screenImage.smooth();
        screenImage.background(220, 253, 102);
        screenImage.noStroke();
        camzoom = 0.12f;
        for (int j = 0; j < CREATURE_COUNT; j++) {
            Creature cj = c2.get(j);
            if (stage == 3)
                cj = c[cj.id - (gen * CREATURE_COUNT) - 1001];
            int j2 = j;
            if (stage == 0) {
                j2 = cj.id - (gen * CREATURE_COUNT) - 1;
                creaturesInPosition[j2] = j;
            }
            int x = j2 % 40;
            int y = floor(j2 / 40);
            if (stage >= 1)
                y++;
            cj.drawCreatureWhole(x * 30 + 55, y * 25 + 40, 1, this);
        }
        timer = 0;
        screenImage.textAlign(CENTER);
        screenImage.textFont(font, 24);
        screenImage.fill(100, 100, 200);
        if (stage == 0) {
            screenImage.rect(900, 664, 260, 40);
            screenImage.fill(0);
            screenImage.text("All 1,000 creatures have been tested.  Now let's sort them!", windowWidth / 2 - 200, 690);
            screenImage.text("Sort", windowWidth - 250, 690);
        } else if (stage == 1) {
            screenImage.rect(900, 670, 260, 40);
            screenImage.fill(0);
            screenImage.text("Fastest creatures at the top!", windowWidth / 2, 30);
            screenImage.text("Slowest creatures at the bottom. (Going backward = slow)", windowWidth / 2 - 200, 700);
            screenImage.text("Kill 500", windowWidth - 250, 700);
        } else if (stage == 2) {
            screenImage.rect(1050, 670, 160, 40);
            screenImage.fill(0);
            screenImage.text(
                    "Faster creatures are more likely to survive because they can outrun their predators.  Slow creatures get eaten.",
                    windowWidth / 2, 30);
            screenImage.text("Because of random chance, a few fast ones get eaten, while a few slow ones survive.",
                    windowWidth / 2 - 130, 700);
            screenImage.text("Reproduce", windowWidth - 150, 700);
            for (int j = 0; j < CREATURE_COUNT; j++) {
                Creature cj = c2.get(j);
                int x = j % 40;
                int y = floor(j / 40) + 1;
                if (cj.isAlive()) {
                    cj.drawCreatureWhole(x * 30 + 55, y * 25 + 40, 0, this);
                } else {
                    screenImage.fill(0);
                    screenImage.rect(x * 30 + 40, y * 25 + 17, 30, 25);
                }
            }
        } else if (stage == 3) {
            screenImage.rect(1050, 670, 160, 40);
            screenImage.fill(0);
            screenImage.text("These are the 1000 creatures of generation #" + (gen + 2) + ".", windowWidth / 2, 30);
            screenImage.text("What perils will they face?  Find out next time!", windowWidth / 2 - 130, 700);
            screenImage.text("Back", windowWidth - 150, 700);
        }
        screenImage.endDraw();
    }

    public void drawpopUpImage() {
        camzoom = 0.015f;
        setAverage();
        cam += (average - cam) * 0.1f;
        popUpImage.beginDraw();
        popUpImage.smooth();
        if (simulationTimer < 900) {
            popUpImage.background(120, 200, 255);
        } else {
            popUpImage.background(60, 100, 128);
        }
        drawPosts(2);
        drawGround(2);
        drawCreature(n, m, -cam / camzoom + 450 / 2, 450 * 0.8f, 2);
        popUpImage.noStroke();
        popUpImage.endDraw();
    }

    public void drawCreature(ArrayList<Node> n, ArrayList<Muscle> m, float x, float y, int toImage) {
        for (int i = 0; i < m.size(); i++) {
            Muscle mi = m.get(i);
            drawMuscle(mi, n.get(mi.c1), n.get(mi.c2), x, y, toImage);
        }
        for (int i = 0; i < n.size(); i++) {
            drawNode(n.get(i), x, y, toImage);
        }
    }

    public void drawHistogram(int x, int y, int hw, int hh) {
        int maxH = 1;
        for (int i = 0; i < barLen; i++) {
            if (barCounts.get(genSelected)[i] > maxH) {
                maxH = barCounts.get(genSelected)[i];
            }
        }
        fill(200);
        noStroke();
        rect(x, y, hw, hh);
        fill(0, 0, 0);
        float barW = (float) hw / barLen;
        float multiplier = (float) hh / maxH * 0.9f;
        textAlign(LEFT);
        textFont(font, 16);
        stroke(128);
        strokeWeight(2);
        int unit = 100;
        if (maxH < 300)
            unit = 50;
        if (maxH < 100)
            unit = 20;
        if (maxH < 50)
            unit = 10;
        for (int i = 0; i < hh / multiplier; i += unit) {
            float theY = y + hh - i * multiplier;
            line(x, theY, x + hw, theY);
            if (i == 0)
                theY -= 5;
            text(i, x + hw + 5, theY + 7);
        }
        textAlign(CENTER);
        for (int i = minBar; i <= maxBar; i++) {
            if (i % 10 == 0) {
                if (i == 0) {
                    stroke(0, 0, 255);
                } else {
                    stroke(128);
                }
                float theX = x + (i - minBar) * barW;
                text(nf((float) i / histBarsPerMeter, 0, 1), theX, y + hh + 14);
                line(theX, y, theX, y + hh);
            }
        }
        noStroke();
        for (int i = 0; i < barLen; i++) {
            float h = min(barCounts.get(genSelected)[i] * multiplier, hh);
            if (i + minBar == floor(percentile.get(min(genSelected, percentile.size() - 1))[14] * histBarsPerMeter)) {
                fill(255, 0, 0);
            } else {
                fill(0, 0, 0);
            }
            rect(x + i * barW, y + hh - h, barW, h);
        }
    }

    public void drawStatusWindow() {
        int x, y, px, py;
        int rank = (statusWindow + 1);
        Creature cj;
        stroke(abs(overallTimer % 30 - 15) * 17);
        strokeWeight(3);
        noFill();
        if (statusWindow >= 0) {
            cj = c2.get(statusWindow);
            if (menu == MENU_7_SHOW_RESULTS) {
                int id = ((cj.id - 1) % CREATURE_COUNT);
                x = id % 40;
                y = floor(id / 40);
            } else {
                x = statusWindow % 40;
                y = floor(statusWindow / 40) + 1;
            }
            px = x * 30 + 55;
            py = y * 25 + 10;
            if (px <= 1140) {
                px += 80;
            } else {
                px -= 80;
            }
            rect(x * 30 + 40, y * 25 + 17, 30, 25);
        } else {
            cj = creatureDatabase.get((genSelected - 1) * 3 + statusWindow + 3);
            x = 760 + (statusWindow + 3) * 160;
            y = 180;
            px = x;
            py = y;
            rect(x, y, 140, 140);
            int[] ranks = { CREATURE_COUNT, 500, 1 };
            rank = ranks[statusWindow + 3];
        }
        noStroke();
        fill(255);
        rect(px - 60, py, 120, 52);
        fill(0);
        textFont(font, 12);
        textAlign(CENTER);
        text("#" + rank, px, py + 12);
        text("ID: " + cj.id, px, py + 24);
        text("Fitness: " + nf(cj.getFitness(), 0, 3), px, py + 36);
        colorMode(HSB, 1);
        int sp = cj.getSpecies();
        fill(getColor(sp, true));
        text("Species: S" + sp, px, py + 48);
        colorMode(RGB, 255);
        if (miniSimulation) {
            int py2 = py - 125;
            if (py >= 360) {
                py2 -= 180;
            } else {
                py2 += 180;
            }
            // py = min(max(py,0),420);
            int px2 = min(max(px - 90, 10), 970);
            drawpopUpImage();
            image(popUpImage, px2, py2, 300, 300);
            fill(255, 255, 255);
            rect(px2 + 240, py2 + 10, 50, 30);
            rect(px2 + 10, py2 + 10, 100, 30);
            fill(0, 0, 0);
            textFont(font, 30);
            textAlign(RIGHT);
            text(PApplet.parseInt(simulationTimer / 60), px2 + 285, py2 + 36);
            textAlign(LEFT);
            text(nf(average / 5.0f, 0, 3), px2 + 15, py2 + 36);
            simulate();
            simulationTimer++;
            timer++;
            int shouldBeWatching = statusWindow;
            if (statusWindow <= -1) {
                cj = creatureDatabase.get((genSelected - 1) * 3 + statusWindow + 3);
                shouldBeWatching = cj.id;
            }
            if (creatureWatching != shouldBeWatching) {
                openMiniSimulation();
            }
        }
    }

    public void setup() {
        size(1280, 720, P2D); // Don't change this. It ruins everything.
        if (USE_RANDOM_SEED) {
            randomSeed(SEED);
        }
        smooth();
        ellipseMode(CENTER);
        Float[] beginPercentile = new Float[29];
        Integer[] beginBar = new Integer[barLen];
        Integer[] beginSpecies = new Integer[101];
        for (int i = 0; i < 29; i++) {
            beginPercentile[i] = 0.0f;
        }
        for (int i = 0; i < barLen; i++) {
            beginBar[i] = 0;
        }
        for (int i = 0; i < 101; i++) {
            beginSpecies[i] = 500;
        }

        percentile.add(beginPercentile);
        barCounts.add(beginBar);
        speciesCounts.add(beginSpecies);
        topSpeciesCounts.add(0);

        graphImage = createGraphics(975, 570, P2D);
        screenImage = createGraphics(1280, 720, P2D);
        popUpImage = createGraphics(450, 450, P2D);
        segBarImage = createGraphics(975, 150, P2D);
        font = loadFont("Helvetica-Bold-96.vlw");
        textFont(font, 96);
        textAlign(CENTER);
        for (int i = 0; i < RECTANGLES.length; i++) {
            rects.add(RECTANGLES[i]);
        }
    }

    public void draw() {
        scale(1);
        if (menu == MENU_0_TITLE_PAGE) {
            background(255);
            fill(100, 200, 100);
            noStroke();
            rect(windowWidth / 2 - 200, 300, 400, 200);
            fill(0);
            text("EVOLUTION!", windowWidth / 2, 200);
            text("START", windowWidth / 2, 430);
        } else if (menu == MENU_1_SHOW_STATS) {
            noStroke();
            fill(0);
            background(255, 200, 130);
            textFont(font, 32);
            textAlign(LEFT);
            textFont(font, 96);
            text("Generation " + max(genSelected, 0), 20, 100);
            textFont(font, 28);
            if (gen == -1) {
                fill(100, 200, 100);
                rect(20, 250, 200, 100);
                fill(0);
                text("Since there are no creatures yet, create 1000 creatures!", 20, 160);
                text("They will be randomly created, and also very simple.", 20, 200);
                text("CREATE", 56, 312);
            } else {
                fill(100, 200, 100);
                rect(760, 20, 460, 40);
                rect(760, 70, 460, 40);
                rect(760, 120, 230, 40);
                if (gensToDo >= 2) {
                    fill(128, 255, 128);
                } else {
                    fill(70, 140, 70);
                }
                rect(990, 120, 230, 40);
                fill(0);
                text("Do 1 step-by-step generation.", 770, 50);
                text("Do 1 quick generation.", 770, 100);
                text("Do 1 gen ASAP.", 770, 150);
                if (gensToDo >= 2) {
                    textFont(font, 15);
                    text("Currently ALAPing.", 996, 136);
                    text("Click & hold anywhere to stop.", 996, 153);
                    textFont(font, 28);
                } else {
                    text("Do gens ALAP.", 1000, 150);
                }
                text("Median Distance", 50, 160);
                textAlign(CENTER);
                textAlign(RIGHT);
                text(PApplet.parseFloat(round(percentile.get(min(genSelected, percentile.size() - 1))[14] * 1000))
                        / 1000 + " m", 700, 160);
                drawHistogram(760, 410, 460, 280);
                drawGraphImage();
            }
            if (gensToDo >= 1) {
                gensToDo--;
                if (gensToDo >= 1) {
                    startASAP();
                }
            }
        } else if (menu == MENU_2_CREATE_INITIAL_POPULATION) {
            camzoom = 0.12f;
            background(220, 253, 102);
            // Create 25 x 40 = 1000 creatures
            for (int y = 0; y < 25; y++) {
                for (int x = 0; x < 40; x++) {

                    int id = y * 40 + x;

                    Creature createdCreature = Creature.createRandomCreature(id + 1, (a, b) -> random(a, b));

                    c[id] = createdCreature;

                    // Draw it.
                    c[id].drawCreatureWhole(x * 30 + 55, y * 25 + 30, 0, this);
                }
            }
            EvolutionSimulator es = new EvolutionSimulator(Arrays.stream(c), rects.stream());
            es.run();
            
            setMenu(MENU_3_RESET_GEN);
            noStroke();
            fill(100, 100, 200);
            rect(900, 664, 260, 40);
            fill(0);
            textAlign(CENTER);
            textFont(font, 24);
            text("Here are your 1000 randomly generated creatures!!!", windowWidth / 2 - 200, 690);
            text("Back", windowWidth - 250, 690);
        } else if (menu == MENU_4_SELECT_OR_SIMULATE) {
            setGlobalVariables(c[creaturesTested]);
            camzoom = 0.01f;
            setMenu(MENU_5_SIMULATE_SINGLE_RUNNING);
            if (stepbystepslow) {
                if (creaturesTested <= 4) {
                    speed = max(creaturesTested, 1);
                } else {
                    speed = min(creaturesTested * 3 - 9, 1000);
                }
            } else {
                ParallelSimulation.simulateFitness(c, rects);
                setMenu(MENU_6_SORT_UPDATE_STATS);
            }
        }
        if (menu == MENU_5_SIMULATE_SINGLE_RUNNING) { // simulate running
            if (timer <= 900) {
                textAlign(CENTER);
                textFont(font, 0.96f / camzoom);
                background(120, 200, 255);
                for (int s = 0; s < speed; s++) {
                    if (timer < 900) {
                        simulate();
                        simulationTimer++;
                        timer++;
                    }
                }
                setAverage();
                if (speed < 30) {
                    for (int s = 0; s < speed; s++) {
                        cam += (average - cam) * 0.03f;
                    }
                } else {
                    cam = average;
                }
                drawPosts(0);
                drawGround(0);
                drawCreature(n, m, -cam / camzoom + windowWidth / 2, windowHeight * 0.8f, 0);
                drawArrow(average);
                textAlign(RIGHT);
                textFont(font, 32);
                fill(0);
                text("Creature ID: " + id, windowWidth - 10, 32);
                if (speed > 60) {
                    timeShow = PApplet.parseInt((timer + creaturesTested * 37) / 60) % 15;
                } else {
                    timeShow = (timer / 60);
                }
                timeShow = round(timeShow);
                text("Time: " + timeShow + " / 15 sec.", windowWidth - 10, 64);
                text("Playback Speed: x" + speed, windowWidth - 10, 96);
            }
            if (timer == 900) {
                if (speed < 30) {
                    noStroke();
                    fill(0, 0, 0, 130);
                    rect(0, 0, windowWidth, windowHeight);
                    fill(0, 0, 0, 255);
                    rect(windowWidth / 2 - 500, 200, 1000, 240);
                    fill(255, 0, 0);
                    textAlign(CENTER);
                    textFont(font, 96);
                    text("Creature's Distance:", windowWidth / 2, 300);
                    text(PApplet.parseFloat(round(average * 200)) / 1000 + " m", windowWidth / 2, 400);
                } else {
                    timer = 1020;
                }
                c[creaturesTested].setFitness(average * 0.2f);
            }
            if (timer >= 1020) {
                setMenu(MENU_4_SELECT_OR_SIMULATE);
                creaturesTested++;
                if (creaturesTested == CREATURE_COUNT) {
                    setMenu(MENU_6_SORT_UPDATE_STATS);
                }
                cam = 0;
            }
            if (timer >= 900) {
                timer += speed;
            }
        }
        if (menu == MENU_6_SORT_UPDATE_STATS) {
            // sort
            c2 = new ArrayList<Creature>(CREATURE_COUNT);
            for (Creature ci : c) {
                c2.add(ci);
            }
            c2 = quickSort(c2);
            percentile.add(new Float[29]);
            for (int i = 0; i < 29; i++) {
                percentile.get(gen + 1)[i] = c2.get(p[i]).getFitness();
            }
            creatureDatabase.add(c2.get(CREATURE_COUNT - 1).copyCreature(-1));
            creatureDatabase.add(c2.get(CREATURE_COUNT / 2 - 1).copyCreature(-1));
            creatureDatabase.add(c2.get(0).copyCreature(-1));

            Integer[] beginBar = new Integer[barLen];
            for (int i = 0; i < barLen; i++) {
                beginBar[i] = 0;
            }
            barCounts.add(beginBar);
            Integer[] beginSpecies = new Integer[101];
            for (int i = 0; i < 101; i++) {
                beginSpecies[i] = 0;
            }
            for (int i = 0; i < CREATURE_COUNT; i++) {
                int bar = floor(c2.get(i).getFitness() * histBarsPerMeter - minBar);
                if (bar >= 0 && bar < barLen) {
                    barCounts.get(gen + 1)[bar]++;
                }
                int species = c2.get(i).getSpecies();
                beginSpecies[species]++;
            }
            speciesCounts.add(new Integer[101]);
            speciesCounts.get(gen + 1)[0] = 0;
            int cum = 0;
            int record = 0;
            int holder = 0;
            for (int i = 0; i < 100; i++) {
                cum += beginSpecies[i];
                speciesCounts.get(gen + 1)[i + 1] = cum;
                if (beginSpecies[i] > record) {
                    record = beginSpecies[i];
                    holder = i;
                }
            }
            topSpeciesCounts.add(holder);
            if (stepbystep) {
                drawScreenImage(0);
                setMenu(MENU_7_SHOW_RESULTS);
            } else {
                setMenu(MENU_10_KILL_CREATURES);
            }
        }
        if (menu == MENU_8_SHOW_SORTING) {
            // cool sorting animation
            camzoom = 0.12f;
            background(220, 253, 102);
            float transition = 0.5f - 0.5f * cos(min(PApplet.parseFloat(timer) / 60, PI));
            for (int j = 0; j < CREATURE_COUNT; j++) {
                Creature cj = c2.get(j);
                int j2 = cj.id - (gen * CREATURE_COUNT) - 1;
                int x1 = j2 % 40;
                int y1 = floor(j2 / 40);
                int x2 = j % 40;
                int y2 = floor(j / 40) + 1;
                float x3 = inter(x1, x2, transition);
                float y3 = inter(y1, y2, transition);
                cj.drawCreatureWhole(x3 * 30 + 55, y3 * 25 + 40, 0, this);
            }
            if (stepbystepslow) {
                timer += 1 * SORT_ANIMATION_SPEED;
            } else {
                timer += 3 * SORT_ANIMATION_SPEED;
            }
            if (timer > 60 * PI) {
                drawScreenImage(1);
                setMenu(MENU_9_SHOW_SORTED_CREATURES);
            }
        }
        float mX = mouseX / WINDOW_SIZE;
        float mY = mouseY / WINDOW_SIZE;
        if ((menu == MENU_7_SHOW_RESULTS || menu == MENU_8_SHOW_SORTING || menu == MENU_9_SHOW_SORTED_CREATURES
                || menu == MENU_10_KILL_CREATURES || menu == MENU_11_SHOW_DEAD) && gensToDo == 0 && !drag) {
            if (abs(mX - 639.5f) <= 599.5f) {
                if (menu == MENU_7_SHOW_RESULTS && abs(mY - 329) <= 312) {
                    statusWindow = creaturesInPosition[floor((mX - 40) / 30) + floor((mY - 17) / 25) * 40];
                } else if ((menu == MENU_9_SHOW_SORTED_CREATURES || menu == MENU_10_KILL_CREATURES
                        || menu == MENU_11_SHOW_DEAD || menu == MENU_12_REPRODUCE || menu == MENU_13_SHOW_NEW_GENERATION) && abs(mY - 354) <= 312) {
                    statusWindow = floor((mX - 40) / 30) + floor((mY - 42) / 25) * 40;
                } else {
                    statusWindow = -4;
                }
            } else {
                statusWindow = -4;
            }
        } else if (menu == MENU_1_SHOW_STATS && genSelected >= 1 && gensToDo == 0 && !drag) {
            statusWindow = -4;
            if (abs(mY - 250) <= 70) {
                if (abs(mX - 990) <= 230) {
                    float modX = (mX - 760) % 160;
                    if (modX < 140) {
                        statusWindow = floor((mX - 760) / 160) - 3;
                    }
                }
            }
        } else {
            statusWindow = -4;
        }
        if (menu == MENU_10_KILL_CREATURES) {
            // Kill!
            for (int j = 0; j < 500; j++) {
                float f = PApplet.parseFloat(j) / CREATURE_COUNT;
                float rand = (pow(random(-1, 1), 3) + 1) / 2; // cube function
                slowDies = (f <= rand);
                int j3;
                if (slowDies) {
                    j3 = 999 - j;
                } else {
                    j3 = j;
                }
                Creature ck = c2.get(j3);
                ck.die();
            }
            if (stepbystep) {
                drawScreenImage(2);
                setMenu(MENU_11_SHOW_DEAD);
            } else {
                setMenu(MENU_12_REPRODUCE);
            }
        }
        if (menu == MENU_12_REPRODUCE) { // Reproduce and mutate
            justGotBack = true;
            for (int j = 0; j < 500; j++) {
                // Creatures are stored in c2 in pairs (x, 999-x). If x is dead, then 999-x is alive and vice versa.
                // The dead creature will be replaced by a modified version of its alive counterpart.
                int liveIndex, deadIndex;
                if (!c2.get(j).isAlive()) { // j is dead
                    liveIndex = CREATURE_COUNT - 1 - j;
                    deadIndex = j;
                } else {
                    liveIndex = j;
                    deadIndex = CREATURE_COUNT - 1 - j;
                }
                Creature liveCreature = c2.get(liveIndex);
                int deadCreatureId = c2.get(deadIndex).id;
                Creature offspring = liveCreature.modified(deadCreatureId + CREATURE_COUNT, () -> r(),
                        (x, y) -> random(x, y));
                c2.set(deadIndex, offspring); // mutated
                c2.set(liveIndex, liveCreature.copyCreature(liveCreature.id + CREATURE_COUNT)); // duplicate
            }
            for (int j = 0; j < CREATURE_COUNT; j++) {
                Creature cj = c2.get(j);
                c[cj.id - (gen * CREATURE_COUNT) - 1001] = cj.copyCreature(-1);
            }
            if (stepbystep) {
                drawScreenImage(3);
            }
            gen++;
            final float median = PApplet
                    .parseFloat(round(percentile.get(min(genSelected, percentile.size() - 1))[14] * 1000)) / 1000;
            PerfRecorder.instance().setLabel("Gen " + gen + ", median: " + median + "m took: ");
            PerfRecorder.instance().recordIteration();
            if (stepbystep) {
                setMenu(MENU_13_SHOW_NEW_GENERATION);
            } else {
                setMenu(MENU_1_SHOW_STATS);
            }
        }
        if ( // menu % 2 == 1 && abs(menu - 10) <= 3
        menu == MENU_13_SHOW_NEW_GENERATION || menu == MENU_11_SHOW_DEAD | menu == MENU_9_SHOW_SORTED_CREATURES
                || menu == MENU_7_SHOW_RESULTS) {
            image(screenImage, 0, 0, windowWidth, windowHeight);
        }
        if (menu == MENU_1_SHOW_STATS || gensToDo >= 1) {
            mX = mouseX / WINDOW_SIZE;
            mY = mouseY / WINDOW_SIZE;
            noStroke();
            if (gen >= 1) {
                textAlign(CENTER);
                if (gen >= 5) {
                    genSelected = round((sliderX - 760) * (gen - 1) / 410) + 1;
                } else {
                    genSelected = round((sliderX - 760) * gen / 410);
                }
                if (drag)
                    sliderX = min(max(sliderX + (mX - 25 - sliderX) * 0.2f, 760), 1170);
                fill(100);
                rect(760, 340, 460, 50);
                fill(220);
                rect(sliderX, 340, 50, 50);
                int fs = 0;
                if (genSelected >= 1) {
                    fs = floor(log(genSelected) / log(10));
                }
                fontSize = fontSizes[fs];
                textFont(font, fontSize);
                fill(0);
                text(genSelected, sliderX + 25, 366 + fontSize * 0.3333f);
            }
            if (genSelected >= 1) {
                textAlign(CENTER);
                camzoom = 0.028f;
                for (int k = 0; k < 3; k++) {
                    fill(220);
                    rect(760 + k * 160, 180, 140, 140);
                    creatureDatabase.get((genSelected - 1) * 3 + k).drawCreatureWhole(830 + 160 * k, 290, 0, this);
                }
                fill(0);
                textFont(font, 16);
                text("Worst Creature", 830, 310);
                text("Median Creature", 990, 310);
                text("Best Creature", 1150, 310);
            }
            if (justGotBack)
                justGotBack = false;
        }
        if (statusWindow >= -3) {
            drawStatusWindow();
            if (statusWindow >= -3 && !miniSimulation) {
                openMiniSimulation();
            }
        }
        overallTimer++;
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "evolution.Evolution3WEB" };
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}

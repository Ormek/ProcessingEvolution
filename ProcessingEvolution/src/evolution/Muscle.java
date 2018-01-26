package evolution;

import java.util.List;

import de.my.function.FloatBinaryOperation;
import de.my.function.FloatSupplier;
import processing.core.PApplet;

public class Muscle {
    int period;
    // These are the indizes into the nodes array outside of this class :(
    int c1;
    int c2;
    float contractTime, contractLength, extendTime, extendLength;
    float thruPeriod;
    boolean contracted;
    float rigidity;

    Muscle(int tperiod, int tc1, int tc2, float tcontractTime, float textendTime, float tcontractLength,
            float textendLength, boolean tcontracted, float trigidity) {
        period = tperiod;
        c1 = tc1;
        c2 = tc2;
        contractTime = tcontractTime;
        extendTime = textendTime;
        contractLength = tcontractLength;
        extendLength = textendLength;
        contracted = tcontracted;
        rigidity = trigidity;
    }

    float calculateTargetLength(int timer2, float cTimer2) {
        float target;
        this.thruPeriod = ((PApplet.parseFloat(timer2) / cTimer2) / PApplet.parseFloat(this.period))
                % PApplet.parseFloat(1);
        if ((this.thruPeriod <= this.extendTime && this.extendTime <= this.contractTime)
                || (this.contractTime <= this.thruPeriod && this.thruPeriod <= this.extendTime)
                || (this.extendTime <= this.contractTime && this.contractTime <= this.thruPeriod)) {
            target = this.contractLength;
            this.contracted = true;
        } else {
            target = this.extendLength;
            this.contracted = false;
        }
        return target;
    }


    /**
     * Muscles tries to get to target length and applies its force accelerating the connected nodes as a side effect
     * 
     * @param target
     *            Length the muscle tries to achieve
     */
    public void applyForce(List<Node> n, float target) {
        Node ni1 = n.get(c1);
        Node ni2 = n.get(c2);
        float distance = Evolution3WEB.dist(ni1.x, ni1.y, ni2.x, ni2.y);
        float angle = Evolution3WEB.atan2(ni1.y - ni2.y, ni1.x - ni2.x);
        // The value -0.4 looks suspicious. Maybe it is the now constant mass/size of any node?
        // Yet, it seems to be an upper and lower bound on the force. Still...
        float force = Evolution3WEB.min(Evolution3WEB.max(1 - (distance / target), -0.4f), 0.4f);
        ni1.vx += Evolution3WEB.cos(angle) * force * rigidity / ni1.m; // This .m is 0.4 always!
        ni1.vy += Evolution3WEB.sin(angle) * force * rigidity / ni1.m;
        ni2.vx -= Evolution3WEB.cos(angle) * force * rigidity / ni2.m;
        ni2.vy -= Evolution3WEB.sin(angle) * force * rigidity / ni2.m;
    }

    public Muscle copyMuscle() {
        return new Muscle(period, c1, c2, contractTime, extendTime, contractLength, extendLength, contracted,
                rigidity);
    }

    public Muscle modifyMuscle(int nodeNum, float mutability, FloatSupplier r, FloatBinaryOperation random) {
        int newc1 = c1;
        int newc2 = c2;
        if (random.applyAsFloat(0, 1) < 0.02f * mutability * Evolution3WEB.MUTABILITY_FACTOR) {
            newc1 = PApplet.parseInt(random.applyAsFloat(0, nodeNum));
        }
        if (random.applyAsFloat(0, 1) < 0.02f * mutability * Evolution3WEB.MUTABILITY_FACTOR) {
            newc2 = PApplet.parseInt(random.applyAsFloat(0, nodeNum));
        }
        float newR = Evolution3WEB.min(Evolution3WEB.max(rigidity * (1 + r.getAsFloat() * 0.9f * mutability * Evolution3WEB.MUTABILITY_FACTOR), 0.01f), 0.08f);
        float maxMuscleChange = 1 + 0.025f / newR;
        float newCL = Evolution3WEB.min(Evolution3WEB.max(contractLength + r.getAsFloat() * mutability * Evolution3WEB.MUTABILITY_FACTOR, 0.4f), 2);
        float newEL = Evolution3WEB.min(Evolution3WEB.max(extendLength + r.getAsFloat() * mutability * Evolution3WEB.MUTABILITY_FACTOR, 0.4f), 2);
        float newCL2 = Evolution3WEB.min(newCL, newEL);
        float newEL2 = Evolution3WEB.min(Evolution3WEB.max(newCL, newEL), newCL2 * maxMuscleChange);
        float newCT = contractTime;
        float newET = extendTime;
        if (random.applyAsFloat(0, 1) < 0.5f) { // contractTime is changed
            newCT = ((contractTime - extendTime) * r.getAsFloat() * mutability * Evolution3WEB.MUTABILITY_FACTOR + newCT + 1) % 1;
        } else { // extendTime is changed
            newET = ((extendTime - contractTime) * r.getAsFloat() * mutability * Evolution3WEB.MUTABILITY_FACTOR + newET + 1) % 1;
        }
        return new Muscle(Evolution3WEB.max(period + PApplet.parseInt(random.applyAsFloat(-0.01f, 1.01f)), 0), newc1, newc2, newCT, newET, newCL2, newEL2,
                Evolution3WEB.isItContracted(newCT, newET), newR);
    }
}
package de.my;

import java.util.Random;

public class Tryit {

    static Random r = new Random();

    public static void main(String[] args) {

        float x = 2.5f;
        float y = 0.4f;
        double q = x / y;
        System.out.println((float) Math.atan(q) + " ?= " + (float) Math.atan2(x, y));
        x = 2.5f;
        y = -0.4f;
        q = x / y;
        System.out.println((float) (Math.atan(q) + Math.PI) + " ?= " + (float) Math.atan2(x, y));
        x = -2.5f;
        y = -0.4f;
        q = x / y;
        System.out.println((float) (Math.atan(q) - Math.PI) + " ?= " + (float) Math.atan2(x, y));
        x = -2.5f;
        y = 0.4f;
        q = x / y;
        System.out.println((float) Math.atan(q) + " ?= " + (float) Math.atan2(x, y));

        for (int i = 0; i < 1000; i++) {
            float x1 = r.nextFloat();
            float y1 = r.nextFloat();
            float soll = (float) Math.atan2(x1, y1);
            float ist = atanInsteadOfAtan2(x1, y1);

            if (soll != ist) {
                System.out.println(
                        String.format("for %f and %f is %f != %f, difference: %e", x1, y1, soll, ist, ist - soll));
            }
        }

        x = 1;
        y = 0;
        float soll = (float) Math.atan2(x, y);
        float ist = atanInsteadOfAtan2(x, y);
        System.out.println(String.format("for %f and %f is %f ~ %f, difference: %e", x, y, soll, ist, ist - soll));

        x = -1;
        y = 0;
        soll = (float) Math.atan2(x, y);
        ist = atanInsteadOfAtan2(x, y);
        System.out.println(String.format("for %f and %f is %f ~ %f, difference: %e", x, y, soll, ist, ist - soll));

        x = 0;
        y = 1;
        soll = (float) Math.atan2(x, y);
        ist = atanInsteadOfAtan2(x, y);
        System.out.println(String.format("for %f and %f is %f ~ %f, difference: %e", x, y, soll, ist, ist - soll));

        x = 0;
        y = 0;
        soll = (float) Math.atan2(x, y);
        ist = atanInsteadOfAtan2(x, y);
        System.out.println(String.format("for %f and %f is %f ~ %f, difference: %e", x, y, soll, ist, ist - soll));

        x = 0;
        y = -1;
        soll = (float) Math.atan2(x, y);
        ist = atanInsteadOfAtan2(x, y);
        System.out.println(String.format("for %f and %f is %f ~ %f, difference: %e", x, y, soll, ist, ist - soll));
    }

    static float atanInsteadOfAtan2(double x, double y) {
        if (y == 0) {
            if (x > 0) {
                return (float) (Math.PI / 2.0);
            } else if (x < 0) {
                return (float) (-Math.PI / 2.0);
            } else { // x=y=0
                return 0;
            }
        } else if (y > 0) {
            return (float) Math.atan(x / y);
        } else if (x < 0) {
            return (float) (Math.atan(x / y) - Math.PI);
        } else {
            return (float) (Math.atan(x / y) + Math.PI);
        }
    }
}

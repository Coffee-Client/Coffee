/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. All rights reserved.
 */

package coffee.client.helper.util;

public class Transitions {

    /**
     * @param value The current value
     * @param goal  The value to transition to
     * @param speed The speed of the operation (BIGGER = SLOWER!)
     *
     * @return The new value
     */
    public static double transition(double value, double goal, double speed) {
        return transition(value, goal, speed, 0.02);
    }

    public static double transition(double value, double goal, double speed, double skipSize) {
        double speed1 = speed < 1 ? 1 : speed;
        double diff = goal - value;
        double diffCalc = diff / speed1;
        if (Math.abs(diffCalc) < skipSize) {
            diffCalc = diff;
        }
        return value + diffCalc;
    }

    public static double easeOutExpo(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;

    }
}

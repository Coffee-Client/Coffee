/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors. Some rights reserved.
 */

package coffee.client.helper.render;

public class Scroller {
    double scroll;
    double velocity;
    double minBound = -1;
    double maxBound = -1;

    public Scroller(double initialScroll) {
        this.scroll = initialScroll;
        this.velocity = 0;
    }

    public void setBounds(double min, double max) {
        this.minBound = min;
        this.maxBound = max;
    }

    public void tick() {
        scroll -= velocity;
        if (maxBound != -1) {
            scroll = Math.min(maxBound, scroll);
        }
        if (minBound != -1) {
            scroll = Math.max(minBound, scroll);
        }
        velocity /= 1.1;
    }

    public double getScroll() {
        return -scroll;
    }

    public void setScroll(double scroll) {
        this.scroll = scroll;
    }

    public void scroll(double howMuch) {
        this.velocity += howMuch;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }
}

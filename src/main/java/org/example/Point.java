package org.example;

import java.util.Date;

public class Point {
    double x;
    double y;
    double r;
    boolean isHit;
    Date startTime;

    public Point(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.isHit = isHit(x, y, r);
        this.startTime = new Date();
    }

    private boolean isHit(double x, double y, double r) {
        if (x >= 0 && y >= 0 && x * x + y * y <= (r / 2) * (r / 2)) return true;

        if (x >= 0 && y <= 0 && 2 * x - r <= y) return true;

        if (x <= 0 && y <= 0 && x >= -r && y >= -r / 2) return true;

        return false;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public boolean isHit() {
        return isHit;
    }

    public void setHit(boolean hit) {
        isHit = hit;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
}

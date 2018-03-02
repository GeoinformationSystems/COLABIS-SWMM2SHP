package de.dresden.tu.swmm;

public class Junction {

    private String name;
    private double elevation;
    private double maxDepth;
    private double initDepth;
    private double surDepth;
    private boolean aponded;

    public Junction(String name, double elevation, double maxDepth, double initDepth, double surDepth, boolean aponded) {
        this.name = name;
        this.elevation = elevation;
        this.maxDepth = maxDepth;
        this.initDepth = initDepth;
        this.surDepth = surDepth;
        this.aponded = aponded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(double maxDepth) {
        this.maxDepth = maxDepth;
    }

    public double getInitDepth() {
        return initDepth;
    }

    public void setInitDepth(double initDepth) {
        this.initDepth = initDepth;
    }

    public double getSurDepth() {
        return surDepth;
    }

    public void setSurDepth(double surDepth) {
        this.surDepth = surDepth;
    }

    public boolean isAponded() {
        return aponded;
    }

    public void setAponded(boolean aponded) {
        this.aponded = aponded;
    }
}

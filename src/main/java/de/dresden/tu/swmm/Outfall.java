package de.dresden.tu.swmm;

public class Outfall {

    private String name;
    private double elevation;
    private String type;
    private String stageData;
    private boolean gated;
    private String routeTo;

    public Outfall(String name, double elevation, String type, String stageData, boolean gated, String routeTo) {
        this.name = name;
        this.elevation = elevation;
        this.type = type;
        this.stageData = stageData;
        this.gated = gated;
        this.routeTo = routeTo;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStageData() {
        return stageData;
    }

    public void setStageData(String stageData) {
        this.stageData = stageData;
    }

    public boolean isGated() {
        return gated;
    }

    public void setGated(boolean gated) {
        this.gated = gated;
    }

    public String getRouteTo() {
        return routeTo;
    }

    public void setRouteTo(String routeTo) {
        this.routeTo = routeTo;
    }
}

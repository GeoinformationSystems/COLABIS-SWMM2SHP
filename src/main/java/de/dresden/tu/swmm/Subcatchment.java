package de.dresden.tu.swmm;

public class Subcatchment {

    private String name;
    private String rain_gage;
    private String outlet;
    private double area;
    private double percentImperv;
    private double width;
    private double percentSlope;
    private double curbLen;

    public Subcatchment(String name, String rain_gage, String outlet, double area, double percentImperv, double width, double percentSlope, double curbLen) {
        this.name = name;
        this.rain_gage = rain_gage;
        this.outlet = outlet;
        this.area = area;
        this.percentImperv = percentImperv;
        this.width = width;
        this.percentSlope = percentSlope;
        this.curbLen = curbLen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRain_gage() {
        return rain_gage;
    }

    public void setRain_gage(String rain_gage) {
        this.rain_gage = rain_gage;
    }

    public String getOutlet() {
        return outlet;
    }

    public void setOutlet(String outlet) {
        this.outlet = outlet;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getPercentImperv() {
        return percentImperv;
    }

    public void setPercentImperv(double percentImperv) {
        this.percentImperv = percentImperv;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getPercentSlope() {
        return percentSlope;
    }

    public void setPercentSlope(double percentSlope) {
        this.percentSlope = percentSlope;
    }

    public double getCurbLen() {
        return curbLen;
    }

    public void setCurbLen(double curbLen) {
        this.curbLen = curbLen;
    }
}

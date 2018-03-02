package de.dresden.tu.swmm;

public class Conduit {

    private String name;
    private String fromNode;
    private String toNode;
    private double length;
    private double roughness;
    private double inOffset;
    private double outOffset;
    private double initFlow;
    private double maxFlow;

    private String shape;
    private double geom1;
    private double geom2;
    private double geom3;
    private double geom4;



    public Conduit(String name, String fromNode, String toNode, double length, double roughness, double inOffset, double outOffset, double initFlow, double maxFlow) {
        this.name = name;
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.length = length;
        this.roughness = roughness;
        this.inOffset = inOffset;
        this.outOffset = outOffset;
        this.initFlow = initFlow;
        this.maxFlow = maxFlow;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFromNode() {
        return fromNode;
    }

    public void setFromNode(String fromNode) {
        this.fromNode = fromNode;
    }

    public String getToNode() {
        return toNode;
    }

    public void setToNode(String toNode) {
        this.toNode = toNode;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getRoughness() {
        return roughness;
    }

    public void setRoughness(double roughness) {
        this.roughness = roughness;
    }

    public double getInOffset() {
        return inOffset;
    }

    public void setInOffset(double inOffset) {
        this.inOffset = inOffset;
    }

    public double getOutOffset() {
        return outOffset;
    }

    public void setOutOffset(double outOffset) {
        this.outOffset = outOffset;
    }

    public double getInitFlow() {
        return initFlow;
    }

    public void setInitFlow(double initFlow) {
        this.initFlow = initFlow;
    }

    public double getMaxFlow() {
        return maxFlow;
    }

    public void setMaxFlow(double maxFlow) {
        this.maxFlow = maxFlow;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public double getGeom1() {
        return geom1;
    }

    public void setGeom1(double geom1) {
        this.geom1 = geom1;
    }

    public double getGeom2() {
        return geom2;
    }

    public void setGeom2(double geom2) {
        this.geom2 = geom2;
    }

    public double getGeom3() {
        return geom3;
    }

    public void setGeom3(double geom3) {
        this.geom3 = geom3;
    }

    public double getGeom4() {
        return geom4;
    }

    public void setGeom4(double geom4) {
        this.geom4 = geom4;
    }
}

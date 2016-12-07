package translator.utils;

public class JoinPointResult {
    
    private Point left;
    private Point center;
    private Point right;
    private String nodeId;
    
    public JoinPointResult(Point right, Point center, Point left, String nodeId) {
        this.left = new Point(left);
        this.center = new Point(center);
        this.right = new Point(right);
        this.nodeId = nodeId;
    }

    public Point getLeft() {
        return left;
    }

    public void setLeft(Point left) {
        this.left = left;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public Point getRight() {
        return right;
    }

    public void setRight(Point right) {
        this.right = right;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
}

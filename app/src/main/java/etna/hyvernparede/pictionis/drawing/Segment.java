package etna.hyvernparede.pictionis.drawing;

import java.util.ArrayList;
import java.util.List;

public class Segment {

    private String id;
    private List<Point> points;
    private int color;
    private float size;

    public Segment() {

    }

    public Segment(int color, float size) {
        this.color = color;
        this.size = size;
        this.points = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void addPoint(int x, int y) {
        Point newPoint = new Point(x, y);
        points.add(newPoint);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }
}

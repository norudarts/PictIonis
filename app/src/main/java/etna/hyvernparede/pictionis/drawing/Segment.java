package etna.hyvernparede.pictionis.drawing;

import java.util.ArrayList;
import java.util.List;

public class Segment {

    private String id;
    private List<Point> points;
    private int color;

    public Segment() {
        this.points = new ArrayList<Point>();
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
}

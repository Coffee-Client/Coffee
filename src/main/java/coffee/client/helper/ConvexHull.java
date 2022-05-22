package coffee.client.helper;

import java.util.ArrayList;
import java.util.List;

public class ConvexHull {
    public static List<Point> convexHull(List<Point> p1) {
        List<Point> p = new ArrayList<>(p1);
        if (p.isEmpty()) return p;
        p.sort(Point::compareTo);
        List<Point> h = new ArrayList<>();

        // lower hull
        for (Point pt : p) {
            while (h.size() >= 2 && !doesCounterClockwiseRotation(h.get(h.size() - 2), h.get(h.size() - 1), pt)) {
                h.remove(h.size() - 1);
            }
            h.add(pt);
        }

        // upper hull
        int t = h.size() + 1;
        for (int i = p.size() - 1; i >= 0; i--) {
            Point pt = p.get(i);
            while (h.size() >= t && !doesCounterClockwiseRotation(h.get(h.size() - 2), h.get(h.size() - 1), pt)) {
                h.remove(h.size() - 1);
            }
            h.add(pt);
        }

        h.remove(h.size() - 1);
        return h;
    }

    private static boolean doesCounterClockwiseRotation(Point a, Point b, Point c) {
        return ((b.x - a.x) * (c.y - a.y)) > ((b.y - a.y) * (c.x - a.x));
    }

    public record Point(double x, double y) implements Comparable<Point> {
        @Override
        public int compareTo(Point o) {
            return Double.compare(x, o.x);
        }

        @Override
        public String toString() {
            return String.format("(%s, %s)", x, y);
        }
    }
}

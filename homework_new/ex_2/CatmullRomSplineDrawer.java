import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

public class CatmullRomSplineDrawer {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void setPixel(Mat img, int x, int y, int r, int g, int b) {
        if (x >= 0 && x < img.cols() && y >= 0 && y < img.rows()) {
            img.put(y, x, new byte[]{(byte) b, (byte) g, (byte) r});
        }
    }

    public static void line(Mat img, int x1, int y1, int x2, int y2, int color) {
        int x = x1, y = y1;
        int dx = x2 - x1, dy = y2 - y1;
        int e, i;

        int ix = (dx > 0) ? 1 : (dx < 0) ? -1 : 0;
        if (ix == -1) dx = -dx;

        int iy = (dy > 0) ? 1 : (dy < 0) ? -1 : 0;
        if (iy == -1) dy = -dy;

        int r = color & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 16) & 0xFF;

        if (dx >= dy) {
            e = 2 * dy - dx;
            for (i = 0; i <= dx; i++) {
                setPixel(img, x, y, r, g, b);
                if (e >= (iy >= 0 ? 0 : 1)) {
                    y += iy;
                    e -= 2 * dx;
                }
                x += ix;
                e += dy * 2;
            }
        } else {
            e = 2 * dx - dy;
            for (i = 0; i <= dy; i++) {
                setPixel(img, x, y, r, g, b);
                if (e >= (ix >= 0 ? 0 : 1)) {
                    x += ix;
                    e -= 2 * dy;
                }
                y += iy;
                e += dx * 2;
            }
        }
    }

    public static Point calculateCatmullRomPoint(Point P0, Point P1, Point P2, Point P3, double t) {
        return new Point(
                0.5 * (-t * Math.pow(1 - t, 2) * P0.x +
                        (2 - 5 * Math.pow(t, 2) + 3 * Math.pow(t, 3)) * P1.x +
                        t * (1 + 4 * t - 3 * Math.pow(t, 2)) * P2.x -
                        Math.pow(t, 2) * (1 - t) * P3.x),
                0.5 * (-t * Math.pow(1 - t, 2) * P0.y +
                        (2 - 5 * Math.pow(t, 2) + 3 * Math.pow(t, 3)) * P1.y +
                        t * (1 + 4 * t - 3 * Math.pow(t, 2)) * P2.y -
                        Math.pow(t, 2) * (1 - t) * P3.y)
        );
    }

    public static double calculateDistance(Point P0, Point P1) {
        return Math.abs(P0.x - P1.x) + Math.abs(P0.y - P1.y);
    }

    public static double calculateMaxDistance(Point P0, Point P1, Point P2, Point P3) {
        double d1 = calculateDistance(new Point(P0.x - 2 * P1.x + P2.x, P0.y - 2 * P1.y + P2.y), new Point(0, 0));
        double d2 = calculateDistance(new Point(P1.x - 2 * P2.x + P3.x, P1.y - 2 * P2.y + P3.y), new Point(0, 0));
        return Math.max(d1, d2);
    }

    public static void drawPoint(Mat img, Point p, byte[] bgr, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    setPixel(img, (int) p.x + dx, (int) p.y + dy, bgr[2] & 0xFF, bgr[1] & 0xFF, bgr[0] & 0xFF);
                }
            }
        }
    }

    public static void catmullRom(Mat img, Point P0, Point P1, Point P2, Point P3) {
        double H = calculateMaxDistance(P0, P1, P2, P3);
        int N = 1 + (int) Math.sqrt(3 * H);

        Point prevPoint = calculateCatmullRomPoint(P0, P1, P2, P3, 0.0);
        for (int i = 1; i <= N; ++i) {
            double t = (double) i / N;
            Point point = calculateCatmullRomPoint(P0, P1, P2, P3, t);
            line(img, (int) prevPoint.x, (int) prevPoint.y, (int) point.x, (int) point.y, 0x000000);
            prevPoint = point;
        }

        for (Point point : new Point[]{P0, P1, P2, P3}) {
            drawPoint(img, point, new byte[]{0x00, 0x00, (byte) 0xFF}, 5); // Красная точка с радиусом 5
        }
    }

    public static void compositeCatmullRom(Mat img, List<Point> controlPoints) {
        int n = controlPoints.size();

        for (int i = 0; i < n - 3; ++i) {
            Point P0 = controlPoints.get(i);
            Point P1 = controlPoints.get(i + 1);
            Point P2 = controlPoints.get(i + 2);
            Point P3 = controlPoints.get(i + 3);

            catmullRom(img, P0, P1, P2, P3);
        }
    }

    public static void main(String[] args) {
        Mat img = new Mat(1000, 1000, CvType.CV_8UC3, new Scalar(255, 255, 255));

        List<Point> controlPoints = new ArrayList<>();
        controlPoints.add(new Point(200, 400));
        controlPoints.add(new Point(350, 120));
        controlPoints.add(new Point(200, 100));
        controlPoints.add(new Point(550, 400));
        controlPoints.add(new Point(700, 750));
        controlPoints.add(new Point(800, 110));
        controlPoints.add(new Point(900, 50));
        controlPoints.add(new Point(900, 600));
        controlPoints.add(new Point(950, 340));
        controlPoints.add(new Point(1000, 180));

        compositeCatmullRom(img, controlPoints);
        Imgcodecs.imwrite("result_ex2/output.png", img);
    }
}

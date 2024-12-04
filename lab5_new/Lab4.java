import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Lab4 extends Lab3_star{
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    static String savePath = "results/";

    public static void main(String[] args) {
        // задаем контрольные точки
        Coordinate[] curve1 = {new Coordinate(100, 100), new Coordinate(200, 350),
                new Coordinate(400, 500), new Coordinate(450, 150)};
        Coordinate[] curve2 = {new Coordinate(100, 300), new Coordinate(200, 410),
                new Coordinate(350, 290), new Coordinate(400, 400)};
        Coordinate[] curve3 = {new Coordinate(100, 100), new Coordinate(300, 450),
                new Coordinate(250, 100), new Coordinate(400, 400)};

// кривые на общем рисунке

        var canvasForBezier = new DrawingBoardNew(1000, 1000);
        canvasForBezier.drawBezier (curve1[0], curve1[1], curve1[2], curve1[3], DrawingBoard.Color.BLACK);
        canvasForBezier.drawBezier (curve2[0], curve2[1], curve2[2], curve2[3], DrawingBoard.Color.GREEN);
        canvasForBezier.drawBezier (curve3[0], curve3[1], curve3[2], curve3[3], DrawingBoard.Color.BLUE);


        var DrawingBoard1 = new DrawingBoardNew(1000, 1000);
        var DrawingBoard2 = new DrawingBoardNew(1000, 1000);
        var DrawingBoard3 = new DrawingBoardNew(1000, 1000);

        DrawingBoard1.drawBezier (curve1[0], curve1[1], curve1[2], curve1[3], DrawingBoard.Color.BLACK);
        DrawingBoard2.drawBezier (curve2[0], curve2[1], curve2[2], curve2[3], DrawingBoard.Color.BLUE);
        DrawingBoard3.drawBezier (curve3[0], curve3[1], curve3[2], curve3[3], DrawingBoard.Color.GREEN);

        // отметим точки
        for (int i = 0; i < 4; i++) {
            DrawingBoard1.drawPoint(curve1[i], DrawingBoard.Color.RED);
            DrawingBoard2.drawPoint(curve2[i], DrawingBoard.Color.RED);
            DrawingBoard3.drawPoint(curve3[i], DrawingBoard.Color.RED);
        }

        saveImage(DrawingBoard1.getImage(), "curve_1");
        saveImage(DrawingBoard2.getImage(), "curve_2");
        saveImage(DrawingBoard3.getImage(), "curve_3");

        var canvasCutting = new DrawingBoardNew(1000, 1000);
        var polygon = new Polygon(100, 250, 200, 110, 450, 150, 500, 410, 350, 410); // not clockwise
        canvasCutting.drawPolygon(polygon, DrawingBoard.Color.BLACK);
        Coordinate[][] lines = {
                {new Coordinate(100, 100), new Coordinate(350, 450) },
                {new Coordinate(450, 0),  new Coordinate(15, 400)},
                {new Coordinate(200, 200),  new Coordinate(300, 300) },
                {new Coordinate(400, 200), new Coordinate(500, 500)},
                {new Coordinate(400, 10),   new Coordinate(500, 60)  },
        };

        for (var line : lines){

            canvasCutting.drawLine(line[0], line[1], DrawingBoard.Color.BLUE);
            canvasCutting.drawPartLine(line[0], line[1], polygon, DrawingBoard.Color.GREEN);
        }

        var canvasCutting2 = new DrawingBoardNew(1000, 1000);
        var polygon2 = new Polygon(160, 200, 300, 100, 500, 300, 500, 500); // not clockwise
        canvasCutting2.drawPolygon(polygon2, DrawingBoard.Color.BLACK);
        Coordinate[][] lines2 = {
                {new Coordinate(50, 100), new Coordinate(350, 200) },
                {new Coordinate(350, 0),  new Coordinate(15, 400)},
                {new Coordinate(200, 200),  new Coordinate(500, 300) },
                {new Coordinate(200, 200), new Coordinate(200, 500)},
                {new Coordinate(400, 10),   new Coordinate(500, 60)  },
                {new Coordinate(10, 40),   new Coordinate(50, 90)  },
        };

        for (var line : lines2){
            canvasCutting2.drawLine(line[0], line[1], DrawingBoard.Color.BLUE);
            canvasCutting2.drawPartLine(line[0], line[1], polygon2, DrawingBoard.Color.GREEN);
        }


        Imgcodecs.imwrite(savePath + "all_curves.png", canvasForBezier.getImage());
        Imgcodecs.imwrite(savePath + "CyrusBeck.png", canvasCutting.getImage());
        Imgcodecs.imwrite(savePath + "CyrusBeck_2.png", canvasCutting2.getImage());


    }
    public static void saveImage(Mat image, String title) {
        Imgcodecs.imwrite(savePath + title + ".png", image);
    }

}


class Coordinate {
    private final double x;
    private final double y;
    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }
    // введем сложение, вычитание и умножение точек
    public Coordinate addTo(Coordinate p){
        return new Coordinate(x + p.x, y + p.y);
    }
    public Coordinate subtractFrom(Coordinate p){
        return new Coordinate(x - p.x, y - p.y);
    }
    public Coordinate multiply(double scalar){
        return new Coordinate(scalar * x, scalar * y);
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
}

class DrawingBoardNew extends DrawingBoard{
    public DrawingBoardNew(int canvasWidth, int canvasHeight) {
        super(canvasWidth, canvasHeight);
    }


    public void drawLine(Coordinate p1, Coordinate p2, Color color) {
        drawLine(p1, p2, color.getBgr());
    }

    public void drawLine(Coordinate p1, Coordinate p2, byte[] bgr) {
        drawLine((int) Math.round(p1.getX()), (int) Math.round(p1.getY()),
                (int) Math.round(p2.getX()), (int) Math.round(p2.getY()), bgr);
    }



    public void drawPoint(Coordinate p, Color color) {
        drawPoint(p, color.getBgr());
    }

    public void drawPoint(Coordinate p, byte[] bgr) {



                    drawPoint((int) Math.round(p.getX()), (int) Math.round(p.getY()), bgr);



    }

    public void drawBezier (Coordinate p0, Coordinate p1, Coordinate p2, Coordinate p3, Color color) {
        drawBezier (p0, p1, p2, p3, color.getBgr());
    }



    public void drawBezier (Coordinate p0, Coordinate p1, Coordinate p2, Coordinate p3, byte[] bgr) {
        double H = Math.max(Dist(p0.addTo(p2.multiply(-2)).addTo(p3)),
                Dist(p0.addTo(p2.multiply(-2)).addTo(p3)));
        double N = 1.0 + Math.sqrt(3 * H);
        // N количество отрезков разбиения кривой
        var lastPoint = p0;
        for (double t = 0; t < 1; t += 1 / N){
            var nowPoint = thirdBezierCurve(p0, p1, p2, p3, t);
            drawLine(lastPoint, nowPoint, bgr);
            lastPoint = nowPoint;
        }
        drawLine(lastPoint, p3, bgr);
    }

    private double Dist(Coordinate p){
        return Math.abs(p.getX()) + Math.abs(p.getY());
    }
    //Вычисляет точку на прямой между p0 и p1 для параметра t
    private Coordinate firstBezierCurve(Coordinate p0, Coordinate p1, double t){
        return p0.multiply(1.0 - t).addTo(p1.multiply(t));
    }
    // Вычисляет точку на квадратичной кривой Безье с использованием линейной интерполяции
    private Coordinate secondBezierCurve(Coordinate p0, Coordinate p1, Coordinate p2, double t){
        return firstBezierCurve(firstBezierCurve(p0, p1, t), firstBezierCurve(p1, p2, t), t);
    }

    private Coordinate thirdBezierCurve(Coordinate p0, Coordinate p1, Coordinate p2, Coordinate p3, double t){
        return firstBezierCurve(secondBezierCurve(p0, p1, p2, t), secondBezierCurve(p1, p2, p3, t), t);
    }

    public void drawPartLine(Coordinate p1, Coordinate p2, Polygon polygon, Color color){
        drawPartLine(p1, p2, polygon, color.getBgr());
    }

    public void drawPartLine(Coordinate p1, Coordinate p2, Polygon polygon, byte[] bgr){
        if (!hasClockwiseOrientation(polygon))
            // меняем ориентацию полигона
            polygon = changeOrientation(polygon);
        if (!polygon.isConvexPolygon())
            throw new IllegalArgumentException("Ваш полигон не является выпуклым");
        // количество вершин многоугольника
        int n = polygon.getVertexNum();
        double t1 = 0, t2 = 1, t;
        double dir_x = p2.getX() - p1.getX(), dir_y = p2.getY() - p1.getY();
        for (int i = 0; i < n; i++) {
            // dir_x, dir_y: Направляющие векторы линии, задающей отрезок p1, p2
            double norm_x = polygon.CoordinatesOfVertex((i+1)%n)[1] - polygon.CoordinatesOfVertex(i)[1];
            double norm_y = polygon.CoordinatesOfVertex(i)[0] - polygon.CoordinatesOfVertex((i+1)%n)[0];
            double directionDotProduct = norm_x * dir_x + norm_y * dir_y;
            double distanceFromEdge = norm_x * (p1.getX() - polygon.CoordinatesOfVertex(i)[0]) +
                    norm_y * (p1.getY() - polygon.CoordinatesOfVertex(i)[1]);
            //
            if (directionDotProduct != 0) {
                t = -distanceFromEdge / directionDotProduct;
                if (directionDotProduct > 0) {
                    if (t > t1)
                        t1 = t;
                }
                else {
                    if (t < t2)
                        t2 = t;
                }
            } else {
                if (Polygon.pointSegmentClassify(polygon.CoordinatesOfVertex(i)[0], polygon.CoordinatesOfVertex(i)[1],
                        polygon.CoordinatesOfVertex((i+1)%n)[0], polygon.CoordinatesOfVertex((i+1)%n)[1],
                        p1.getX(), p1.getY()) == Polygon.pointLocation.LEFT_OF_LINE)
                    return;
            }
            // проверка на пересечение
            // если t1>t2 ---> видимая часть линии отсутствует
            if (t1 > t2)
                return;
        }
        if (t1 <= t2) {
            // вычисляем точки отсеченного отрезка
            Coordinate p1Cut = p1.addTo(p2.subtractFrom(p1).multiply(t1));
            Coordinate p2Cut = p1.addTo(p2.subtractFrom(p1).multiply(t2));
            drawLine(p1Cut, p2Cut, bgr);
        }
    }
    // изменение ориентации полигона
    private static Polygon changeOrientation(Polygon polygon){
        int n = polygon.getVertexNum();

        int[] xCoords = new int[n];
        int[] yCoords = new int[n];

        for (int i = 0; i < n; i++){
            xCoords[i] = polygon.CoordinatesOfVertex(n - 1 - i)[0];
            yCoords[i] = polygon.CoordinatesOfVertex(n - 1 - i)[1];
        }
        return new Polygon(xCoords, yCoords);
    }

    private static boolean hasClockwiseOrientation(Polygon polygon){
        int n = polygon.getVertexNum();

        boolean isCounterClockwiseDetected = false; // используется для отслеживания, было ли обнаружено,
        // когда вектор поворота между последовательными вершинами направлен против часовой стрелки


        for (int i = 0; i < n; i++){
            int[] a = polygon.CoordinatesOfVertex(i);
            int[] b = polygon.CoordinatesOfVertex((i + 1) % n);
            int[] c = polygon.CoordinatesOfVertex((i + 2) % n);
            int abx = b[0] - a[0];
            int aby = b[1] - a[1];
            int bcx = c[0] - b[0];
            int bcy = c[1] - b[1];
            int product = abx * bcy - aby * bcx;
            if (product > 0)
                isCounterClockwiseDetected = true;
            if (isCounterClockwiseDetected)
                return false;
        }
        return true;
    }
}

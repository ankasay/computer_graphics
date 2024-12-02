import org.opencv.core.*;
import java.util.Arrays;
import java.util.function.BiFunction;
import org.opencv.imgcodecs.Imgcodecs;

class Polygon{
    private final int[] X;
    private final int[] Y;
    private final boolean isSelfIntersected;
    private final boolean isConvexPolygon;

    public Polygon(int[] X, int[] Y){
        this.X = X.clone();
        this.Y = Y.clone();
        isSelfIntersected = validateSelfIntersections();
        isConvexPolygon = validateConvexity();
    }

    public Polygon(int... coords){
        X = new int[coords.length / 2];
        Y = new int[coords.length / 2];
        for (int i = 0; i < coords.length; i++) {
            if (i % 2 == 0)
                X[i / 2] = coords[i];
            else
                Y[i / 2] = coords[i];
        }
        isSelfIntersected = validateSelfIntersections();
        isConvexPolygon = validateConvexity();
    }

    public int getVertexNum(){
        return X.length;
    }

    public int[] CoordinatesOfVertex(int index){
        return new int[]{X[index], Y[index]};
    }

    public int maxX(){
        return Arrays.stream(X).max().getAsInt();
    }

    public int minX(){
        return Arrays.stream(X).min().getAsInt();
    }

    public int maxY(){
        return Arrays.stream(Y).max().getAsInt();
    }

    public int minY(){
        return Arrays.stream(Y).min().getAsInt();
    }

    public boolean isConvexPolygon(){
        return isConvexPolygon;
    }

    public boolean hasSelfIntersection(){
        return isSelfIntersected;
    }

    // для определения положения точки относительно отрезка

    protected enum pointLocation{
        LEFT_OF_LINE,
        RIGHT_ON_LINE,
        PAST_LINE,
        BEHIND_LINE,
        BETWEEN_LINE_SEGMENTS,
        AT_ORIGIN,
        AT_END_POINT,
    }

    protected static pointLocation pointSegmentClassify(double startX, double startY,
                                                        double endX, double endY,
                                                        double pointX, double pointY) {
        // Векторы отрезка и точки
        double segmentDX = endX - startX; // Разница по оси X для отрезка
        double segmentDY = endY - startY; // Разница по оси Y для отрезка
        double pointDX = pointX - startX;  // Разница по оси X для точки
        double pointDY = pointY - startY;  // Разница по оси Y для точки

        // Вычисляем ориентированную площадь (определитель)
        double crossProduct = segmentDX * pointDY - pointDX * segmentDY;

        // Определяем положение точки относительно отрезка
        if (crossProduct > 0) {
            return pointLocation.LEFT_OF_LINE; // Точка слева от отрезка
        }
        if (crossProduct < 0) {
            return pointLocation.RIGHT_ON_LINE; // Точка справа от отрезка
        }

        // Проверяем, находится ли точка позади отрезка
        if ((segmentDX * pointDX < 0) || (segmentDY * pointDY < 0)) {
            return pointLocation.BEHIND_LINE; // Точка позади отрезка
        }

        // Проверяем, находится ли точка перед отрезком
        if ((segmentDX * segmentDX + segmentDY * segmentDY) < (pointDX * pointDX + pointDY * pointDY)) {
            return pointLocation.PAST_LINE; // Точка перед отрезком
        }

        // Проверяем, совпадает ли точка с началом или концом отрезка
        if (startX == pointX && startY == pointY) {
            return pointLocation.AT_ORIGIN; // Точка совпадает с началом отрезка
        }
        if (endX == pointX && endY == pointY) {
            return pointLocation.AT_END_POINT; // Точка совпадает с концом отрезка
        }

        // Если ни одно из условий не выполнено, точка находится между концами отрезка
        return pointLocation.BETWEEN_LINE_SEGMENTS;
    }

    private enum lineType {
        IDENTICAL,           // Совпадающие
        PARALLEL_LINES,      // Параллельные
        NON_PARALLEL,        // Непараллельные (не пересекающиеся)
        INTERSECTING_LINES,  // Пересекающиеся
        DISJOINT             // Не пересекающиеся
    }

    private static lineType intersectSegmentLine(double segmentDX, double segmentDY,
                                                 double pointDX, double pointDY,
                                                 double cx, double cy,
                                                 double dx, double dy,
                                                 double[] t){
        if (t.length != 1)
            throw new IllegalArgumentException("в массиве не один элемент");
        double nx = dy - cy;
        double ny = cx - dx;
        pointLocation pointLocation;
        double denom = nx * (pointDX - segmentDX) + ny * (pointDY - segmentDY);
        if (denom == 0){
            pointLocation = pointSegmentClassify(cx, cy, dx, dy, segmentDX, segmentDY);
            if (pointLocation == pointLocation.LEFT_OF_LINE || pointLocation == pointLocation.RIGHT_ON_LINE)
                return lineType.PARALLEL_LINES;
            else
                return lineType.IDENTICAL;
        }
        double num = nx * (segmentDX - cx) + ny * (segmentDY - cy);
        t[0] = -num/denom;
        return lineType.NON_PARALLEL;
    }

    private static lineType intersectSegmentSegment(double segmentDX, double segmentDY,
                                                    double pointDX, double pointDY,
                                                    double cx, double cy,
                                                    double dx, double dy){
        double[] tab = new double[1], tcd = new double[1];
        lineType intersectType = intersectSegmentLine(segmentDX, segmentDY, pointDX, pointDY, cx, cy, dx, dy, tab);
        if (intersectType == lineType.IDENTICAL || intersectType == lineType.PARALLEL_LINES)
            return intersectType;
        if ((tab[0] < 0) || (tab[0] > 1))
            return lineType.DISJOINT;
        intersectSegmentLine(cx, cy, dx, dy, segmentDX, segmentDY, pointDX, pointDY, tcd);
        if ((tcd[0] < 0) || (tcd[0] > 1))
            return lineType.DISJOINT;
        return lineType.INTERSECTING_LINES;
    }

    private enum EType {
        TOUCHING,
        CROSS_LEFT,
        CROSS_RIGHT,
        INESSENTIAL,
    }

    private static EType edgeRayClassify(double segmentDX, double segmentDY, double pointDX, double pointDY, double rayStartX, double rayStartY){
        switch (pointSegmentClassify(segmentDX, segmentDY, pointDX, pointDY, rayStartX, rayStartY)) {
            case LEFT_OF_LINE:
                if (rayStartY > segmentDY && rayStartY <= pointDY)
                    return EType.CROSS_LEFT;
                return EType.INESSENTIAL;
            case RIGHT_ON_LINE:
                if (rayStartY > pointDY && rayStartY <= segmentDY)
                    return EType.CROSS_RIGHT;
                return EType.INESSENTIAL;
            case BETWEEN_LINE_SEGMENTS:
            case AT_ORIGIN:
            case AT_END_POINT:
                return EType.TOUCHING;
            default:
                return EType.INESSENTIAL;
        }
    }

    public boolean isPointInPolygonEOMode(double pointX, double pointY){
        int totalVertices = getVertexNum();
        int param = 0;
        for (int i = 0; i < totalVertices; i++){
            int[] aCoords = CoordinatesOfVertex(i);
            int[] bCoords = CoordinatesOfVertex((i + 1) % totalVertices);
            switch (edgeRayClassify(aCoords[0], aCoords[1], bCoords[0], bCoords[1], pointX, pointY)){
                case TOUCHING:
                    return true;
                case CROSS_LEFT:

                case CROSS_RIGHT:
                    param = 1 - param;
            }
        }
        return param == 1;
    }

    public boolean isPointInPolygonNZWMode(double pointX, double pointY){
        int totalVertices = getVertexNum();
        int param = 0;
        for (int i = 0; i < totalVertices; i++){
            int[] aCoords = CoordinatesOfVertex(i);
            int[] bCoords = CoordinatesOfVertex((i + 1) % totalVertices);
            switch (edgeRayClassify(aCoords[0], aCoords[1], bCoords[0], bCoords[1], pointX, pointY)){
                case TOUCHING:
                    return true;
                case CROSS_LEFT:
                    param++;
                    break;
                case CROSS_RIGHT:
                    param--;
                    break;
            }
        }
        return param != 0;
    }

    private boolean validateSelfIntersections() {
        int totalVertices = getVertexNum();

        // Если количество вершин меньше 4, нет возможности для пересечения
        if (totalVertices < 4) {
            return false;
        }

        // Проходим по всем парам отрезков
        for (int i = 0; i < totalVertices; i++) {
            // Получаем текущий отрезок AB
            int[] vertexA = CoordinatesOfVertex(i);
            int[] vertexB = CoordinatesOfVertex((i + 1) % totalVertices);

            // Проверяем отрезки, которые не являются соседними
            for (int j = i + 2; j < totalVertices; j++) {
                // Пропускаем проверку для отрезков, соединяющих первую и последнюю вершины
                if (i == 0 && j == totalVertices - 1) {
                    continue;
                }

                // Получаем отрезок CD
                int[] vertexC = CoordinatesOfVertex(j);
                int[] vertexD = CoordinatesOfVertex((j + 1) % totalVertices);

                // Проверяем, пересекаются ли отрезки AB и CD
                lineType intersectionResult = intersectSegmentSegment(
                        vertexA[0], vertexA[1],
                        vertexB[0], vertexB[1],
                        vertexC[0], vertexC[1],
                        vertexD[0], vertexD[1]
                );

                // Если отрезки пересекаются, возвращаем true
                if (intersectionResult == lineType.INTERSECTING_LINES) {
                    return true;
                }
            }
        }

        // Если пересечений не найдено, возвращаем false
        return false;
    }

    private boolean validateConvexity() {
        int totalVertices = getVertexNum();

        // Если количество вершин меньше 3, это не многоугольник, возвращаем true
        if (totalVertices < 3) {
            return true;
        }

        // Проверяем на самопересечения
        if (validateSelfIntersections()) {
            return false;
        }

        boolean hasClockwiseRotation = false;
        boolean hasCounterClockwiseRotation = false;

        // Проходим по всем вершинам многоугольника
        for (int i = 0; i < totalVertices; i++) {
            // Получаем три последовательные вершины
            int[] vertexA = CoordinatesOfVertex(i);
            int[] vertexB = CoordinatesOfVertex((i + 1) % totalVertices);
            int[] vertexC = CoordinatesOfVertex((i + 2) % totalVertices);

            // Вычисляем векторы AB и BC
            int vectorABx = vertexB[0] - vertexA[0];
            int vectorABy = vertexB[1] - vertexA[1];
            int vectorBCx = vertexC[0] - vertexB[0];
            int vectorBCy = vertexC[1] - vertexB[1];

            // Вычисляем векторное произведение
            int crossProduct = vectorABx * vectorBCy - vectorABy * vectorBCx;

            // Определяем ориентацию
            if (crossProduct > 0) {
                hasCounterClockwiseRotation = true;
            } else if (crossProduct < 0) {
                hasClockwiseRotation = true;
            }

            // Если обнаружены обе ориентации, многоугольник не выпуклый
            if (hasClockwiseRotation && hasCounterClockwiseRotation) {
                return false;
            }
        }

        // Если все углы имеют одну ориентацию, многоугольник выпуклый
        return true;
    }

}

class DrawingBoard {
    private final Mat image;

    public enum Color{
        RED(new byte[]{0, 0, (byte) 255}),
        BLUE(new byte[]{(byte) 255, 0, 0}),
        GREEN(new byte[]{0, (byte) 255, 0}),
        BLACK(new byte[]{0, 0, 0}),
        WHITE(new byte[]{(byte) 255, (byte) 255, (byte) 255}),
        PINK(new byte[]{(byte) 255, (byte) 192, (byte) 203}),
        MAGENTA(new byte[]{(byte) 255, 0, (byte) 255}),
        GRAY(new byte[]{(byte) 128, (byte) 128, (byte) 128}),
        ORANGE(new byte[]{0, (byte) 165, (byte) 255}),
        PURPLE(new byte[]{(byte) 128, 0, (byte) 128});

        private final byte[] bgr;
        Color(byte[] bgr){
            this.bgr = bgr;
        }

        public byte[] getBgr(){
            return bgr;
        }
    }


    // создание белой области для рисования
    public DrawingBoard (int canvasWidth, int canvasHeight){


        image = new Mat(canvasHeight, canvasWidth, CvType.CV_8UC3);
        image.setTo(new Scalar(255, 255, 255));

    }

    public Mat getImage(){
        return image.clone();
    }



    public int getWidth(){
        return image.width();
    }

    public int getHeight(){
        return image.height();
    }

    public void drawPoint(int pointX, int pointY, Color color){
        drawPoint(pointX, pointY, color.getBgr());
    }

    public void drawPoint(int pointX, int pointY, byte[] bgr) {
        if (pointX >= 0 && pointX < getWidth() && pointY >= 0 && pointY < getHeight()) {
            image.put(pointY, pointX, bgr);
        }
    }

    public void drawLine(int startX, int startY, int endX, int endY, Color color){
        drawLine(startX, startY, endX, endY, color.getBgr());
    }

    public void drawLine(int startX, int startY, int endX, int endY, byte[] bgr) {
        // Добавьте проверки для startX, startY, endX, endY
        if (startX < 0 || startX >= getWidth() || startY < 0 || startY >= getHeight() ||
                endX < 0 || endX >= getWidth() || endY < 0 || endY >= getHeight()) {
            return; // Не рисуем, если координаты вне границ
        }
        int pointX = startX, pointY = startY;
        int dx = endX - startX, dy = endY - startY;
        int e, i;
        int ix = Integer.compare(dx, 0);
        int iy = Integer.compare(dy, 0);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if (dx >= dy) {
            e = 2 * dy - dx;
            if (iy >= 0) {
                for (i = 0; i <= dx; i++) {
                    drawPoint(pointX, pointY, bgr);
                    if (e >= 0) {
                        pointY += iy;
                        e -= 2 * dx;
                    }
                    pointX += ix;
                    e += dy * 2;
                }
            } else {
                for (i = 0; i <= dx; i++) {
                    drawPoint(pointX, pointY, bgr);
                    if (e > 0) {
                        pointY += iy;
                        e -= 2 * dx;
                    }
                    pointX += ix;
                    e += dy * 2;
                }
            }
        } else {
            e = 2 * dx - dy;
            if (ix >= 0) {
                for (i = 0; i <= dy; i++) {
                    drawPoint(pointX, pointY, bgr);
                    if (e >= 0) {
                        pointX += ix;
                        e -= 2 * dy;
                    }
                    pointY += iy;
                    e += dx * 2;
                }
            } else {
                for (i = 0; i <= dy; i++) {
                    drawPoint(pointX, pointY, bgr);
                    if (e > 0) {
                        pointX += ix;
                        e -= 2 * dy;
                    }
                    pointY += iy;
                    e += dx * 2;
                }
            }
        }
    }

    public void drawPolygon(Polygon poly, Color color){
        drawPolygon(poly, color.getBgr());
    }

    public void drawPolygon(Polygon poly, byte[] bgr){
        int vertexNum = poly.getVertexNum();

        if (vertexNum == 0)
            return;
        if (vertexNum == 1){
            drawPoint(poly.CoordinatesOfVertex(0)[0], poly.CoordinatesOfVertex(0)[1], bgr);
            return;
        }
        if (vertexNum == 2){
            int[] coords1 = poly.CoordinatesOfVertex(0);
            int[] coords2 = poly.CoordinatesOfVertex(1);
            drawLine(coords1[0], coords1[1], coords2[0], coords2[1], bgr);
            return;
        }

        int[] coordsPrev = poly.CoordinatesOfVertex(0);
        int[] coordsNext;
        for (int i = 1; i < vertexNum; i++){
            coordsNext = poly.CoordinatesOfVertex(i);
            drawLine(coordsPrev[0], coordsPrev[1], coordsNext[0], coordsNext[1], bgr);
            coordsPrev = coordsNext;
        }
        coordsNext = poly.CoordinatesOfVertex(0);
        drawLine(coordsPrev[0], coordsPrev[1], coordsNext[0], coordsNext[1], bgr);
    }

    public void fillPolygonEOMode(Polygon poly, Color color){
        fillPolygonEOMode(poly, color.getBgr());
    }

    public void fillPolygonEOMode(Polygon poly, byte[] bgr){
        fillPolygon(poly, bgr, poly::isPointInPolygonEOMode);
    }

    public void fillPolygonNZWMode(Polygon poly, Color color){
        fillPolygonNZWMode(poly, color.getBgr());
    }

    public void fillPolygonNZWMode(Polygon poly, byte[] bgr){
        fillPolygon(poly, bgr, poly::isPointInPolygonNZWMode);
    }

    private void fillPolygon(Polygon poly, byte[] bgr, BiFunction<Integer, Integer, Boolean> isInPolygonFunc){
        int minX = poly.minX();
        int minY = poly.minY();
        int maxX = poly.maxX();
        int maxY = poly.maxY();

        for (int pointX = minX; pointX <= maxX; pointX++){
            for (int pointY = minY; pointY <= maxY; pointY++){
                if (isInPolygonFunc.apply(pointX, pointY))
                    image.put(pointY, pointX, bgr);
            }
        }
    }
}
public class Lab3_star {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String outputDirectory = "results/";

// вычерчивание отрезков прямых линиц толщиной в пиксель

    public static void main(String[] args) {
        var lineSegment = new DrawingBoard (1000, 1000);
        lineSegment.drawLine(200, 300, 800, 100, DrawingBoard .Color.PINK);
        lineSegment.drawLine(250, 350, 750, 900, DrawingBoard .Color.MAGENTA );
        lineSegment.drawLine(100, 500, 600, 300, DrawingBoard .Color.BLUE);
        lineSegment.drawLine(300, 700, 100, 400, DrawingBoard .Color.GRAY);
        lineSegment.drawLine(500, 100, 300, 800, DrawingBoard .Color.ORANGE );
        lineSegment.drawLine(900, 400, 200, 300, DrawingBoard .Color.PURPLE);


        Imgcodecs.imwrite(outputDirectory  + "lines.png", lineSegment.getImage());

        // задание и отрисовка полигонов

        var polygonDrawer_1 = new DrawingBoard (1000, 1000);
        var Polygon1 = new Polygon(100, 300, 700, 900, 250, 900, 800, 500, 200, 500, 500, 800, 200, 800, 900, 300);
        var Polygon2 = new Polygon(100, 200, 600, 200, 750, 500, 800, 400, 900, 700, 100, 700);
        var Polygon3 = new Polygon(100, 200, 900, 300, 800, 900, 100, 900);
        polygonDrawer_1.drawPolygon(Polygon1, DrawingBoard .Color.BLACK);
        var polygonDrawer_2 = new DrawingBoard (1000, 1000);
        polygonDrawer_2.drawPolygon(Polygon2, DrawingBoard .Color.BLACK);
        var polygonDrawer_3 = new DrawingBoard (1000, 1000);
        polygonDrawer_3.drawPolygon(Polygon3, DrawingBoard .Color.BLACK);
        Imgcodecs.imwrite(outputDirectory + "1.png", polygonDrawer_1.getImage());
        Imgcodecs.imwrite(outputDirectory + "2.png", polygonDrawer_2.getImage());
        Imgcodecs.imwrite(outputDirectory + "3.png", polygonDrawer_3.getImage());

        // определение типа полигона

        System.out.println("У полигона 1 " + (Polygon1.hasSelfIntersection() ? "есть" : "нет") + " самопересечения.");
        System.out.println("Полигон 1 " + (Polygon1.isConvexPolygon
                () ? "выпуклый." : "не выпуклый."));
        System.out.println("У полигона 2 " + (Polygon2.hasSelfIntersection() ? "есть" : "нет") + " самопересечения.");
        System.out.println("Полигон 2 " + (Polygon2.isConvexPolygon
                () ? "выпуклый." : "не выпуклый."));
        System.out.println("У полигона 3 " + (Polygon3.hasSelfIntersection() ? "есть" : "нет") + " самопересечения.");
        System.out.println("Полигон 3 " + (Polygon3.isConvexPolygon
                () ? "выпуклый." : "не выпуклый."));


// Закрашивание полигонов

        var methodEO = new DrawingBoard (1000, 1000);
        methodEO.drawPolygon(Polygon1, DrawingBoard .Color.BLACK);
        methodEO.fillPolygonEOMode(Polygon1, DrawingBoard .Color.PINK);
        Imgcodecs.imwrite(outputDirectory + "EO_1.png", methodEO.getImage());

        var methodNZW = new DrawingBoard (1000, 1000);
        methodNZW.drawPolygon(Polygon1, DrawingBoard .Color.BLACK);
        methodNZW.fillPolygonNZWMode(Polygon1, DrawingBoard .Color.PINK);
        Imgcodecs.imwrite(outputDirectory + "NZW_1.png", methodNZW.getImage());

        // для другого полигона

        var methodEO_2 = new DrawingBoard (1000, 1000);
        methodEO_2.drawPolygon(Polygon2, DrawingBoard .Color.BLACK);
        methodEO_2.fillPolygonEOMode(Polygon2, DrawingBoard .Color.PINK);
        Imgcodecs.imwrite(outputDirectory + "EO_2.png", methodEO_2.getImage());

        var methodNZW_2 = new DrawingBoard (1000, 1000);
        methodNZW_2.drawPolygon(Polygon2, DrawingBoard .Color.BLACK);
        methodNZW_2.fillPolygonNZWMode(Polygon2, DrawingBoard .Color.PINK);
        Imgcodecs.imwrite(outputDirectory + "NZW_2.png", methodNZW_2.getImage());

        var methodEO_3 = new DrawingBoard (1000, 1000);
        methodEO_3.drawPolygon(Polygon3, DrawingBoard .Color.BLACK);
        methodEO_3.fillPolygonEOMode(Polygon3, DrawingBoard .Color.PINK);
        Imgcodecs.imwrite(outputDirectory + "EO_3.png", methodEO_3.getImage());

        var methodNZW_3 = new DrawingBoard (1000, 1000);
        methodNZW_3.drawPolygon(Polygon3, DrawingBoard .Color.BLACK);
        methodNZW_3.fillPolygonNZWMode(Polygon3, DrawingBoard .Color.PINK);
        Imgcodecs.imwrite(outputDirectory + "NZW_3.png", methodNZW_3.getImage());

    }
}
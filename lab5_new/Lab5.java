import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.Iterator;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Lab5 extends Lab4 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static String savePath = "results/";

    public static void main(String[] args) {


        var drawingBoardParallel = new DrawingBoardLab5(1000, 1000);
        var drawingBoardPerspective = new DrawingBoardLab5(1000, 1000);
        var drawingBoardParallelRemov = new DrawingBoardLab5(1000, 1000);
        var drawingBoardPerspectiveRemov = new DrawingBoardLab5(1000, 1000);
        var figure = new Parallelepiped(new Sides[]{
                new Sides(new Coordinate3D(300, 300, -200), new Coordinate3D(700, 300, -200),
                        new Coordinate3D(300, 700, -200), new Coordinate3D(700, 700, -200)),

                new Sides(new Coordinate3D(300, 300, -200), new Coordinate3D(400, 400, 200),
                        new Coordinate3D(700, 300, -200), new Coordinate3D(800, 400, 200)),

                new Sides(new Coordinate3D(300, 300, -200), new Coordinate3D(300, 700, -200),
                        new Coordinate3D(400, 400, 200), new Coordinate3D(400, 800, 200)),

                new Sides(new Coordinate3D(300, 700, -200), new Coordinate3D(700, 700, -200),
                        new Coordinate3D(400, 800, 200), new Coordinate3D(800, 800, 200)),

                new Sides(new Coordinate3D(700, 700, -200), new Coordinate3D(700, 300, -200),
                        new Coordinate3D(800, 800, 200), new Coordinate3D(800, 400, 200)),

                new Sides(new Coordinate3D(400, 400, 200), new Coordinate3D(400, 800, 200),
                        new Coordinate3D(800, 400, 200), new Coordinate3D(800, 800, 200)),
        });


        figure = figure.spin(Math.PI / 10, new Coordinate3D(1, 1.6, 0));

        // параллельная проекция
        var parallelProjectionLines = figure.ParallelforXY();
        if (parallelProjectionLines != null) {
            for (var line : parallelProjectionLines) {
                drawingBoardParallel.drawLine(line[0], line[1], DrawingBoard.Color.BLUE);
            }
        }

        Imgcodecs.imwrite(savePath + "parallel.png", drawingBoardParallel.getImage());
        // перспективная проекция
        // центр проекции [0, 0, k]
        var perspectiveProjectionLines = figure.PerspectiveforYandZ(0.002, 0.002);
        for (var line : perspectiveProjectionLines) {
            drawingBoardPerspective.drawLine(line[0], line[1], DrawingBoard.Color.BLUE);
        }
        // с удалением линий
        var parallelEdgesRemoved = figure.ParallelforXY(new Coordinate3D(0, 0, -10));
        for (var line : parallelEdgesRemoved) {
            drawingBoardParallelRemov.drawLine(line[0], line[1], DrawingBoard.Color.BLUE);
        }
        Imgcodecs.imwrite(savePath + "parallel_edges_removed.png", drawingBoardParallelRemov.getImage());

        var perspectiveEdgesRemoved = figure.PerspectiveforYandZ(0.002, 0.002, new Coordinate3D(0, 0, -10));
        for (var line : perspectiveEdgesRemoved) {
            drawingBoardPerspectiveRemov.drawLine(line[0], line[1], DrawingBoard.Color.BLUE);
        }

        Imgcodecs.imwrite(savePath + "perspective_edges_removed.png", drawingBoardPerspectiveRemov.getImage());


        Imgcodecs.imwrite(savePath + "perspective.png", drawingBoardPerspective.getImage());



        try {
            ParallelAnimation(savePath + "parallel_anim.gif", 110, 10, figure);
            PerspectiveAnimaton(savePath + "perspective_anim.gif", 110, 10, figure);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void ParallelAnimation(String filePath, int animationFrames, int delay, Parallelepiped figure) throws Exception {
        ImageWriter gifWriter = getGifWriter();
        ImageWriteParam gifParams = gifWriter.getDefaultWriteParam();
        ImageOutputStream output = ImageIO.createImageOutputStream(new File(filePath));
        gifWriter.setOutput(output);
        gifWriter.prepareWriteSequence(null);

        for (double rotationAngle = 0; rotationAngle < 2 * Math.PI; rotationAngle += Math.PI / (animationFrames / 2.0)) {
            var drawingBoad = new DrawingBoardLab5(1500, 1500);
            var figureAfterRotation = figure.spin(rotationAngle, new Coordinate3D(1, 2, 0));

            // Сдвигаем фигуру вправо
            figureAfterRotation = shiftFigureRight(figureAfterRotation, 300);

            var parallelEdgesRemoved = figureAfterRotation.ParallelforXY(new Coordinate3D(0, 0, -10));
            for (var line : parallelEdgesRemoved) {
                drawingBoad.drawLine(line[0], line[1], DrawingBoard.Color.BLUE);
            }
            Mat matFrame = drawingBoad.getImage();
            BufferedImage bufferedImage = matToBufferedImage(matFrame);

            IIOMetadata metadata = gifWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bufferedImage), gifParams);
            setGifAnimationProperties(metadata, delay);

            gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metadata), gifParams);
        }

        gifWriter.endWriteSequence();
        output.close();
    }

    public static void PerspectiveAnimaton(String filePath, int animationFrames, int delay, Parallelepiped figure) throws Exception {
        ImageWriter gifWriter = getGifWriter();
        ImageWriteParam gifParams = gifWriter.getDefaultWriteParam();
        ImageOutputStream output = ImageIO.createImageOutputStream(new File(filePath));
        gifWriter.setOutput(output);
        gifWriter.prepareWriteSequence(null);

        for (double rotationAngle = 0; rotationAngle < 2 * Math.PI; rotationAngle += Math.PI / (animationFrames / 2.0)) {
            var drawingBoad = new DrawingBoardLab5(1500, 1500);
            var figureAfterRotation = figure.spin(rotationAngle, new Coordinate3D(1, 2, 0));

            // Сдвигаем фигуру вправо
            figureAfterRotation = shiftFigureRight(figureAfterRotation, 300);

            var parallelEdgesRemoved = figureAfterRotation.PerspectiveforYandZ(0.002, 0.002, new Coordinate3D(0, 0, -10));
            for (var line : parallelEdgesRemoved) {
                drawingBoad.drawLine(line[0], line[1], DrawingBoard.Color.BLUE);
            }
            Mat matFrame = drawingBoad.getImage();
            BufferedImage bufferedImage = matToBufferedImage(matFrame);

            IIOMetadata metadata = gifWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(bufferedImage), gifParams);
            setGifAnimationProperties(metadata, delay);

            gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metadata), gifParams);
        }

        gifWriter.endWriteSequence();
        output.close();
    }


    private static Parallelepiped shiftFigureRight(Parallelepiped figure, double offsetX) {
        Sides[] shiftedSides = new Sides[figure.getSides().length];
        for (int i = 0; i < figure.getSides().length; i++) {
            shiftedSides[i] = new Sides(
                    new Coordinate3D(figure.getSides()[i].getPoints()[0].getX() + offsetX, figure.getSides()[i].getPoints()[0].getY(), figure.getSides()[i].getPoints()[0].getZ()),
                    new Coordinate3D(figure.getSides()[i].getPoints()[1].getX() + offsetX, figure.getSides()[i].getPoints()[1].getY(), figure.getSides()[i].getPoints()[1].getZ()),
                    new Coordinate3D( figure.getSides()[i].getPoints()[2].getX() + offsetX, figure.getSides()[i].getPoints()[2].getY(), figure.getSides()[i].getPoints()[2].getZ()),
                    new Coordinate3D(figure.getSides()[i].getPoints()[3].getX() + offsetX, figure.getSides()[i].getPoints()[3].getY(), figure.getSides()[i].getPoints()[3].getZ())
            );
        }
        return new Parallelepiped(shiftedSides);
    }

    private static ImageWriter getGifWriter() {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!writers.hasNext()) throw new IllegalStateException("Не удалось найти модуль для записи в формате gif");
        return writers.next();
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
        mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        return image;
    }

    // настройки метаданных изображения в формате GIF с использованием библиотеки javax.imageio
    private static void setGifAnimationProperties(IIOMetadata metadata, int delay) throws Exception {
        String metaFormatName = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        IIOMetadataNode applicationExtensions = new IIOMetadataNode("ApplicationExtensions");
        IIOMetadataNode applicationExtension = new IIOMetadataNode("ApplicationExtension");
        applicationExtension.setAttribute("applicationID", "NETSCAPE");
        applicationExtension.setAttribute("authenticationCode", "2.0");

        byte[] loopContinuously = {0x1, 0x0, 0x0};
        applicationExtension.setUserObject(loopContinuously);
        applicationExtensions.appendChild(applicationExtension);
        root.appendChild(applicationExtensions);

        metadata.setFromTree(metaFormatName, root);
    }

    // Получение узла метаданных (в виде IIOMetadataNode) из дерева метаданных.
    // Если узел с указанным именем не найден, он создается и добавляется в дерево.
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }

}
class DrawingBoardLab5 extends DrawingBoardNew{
    public DrawingBoardLab5(int canvasWidth, int canvasHeight) {
        super(canvasWidth, canvasHeight);
    }

}

class Coordinate3D {
    private final double x;
    private final double y;
    private final double z;

    public Coordinate3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coordinate3D(Coordinate p, double z){

        this(p.getX(), p.getY(), z);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Coordinate3D add(Coordinate3D p) {
        return new Coordinate3D(p.getX() + getX(), p.getY() + getY(), p.getZ() + getZ());
    }

    public Coordinate3D sub(Coordinate3D p) {
        return new Coordinate3D(getX() - p.getX(), getY() - p.getY(), getZ() - p.getZ());
    }

    public Coordinate3D multiply(double scalar) {
        return new Coordinate3D(getX() * scalar, getY() * scalar, getZ() * scalar);
    }

    public double dot(Coordinate3D p) {
        return getX() * p.getX() + getY() * p.getY() + getZ() * p.getZ();
    }

    public Coordinate3D spin(double rotationAngle, Coordinate3D axis) {
        double axisVectorLength = Math.sqrt(axis.getX() * axis.getX() +
                axis.getY() * axis.getY() +
                axis.getZ() * axis.getZ());
        double nx = axis.getX() / axisVectorLength;
        double ny = axis.getY() / axisVectorLength;
        double nz = axis.getZ() / axisVectorLength;

        // Выписываем матрицу преобразования
        double[][] T = {
                {       cos(rotationAngle) + nx * nx * (1 - cos(rotationAngle)),
                        nx * ny * (1 - cos(rotationAngle)) + nz * sin(rotationAngle),
                        nx * nz * (1 - cos(rotationAngle)) - ny * sin(rotationAngle),
                        0
                },
                {
                        nx * ny * (1 - cos(rotationAngle)) - nz * sin(rotationAngle),
                        cos(rotationAngle) + ny * ny * (1 - cos(rotationAngle)),
                        ny * nz * (1 - cos(rotationAngle)) + nx * sin(rotationAngle),
                        0
                },
                {
                        nx * nz * (1 - cos(rotationAngle)) + ny * sin(rotationAngle),
                        ny * nz * (1 - cos(rotationAngle)) - nx * sin(rotationAngle),
                        cos(rotationAngle) + nz * nz * (1 - cos(rotationAngle)),
                        0
                },
                {0, 0, 0, 1}
        };

        return modify(T);
    }

    // Функция для преобразования координат: [X, Y, Z, H] = [x, y, z, 1]T и их нормализации
    public Coordinate3D modify(double[][] T){
        double[] homCoords = {getX(), getY(), getZ(), 1};
        double X = 0, Y = 0, Z = 0, H = 0;
        for (int i = 0; i < T[0].length; i++) {
            X += T[i][0] * homCoords[i];
            Y += T[i][1] * homCoords[i];
            Z += T[i][2] * homCoords[i];
            H += T[i][3] * homCoords[i];
        }
        // нормализуем
        X /= H;
        Y /= H;
        Z /= H;
        return new Coordinate3D(X, Y, Z);
    }

    // проверка равенства двух объектов
    // переопределение метода equals
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordinate3D other = (Coordinate3D) obj;
        return Double.compare(other.getX(), getX()) == 0 &&
                Double.compare(other.getY(), getY()) == 0 &&
                Double.compare(other.getZ(), getZ()) == 0;
    }
}
// необходимо для корректного сравнения двух объектов
class Sides {
    private final Coordinate3D vertex1;
    private final Coordinate3D vertex2;
    private final Coordinate3D vertex3;
    private final Coordinate3D vertex4;

    public Sides(Coordinate3D vertex1, Coordinate3D vertex2, Coordinate3D vertex3, Coordinate3D vertex4) {
        if (vertex1 == null || vertex2 == null || vertex3 == null || vertex4 == null)
            throw new IllegalArgumentException("Вершины быть null");

        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.vertex3 = vertex3;
        this.vertex4 = vertex4;
    }

    public Edge3D[] getEdges() {
        return new Edge3D[]{
                new Edge3D(vertex1, vertex2),
                new Edge3D(vertex1, vertex3),
                new Edge3D(vertex3, vertex4),
                new Edge3D(vertex2, vertex4),
        };
    }

    public Coordinate3D[] getPoints() {
        return new Coordinate3D[] {vertex1, vertex2, vertex3, vertex4};
    }

    public Coordinate3D getNormal() {
        // Вернет точку, определяющую радиус-вектор, совпадающий с вектором нормали
        var vector1 = vertex2.sub(vertex1);
        var vector2 = vertex3.sub(vertex1);
        var normalVector = new Coordinate3D(vector1.getY() * vector2.getZ() - vector1.getZ() * vector2.getY(),
                vector1.getZ() * vector2.getX() - vector1.getX() * vector2.getZ(),
                vector1.getX() * vector2.getY() - vector1.getY() * vector2.getX());
        double length = Math.sqrt(normalVector.getX() * normalVector.getX() + normalVector.getY() * normalVector.getY() + normalVector.getZ() * normalVector.getZ());
        return normalVector.multiply(1 / length);
    }

    public Sides modify(double[][] T){
        return new Sides(vertex1.modify(T), vertex2.modify(T), vertex3.modify(T), vertex4.modify(T));
    }

    public Sides spin(double rotationAngle, Coordinate3D axis) {
        return new Sides(vertex1.spin(rotationAngle, axis), vertex2.spin(rotationAngle, axis),
                vertex3.spin(rotationAngle, axis), vertex4.spin(rotationAngle, axis));
    }
}

class Edge3D {
    private final Coordinate3D vertex1;
    private final Coordinate3D vertex2;

    public Edge3D(Coordinate3D vertex1, Coordinate3D vertex2) {
        this.vertex1 = new Coordinate3D(vertex1.getX(), vertex1.getY(), vertex1.getZ());
        this.vertex2 = new Coordinate3D(vertex2.getX(), vertex2.getY(), vertex2.getZ());
    }

    public Coordinate3D getP1() {
        return vertex1;
    }

    public Coordinate3D getP2() {
        return vertex2;
    }
}
















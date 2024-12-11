// Main.java
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Main {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Пример 1: Геометрическая фигура
        Mat geometricImg = new Mat(400, 400, CvType.CV_8UC3, new Scalar(255, 255, 255));
        Imgproc.circle(geometricImg, new Point(150, 200), 100, new Scalar(0, 0, 255), -1);
        Imgproc.circle(geometricImg, new Point(250, 200), 100, new Scalar(255, 0, 0), -1);
        Imgproc.rectangle(geometricImg, new Point(100, 150), new Point(300, 250), new Scalar(0, 255, 0), -1);
        Imgcodecs.imwrite("result_ex1/original_geometric.png", geometricImg);

        for (int y = 0; y < geometricImg.rows(); y++) {
            for (int x = 0; x < geometricImg.cols(); x++) {
                if (ImageFloodFill.getPixel(geometricImg, x, y) == 0x00FF00) {
                    ImageFloodFill.applyFloodFill(geometricImg, x, y, 0xFFFF00, 0x00FF00); // зеленый -> жёлтый
                }
            }
        }

        Imgcodecs.imwrite("result_ex1/filled_geometric.png", geometricImg);

        // Пример 2: Пересекающиеся круги
        Mat overlapImg = new Mat(400, 400, CvType.CV_8UC3, new Scalar(255, 255, 255));
        Imgproc.circle(overlapImg, new Point(150, 200), 100, new Scalar(0, 0, 255), -1); // Красный
        Imgproc.circle(overlapImg, new Point(250, 200), 100, new Scalar(0, 255, 0), -1); // Зеленый
        Imgproc.circle(overlapImg, new Point(200, 150), 100, new Scalar(255, 0, 0), -1); // Синий
        Imgcodecs.imwrite("result_ex1/original_overlap.png", overlapImg);

        for (int y = 0; y < overlapImg.rows(); y++) {
            for (int x = 0; x < overlapImg.cols(); x++) {
                if (ImageFloodFill.getPixel(overlapImg, x, y) == 0xFF0000) { // Красный цвет
                    ImageFloodFill.applyFloodFill(overlapImg, x, y, 0xFFFF00, 0xFF0000); // Красный -> Желтый
                }
            }
        }

        Imgcodecs.imwrite("result_ex1/filled_overlap.png", overlapImg);

        // Пример 3: Квадрат с кружочками
        Mat squareImg = new Mat(400, 400, CvType.CV_8UC3, new Scalar(255, 255, 255));

        Imgproc.rectangle(squareImg, new Point(100, 100), new Point(300, 300), new Scalar(0, 0, 0), -1);

        for (int i = 120; i < 300; i += 40) {
            for (int j = 120; j < 300; j += 40) {
                Scalar color = (i + j) % 80 == 0 ? new Scalar(0, 0, 255) : new Scalar(255, 0, 0); // Чередуем красный и синий
                Imgproc.circle(squareImg, new Point(i, j), 15, color, -1);
            }
        }

        Imgcodecs.imwrite("result_ex1/original_square.png", squareImg);

        for (int y = 0; y < squareImg.rows(); y++) {
            for (int x = 0; x < squareImg.cols(); x++) {
                if (ImageFloodFill.getPixel(squareImg, x, y) == 0xFF0000) {
                    ImageFloodFill.applyFloodFill(squareImg, x, y, 0xFFFF00, 0xFF0000); // красный -> жёлтый
                }
            }
        }

        Imgcodecs.imwrite("result_ex1/filled_square.png", squareImg);
    }
}

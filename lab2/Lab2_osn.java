import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Lab2_osn {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Загрузка изображений
        Mat gojoImage = loadImage("C:\\Users\\1\\IdeaProjects\\lab_2_CG\\im_1.png");
        Mat cubeImage = toMonochrome(loadImage("C:\\Users\\1\\IdeaProjects\\lab_2_CG\\im_2.png"));

        // Применение дитеринга Флойда-Стейнберга
        Mat gojoDithered = applyFloydSteinbergDithering(gojoImage, 1);


        // Применение дитеринга Флойда-Стейнберга
        for (int i = 1; i <= 6; i++) {
            Mat curCubeImage = applyFloydSteinbergDithering(cubeImage, i);
            saveImage(curCubeImage, "C:\\Users\\1\\IdeaProjects\\lab_2_CG\\floyd_im_1_" + i + "bpp.png");

            Mat curGojoImage = applyFloydSteinbergDithering(gojoImage, i);
            saveImage(curGojoImage, "C:\\Users\\1\\IdeaProjects\\lab_2_CG\\floyd_grey_im_2_" + i + "bpp.png");
        }

    }

    // Загрузка изображения и возвращение его в виде матрицы
    private static Mat loadImage(String path) {
        return Imgcodecs.imread(path);
    }

    // Сохранение изображения на указанный путь
    private static void saveImage(Mat image, String path) {
        Imgcodecs.imwrite(path, image);
    }

    // Преобразование изображения в полутоновое
    private static Mat toMonochrome(Mat image) {
        // Проверка на то, что изображение загружено корректно
        if (image == null || !(image.type() == CvType.CV_8UC3 || image.type() == CvType.CV_8UC4)) {
            throw new IllegalArgumentException("На вход пришло не 3-х или 4-х канальное (RGB или RGBA) 8bpp изображение");
        }
        var res_image = new Mat(image.size(), CvType.CV_8UC1); // создается новое изображение такого же размера,
        // как и исходное, но с одним цветовым каналом (оттенки серого),
        // где каждый пиксель представлен 1 байтом.

        byte[] bgr = new byte[3];

        byte[]  gray = new byte[1];

        for (int x = 0; x < image.cols(); x++) {
            for (int y = 0; y < image.rows(); y++) {
                image.get(y, x, bgr);
                gray[0] = (byte) (0.3 * (bgr[2] & 0xFF) +
                        0.6 * (bgr[1] & 0xFF) +
                        0.1 * (bgr[0] & 0xFF));
                res_image.put(y, x, gray);
            }
        }
        return res_image;
    }

    // Получение равномерной палитры
    private static byte[] getUniformPalette(int n) {
        int colorsCount = (int) Math.pow(2, n);
        int step = 255 / (colorsCount - 1);

        var colors = new byte[colorsCount];
        for (int i = 0; i < colorsCount; i++) {
            colors[i] = (byte) (i * step);
        }
        return colors;
    }

    // Получение ближайшего цвета в палитре
    private static byte getClosestColorInPalette(byte value, byte[] palette) {
        byte res_image = palette[0];
        int minDiff = Math.abs((value & 0xFF) - (res_image & 0xFF));
        for (var color : palette) {
            int curDiff = Math.abs((color & 0xFF) - (value & 0xFF));
            if (curDiff < minDiff) {
                minDiff = curDiff;
                res_image = color;
            }
        }
        return res_image;
    }

    // Применение дитеринга Флойда-Стейнберга
    public static Mat applyFloydSteinbergDithering(Mat image, int n) {
        if (image == null) {
            throw new IllegalArgumentException("На вход null");
        }
        if (n > 8) {
            throw new IllegalArgumentException("n < 8");
        }
        if (n == 8) {
            return image;
        }

        var res_image = image.clone();
        var palette = getUniformPalette(n);

        var ditheringMatrix = new int[][]{
                {0, 0, 7},
                {3, 5, 1},
        };

        int colorChannelsCount = Math.min(image.channels(), 3);
        var pixel = new byte[image.channels()];
        var errs = new int[colorChannelsCount];

        for (int y = 0; y < image.rows(); y++) {
            for (int x = 0; x < image.cols(); x++) {
                res_image.get(y, x, pixel);
                for (int i = 0; i < colorChannelsCount; i++) {
                    int oldValue = pixel[i] & 0xFF;
                    pixel[i] = getClosestColorInPalette(pixel[i], palette);
                    res_image.put(y, x, pixel);
                    errs[i] = oldValue - (pixel[i] & 0xFF);
                }

                for (int i = 0; i < ditheringMatrix.length; i++) {
                    for (int j = 0; j < ditheringMatrix[i].length; j++) {
                        int y_ = y + i;
                        int x_ = x + j - 1;
                        if (y_ < image.rows() && x_ >= 0 && x_ < image.cols()) {
                            var curPixel = new byte[image.channels()];
                            res_image.get(y_, x_, curPixel);
                            for (int k = 0; k < colorChannelsCount; k++) {
                                curPixel[k] = (byte) Math.min(Math.max((curPixel[k] & 0xFF) + ((errs[k] * ditheringMatrix[i][j]) >> 4), 0), 255);
                            }
                            res_image.put(y_, x_, curPixel);
                        }
                    }
                }
            }
        }

        return res_image;
    }
}

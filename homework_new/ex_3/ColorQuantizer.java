import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.*;
import java.util.stream.Collectors;

public class ColorQuantizer {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // Класс для представления куба
    static class rgbBox {
        Scalar minColorValues; // минимальные значения RGB
        Scalar maxColorValues; // максимальные значения RGB
        List<Scalar> colors; // цвета в кубе

        rgbBox(List<Scalar> points) {
            if (points.isEmpty()) return;
            minColorValues = new Scalar(255, 255, 255);
            maxColorValues = new Scalar(0, 0, 0);
            colors = new ArrayList<>(points);

            for (Scalar color : colors) {
                for (int i = 0; i < 3; i++) {
                    minColorValues.val[i] = Math.min(minColorValues.val[i], color.val[i]);
                    maxColorValues.val[i] = Math.max(maxColorValues.val[i], color.val[i]);
                }
            }
        }


        // находит индекс компонента (R, G или B) с наибольшей длиной диапазона.
        int findLongestColorDimension() {
            double[] lengths = {
                    maxColorValues.val[0] - minColorValues.val[0],
                    maxColorValues.val[1] - minColorValues.val[1],
                    maxColorValues.val[2] - minColorValues.val[2]
            };
            if (lengths[0] >= lengths[1] && lengths[0] >= lengths[2]) {
                return 0;
            } else if (lengths[1] >= lengths[0] && lengths[1] >= lengths[2]) {
                return 1;
            } else {
                return 2;
            }
        }


        // вычисляет медианное значение компонента с наибольшей длиной
        double calculateMedianValue(int longestDimensionIndex) {
            List<Double> values = colors.stream()
                    .map(color -> color.val[longestDimensionIndex])
                    .sorted()
                    .collect(Collectors.toList());
            return values.get(values.size() / 2);
        }
    }

    static void medianCut(List<Scalar> colors, List<Scalar> palette, int numColors) {
        Queue<rgbBox> colorBox = new LinkedList<>();
        colorBox.add(new rgbBox(colors));

        while (colorBox.size() < numColors) {
            rgbBox current = colorBox.poll();
            if (current == null) break;

            int longestDimensionIndex = current.findLongestColorDimension();
            double medianValue = current.calculateMedianValue(longestDimensionIndex);

            List<Scalar> leftColorGroup = new ArrayList<>();
            List<Scalar> rightColorGroup = new ArrayList<>();

            for (Scalar color : current.colors) {
                if (color.val[longestDimensionIndex] < medianValue) {
                    leftColorGroup.add(color);
                } else {
                    rightColorGroup.add(color);
                }
            }

            if (!leftColorGroup.isEmpty()) {
                colorBox.add(new rgbBox(leftColorGroup));
            }
            if (!rightColorGroup.isEmpty()) {
                colorBox.add(new rgbBox(rightColorGroup));
            }
        }

        for (rgbBox cuboid : colorBox) {
            Scalar averageColor = new Scalar(0, 0, 0);
            for (Scalar color : cuboid.colors) {
                for (int i = 0; i < 3; i++) {
                    averageColor.val[i] += color.val[i];
                }
            }
            for (int i = 0; i < 3; i++) {
                averageColor.val[i] /= cuboid.colors.size();
            }
            palette.add(averageColor);
        }
    }

    static Mat quantizeColorsInImage(Mat img, int numColors) {
        List<Scalar> colors = new ArrayList<>();
        for (int y = 0; y < img.rows(); y++) {
            for (int x = 0; x < img.cols(); x++) {
                double[] pixel = img.get(y, x);
                colors.add(new Scalar(pixel));
            }
        }

        List<Scalar> palette = new ArrayList<>();
        medianCut(colors, palette, numColors);

        Mat outputImg = new Mat(img.size(), img.type());
        for (int y = 0; y < img.rows(); y++) {
            for (int x = 0; x < img.cols(); x++) {
                double[] pixel = img.get(y, x);
                Scalar color = new Scalar(pixel);

                Scalar closestColor = null;
                double closestDistance = Double.MAX_VALUE;
                for (Scalar palColor : palette) {
                    double distance = Math.sqrt(
                            Math.pow(color.val[0] - palColor.val[0], 2) +
                                    Math.pow(color.val[1] - palColor.val[1], 2) +
                                    Math.pow(color.val[2] - palColor.val[2], 2)
                    );
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestColor = palColor;
                    }
                }
                // Используем только три компонента (RGB)
                outputImg.put(y, x, new double[] { closestColor.val[0], closestColor.val[1], closestColor.val[2] });
            }
        }
        return outputImg;
    }

    public static void main(String[] args) {
        String inputPath = "result_ex3/input_image.png";
        String outputPath = "result_ex3/output_image.png";
        int numColors = 600;

        Mat img = Imgcodecs.imread(inputPath);
        Mat outputImg = quantizeColorsInImage(img, numColors);
        Imgcodecs.imwrite(outputPath, outputImg);
    }
}
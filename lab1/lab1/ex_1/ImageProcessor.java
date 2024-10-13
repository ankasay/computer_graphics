import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessor {

    public static BufferedImage readImage(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Файл не может быть прочитан");
        }
        return ImageIO.read(file);
    }

    public static void writeImageToFile(BufferedImage image, String outputFileName) throws IOException {
        File file = new File(outputFileName);
        if (!ImageIO.write(image, "png", file)) {
            throw new IOException("Ошибка записи PNG файла");
        }
    }

    public static BufferedImage createHalftoneCircle(BufferedImage input) {
        int width = input.getWidth();
        int height = input.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(centerX, centerY);

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int dx = x - centerX;
                int dy = y - centerY;
                int distanceSquared = dx * dx + dy * dy;

                if (distanceSquared <= radius * radius) {
                    int rgb = input.getRGB(x, y);
                    int gray = (int) (0.3 * ((rgb >> 16) & 0xFF) + 0.6* ((rgb >> 8) & 0xFF) + 0.1 * (rgb & 0xFF));
                    int grayRgb = (gray << 16) | (gray << 8) | gray | (0xFF << 24);
                    output.setRGB(x, y, grayRgb);
                } else {
                    output.setRGB(x, y, 0x00000000); // Transparent
                }
            }
        }

        return output;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Использование: java ImageProcessor <input_file> [<output_file>]");
            return;
        }

        String inputFileName = args[0];
        String outputFileName = args.length > 1 ? args[1] : "output.png";

        try {
            BufferedImage inputImage = readImage(inputFileName);
            BufferedImage outputImage = createHalftoneCircle(inputImage);
            writeImageToFile(outputImage, outputFileName);
            System.out.println("Изображение успешно обработано и сохранено в " + outputFileName);
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
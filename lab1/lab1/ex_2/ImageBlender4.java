import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageBlender4 {

    public enum BlendMode {
        NORMAL,
        MULTIPLY,
        SCREEN,
        DARKEN,
        LIGHTEN,
        DIFFERENCE,
        COLOR_DODGE,
        COLOR_BURN,
        SOFT_LIGHT
    }

    public static BufferedImage blendImages(BufferedImage image1, BufferedImage image2, BufferedImage alphaImage1, BufferedImage alphaImage2, BlendMode mode) {
        int width = image1.getWidth();
        int height = image1.getHeight();

        if (width != image2.getWidth() || height != image2.getHeight() || width != alphaImage1.getWidth() || height != alphaImage1.getHeight() || width != alphaImage2.getWidth() || height != alphaImage2.getHeight()) {
            throw new IllegalArgumentException("Изображения имеют разный размер");
        }

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Чтение значений альфа-каналов
                int alpha1 = alphaImage1.getRGB(x, y) & 0xFF;
                int alpha2 = alphaImage2.getRGB(x, y) & 0xFF;

                // Нормализация альфа-каналов
                double a1 = alpha1 / 255.0;
                double a2 = alpha2 / 255.0;

                // Чтение цветовых компонентов изображений
                double Cb_r = ((image1.getRGB(x, y) >> 16) & 0xFF) / 255.0;
                double Cb_g = ((image1.getRGB(x, y) >> 8) & 0xFF) / 255.0;
                double Cb_b = (image1.getRGB(x, y) & 0xFF) / 255.0;

                double Cs_r = ((image2.getRGB(x, y) >> 16) & 0xFF) / 255.0;
                double Cs_g = ((image2.getRGB(x, y) >> 8) & 0xFF) / 255.0;
                double Cs_b = (image2.getRGB(x, y) & 0xFF) / 255.0;

                double B_r = 0, B_g = 0, B_b = 0;

                // Применение режима смешивания
                switch (mode) {
                    case NORMAL:
                        B_r = Cs_r;
                        B_g = Cs_g;
                        B_b = Cs_b;
                        break;
                    case MULTIPLY:
                        B_r = Cb_r * Cs_r;
                        B_g = Cb_g * Cs_g;
                        B_b = Cb_b * Cs_b;
                        break;
                    case SCREEN:
                        B_r = 1 - ((1 - Cb_r) * (1 - Cs_r));
                        B_g = 1 - ((1 - Cb_g) * (1 - Cs_g));
                        B_b = 1 - ((1 - Cb_b) * (1 - Cs_b));
                        break;
                    case DARKEN:
                        B_r = Math.min(Cb_r, Cs_r);
                        B_g = Math.min(Cb_g, Cs_g);
                        B_b = Math.min(Cb_b, Cs_b);
                        break;
                    case LIGHTEN:
                        B_r = Math.max(Cb_r, Cs_r);
                        B_g = Math.max(Cb_g, Cs_g);
                        B_b = Math.max(Cb_b, Cs_b);
                        break;
                    case DIFFERENCE:
                        B_r = Math.abs(Cb_r - Cs_r);
                        B_g = Math.abs(Cb_g - Cs_g);
                        B_b = Math.abs(Cb_b - Cs_b);
                        break;
                    case COLOR_DODGE:
                        B_r = Cs_r < 1 ? Math.min(1, Cb_r / (1 - Cs_r)) : 1;
                        B_g = Cs_g < 1 ? Math.min(1, Cb_g / (1 - Cs_g)) : 1;
                        B_b = Cs_b < 1 ? Math.min(1, Cb_b / (1 - Cs_b)) : 1;
                        break;
                    case COLOR_BURN:
                        B_r = Cs_r > 0 ? 1 - Math.min(1, (1 - Cb_r) / Cs_r) : 0;
                        B_g = Cs_g > 0 ? 1 - Math.min(1, (1 - Cb_g) / Cs_g) : 0;
                        B_b = Cs_b > 0 ? 1 - Math.min(1, (1 - Cb_b) / Cs_b) : 0;
                        break;
                    case SOFT_LIGHT:
                        B_r = softLightBlend(Cb_r, Cs_r);
                        B_g = softLightBlend(Cb_g, Cs_g);
                        B_b = softLightBlend(Cb_b, Cs_b);
                        break;
                }

                // Применение смешивания альфа-каналов
                double blendedR = (a1 * Cb_r + a2 * Cs_r) / (a1 + a2);
                double blendedG = (a1 * Cb_g + a2 * Cs_g) / (a1 + a2);
                double blendedB = (a1 * Cb_b + a2 * Cs_b) / (a1 + a2);

                // Конвертация обратно в [0, 255]
                int blendedColor = (255 << 24) | ((int) (blendedR * 255) << 16) | ((int) (blendedG * 255) << 8) | (int) (blendedB * 255);
                outputImage.setRGB(x, y, blendedColor);
            }
        }

        return outputImage;
    }

    private static double softLightBlend(double Cb, double Cs) {
        if (Cs < 0.5) {
            return Cb - (1 - 2 * Cs) * Cb * (1 - Cb);
        } else {
            double D = (Cb <= 0.25) ? ((16 * Cb - 12) * Cb + 4) * Cb : Math.sqrt(Cb);
            return Math.min(1, Math.max(0, Cb + (2 * Cs - 1) * (D - Cb) * 255));
        }
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java ImageBlender <input_file1> <input_file2> <alpha_file1> <alpha_file2> [<output_file>]");
            return;
        }

        String inputFileName1 = args[0];
        String inputFileName2 = args[1];
        String alphaFileName1 = args[2];
        String alphaFileName2 = args[3];
        String outputFileName = args.length > 4 ? args[4] : "output.png";

        try {
            BufferedImage image1 = ImageIO.read(new File(inputFileName1));
            BufferedImage image2 = ImageIO.read(new File(inputFileName2));
            BufferedImage alphaImage1 = ImageIO.read(new File(alphaFileName1));
            BufferedImage alphaImage2 = ImageIO.read(new File(alphaFileName2));

            BufferedImage blendedImage = blendImages(image1, image2, alphaImage1, alphaImage2, BlendMode.SOFT_LIGHT);

            ImageIO.write(blendedImage, "png", new File(outputFileName));
            System.out.println("Изображения успешно смешаны. Результат сохранен в " + outputFileName);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
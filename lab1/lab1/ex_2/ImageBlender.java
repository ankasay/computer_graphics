import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageBlender {

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

    public static BufferedImage blendImages(BufferedImage image1, BufferedImage image2, BufferedImage alphaImage, BlendMode mode) {
        int width = image1.getWidth();
        int height = image1.getHeight();

        if (width != image2.getWidth() || height != image2.getHeight() || width != alphaImage.getWidth() || height != alphaImage.getHeight()) {
            throw new IllegalArgumentException("Изображения имеют разный размер");
        }

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = alphaImage.getRGB(x, y) & 0x000000ff;

                int Cb_r = (image1.getRGB(x, y) >> 16) & 0xFF;
                int Cb_g = (image1.getRGB(x, y) >> 8) & 0xFF;
                int Cb_b = image1.getRGB(x, y) & 0xFF;

                int Cs_r = (image2.getRGB(x, y) >> 16) & 0xFF;
                int Cs_g = (image2.getRGB(x, y) >> 8) & 0xFF;
                int Cs_b = image2.getRGB(x, y) & 0xFF;

                int B_r = 0, B_g = 0, B_b = 0;

                switch (mode) {
                    case NORMAL:
                        B_r = Cs_r;
                        B_g = Cs_g;
                        B_b = Cs_b;
                        break;
                    case MULTIPLY:
                        B_r = Cb_r * Cs_r / 255;
                        B_g = Cb_g * Cs_g / 255;
                        B_b = Cb_b * Cs_b / 255;
                        break;
                    case SCREEN:
                        B_r = 255 - ((255 - Cb_r) * (255 - Cs_r) / 255);
                        B_g = 255 - ((255 - Cb_g) * (255 - Cs_g) / 255);
                        B_b = 255 - ((255 - Cb_b) * (255 - Cs_b) / 255);
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
                        B_r = Cs_r < 255 ? Math.min(255, 255 * Cb_r / (255 - Cs_r)) : 255;
                        B_g = Cs_g < 255 ? Math.min(255, 255 * Cb_g / (255 - Cs_g)) : 255;
                        B_b = Cs_b < 255 ? Math.min(255, 255 * Cb_b / (255 - Cs_b)) : 255;
                        break;
                    case COLOR_BURN:
                        B_r = Cs_r > 0 ? 255 - Math.min(255, 255 * (255 - Cb_r) / Cs_r) : 0;
                        B_g = Cs_g > 0 ? 255 - Math.min(255, 255 * (255 - Cb_g) / Cs_g) : 0;
                        B_b = Cs_b > 0 ? 255 - Math.min(255, 255 * (255 - Cb_b) / Cs_b) : 0;
                        break;
                    case SOFT_LIGHT:
                        B_r = softLightBlend(Cb_r, Cs_r);
                        B_g = softLightBlend(Cb_g, Cs_g);
                        B_b = softLightBlend(Cb_b, Cs_b);
                        break;
                }

                int blendedR = (int) ((1 - alpha / 255.0) * Cb_r + (alpha / 255.0) * B_r);
                int blendedG = (int) ((1 - alpha / 255.0) * Cb_g + (alpha / 255.0) * B_g);
                int blendedB = (int) ((1 - alpha / 255.0) * Cb_b + (alpha / 255.0) * B_b);

                int blendedColor = (255 << 24) | (blendedR << 16) | (blendedG << 8) | blendedB;
                outputImage.setRGB(x, y, blendedColor);
            }
        }

        return outputImage;
    }

    private static int softLightBlend(int Cb, int Cs) {
        if (Cs < 128) {
            return Cb - (int)((1 - 2 * Cs / 255.0) * Cb * (1 - Cb / 255.0));
        } else {
            double D = (Cb <= 64) ? ((16 * Cb - 12) * Cb + 4) * Cb : Math.sqrt(Cb);
            return (int) (Cb + (2 * Cs / 255.0 - 1) * (D - Cb / 255.0) * 255);
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java ImageBlender <input_file1> <input_file2> <alpha_file> [<output_file>]");
            return;
        }

        String inputFileName1 = args[0];
        String inputFileName2 = args[1];
        String alphaFileName = args[2];
        String outputFileName = args.length > 3 ? args[3] : "output.png";

        try {
            BufferedImage image1 = ImageIO.read(new File(inputFileName1));
            BufferedImage image2 = ImageIO.read(new File(inputFileName2));
            BufferedImage alphaImage = ImageIO.read(new File(alphaFileName));

            BufferedImage blendedImage = blendImages(image1, image2, alphaImage, BlendMode.NORMAL);

            ImageIO.write(blendedImage, "png", new File(outputFileName));
            System.out.println("Изображения смешаны и результат сохранен в  " + outputFileName);
        } catch (IOException e) {
            System.err.println("Error : " + e.getMessage());
        }
    }
}
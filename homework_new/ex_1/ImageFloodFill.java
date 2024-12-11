import org.opencv.core.*;
import java.util.Stack;

public class ImageFloodFill {

    public static int getPixel(Mat img, int x, int y) {
        if (x >= 0 && x < img.cols() && y >= 0 && y < img.rows()) {
            double[] color = img.get(y, x);
            return (int) color[0] | ((int) color[1] << 8) | ((int) color[2] << 16);
        }
        return -1;
    }

    public static void setPixel(Mat img, int x, int y, int r, int g, int b) {
        if (x >= 0 && x < img.cols() && y >= 0 && y < img.rows()) {
            img.put(y, x, new double[]{b, g, r});
        }
    }

    public static void applyFloodFill(Mat img, int x, int y, int newColor, int oldColor) {
        if (oldColor == newColor) return;

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point point = stack.pop();
            x = (int) point.x;
            y = (int) point.y;

            int xLeft = x;
            int xRight = x;

            // Заполняем пиксели влево
            while (xLeft >= 0 && getPixel(img, xLeft, y) == oldColor) {
                setPixel(img, xLeft, y, newColor & 0xFF, (newColor >> 8) & 0xFF, (newColor >> 16) & 0xFF);
                xLeft--;
            }

            // Заполняем пиксели вправо
            while (xRight < img.cols() && getPixel(img, xRight, y) == oldColor) {
                setPixel(img, xRight, y, newColor & 0xFF, (newColor >> 8) & 0xFF, (newColor >> 16) & 0xFF);
                xRight++;
            }

            // Проверка строки выше
            if (y > 0) {
                for (int i = xLeft + 1; i < xRight; i++) {
                    if (getPixel(img, i, y - 1) == oldColor) {
                        stack.push(new Point(i, y - 1));
                    }
                }
            }

            // Проверка строки ниже
            if (y < img.rows() - 1) {
                for (int i = xLeft + 1; i < xRight; i++) {
                    if (getPixel(img, i, y + 1) == oldColor) {
                        stack.push(new Point(i, y + 1));
                    }
                }
            }
        }
    }
}

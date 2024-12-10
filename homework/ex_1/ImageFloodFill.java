import org.opencv.core.Mat;
import java.util.Stack;

public class ImageFloodFill {
    public static void applyFloodFill(Mat img, int startX, int startY, int oldColor, int newColor) {
        // Если старый и новый цвет одинаковые, то не делаем ничего
        if (oldColor == newColor) return;

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY}); // Добавляем затравочную точку

        // Получаем цвет затравочной точки
        int initialColor = getPixel(img, startX, startY);
        if (initialColor != oldColor) return; // Если цвет затравочной точки не совпадает с oldColor, выходим

        while (!stack.isEmpty()) {
            int[] point = stack.pop();
            int cx = point[0];
            int cy = point[1];

            // Проверяем текущий пиксель. Если его цвет не соответствует старому, пропускаем его.
            if (getPixel(img, cx, cy) != oldColor) continue;

            // Заливаем текущий пиксель
            setPixel(img, cx, cy, newColor);

            // Добавляем все соседние пиксели (8 направлений)
            if (cx > 0) stack.push(new int[]{cx - 1, cy});     // Лево
            if (cx < img.cols() - 1) stack.push(new int[]{cx + 1, cy}); // Право
            if (cy > 0) stack.push(new int[]{cx, cy - 1});     // Верх
            if (cy < img.rows() - 1) stack.push(new int[]{cx, cy + 1}); // Низ

            if (cx > 0 && cy > 0) stack.push(new int[]{cx - 1, cy - 1});     // Лево-верх
            if (cx > 0 && cy < img.rows() - 1) stack.push(new int[]{cx - 1, cy + 1}); // Лево-низ
            if (cx < img.cols() - 1 && cy > 0) stack.push(new int[]{cx + 1, cy - 1}); // Право-верх
            if (cx < img.cols() - 1 && cy < img.rows() - 1) stack.push(new int[]{cx + 1, cy + 1}); // Право-низ
        }
    }

    // Получить цвет пикселя в формате RGB
    public static int getPixel(Mat img, int x, int y) {
        if (x >= 0 && x < img.cols() && y >= 0 && y < img.rows()) {
            double[] color = img.get(y, x);
            return ((int) color[2] << 16) | ((int) color[1] << 8) | (int) color[0];
        }
        return -1;
    }

    // Установить цвет пикселя
    public static void setPixel(Mat img, int x, int y, int color) {
        if (x >= 0 && x < img.cols() && y >= 0 && y < img.rows()) {
            int b = color & 0xFF;
            int g = (color >> 8) & 0xFF;
            int r = (color >> 16) & 0xFF;
            img.put(y, x, new double[]{b, g, r});
        }
    }
}

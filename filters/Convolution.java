import java.awt.*;
import java.awt.image.BufferedImage;
public class Convolution {


    public static BufferedImage convolution (BufferedImage givenImage, double[][] filter, boolean hasDivisor) {

        for (int y = 1; y + 1 < givenImage.getHeight(); y++) {
            for (int x = 1; x + 1 < givenImage.getWidth(); x++) {
                Color tempColor = getFilteredValue(givenImage, y, x, filter,hasDivisor);
                givenImage.setRGB(x-1, y-1, tempColor.getRGB());

            }
        }
        return givenImage;
    }

    private static Color getFilteredValue(final BufferedImage givenImage, int y, int x, double[][] filter,boolean hasDivisor) throws IllegalArgumentException {
        int r = 0, g = 0, b = 0 ,a=0;
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                r += ((filter[1 + j][1 + k] * (new Color(givenImage.getRGB(x + k, y + j))).getRed()));
                g += ((filter[1 + j][1 + k] * (new Color(givenImage.getRGB(x + k, y + j))).getGreen()));
                b += ((filter[1 + j][1 + k] * (new Color(givenImage.getRGB(x + k, y + j))).getBlue()));
                a += ((filter[1 + j][1 + k] * (new Color(givenImage.getRGB(x + k, y + j))).getAlpha()));

            }
        }
        if(hasDivisor){
            r = r / sum(filter);
            g = g /sum(filter);
            b = b / sum(filter);
            a = a / sum(filter);

        }

        Color q = new Color(fixOutOfRangeRGBValues(r),fixOutOfRangeRGBValues(g),fixOutOfRangeRGBValues(b),fixOutOfRangeRGBValues(a));
        return q;
    }
    public static BufferedImage sobelFilter (BufferedImage image){
        int x = image.getWidth();
        int y = image.getHeight();

        int maxGval = 0;
        int[][] edgeColors = new int[x][y];
        int maxGradient = -1;

        for (int i = 1; i < x - 1; i++) {
            for (int j = 1; j < y - 1; j++) {

                int val00 = getGrayScale(image.getRGB(i - 1, j - 1));
                int val01 = getGrayScale(image.getRGB(i - 1, j));
                int val02 = getGrayScale(image.getRGB(i - 1, j + 1));

                int val10 = getGrayScale(image.getRGB(i, j - 1));
                int val11 = getGrayScale(image.getRGB(i, j));
                int val12 = getGrayScale(image.getRGB(i, j + 1));

                int val20 = getGrayScale(image.getRGB(i + 1, j - 1));
                int val21 = getGrayScale(image.getRGB(i + 1, j));
                int val22 = getGrayScale(image.getRGB(i + 1, j + 1));

                int gx =  ((1 * val00) + (0 * val01) + (-1 * val02))
                        + ((2 * val10) + (0 * val11) + (-2 * val12))
                        + ((1 * val20) + (0 * val21) + (-1 * val22));

                int gy =  ((-1 * val00) + (-2 * val01) + (-1 * val02))
                        + ((0 * val10) + (0 * val11) + (0 * val12))
                        + ((1 * val20) + (2 * val21) + (1 * val22));

                double gval = Math.sqrt((gx * gx) + (gy * gy));
                int g = (int) gval;

                if(maxGradient < g) {
                    maxGradient = g;
                }

                edgeColors[i][j] = g;
            }
        }

        double scale = 255.0 / maxGradient;

        for (int i = 1; i < x - 1; i++) {
            for (int j = 1; j < y - 1; j++) {
                int edgeColor = edgeColors[i][j];
                edgeColor = (int)(edgeColor * scale);
                edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

                image.setRGB(i, j, edgeColor);
            }
        }

        return image;

    }

    private static int sum(double[][] filter) {
        int sum = 0;
        for (int y = 0; y < filter.length; y++) {
            for (int x = 0; x < filter[y].length; x++) {
                sum += filter[y][x];
            }
        }
        return sum;
    }

    public static int fixOutOfRangeRGBValues(double value) {
        if (value < 0) return 0;
        if (value > 255) return 255;
        else return (int) value;
    }

    public static int  getGrayScale(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;
        int gray = (int)(0.2126 * r + 0.7152 * g + 0.0722 * b);
        return gray;
    }
}



import com.sun.media.sound.FFT;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Random;

public class App {

    public static void main(String[] args) throws IOException {

        BufferedImage img = ImageIO.read(new File("sourceImages/lena.png"));
        ImageIO.write(saltPepperNoise(img, 10), "png", new File("output/salt&PepperNoised.png"));
        ImageIO.write(fixSaltPepper(ImageIO.read(new File("output/salt&PepperNoised.png"))), "png", new File("output/salt&fix.png"));
        ImageIO.write(gaussianNoise(img, 10), "png", new File("output/gaussianNoised.png"));
        img = ImageIO.read(new File("sourceImages/lena.png"));
        ImageIO.write(fastFourierOnImage(img), "png", new File("output/fft.png"));
        //twoDfft(img);

    }

    //salt pepper
    public static BufferedImage saltPepperNoise(BufferedImage img, double prob) {
        int w = img.getWidth(), h = img.getHeight();
        img = makeGray(img);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Color c = new Color(img.getRGB(j, i));
                int red = (int) (c.getRed());
                int green = (int) (c.getGreen());
                int blue = (int) (c.getBlue());
                Color newColor = new Color(red, green, blue);
                int rgb = range(newColor.getRGB(), prob);
                img.setRGB(j, i, rgb);
            }
        }
        System.out.println("Salt and pepper noise with probability of " + prob + " applied successfully");
        return img;
    }

    public static int range(int n, double prob) {
        if (prob == 0) return 255;
        double res = ((100 * prob) / 10);

        int[] array = new int[(int) res];
        array[0] = 1;
        array[1] = 255;


        for (int i = 2; i <= res - 2; i++) {
            array[i] = n;
        }
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    public static BufferedImage makeGray(BufferedImage img) {

        for (int x = 0; x < img.getWidth(); ++x) {
            for (int y = 0; y < img.getHeight(); ++y) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                // Normalize and gamma correct:
                double rr = Math.pow(r / 255.0, 2.2);
                double gg = Math.pow(g / 255.0, 2.2);
                double bb = Math.pow(b / 255.0, 2.2);
                // Calculate luminance:


                double lum = 0.2126 * rr + 0.7152 * gg + 0.0722 * bb;
                // Gamma compand and rescale to byte range:
                int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / 2.2));
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;

                Color q = new Color(gray);
                img.setRGB(x, y, q.getRGB());
            }
        }
        return img;
    }

    //Fix salt pepper
    public static BufferedImage medianFilter(BufferedImage img) {
        Color[] pixel = new Color[9];
        int[] R = new int[9];
        int[] B = new int[9];
        int[] G = new int[9];
        for (int i = 1; i < img.getWidth() - 1; i++)
            for (int j = 1; j < img.getHeight() - 1; j++) {
                pixel[0] = new Color(img.getRGB(i - 1, j - 1));
                pixel[1] = new Color(img.getRGB(i - 1, j));
                pixel[2] = new Color(img.getRGB(i - 1, j + 1));
                pixel[3] = new Color(img.getRGB(i, j + 1));
                pixel[4] = new Color(img.getRGB(i + 1, j + 1));
                pixel[5] = new Color(img.getRGB(i + 1, j));
                pixel[6] = new Color(img.getRGB(i + 1, j - 1));
                pixel[7] = new Color(img.getRGB(i, j - 1));
                pixel[8] = new Color(img.getRGB(i, j));
                for (int k = 0; k < 9; k++) {
                    R[k] = pixel[k].getRed();
                    B[k] = pixel[k].getBlue();
                    G[k] = pixel[k].getGreen();
                }
                Arrays.sort(R);
                Arrays.sort(G);
                Arrays.sort(B);
                img.setRGB(i, j, new Color(R[4], B[4], G[4]).getRGB());
            }
        return img;
    }

    public static BufferedImage fixSaltPepper(BufferedImage img) {
        System.out.println("Salt and pepper noise fixed with median filter");
        return medianFilter(img);
    }

    //gausisan noise
    public static BufferedImage gaussianNoise(BufferedImage originalImage, double sigma) {
        double mean = 0;

        double variance = sigma * sigma;


        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage filteredImage = new BufferedImage(width, height, originalImage.getType());

        double a = 0.0;
        double b = 0.0;


        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                while (a == 0.0)
                    a = Math.random();
                b = Math.random();

                double x = Math.sqrt(-2 * Math.log(a)) * Math.cos(2 * Math.PI * b);
                double noise = mean + Math.sqrt(variance) * x;


                int gray = new Color(originalImage.getRGB(i, j)).getRed();
                int alpha = new Color(originalImage.getRGB(i, j)).getAlpha();

                double color = gray + noise;
                if (color > 255)
                    color = 255;
                if (color < 0)
                    color = 0;

                int newColor = (int) Math.round(color);
                Color c = new Color(newColor, newColor, newColor, alpha);

                filteredImage.setRGB(i, j, c.getRGB());

            }
        }


        System.out.println("Gaussian noise with sigma " + sigma + " applied successfully");
        return filteredImage;
    }

    //fft




    public static BufferedImage RGBtoImage(int[][][] m) {
        int w = m.length;
        int h = m[0].length;
        BufferedImage output_img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                Color color = new Color(m[i][j][0], m[i][j][1], m[i][j][2]);
                output_img.setRGB(i, j, color.getRGB());
            }
        }
        return output_img;
    }

    // One dimensional Fast Fourier Transform, online open source
    public static Complex[] fft1(Complex[] array) {
        int n = array.length;

        // base case
        if (n == 1) return new Complex[]{array[0]};

        // radix 2 Cooley-Tukey FFT

        if (n % 2 != 0) {
            throw new RuntimeException("n is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = array[2 * k];
        }
        Complex[] q = fft1(even);

        // fft of odd terms
        Complex[] odd = even;  // reuse the array
        for (int k = 0; k < n / 2; k++) {
            odd[k] = array[2 * k + 1];
        }
        Complex[] r = fft1(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + n / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

    //Two dimensional FFT
    public static Complex[][][] fft2(Complex[][][] m){
        int w = m.length;
        int h = m[0].length;

        Complex[][][] output = new Complex[w][h][3];
        Complex [] row = new Complex [w];
        Complex [] col = new Complex [h];
        for (int k = 0; k <3; k++){
            for (int j = 0; j < h; j++){
                for (int i = 0; i < w; i++){
                    row[i] = m[i][j][k];
                }
                row = fft1(row);
                for (int i = 0; i < w; i++)
                    output[i][j][k] = row[i];
            }
            for (int i = 0; i < w; i++){
                for (int j = 0; j < h; j++){
                    col[j] = output[i][j][k];
                }
                col = fft1(col);
                for (int j = 0; j < h; j++)
                    output[i][j][k] = col[j];
            }
        }
        return output;
    }
    public static BufferedImage fastFourierOnImage(BufferedImage image){
        int w0 = image.getWidth();
        int h0 = image.getHeight();
        int [][][] colors = shift_to_center(imgToMatrix(image, w0, h0));
        Complex [][][] f = getComplexMatrix(colors);
        Complex [][][] F = fft2(f);


        return complexToImg(F, true);
    }


    // inverse FFT, ifft online open source
    public static Complex[] ifft1(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];

        // take conjugate
        for (int i = 0; i < n; i++) {
            y[i] = x[i].conjugate();
        }
        // compute forward FFT
        y = fft1(y);
        // take conjugate again
        for (int i = 0; i < n; i++) {
            y[i] = y[i].conjugate();
        }
        // divide by n
        for (int i = 0; i < n; i++) {
            y[i] = y[i].scale(1.0 / n);
        }
        return y;
    }

    //two dimensional inverse fast Fourier Transform
    public static BufferedImage ifft2(BufferedImage img) {
        Complex[][][] m = getComplexMatrix(imgToMatrix(img,img.getWidth(),img.getHeight()));
        int w = m.length;
        int h = m[0].length;
        Complex[][][] output = new Complex[w][h][3];
        Complex[] row = new Complex[w];
        Complex[] col = new Complex[h];
        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    row[i] = m[i][j][k];
                }
                row = ifft1(row);
                for (int i = 0; i < w; i++)
                    output[i][j][k] = row[i];
            }
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    col[j] = output[i][j][k];
                }
                col = ifft1(col);
                for (int j = 0; j < h; j++)
                    output[i][j][k] = col[j];
            }
        }
        return complexToImg(output,true);
    }


    // convert image to matrix, also padding 0 if size is not power of 2 integer
    public static int[][][] imgToMatrix(BufferedImage img, int w, int h) {
        int w1;
        int h1;
        int[][][] output;
        if ((w & (w - 1)) != 0 || (h & (h - 1)) != 0) { // check if it is w or h is power of 2 int
            if ((w & (w - 1)) != 0) {
                w1 = (int) Math.pow(2, Math.ceil(Math.log(w) / Math.log(2)));
            } else {
                w1 = w;
            }
            if ((h & (h - 1)) != 0) {
                h1 = (int) Math.pow(2, Math.ceil(Math.log(h) / Math.log(2)));
            } else {
                h1 = h;
            }

            output = new int[w1][h1][3];
            for (int k = 0; k < 3; k++) {
                for (int j = 0; j < h1; j++) {
                    for (int i = 0; i < w1; i++) {
                        output[i][j][k] = 0;
                    }
                }
            }
        } else {
            output = new int[w][h][3];
        }

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int pixel = img.getRGB(i, j);
                Color color = new Color(pixel);
                output[i][j][0] = color.getRed();
                output[i][j][1] = color.getGreen();
                output[i][j][2] = color.getBlue();
            }
        }

        return output;
    }

    //The high frequency values will be shifted to the center in spectrum after applying "shift_to_center"
    public static int[][][] shift_to_center(int[][][] matrix) {
        int w = matrix.length;
        int h = matrix[0].length;

        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    if ((i + j) % 2 != 0) {
                        matrix[i][j][k] = ~matrix[i][j][k];
                    }
                }
            }
        }
        return matrix;
    }

    //convert from int matrix to complex number matrix
    public static Complex[][][] getComplexMatrix(int[][][] matrix) {
        int w = matrix.length;
        int h = matrix[0].length;
        Complex[][][] output = new Complex[w][h][3];
        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    output[i][j][k] = new Complex((double) (matrix[i][j][k]), (double) (0));
                }
            }
        }
        return output;
    }

    public static int max(int a, int b) {
        if (a >= b) {
            return a;
        } else {
            return b;
        }
    }

    public static int min(int a, int b) {
        if (a < b) {
            return a;
        } else {
            return b;
        }
    }

    public static int scale(int a) { //keep image values within 0 to 255
        if (a < 0) {
            return 0;
        } else if (a > 255) {
            return 255;
        } else {
            return a;
        }
    }


    // convert complex numbers matrix into image, spectrum = True means we want spectrum from this matrix,
    // otherwise it will be converted into regular image
    public static BufferedImage complexToImg(Complex[][][] matrix, Boolean spectrum) {

        int w = matrix.length;
        int h = matrix[0].length;
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[][][] m = new int[w][h][3];
        int max = 0;
        int min = 255;
        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    if (spectrum) {
                        m[i][j][k] = (int) Math.log(matrix[i][j][k].abs() + 1);
                    } else {
                        m[i][j][k] = (int) matrix[i][j][k].abs();
                        m[i][j][k] = scale(m[i][j][k]);
                    }
                    max = max(max, m[i][j][k]);
                }
            }
        }

        if (spectrum) {
            int C = 255 / max;    //cofficient C used to scale the image
            for (int k = 0; k < 3; k++) {
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        m[i][j][k] = (int) (m[i][j][k] * C);
                    }
                }
            }
        }

        output = RGBtoImage(m);
        return output;
    }




}



import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FFT {


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

    // One dimensional Fast Fourier Transform
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
    public static void fastFourierOnImage(BufferedImage image) throws IOException

    {
        int w0 = image.getWidth();
        int h0 = image.getHeight();
        int [][][] colors = shift_to_center(imgToMatrix(image, w0, h0));
        Complex [][][] f = getComplexMatrix(colors);
        Complex [][][] F = fft2(f);
        BufferedImage fe = complexToImg(F,true); //filtered image
        System.out.println("FFT applied succesfully");
        ImageIO.write(fe, "png", new File( "output/fftOutputs/fftImage.png")); //save filtered image to current folder


        int w = F.length;
        int h = F[0].length;
        Complex [][] H = new Complex [w][h];
        int r0 = 55;
        H = ButterWorth(w,h,r0,15,true);
        Complex [][][] m_filt = FtimesH(F, H); //apply filter on image F


        BufferedImage fftImg = complexToImg(m_filt,true); //filtered spectrum
        Complex [][][] filted = ifft2(m_filt);

        if (w != w0 || h != h0) filted = crop(filted, w0, h0); //if size changed, then resize it

        BufferedImage fftImge = complexToImg(filted,false); //filtered image
        System.out.println("Butterworth filter applied to spectrum successfully!");
        ImageIO.write(fftImg, "png", new File(  "output/fftOutputs/fftFiltImg.jpg")); //save filtered spectrum image to current folder
        System.out.println("The fixed image created successfully");
        ImageIO.write(fftImge, "png", new File( "output/fftOutputs/filtImage.jpg")); //save filtered image to current folder


    }

    public static BufferedImage InversefastFourierOnImage(BufferedImage image){
        int w0 = image.getWidth();
        int h0 = image.getHeight();
        int [][][] colors = shift_to_center(imgToMatrix(image, w0, h0));
        Complex [][][] f = getComplexMatrix(colors);
        Complex [][][] filted = ifft2(f);
        return complexToImg(filted,true);

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
    //w h is the original size of image, Crop the padded image to original size
    public static Complex [][][] crop(Complex [][][] F, int w, int h){
        Complex [][][] output = new Complex [w][h][3];
        for (int k = 0; k <3; k++){
            for (int j = 0; j < h; j++){
                for (int i = 0; i < w; i++){
                    output[i][j][k] = F[i][j][k];

                }
            }
        }
        return output;
    }
    public static Complex [][][] FtimesH(Complex[][][] F, Complex[][] H){
        int w = F.length;
        int h = F[0].length;
        Complex [][][] result = new Complex [w][h][3];
        for (int k = 0; k <3; k++){
            for (int j = 0; j < h; j++){
                for (int i = 0; i < w; i++){
                    result[i][j][k] = F[i][j][k].times(H[i][j]);

                }
            }
        }
        return result;
    }
    //two dimensional inverse fast Fourier Transform
    public static Complex[][][] ifft2 (Complex[][][] m){
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
                row = ifft1(row);
                for (int i = 0; i < w; i++)
                    output[i][j][k] = row[i];
            }
            for (int i = 0; i < w; i++){
                for (int j = 0; j < h; j++){
                    col[j] = output[i][j][k];
                }
                col = ifft1(col);
                for (int j = 0; j < h; j++)
                    output[i][j][k] = col[j];
            }
        }
        return output;
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


    //Circular filter, ro is cutoff frequency, LowPass = False means highpass
    public static Complex [][] circularFilter(int w, int h, int r0, Boolean LowPass){
        Complex [][] filter = new Complex [w][h];
        int centerX = w/2;
        int centerY = h/2;

        for (int j = 0; j < h; j++){
            for (int i = 0; i < w; i++){
                int r = (int)Math.hypot(i-centerX,j-centerY);
                if (LowPass){
                    if (r <= r0){
                        filter[i][j] = new Complex(1,0);
                    }
                    else{
                        filter[i][j] = new Complex(0,0);
                    }
                }
                else{
                    if (r > r0){
                        filter[i][j] = new Complex(1,0);
                    }
                    else{
                        filter[i][j] = new Complex(0,0);
                    }
                }
            }
        }
        return filter;
    }
    public static Complex [][] ButterWorth(int w, int h, int r0, int p, Boolean LowPass){
        Complex [][] H = new Complex [w][h];
        int centerX = w/2;
        int centerY = h/2;
        for (int j = 0; j < h; j++){
            for (int i = 0; i < w; i++){
                double r = Math.hypot(i-centerX,j-centerY);
                double z = 0;
                if (LowPass){
                    z = r/(double)r0;
                }
                else{
                    z = (double)r0/r;
                }
                double re = 1/(1+Math.pow(z, 2*p));
                H[i][j] = new Complex(re,0);
            }
        }
        return H;
    }
}


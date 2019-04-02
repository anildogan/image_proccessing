
import java.awt.image.BufferedImage;

public class filters {
    private static final double[][] BOX_FILTER = {{1, 1, 0}, {1, 1, 1}, {1, 1, 1}};
    private static final double[][] SHARPEN_FILTER = {{0,-1,0}, {-1,5,-1}, {0,-1,0}};
    private static final double[][] EMBOSS_FILTER= {{-2,-1,0},{-1,1,1},{0,1,2}};
    private static final double[][] HIGHPASS_FILTER= {{0,-1,0},{-1,8,-1},{0,-1,0}};



    public BufferedImage boxFilter(BufferedImage img) {
        Convolution c = new Convolution();
        System.out.println("Box filtered successfully!");
        return c.convolution(img,BOX_FILTER,true);

    }

    public BufferedImage sharpenFilter(BufferedImage img){
        Convolution c = new Convolution();
        System.out.println("Sharpen filtered successfully!");
        return c.convolution(img,SHARPEN_FILTER,false);
    }

    public BufferedImage embossFilter(BufferedImage img) {
        Convolution c = new Convolution();
        System.out.println("Emboss filtered successfully!");
        return c.convolution(img,EMBOSS_FILTER,true);

    }

    public BufferedImage sobelFilter(BufferedImage img){
        Convolution c = new Convolution();
        System.out.println("Sobel filtered successfully!");
        return c.sobelFilter(img);
    }

    public BufferedImage highpassFilter(BufferedImage img){
        Convolution c = new Convolution();
        System.out.println("Highpass filtered successfully!");
        return c.convolution(img,HIGHPASS_FILTER,true);
    }

    public BufferedImage gaussianFilter(BufferedImage img,double sigma){
        Convolution c = new Convolution();
        System.out.println("Gaussian filter with sigma "+sigma+" filtered successfully!");
        return  c.convolution(img,getGaussianMatrice(sigma,3),false);
    }

    public static double gaussianDiscrete2D(double theta, int x, int y){
        double g = 0;
        for(double ySubPixel = y - 0.5; ySubPixel < y + 0.55; ySubPixel += 0.1){
            for(double xSubPixel = x - 0.5; xSubPixel < x + 0.55; xSubPixel += 0.1){
                g = g + ((1/(2*Math.PI*theta*theta)) *
                        Math.pow(Math.E,-(xSubPixel*xSubPixel+ySubPixel*ySubPixel)/
                                (2*theta*theta)));
            }
        }
        g = g/121;
        return g;
    }
    public static double [][] getGaussianMatrice(double theta, int size){
        double [][] kernel = new double [size][size];
        for(int j=0;j<size;++j){
            for(int i=0;i<size;++i){
                kernel[i][j]=gaussianDiscrete2D(theta,i-(size/2),j-(size/2));
            }
        }

        double sum = 0;
        for(int j=0;j<size;++j){
            for(int i=0;i<size;++i){
                sum = sum + kernel[i][j];

            }
        }

        return kernel;
    }


}
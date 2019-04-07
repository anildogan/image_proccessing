
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

        BufferedImage img = ImageIO.read(new File("sourceImages/sampleImage.png"));
        ImageIO.write(saltPepperNoise(img,10),"png",new File("output/salt&PepperNoised.png"));
        ImageIO.write(medianFilter(ImageIO.read(new File("output/salt&PepperNoised.png"))),"png",new File("output/salt&fix.png"));
        //twoDfft(img);
    }

    //salt pepper
    public static BufferedImage saltPepperNoise(BufferedImage img,double prob){
        int w = img.getWidth(),h=img.getHeight();
        img = makeGray(img);
        for(int i=0;i<h;i++){
            for(int j=0;j<w;j++){
                Color c = new Color(img.getRGB(j, i));
                int red = (int)(c.getRed() );
                int green = (int)(c.getGreen() );
                int blue = (int)(c.getBlue() );
                Color newColor = new Color(red,green,blue);
                int rgb = range(newColor.getRGB(),prob);
                img.setRGB(j,i,rgb);
            }
        }
        return img;
    }
    public static int range(int n, double prob) {
        if (prob == 0) return 255;
        double res = ((100 * prob)/10);

        int[]array = new int[(int)res];
        array[0]= 1;
        array[1]=255;



        for (int i = 2 ; i <= res - 2; i++)
        {
            array[i] = n;
        }
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }
    public static BufferedImage makeGray(BufferedImage img)
    {

        for (int x = 0; x < img.getWidth(); ++x){
            for (int y = 0; y < img.getHeight(); ++y)
            {
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
            }}
            return img;
    }


    //Fix salt pepper
    public static BufferedImage medianFilter(BufferedImage img){
        Color[] pixel=new Color[9];
        int[] R=new int[9];
        int[] B=new int[9];
        int[] G=new int[9];
        for(int i=1;i<img.getWidth()-1;i++)
            for(int j=1;j<img.getHeight()-1;j++)
            {
                pixel[0]=new Color(img.getRGB(i-1,j-1));
                pixel[1]=new Color(img.getRGB(i-1,j));
                pixel[2]=new Color(img.getRGB(i-1,j+1));
                pixel[3]=new Color(img.getRGB(i,j+1));
                pixel[4]=new Color(img.getRGB(i+1,j+1));
                pixel[5]=new Color(img.getRGB(i+1,j));
                pixel[6]=new Color(img.getRGB(i+1,j-1));
                pixel[7]=new Color(img.getRGB(i,j-1));
                pixel[8]=new Color(img.getRGB(i,j));
                for(int k=0;k<9;k++){
                    R[k]=pixel[k].getRed();
                    B[k]=pixel[k].getBlue();
                    G[k]=pixel[k].getGreen();
                }
                Arrays.sort(R);
                Arrays.sort(G);
                Arrays.sort(B);
                img.setRGB(i,j,new Color(R[4],B[4],G[4]).getRGB());
            }
            return img;
    }
    public static BufferedImage fixSaltPepper(BufferedImage img){
        return medianFilter(img);
    }

    //gausisan noise
    public static BufferedImage gaussianNoise(BufferedImage originalImage,double sigma) {
        double mean=0;

        double variance = sigma*sigma;


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

                double x = Math.sqrt(-2*Math.log(a)) * Math.cos(2*Math.PI*b);
                double noise = mean + Math.sqrt(variance) * x;


                int gray = new Color(originalImage.getRGB(i, j)).getRed();
                int alpha = new Color(originalImage.getRGB(i, j)).getAlpha();

                double color = gray + noise;
                if (color > 255)
                    color = 255;
                if (color < 0)
                    color = 0;

                int newColor = (int) Math.round(color);
                Color c = new Color(newColor,newColor,newColor,alpha);

                filteredImage.setRGB(i, j, c.getRGB());

            }//j
        }//i


        return filteredImage;
    }
    /*public static void twoDfft(BufferedImage inputData)

    {
        int height = inputData.getHeight();
        int width = inputData.getWidth();
        BufferedImage realOut = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage amplitudeOut = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
        // Two outer loops iterate on output data.
        for (int yWave = 0; yWave < height; yWave++)
        {
            for (int xWave = 0; xWave < width; xWave++)
            {
                // Two inner loops iterate on input data.
                for (int ySpace = 0; ySpace < height; ySpace++)
                {
                    for (int xSpace = 0; xSpace < width; xSpace++)
                    {
                        // Compute real, imag, and ampltude.
                        realOut.setRGB(yWave,xWave,(realOut.getRGB(yWave,xWave)+(inputData.getRGB(ySpace,xSpace) * Math
                                .cos(2
                                        * Math.PI
                                        * ((1.0 * xWave * xSpace / width) + (1.0
                                        * yWave * ySpace / height))))
                                / Math.sqrt(width * height)));

                        imagOut[yWave][xWave] -= (inputData.getRGB(ySpace,xSpace) * Math

                                .sin(2
                                        * Math.PI

                                        * ((1.0 * xWave * xSpace / width) + (1.0

                                        * yWave * ySpace / height))))

                                / Math.sqrt(width * height);

                        amplitudeOut[yWave][xWave] = Math

                                .sqrt(realOut[yWave][xWave]

                                        * realOut[yWave][xWave]

                                        + imagOut[yWave][xWave]

                                        * imagOut[yWave][xWave]);

                    }

                    System.out.println(realOut[yWave][xWave] + " + "

                            + imagOut[yWave][xWave] + " i");

                }

            }*/

        }



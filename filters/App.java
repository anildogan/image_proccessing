import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class App {
    public static void main(String args[]) throws IOException{
        //Resize operations
        BufferedImage resizeImg = ImageIO.read(new File("sourceImages/sampleImage.png"));
        ImageIO.write(neighbour_resize(resizeImg,1920,1080), "png", new File("output/sampleImage_nn.png"));
        resizeImg = ImageIO.read(new File("sourceImages/sampleImage.png"));
        ImageIO.write(bilinear_resize(resizeImg,1920,1080), "png", new File("output/sampleImage_bilinear.png"));

        //Various Filters
        filters f = new filters(); //Call filters class
        BufferedImage filterImage = ImageIO.read(new File("sourceImages/man.png"));
        ImageIO.write(f.boxFilter(filterImage),"png",new File("output/boxFiltered.png"));
        ImageIO.write(f.embossFilter(filterImage),"png",new File("output/embossFiltered.png"));
        ImageIO.write(f.highpassFilter(filterImage),"png",new File("output/highpassFiltered.png"));
        ImageIO.write(f.sharpenFilter(filterImage),"png",new File("output/sharpenFiltered.png"));
        ImageIO.write(f.sobelFilter(filterImage),"png",new File("output/sobelFiltered.png"));
            //Gaussian Filter
        BufferedImage einstein = ImageIO.read(new File("sourceImages/einstein.png"));
        BufferedImage monroe = ImageIO.read(new File("sourceImages/monroe.png"));
        BufferedImage output = f.gaussianFilter(f.embossFilter(einstein),1.5);//Aplly gaussian filter with sigma
        ImageIO.write(add_img(monroe,output),"png",new File("output/mergedImage.png"));//merge 2 image and save

    }

    //NEAREST NEIGHBOUR INTERPOLATION
    public static BufferedImage neighbour_resize(BufferedImage img,int w,int h){
        BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        int h2 = img.getHeight();
        int w2 = img.getWidth();
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                float y = j * ((float) h2 / (float) h);
                float x = i * ((float) w2 / (float) w);
                newImage.setRGB(i, j, img.getRGB((int) x, (int) y));
            }
        }
        System.out.println("Image neihgbour resized to "+w+" x "+h+" Successfully !");
        return newImage;


    }

    //BILINEAR INTERPOLATION
    public static BufferedImage bilinear_resize(BufferedImage self, int newWidth, int newHeight) {

        BufferedImage newImage = new BufferedImage(newWidth, newHeight, self.getType());
        for (int x = 0; x < newWidth; ++x) {
            for (int y = 0; y < newHeight; ++y) {
                float gx = ((float) x) / newWidth * (self.getWidth() - 1);
                float gy = ((float) y) / newHeight * (self.getHeight() - 1);
                int gxi = (int) gx;
                int gyi = (int) gy;
                int rgb = 0;
                int c00 = self.getRGB(gxi, gyi);
                int c10 = self.getRGB(gxi + 1, gyi);
                int c01 = self.getRGB(gxi, gyi + 1);
                int c11 = self.getRGB(gxi + 1, gyi + 1);
                for (int i = 0; i <= 2; ++i) {
                    float b00 = get(c00, i);
                    float b10 = get(c10, i);
                    float b01 = get(c01, i);
                    float b11 = get(c11, i);
                    int ble = ((int) blerp(b00, b10, b01, b11, gx - gxi, gy - gyi)) << (8 * i);
                    rgb = rgb | ble;
                }
                newImage.setRGB(x, y, rgb);
            }
        }
        System.out.println("Image bilinear resized to "+newWidth+" x "+newHeight+" Successfully !");

        return newImage;
    }
    private static int get(int self, int n) {
        return (self >> (n * 8)) & 0xFF;
    }

    private static float lerp(float s, float e, float t) {
        return s + (e - s) * t;
    }

    private static float blerp(final Float c00, float c10, float c01, float c11, float tx, float ty) {
        return lerp(lerp(c00, c10, tx), lerp(c01, c11, tx), ty);
    }



    public static BufferedImage add_img(BufferedImage image,BufferedImage deleteImg){
        BufferedImage returnImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int w = 0;w<image.getWidth();w++){
            for (int h = 0;h<image.getHeight();h++){
                int r = ((new Color (image.getRGB(w,h)).getRed())+(new Color (deleteImg.getRGB(w,h)).getRed()));
                int g = ((new Color (image.getRGB(w,h)).getGreen())+(new Color (deleteImg.getRGB(w,h)).getGreen()));
                int b = ((new Color (image.getRGB(w,h)).getBlue())+(new Color (deleteImg.getRGB(w,h)).getBlue()));
                int a = ((new Color (image.getRGB(w,h)).getAlpha())+(new Color (deleteImg.getRGB(w,h)).getAlpha()));

                Color q = new Color(fixOutOfRangeRGBValues(r),fixOutOfRangeRGBValues(g),fixOutOfRangeRGBValues(b),fixOutOfRangeRGBValues(a));

                returnImg.setRGB(w,h,q.getRGB());

            }

        }
        return returnImg;

    }
    public static BufferedImage sub_img(BufferedImage image,BufferedImage deleteImg){
        BufferedImage returnImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int w = 0;w<image.getWidth();w++){
            for (int h = 0;h<image.getHeight();h++){
                int r = ((new Color (image.getRGB(w,h)).getRed())-(new Color (deleteImg.getRGB(w,h)).getRed()));
                int g = ((new Color (image.getRGB(w,h)).getGreen())-(new Color (deleteImg.getRGB(w,h)).getGreen()));
                int b = ((new Color (image.getRGB(w,h)).getBlue())-(new Color (deleteImg.getRGB(w,h)).getBlue()));
                int a = ((new Color (image.getRGB(w,h)).getAlpha())-(new Color (deleteImg.getRGB(w,h)).getAlpha()));

                Color q = new Color(fixOutOfRangeRGBValues(r),fixOutOfRangeRGBValues(g),fixOutOfRangeRGBValues(b),fixOutOfRangeRGBValues(a));

                returnImg.setRGB(w,h,q.getRGB());

            }

        }
        return returnImg;

    }
    public static int fixOutOfRangeRGBValues(double value) {
        if (value < 0) return 0;
        if (value > 255) return 255;
        else return (int) value;
    }



}

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.io.IOException;

import javax.imageio.ImageIO;

/*
Mikkel Danielsen, s183913
Frederik Koefoed, s195463
Muhammad Talha, s195475
Volkan Isik, s180103
Lasse Strunge, s19548
Mark Mortensen, s174881
 */

public class Generator {

    //Constants
    static int scale = 1; //.txt data only tested with scale = 1
    int columns = 7;
    int padding = 50 * scale;
    int cardHorizontalSpacing = 20 * scale;
    int cardVerticalSpacing = 90 * scale;
    static int bgWidth = 1920 * scale;
    static int bgHeight = 1400 * scale;
    int bondaryBoxWidth = 32 * scale;
    int bondaryBoxHeight = 80 * scale;
    double cardRatio = 1.4;
    int cardWidth = (bgWidth - (columns - 1) * cardHorizontalSpacing - 2 * padding) / columns;
    int cardHeight = (int) (cardWidth * cardRatio);
    String cardRoot = "cards/";
    String bgRoot = "surfaces/";
    int maxCardsInCol = 7;
    double backCardChance = 1.0;
    static String cardTypes[] = { //Could be enum
            "2c", "2d", "2h", "2s",
            "3c", "3d", "3h", "3s",
            "4c", "4d", "4h", "4s",
            "5c", "5d", "5h", "5s",
            "6c", "6d", "6h", "6s",
            "7c", "7d", "7h", "7s",
            "8c", "8d", "8h", "8s",
            "9c", "9d", "9h", "9s",
            "10c", "10d", "10h", "10s",
            "Ac", "Ad", "Ah", "As",
            "Jc", "Jd", "Jh", "Js",
            "Kc", "Kd", "Kh", "Ks",
            "Qc", "Qd", "Qh", "Qs",
            "backside" };
    String nameOfBackside = "backside";
    int typesOfCards = cardTypes.length;
    int numbersOfBrightness = 20;
    String cardFileType = ".png";
    String bgFileType = ".jpg";
    String outputImgType = ".png";
    String outputTxtType = ".txt";
    String outputPath = "/Users/mikkeldanielsen/Desktop/";
    int numberOfSurfaces = 177;
    int cardEdgeTop = 12 * scale;
    int cardEdgeSide = 8 * scale;
    int maxDegrees = 10;

    Random rand = new Random();
    FileWriter txtWriter;

    int loop = 1;

    public static void main(String[] args) throws IOException {
        new Generator();
    }

    public Generator() {
        for (int i = 0; i < loop; i++) {
            generateRandomBoard(i);
            new Tester(i);
        }
    }

    private void generateRandomBoard(int name) {
        BufferedImage combinedImage;

        try{
            //Load random background
            BufferedImage bg = ImageIO.read(new File(bgRoot + "s" + rand.nextInt(numberOfSurfaces) + bgFileType));
            bg = resize(bg, bgWidth, bgHeight);

            //Create .txt for location data
            File txt = new File(outputPath + name +"_data" + outputTxtType);
            txtWriter = new FileWriter(outputPath + name + "_data" + outputTxtType);

            //Combine images
            combinedImage = new BufferedImage(bgWidth, bgHeight, BufferedImage.TYPE_INT_ARGB); //Canvas
            Graphics2D g = combinedImage.createGraphics();
            g.drawImage(bg, 0,0,null); //Background

            //Top row
            for (int i = 0; i < columns; i++) {
                int x = padding + i * (cardHorizontalSpacing + cardWidth);
                int y = padding;

                if(i != 2) {
                    MyImage myImage = getRandomImage();

                    //Draw image
                    g.drawImage(myImage.image, x, y, null);

                    //Write location data
                    if(!myImage.type.equals(nameOfBackside)){ //If Card is not backside

                        //Add relative position and offset for edge of card (approximate but seems okay when degrees < 20)
                        myImage.x1 += x + cardEdgeSide;
                        myImage.y1 += y + cardEdgeTop;
                        myImage.x2 += x - cardEdgeSide;
                        myImage.y2 += y - cardEdgeTop;

                        //Save bondary box
                        writeBoundaryBoxUpper(myImage.type, myImage.x1, myImage.y1, myImage.radians);
                        writeBoundaryBoxLower(myImage.type, myImage.x2, myImage.y2, myImage.radians);
                    } else { //Else card is backside
                        writeBoundaryBoxBackside(myImage.type, x, y, myImage.radians);
                    }
                }
            }

            //Piles
            for (int i = 0; i < columns; i++) { //Column
                int js = rand.nextInt(maxCardsInCol) + 1; //1-7
                for (int j = 0; j < js; j++) { //Cards
                    MyImage myImage = getRandomImage();
                    int x = padding + i * (cardHorizontalSpacing + cardWidth);
                    int y = padding + cardHeight + cardVerticalSpacing * (1 + j);

                    //Draw image in position
                    g.drawImage(myImage.image, x, y,null);

                    //Write location data
                    if(myImage.type.equals(nameOfBackside)) { //If card is backside
                        writeBoundaryBoxBackside(myImage.type, x, y, myImage.radians);
                    } else { // Else card is front

                        //Add relative position and offset for edge of card (approximate but seems okay when degrees < 20)
                        myImage.x1 += x + cardEdgeSide;
                        myImage.y1 += y + cardEdgeTop;
                        myImage.x2 += x - cardEdgeSide;
                        myImage.y2 += y - cardEdgeTop;

                        writeBoundaryBoxUpper(myImage.type, myImage.x1, myImage.y1, myImage.radians);

                        if(j == js - 1) //If card is at the front
                            writeBoundaryBoxLower(myImage.type, myImage.x2, myImage.y2, myImage.radians);
                    }
                }
            }

            //Add noise
            BufferedImage noise = getRandomNoise();
            float opacity = rand.nextFloat();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.drawImage(noise, 0,0,null);

            //Wrap up
            g.dispose();
            txtWriter.close();

            //Export image
            ImageIO.write(combinedImage, "PNG", new File(outputPath + name + "_img" + outputImgType));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Adds cardWidth and cardVerticalPadding to x and y
    private void writeBoundaryBoxBackside(String type, double x, double y, double radians) {

        //Calculate positions
        double length, height;
        height = Math.sin(Math.abs(radians)) * cardWidth + Math.cos(Math.abs(radians)) * cardVerticalSpacing;
        if(radians < 0) length = Math.tan(Math.abs(radians)) * height + Math.cos(Math.abs(radians)) * cardWidth;
        else {
            length = Math.cos(radians) * cardWidth + Math.tan(radians) * height;
            x += Math.sin(radians) * cardHeight - Math.tan(radians) * height;
        }

        try {
            txtWriter.write(indexInCardTypes(type) + " ");
            txtWriter.write((x / bgWidth) + " ");
            txtWriter.write((y / bgHeight) + " ");
            txtWriter.write(((double) length / bgWidth) + " ");
            txtWriter.write(((double) height / bgHeight) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Adds bondaryBoxWidth and bondaryBoxHeight to x and y (upper left corer)
    private void writeBoundaryBoxUpper(String type, double x, double y, double radians) {

        //Rotated image need bigger box
        int canvasWidth = (int) (Math.cos(Math.abs(radians)) * bondaryBoxWidth + Math.sin(Math.abs(radians)) * bondaryBoxHeight);
        int canvasHeight = (int) (Math.sin(Math.abs(radians)) * bondaryBoxWidth + Math.cos(Math.abs(radians)) * bondaryBoxHeight);

        //Calculate new x and y according to radians
        if(radians > 0) x -= Math.sin(radians) * bondaryBoxHeight;
        else y += Math.sin(radians) * bondaryBoxWidth;

        try {
            txtWriter.write(indexInCardTypes(type) + " ");
            txtWriter.write((x / bgWidth) + " ");
            txtWriter.write((y / bgHeight) + " ");
            txtWriter.write(((double) canvasWidth / bgWidth) + " ");
            txtWriter.write(((double) canvasHeight / bgHeight) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Adds bondaryBoxWidth and bondaryBoxHeight to x and y (lower right corner)
    private void writeBoundaryBoxLower(String type, double x, double y, double radians) {
        x += Math.sin(radians) * bondaryBoxHeight - Math.cos(radians) * bondaryBoxWidth + 2; //+2 since our cards had a defect
        y -= Math.sin(radians) * bondaryBoxWidth + Math.cos(radians) * bondaryBoxHeight;
        writeBoundaryBoxUpper(type, x, y, radians);
    }

    private int indexInCardTypes(String str) {
        for (int i = 0; i < cardTypes.length; i++) {
            if(cardTypes[i].equals(str)) return i;
        }
        return -1;
    }

    private MyImage getRandomImage() {
        MyImage out = new MyImage();

        //Load random image
        try {
            //Increase change of getting a backside card
            if(rand.nextDouble() < backCardChance) out.type = nameOfBackside;
            else out.type = cardTypes[rand.nextInt(typesOfCards - 1)]; //-1 to exclude backside

            int brightness = rand.nextInt(numbersOfBrightness);
            String path = cardRoot + out.type + brightness + cardFileType;
            BufferedImage image = ImageIO.read(new File(path));
            image = resize(image, cardWidth, cardHeight);

            //Apply small random rotation
            int degrees = (int) rand.nextGaussian() * 3; //Draw from normal distribution
            if(degrees > maxDegrees || degrees < -maxDegrees) degrees = 0;
            out.radians = Math.toRadians(degrees);

            //Rotated image need bigger box
            out.canvasWidth = (Math.cos(Math.abs(out.radians)) * cardWidth + Math.sin(Math.abs(out.radians)) * cardHeight);
            out.canvasHeight = (Math.sin(Math.abs(out.radians)) * cardWidth + Math.cos(Math.abs(out.radians)) * cardHeight);

            out.image = new BufferedImage((int) out.canvasWidth, (int) out.canvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = out.image.createGraphics();

            //Calculate offset where card starts and ends
            if(out.radians > 0) {
                out.x1 = Math.sin(out.radians) * cardHeight;
                out.y1 = 0;
                out.x2 = Math.cos(out.radians) * cardWidth;
                out.y2 = out.canvasHeight;
                g2d.translate(out.x1, 0);
            } else {
                out.x1 = 0;
                out.y1 = Math.sin(Math.abs(out.radians)) * cardWidth;
                out.x2 = out.canvasWidth;
                out.y2 = Math.cos(Math.abs(out.radians)) * cardHeight;
                g2d.translate(0, out.y1);
            }

            g2d.rotate(out.radians);
            g2d.drawImage(image, 0, 0, null);

            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //src: https://www.geeksforgeeks.org/image-processing-java-set-7-creating-random-pixel-image/
    private BufferedImage getRandomNoise() {
        BufferedImage img = new BufferedImage(bgWidth, bgHeight, BufferedImage.TYPE_INT_ARGB);

        // create random values pixel by pixel
        for (int y = 0; y < bgHeight; y++) {
            for (int x = 0; x < bgWidth; x++)
            {
                int a = (int)(Math.random()*256); //generating
                int r = (int)(Math.random()*256); //values
                int g = (int)(Math.random()*256); //less than
                int b = (int)(Math.random()*256); //256

                int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel

                img.setRGB(x, y, p);
            }
        }
        return img;
    }

    //src: https://stackoverflow.com/questions/9417356/bufferedimage-resize
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    private class MyImage {
        public BufferedImage image;
        public String type;
        public double radians;
        public double canvasWidth;
        public double canvasHeight;
        public double x1, y1, x2, y2;
    }
}

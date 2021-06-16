import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Tester {

    String root = "/Users/mikkeldanielsen/Desktop/";
    String outputPath = "/Users/mikkeldanielsen/Desktop/";
    String inputPng = "_img.png";
    String inputTxt = "_data.txt";
    int strokeWidth = 2;

    FileReader txtReader;

    public static void main(String[] args) {
        new Tester(0);
    }

    public Tester(int name) {

        try {
            //Load image
            BufferedImage canvas = ImageIO.read(new File(root + name + inputPng));

            //Load location data
            txtReader = new FileReader(root + name + inputTxt);
            ArrayList<String> txt = new ArrayList<>();
            BufferedReader br = new BufferedReader(txtReader);
            String line;

            while((line = br.readLine()) != null) { txt.add(line); }
            txtReader.close();

            //Draw location data
            Graphics2D g = canvas.createGraphics();
            g.setColor(Color.GREEN);
            g.setFont(new Font(null, Font.PLAIN, 30));
            g.setStroke(new BasicStroke(strokeWidth));

            for (int i = 0; i < txt.size(); i++) { //Loop through all entries in location data
                RectData data = lineToRectData(txt.get(i).split(" "));

                if(data.type.equals("backside")) {
                    data.x = (int) (data.x + (data.width * 0.5) / 2);
                    data.height = (int) (data.height * 0.5);
                    data.width = (int) (data.width * 0.5);
                }

                g.drawRect(data.x, data.y, data.width, data.height);
                g.drawString(data.type, data.x + strokeWidth, data.y - strokeWidth);
            }

            g.dispose();

            //Export result
            ImageIO.write(canvas, "PNG", new File(outputPath + name + "_test.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Extract data from line
    private RectData lineToRectData(String[] line) {
        RectData out = new RectData();

        out.type = indexToType(Integer.parseInt(line[0]));
        out.x = (int) (Generator.bgWidth * Double.parseDouble(line[1]));
        out.y = (int) (Generator.bgHeight * Double.parseDouble(line[2]));
        out.width = (int) (Generator.bgWidth * Double.parseDouble(line[3]));
        out.height = (int) (Generator.bgHeight * Double.parseDouble(line[4]));

        return out;
    }

    private class RectData {
        public String type;
        public int x;
        public int y;
        public int width;
        public int height;
    }

    private String indexToType(int index) {
        return Generator.cardTypes[index];
    }

}

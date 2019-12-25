package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PPMImage {

    private static String FORMAT = "P3";
    private int width;
    private int height;
    private int maxColorValue = 255;
    private List<List<RGB>> rgbData;

    public PPMImage() {
    }

    public PPMImage(String filename) {
        readImage(filename);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<List<RGB>> getRgbData() {
        return rgbData;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setMaxColorValue(int maxColorValue) {
        this.maxColorValue = maxColorValue;
    }

    public void setRgbData(List<List<RGB>> rgbData) {
        this.rgbData = rgbData;
    }

    private void readImage(String filename) {
        File file = new File(getClass().getClassLoader().getResource(filename).getFile());
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert scanner != null;
        if (scanner.hasNextLine()) {
            //read header
            FORMAT = scanner.nextLine();
            scanner.nextLine();
            String[] dimensions = scanner.nextLine().split(" ");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
            maxColorValue = Integer.parseInt(scanner.nextLine());

            //read rgb values
            rgbData = new ArrayList<List<RGB>>();
            for (int i = 0; i < height; i++) {
                List<RGB> line = new ArrayList<RGB>();
                for (int j = 0; j < width; j++) {
                    int r = Integer.parseInt(scanner.nextLine());
                    int g = Integer.parseInt(scanner.nextLine());
                    int b = Integer.parseInt(scanner.nextLine());
                    line.add(new RGB(r, g, b));
                }
                rgbData.add(line);
            }
        }
    }

    public void writeToFile() {
        File file = new File("decodedImage.ppm");
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));

            //write header
            writer.println(FORMAT);
            writer.println("# This is a comment");
            writer.println(width + " " + height);
            writer.println(maxColorValue);

            //write rgb values
            rgbData.forEach(line -> line.forEach(pixel -> {
                writer.println(pixel.getR());
                writer.println(pixel.getG());
                writer.println(pixel.getB());
            }));

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

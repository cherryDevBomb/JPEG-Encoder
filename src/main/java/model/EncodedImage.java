package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class EncodedImage {

    List<List<Block>> y;
    List<List<Block>> u;
    List<List<Block>> v;

    public EncodedImage(List<List<Block>> y, List<List<Block>> u, List<List<Block>> v) {
        this.y = y;
        this.u = u;
        this.v = v;
    }

    public List<List<Block>> getY() {
        return y;
    }

    public List<List<Block>> getU() {
        return u;
    }

    public List<List<Block>> getV() {
        return v;
    }

    public void printEncoded() {
        File file = new File("encodedImage.txt");
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(file));

            writer.println("Y blocks:");
            y.forEach(line -> line.forEach(block -> writer.println(block.toString())));

            writer.println("U blocks:");
            u.forEach(line -> line.forEach(block -> writer.println(block.toString())));

            writer.println("V blocks:");
            v.forEach(line -> line.forEach(block -> writer.println(block.toString())));

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

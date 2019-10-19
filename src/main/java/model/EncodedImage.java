package model;

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

    public void printImage() {
        System.out.println("Y blocks:");
        y.forEach(line -> line.forEach(Block::printBlock));

        System.out.println("U blocks:");
        u.forEach(line -> line.forEach(Block::printBlock));

        System.out.println("V blocks:");
        v.forEach(line -> line.forEach(Block::printBlock));
    }

}

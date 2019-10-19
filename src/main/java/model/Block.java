package model;

import java.util.List;


public class Block {

    private List<List<Double>> values;

    public Block(List<List<Double>> values) {
        this.values = values;
    }

    public List<List<Double>> getValues() {
        return values;
    }

    public int getSize() {
        return values.size();
    }

    public void printBlock() {
        values.forEach(line -> line.forEach((v -> System.out.println(v + " "))));
        System.out.println("\n");
    }
}

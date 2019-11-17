package model;

import java.util.List;


public class Block {

    private List<List<Integer>> values;

    public Block(List<List<Integer>> values) {
        this.values = values;
    }

    public List<List<Integer>> getValues() {
        return values;
    }

    public void setValues(List<List<Integer>> values) {
        this.values = values;
    }

    public int getSize() {
        return values.size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (List<Integer> line : values) {
            for (Integer value : line) {
                builder.append(value).append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}

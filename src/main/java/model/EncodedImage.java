package model;

import java.util.List;

public class EncodedImage {

    private List<Integer> encodedBytes;
    private int nrBlocksW;
    private int getNrBlocksH;

    public EncodedImage(List<Integer> encodedBytes, int nrBlocksW, int getNrBlocksH) {
        this.encodedBytes = encodedBytes;
        this.nrBlocksW = nrBlocksW;
        this.getNrBlocksH = getNrBlocksH;
    }

    public List<Integer> getEncodedBytes() {
        return encodedBytes;
    }

    public int getNrBlocksW() {
        return nrBlocksW;
    }

    public int getGetNrBlocksH() {
        return getNrBlocksH;
    }
}

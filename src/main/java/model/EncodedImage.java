package model;

import java.util.List;

public class EncodedImage {

    private List<Byte> encodedBytes;
    private int nrBlocksW;
    private int getNrBlocksH;

    public EncodedImage(List<Byte> encodedBytes, int nrBlocksW, int getNrBlocksH) {
        this.encodedBytes = encodedBytes;
        this.nrBlocksW = nrBlocksW;
        this.getNrBlocksH = getNrBlocksH;
    }

    public List<Byte> getEncodedBytes() {
        return encodedBytes;
    }

    public int getNrBlocksW() {
        return nrBlocksW;
    }

    public int getGetNrBlocksH() {
        return getNrBlocksH;
    }
}

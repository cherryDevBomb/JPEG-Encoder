package codec;

import model.*;

import java.util.ArrayList;
import java.util.List;


public class Decoder {

    public PPMImage decode(EncodedImage image) {

        List<List<Block>> yBlocks = image.getY();
        List<List<Block>> uBlocks = image.getU();
        List<List<Block>> vBlocks = image.getV();

        //perform Inverse Discrete Cosine Transform and deQuantization
        yBlocks.forEach(line -> line.forEach(block ->
                block.setValues(inverseDiscreteCosineTransform(performDeQuantization(block.getValues())))
        ));
        uBlocks.forEach(line -> line.forEach(block ->
                block.setValues(inverseDiscreteCosineTransform(performDeQuantization(block.getValues())))
        ));
        vBlocks.forEach(line -> line.forEach(block ->
                block.setValues(inverseDiscreteCosineTransform(performDeQuantization(block.getValues())))
        ));

        //build YUV matrices
        int nrBlocksW = image.getY().get(0).size();
        int nrBlocksH = image.getY().size();
        int width = nrBlocksW * 8;
        int height = nrBlocksH * 8;
        int[][] y = blocksToMatrix(yBlocks, nrBlocksW, nrBlocksH, width, height);
        int[][] u = blocksToMatrix(uBlocks, nrBlocksW, nrBlocksH, width, height);
        int[][] v = blocksToMatrix(vBlocks, nrBlocksW, nrBlocksH, width, height);

        //build RGB matrix
        List<List<RGB>> rgbValues = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            List<RGB> line = new ArrayList<>();
            for (int j = 0; j < width; j++) {
                int r = (int) (y[i][j] + 1.140*v[i][j]);
                int g = (int) (y[i][j] - 0.395*u[i][j] - 0.581*v[i][j]);
                int b = (int) (y[i][j] + 2.032*u[i][j]);
                r = r > 255 ? 255 : r;
                g = g > 255 ? 255 : g;
                b = b > 255 ? 255 : b;

                r = r < 0 ? 0 : r;
                g = g < 0 ? 0 : g;
                b = b < 0 ? 0 : b;

                line.add(new RGB(r, g, b));
            }
            rgbValues.add(line);
        }

        //create decoded image
        PPMImage decodedImage = new PPMImage();
        decodedImage.setWidth(width);
        decodedImage.setHeight(height);
        decodedImage.setMaxColorValue(255);
        decodedImage.setRgbData(rgbValues);

        return decodedImage;
    }

    /**
     * Transform a matrix of blocks into a matrix of integer pixel values
     *
     * @param blocks matrix of blocks
     * @param nrBlocksW number of blocks in a row
     * @param nrBlocksH number of blocks in a column
     * @param width of the image
     * @param height of the image
     * @return matrix of pixel values
     */
    private int[][] blocksToMatrix(List<List<Block>> blocks, int nrBlocksW, int nrBlocksH, int width, int height) {
        int[][] matrix = new int[height][width];
        for (int i = 0; i < nrBlocksH; i++) {
            for (int j = 0; j < nrBlocksW; j++) {
                Block block = blocks.get(i).get(j);
                for (int n = 0; n < 8; n++) {
                    for (int m = 0; m < 8; m++) {
                        matrix[i*8+n][j*8+m] = block.getValues().get(n).get(m);
                    }
                }
            }
        }
        return matrix;
    }

    /**
     * Obtain an 8x8 block by multiplying the input to a quantization matrix
     *
     * @param quantizedValues 8x8 quantized block
     * @return 8x8 deQuantized coefficients block
     */
    private List<List<Integer>> performDeQuantization(List<List<Integer>> quantizedValues) {
        List<List<Integer>> deQuantizationResult = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            List<Integer> quantizationLine = new ArrayList<>();
            for (int j = 0; j < 8; j++) {
                int result = quantizedValues.get(i).get(j) * Quantization.matrix.get(i).get(j);
                quantizationLine.add(result);
            }
            deQuantizationResult.add(quantizationLine);
        }
        return deQuantizationResult;
    }


    /**
     * Transform an 8x8 DCT coefficient block into a Y/U/V block
     *
     * @param block DCT coefficient block
     * @return Y/U/V block
     */
    private List<List<Integer>> inverseDiscreteCosineTransform(List<List<Integer>> dctValues) {
        List<List<Integer>> values = new ArrayList<>();
        for (int x = 0; x < 8; x++) {
            List<Integer> line = new ArrayList<>();
            for (int y = 0; y < 8; y++) {
                //calculate value of f(x,y) in the output block matrix
                double sum = 0;
                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {
                        double alphaU = u == 0 ? (1 / Math.sqrt(2)) : 1;
                        double alphaV = v == 0 ? (1 / Math.sqrt(2)) : 1;
                        sum += alphaU * alphaV * dctValues.get(u).get(v) * Math.cos(((2*x+1)*u*Math.PI)/16) * Math.cos(((2*y+1)*v*Math.PI)/16);
                    }
                }
                int result = ((int) ((1.0/4.0) * sum)) + 128;
                line.add(result);
            }
            values.add(line);
        }
        return values;
    }
}

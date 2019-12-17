package codec;

import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Decoder {

    public PPMImage decode(EncodedImage image) {

        //perform Entropy Decoding
        List<Byte> byteArray = image.getEncodedBytes();
        Map<String, List<List<Block>>> YUVBlocks = performEntropyDecoding(byteArray);

        List<List<Block>> yBlocks = YUVBlocks.get("Y");
        List<List<Block>> uBlocks = YUVBlocks.get("U");
        List<List<Block>> vBlocks = YUVBlocks.get("V");

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
        int nrBlocksW = image.getNrBlocksW();
        int nrBlocksH = image.getGetNrBlocksH();
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
     * @param dctValues values of a DCT coefficient block matrix
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

    private Map<String, List<List<Block>>> performEntropyDecoding(List<Byte> byteArray) {
        return null;
    }

    /**
     * Obtain a list representing coefficients that should be passed to the zig-zag parser
     *
     * @param byteArray sublist of the encoded byteArray that contains values from a single block
     * @return array of corresponding integer coefficients
     */
    private List<Integer> performRunlengthDecoding(List<Byte> byteArray) {
        List<Integer> result = new ArrayList<>();

        //read values corresponding to the DC coefficient
        result.add(Byte.toUnsignedInt(byteArray.get(1)));

        //read values corresponding to all AC coefficients
        for (int i = 2; i < byteArray.size()-2; i++) {
            byte runlength = byteArray.get(i);
            byte amplitude = byteArray.get(i+2);
            if (runlength != 0) {
                for (int r = 0; r < runlength; r++) {
                    result.add(0);
                }
            }
            result.add(Byte.toUnsignedInt(amplitude));
        }
        //add missing zeros if array ends in (0,0)
        if (byteArray.get(byteArray.size()-2) == 0 && byteArray.get(byteArray.size()-1) == 0) {
            while (result.size() < 64) {
                result.add(0);
            }
        }

        return result;
    }

    /**
     * Reconstruct a matrix from its zig-zag parsing representation
     *
     * @param coefficients array of matrix coefficients parsed in zig-zag order
     * @return reconstructed matrix
     */
    private static List<List<Integer>> parseZigZag(List<Integer> coefficients) {
        int size = (int) Math.sqrt(coefficients.size());

        int index = 0;
        List<List<Integer>> diagonals = new ArrayList<>();
        for (int i = 1; i < size * 2; i++) {
            int diagonalLength = i < size ? i : size * 2 - i;
            diagonals.add(coefficients.subList(index, index + diagonalLength));
            index += diagonalLength;
        }

        List<List<Integer>> matrix = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            matrix.add(Arrays.asList(0,0,0,0,0,0,0,0));
        }

//        for (int i = 0; i < size; i++) {
//            for (int j = 0; j < size; j++) {
//                int indexSum = i + j;
//                if (indexSum % 2 == 0) {
//                    matrix.get(i).set(j, diagonals.get(indexSum).get(diagonals.get(indexSum).size()-1));
//                }
//                else {
//                    matrix.get(i).set(j, diagonals.get(indexSum).get(0));
//                }
//            }
//        }

//        for (int i = size-1; i >= 0; i--) {
//            for (int j = size-1; j >= 0; j--) {
//                int indexSum = i + j;
//                if (indexSum % 2 == 0) {
//                    matrix.get(i).set(j, diagonals.get(indexSum).get(0));
//                }
//                else {
//                    matrix.get(i).set(j, diagonals.get(indexSum).get(diagonals.get(indexSum).size()-1));
//                }
//            }
//        }

        return matrix;
    }


    public static void main(String[] args) {
        List<Integer> test = Arrays.asList(150, 80, 92, 26, 75, 20, 4, 18, 19, 3, 1, 2, 13, 3, 1, 0, 1, 2, 2, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        List<List<Integer>> res = parseZigZag(test);
        int i = 0;
    }
}

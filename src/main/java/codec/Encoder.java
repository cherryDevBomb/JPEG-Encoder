package codec;

import com.google.common.primitives.Ints;
import model.Block;
import model.EncodedImage;
import model.PPMImage;
import model.Quantization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Encoder {

    public EncodedImage encode(PPMImage image) {

        //convert to YUV
        List<List<Integer>> y = new ArrayList<>();
        List<List<Integer>> u = new ArrayList<>();
        List<List<Integer>> v = new ArrayList<>();

        image.getRgbData().forEach(line -> {
            List<Integer> lineY = new ArrayList<>();
            List<Integer> lineU = new ArrayList<>();
            List<Integer> lineV = new ArrayList<>();
            line.forEach(pixel -> {
                lineY.add((int) (0.299*pixel.getR() + 0.587*pixel.getG() + 0.114*pixel.getB()));
                lineU.add((int) (-0.147*pixel.getR() - 0.289*pixel.getG() + 0.436*pixel.getB()));
                lineV.add((int) (0.615*pixel.getR() - 0.515*pixel.getG() - 0.1*pixel.getB()));
            });
            y.add(lineY);
            u.add(lineU);
            v.add(lineV);
        });

        //divide into 8x8 blocks and do subsampling for U and V
        List<List<Block>> yBlocks = divideIntoBlocks(y, image.getWidth(), image.getHeight());
        List<List<Block>> uBlocks = divideIntoBlocksAndSubSample(u, image.getWidth(), image.getHeight());
        List<List<Block>> vBlocks = divideIntoBlocksAndSubSample(v, image.getWidth(), image.getHeight());

        //do upsampling for U and V
        int nrBlocksW = yBlocks.get(0).size();
        int nrBlocksH = yBlocks.size();
        upSample(uBlocks, nrBlocksW, nrBlocksH);
        upSample(vBlocks, nrBlocksW, nrBlocksH);

        //perform Forward Discrete Cosine Transform and quantization
        yBlocks.forEach(line -> line.forEach(block ->
                block.setValues(performQuantization(forwardDiscreteCosineTransform(block)))
        ));
        uBlocks.forEach(line -> line.forEach(block ->
                block.setValues(performQuantization(forwardDiscreteCosineTransform(block)))
        ));
        vBlocks.forEach(line -> line.forEach(block ->
                block.setValues(performQuantization(forwardDiscreteCosineTransform(block)))
        ));

        //perform Entropy Encoding
        List<Integer> encodedByteArray = performEntropyEncoding(yBlocks, uBlocks, vBlocks);

        return new EncodedImage(encodedByteArray, nrBlocksW, nrBlocksH);
    }


    /**
     * Divide image matrix into 8x8 blocks
     *
     * @param matrix image matrix
     * @param width of the image
     * @param height of the image
     * @return matrix of blocks
     */
    private List<List<Block>> divideIntoBlocks(List<List<Integer>> matrix, int width, int height) {
        List<List<Block>> blocks = new ArrayList<>();

        for (int i = 0; i < height; i+=8) {
            List<Block> lineBlocks = new ArrayList<>();
            for (int j = 0; j < width; j+=8) {
                //construct the 8x8 block
                int[][] blockTmp = new int[8][8];
                for (int n = 0; n < 8; n++) {
                    for (int m = 0; m < 8; m++) {
                        blockTmp[n][m] = matrix.get(i+n).get(j+m);
                    }
                }
                List<List<Integer>> blockValues = Arrays.stream(blockTmp)
                        .map(Ints::asList)
                        .collect(Collectors.toList());
                lineBlocks.add(new Block(blockValues));
            }
            blocks.add(lineBlocks);
        }
        return blocks;
    }

    /**
     * Divide image matrix into 8x8 blocks and convert each one to a 4x4 block by downSampling
     *
     * @param matrix image matrix
     * @param width of the image
     * @param height of the image
     * @return matrix of blocks
     */
    private List<List<Block>> divideIntoBlocksAndSubSample(List<List<Integer>> matrix, int width, int height) {
        List<List<Block>> blocks = new ArrayList<>();

        for (int i = 0; i < height; i+=8) {
            List<Block> lineBlocks = new ArrayList<>();
            for (int j = 0; j < width; j+=8) {
                //construct the 4x4 block
                int[][] blockTmp = new int[4][4];
                for (int n = 0; n < 8; n+=2) {
                    for (int m = 0; m < 8; m+=2) {
                        int sum = matrix.get(i+n).get(j+m) + matrix.get(i+n+1).get(j+m) + matrix.get(i+n).get(j+m+1) + matrix.get(i+n+1).get(j+m+1);
                        blockTmp[n/2][m/2] = (int) (Math.round(sum)) / 4;
                    }
                }
                List<List<Integer>> blockValues = Arrays.stream(blockTmp)
                        .map(Ints::asList)
                        .collect(Collectors.toList());
                lineBlocks.add(new Block(blockValues));
            }
            blocks.add(lineBlocks);
        }
        return blocks;
    }

    /**
     * Convert a matrix of 4x4 blocks to 8x8 blocks by upSampling
     *
     * @param blocks matrix of 4x4 blocks
     * @param nrBlocksW number of blocks in a line
     * @param nrBlocksH number of blocks in a column
     */
    private void upSample(List<List<Block>> blocks, int nrBlocksW, int nrBlocksH) {
        for (int i = 0; i < nrBlocksH; i++) {
            for (int j = 0; j < nrBlocksW; j++) {
                Block block = blocks.get(i).get(j);
                int[][] blockTmp = new int[8][8];
                for (int n = 0; n < 4; n++) {
                    for (int m = 0; m < 4; m++) {
                        int averageValue = block.getValues().get(n).get(m);
                        blockTmp[n * 2][m * 2] = averageValue;
                        blockTmp[n * 2][m * 2 + 1] = averageValue;
                        blockTmp[n * 2 + 1][m * 2] = averageValue;
                        blockTmp[n * 2 + 1][m * 2 + 1] = averageValue;
                    }
                }
                List<List<Integer>> blockValues = Arrays.stream(blockTmp)
                        .map(Ints::asList)
                        .collect(Collectors.toList());
                blocks.get(i).set(j, new Block(blockValues));
            }
        }
    }

    /**
     * Transform an Y/U/V 8x8 block into another 8x8 DCT coefficient block
     *
     * @param block initial block
     * @return DCT coefficient block
     */
    private List<List<Double>> forwardDiscreteCosineTransform(Block block) {
        List<List<Double>> dctValues = new ArrayList<>();
        for (int u = 0; u < 8; u++) {
            List<Double> dctLine = new ArrayList<>();
            for (int v = 0; v < 8; v++) {
                //calculate value of G(u,v) in the output DCT block matrix
                double alphaU = u == 0 ? (1 / Math.sqrt(2)) : 1;
                double alphaV = v == 0 ? (1 / Math.sqrt(2)) : 1;
                double sum = 0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        sum += (block.getValues().get(x).get(y)-128) * Math.cos(((2*x+1)*u*Math.PI)/16) * Math.cos(((2*y+1)*v*Math.PI)/16);
                    }
                }
                double dctResult = (1.0/4.0) * alphaU * alphaV * sum;
                dctLine.add(dctResult);
            }
            dctValues.add(dctLine);
        }
        return dctValues;
    }

    /**
     * Obtain an 8x8 block by dividing the input to a quantization matrix
     *
     * @param dctValues 8x8 block of DCT coefficients
     * @return 8x8 quantized coefficients block
     */
    private List<List<Integer>> performQuantization(List<List<Double>> dctValues) {
        List<List<Integer>> quantizationResult = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            List<Integer> quantizationLine = new ArrayList<>();
            for (int j = 0; j < 8; j++) {
                int result = (int) (dctValues.get(i).get(j) / Quantization.matrix.get(i).get(j));
                quantizationLine.add(result);
            }
            quantizationResult.add(quantizationLine);
        }
        return quantizationResult;
    }

    /**
     * Perform zig-zag parsing and runlength encoding of each Y, U and V block
     *
     * @param yBlocks Y blocks
     * @param uBlocks U blocks
     * @param vBlocks V blocks
     * @return resulting byte array
     */
    private List<Integer> performEntropyEncoding(List<List<Block>> yBlocks, List<List<Block>> uBlocks, List<List<Block>> vBlocks) {
        List<List<Integer>> byteArray = new ArrayList<>();

        int nrBlocksH = yBlocks.size();
        int nrBlocksW = yBlocks.get(0).size();

        for (int i = 0; i < nrBlocksH; i++) {
            for (int j = 0; j < nrBlocksW; j++) {
                byteArray.add(performRunLengthEncoding(parseZigZag(yBlocks.get(i).get(j).getValues())));
                byteArray.add(performRunLengthEncoding(parseZigZag(uBlocks.get(i).get(j).getValues())));
                byteArray.add(performRunLengthEncoding(parseZigZag(vBlocks.get(i).get(j).getValues())));
            }
        }

        return byteArray.stream().flatMap(List::stream).collect(Collectors.toList());
    }


    /**
     * Obtain an array representing the zig-zag parsing of a matrix
     *
     * @param matrix input matrix
     * @return matrix parsed in zig-zag
     */
    private List<Integer> parseZigZag(List<List<Integer>> matrix) {
        int size = matrix.size();

        List<List<Integer>> diagonals = new ArrayList<>();
        for (int i = 0; i < size * 2 - 1; i++) {
            diagonals.add(new ArrayList<>());
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int indexSum = i + j;
                if (indexSum % 2 == 0) {
                    diagonals.get(indexSum).add(0, matrix.get(i).get(j));
                }
                else {
                    diagonals.get(indexSum).add(matrix.get(i).get(j));
                }
            }
        }

        return diagonals.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Obtain an array of maximum 64*3-1 bytes by performing runlength encoding
     *
     * @param coefficients an array of 64 integer values
     * @return encoded byte array
     */
    private List<Integer> performRunLengthEncoding(List<Integer> coefficients) {

        List<Integer> result = new ArrayList<>();

        // add size and amplitude of the DC coefficient
        result.add(countBits(coefficients.get(0)));
        result.add(coefficients.get(0));

        // add runlength, size and amplitude of all AC coefficients
        int runLengthCounter = 0;
        for (Integer elem : coefficients.subList(1, coefficients.size())) {
            if (elem != 0) {
                result.add(runLengthCounter);
                result.add(countBits(elem));
                result.add(elem);
                runLengthCounter = 0;
            }
            else {
                runLengthCounter++;
            }
        }
        // add (0,0) if the block ends with a consecutive sequence of zeroes
        if (runLengthCounter > 0) {
            result.add(0);
            result.add(0);
        }

        return result;
    }

    /**
     * Get the number of bits needed to represent a number
     *
     * @param number an integer number
     * @return number of bits
     */
    private Integer countBits(int number) {
        return (int) (Math.log(Math.abs(number)) / Math.log(2) + 1);
    }
}

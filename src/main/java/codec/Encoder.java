package codec;

import com.google.common.primitives.Doubles;
import model.Block;
import model.EncodedImage;
import model.PPMImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Encoder {

    public EncodedImage encode(PPMImage image) {

        //convert to YUV
        List<List<Double>> y = new ArrayList<>();
        List<List<Double>> u = new ArrayList<>();
        List<List<Double>> v = new ArrayList<>();

        image.getRgbData().forEach(line -> {
            List<Double> lineY = new ArrayList<>();
            List<Double> lineU = new ArrayList<>();
            List<Double> lineV = new ArrayList<>();
            line.forEach(pixel -> {
                lineY.add(0.299*pixel.getR() + 0.587*pixel.getG() + 0.114*pixel.getB());
                lineU.add(128 - 0.1687*pixel.getR() - 0.3312*pixel.getG() + 0.5*pixel.getB());
                lineV.add(128 + 0.5*pixel.getR() - 0.4186*pixel.getG() - 0.0813*pixel.getB());
            });
            y.add(lineY);
            u.add(lineU);
            v.add(lineV);
        });

        //divide Y into 8x8 blocks
        List<List<Block>> yBlocks = new ArrayList<>();

        for (int i = 0; i < image.getHeight(); i+=8) {
            List<Block> lineBlocks = new ArrayList<>();
            for (int j = 0; j < image.getWidth(); j+=8) {
                //construct the 8x8 block
                double[][] blockTmp = new double[8][8];
                for (int n = 0; n < 8; n++) {
                    for (int m = 0; m < 8; m++) {
                        blockTmp[n][m] = y.get(i).get(j);
                    }
                }
                List<List<Double>> blockValues = Arrays.stream(blockTmp)
                        .map(Doubles::asList)
                        .collect(Collectors.toList());
                lineBlocks.add(new Block(blockValues));
            }
            yBlocks.add(lineBlocks);
        }

        //divide U into 8x8 blocks and do subsampling
        List<List<Block>> uBlocks = new ArrayList<>();

        for (int i = 0; i < image.getHeight(); i+=8) {
            List<Block> lineBlocks = new ArrayList<>();
            for (int j = 0; j < image.getWidth(); j+=8) {
                //construct the 4x4 block
                double[][] blockTmp = new double[4][4];
                for (int n = 0; n < 4; n++) {
                    for (int m = 0; m < 4; m++) {
                        double sum = y.get(i).get(j) + y.get(i+1).get(j) + y.get(i).get(j+1) + y.get(i+1).get(j+1);
                        blockTmp[n][m] = sum / 4;
                    }
                }
                List<List<Double>> blockValues = Arrays.stream(blockTmp)
                        .map(Doubles::asList)
                        .collect(Collectors.toList());
                lineBlocks.add(new Block(blockValues));
            }
            uBlocks.add(lineBlocks);
        }

        //divide U into 8x8 blocks and do subsampling
        List<List<Block>> vBlocks = new ArrayList<>();

        for (int i = 0; i < image.getHeight(); i+=8) {
            List<Block> lineBlocks = new ArrayList<>();
            for (int j = 0; j < image.getWidth(); j+=8) {
                //construct the 4x4 block
                double[][] blockTmp = new double[4][4];
                for (int n = 0; n < 4; n++) {
                    for (int m = 0; m < 4; m++) {
                        double sum = y.get(i).get(j) + y.get(i+1).get(j) + y.get(i).get(j+1) + y.get(i+1).get(j+1);
                        blockTmp[n][m] = sum / 4;
                    }
                }
                List<List<Double>> blockValues = Arrays.stream(blockTmp)
                        .map(Doubles::asList)
                        .collect(Collectors.toList());
                lineBlocks.add(new Block(blockValues));
            }
            vBlocks.add(lineBlocks);
        }

        return new EncodedImage(yBlocks, uBlocks, vBlocks);
    }
}

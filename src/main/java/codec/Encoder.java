package codec;

import com.google.common.primitives.Ints;
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
        List<List<Integer>> y = new ArrayList<>();
        List<List<Integer>> u = new ArrayList<>();
        List<List<Integer>> v = new ArrayList<>();

        image.getRgbData().forEach(line -> {
            List<Integer> lineY = new ArrayList<>();
            List<Integer> lineU = new ArrayList<>();
            List<Integer> lineV = new ArrayList<>();
            line.forEach(pixel -> {
                lineY.add((int) Math.round(0.299*pixel.getR() + 0.587*pixel.getG() + 0.114*pixel.getB()));
                lineU.add((int) Math.round(128 - 0.1687*pixel.getR() - 0.3312*pixel.getG() + 0.5*pixel.getB()));
                lineV.add((int) Math.round(128 + 0.5*pixel.getR() - 0.4186*pixel.getG() - 0.0813*pixel.getB()));
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
                int[][] blockTmp = new int[8][8];
                for (int n = 0; n < 8; n++) {
                    for (int m = 0; m < 8; m++) {
                        blockTmp[n][m] = y.get(i+n).get(j+m);
                    }
                }
                List<List<Integer>> blockValues = Arrays.stream(blockTmp)
                        .map(Ints::asList)
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
                int[][] blockTmp = new int[4][4];
                for (int n = 0; n < 8; n+=2) {
                    for (int m = 0; m < 8; m+=2) {
                        int sum = u.get(i+n).get(j+m) + u.get(i+n+1).get(j+m) + u.get(i+n).get(j+m+1) + u.get(i+n+1).get(j+m+1);
                        blockTmp[n/2][m/2] = (int) (Math.round(sum)) / 4;
                    }
                }
                List<List<Integer>> blockValues = Arrays.stream(blockTmp)
                        .map(Ints::asList)
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
                int[][] blockTmp = new int[4][4];
                for (int n = 0; n < 8; n+=2) {
                    for (int m = 0; m < 8; m+=2) {
                        int sum = v.get(i+n).get(j+m) + v.get(i+n+1).get(j+m) + v.get(i+n).get(j+m+1) + v.get(i+n+1).get(j+m+1);
                        blockTmp[n/2][m/2] = (int) (Math.round(sum)) / 4;
                    }
                }
                List<List<Integer>> blockValues = Arrays.stream(blockTmp)
                        .map(Ints::asList)
                        .collect(Collectors.toList());
                lineBlocks.add(new Block(blockValues));
            }
            vBlocks.add(lineBlocks);
        }

        return new EncodedImage(yBlocks, uBlocks, vBlocks);
    }
}

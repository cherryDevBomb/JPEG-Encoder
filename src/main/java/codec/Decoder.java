package codec;

import model.Block;
import model.EncodedImage;
import model.PPMImage;
import model.RGB;

import java.util.ArrayList;
import java.util.List;

public class Decoder {

    public PPMImage decode(EncodedImage image) {

        List<List<Block>> yBlocks = image.getY();
        List<List<Block>> uBlocks = image.getU();
        List<List<Block>> vBlocks = image.getV();

        int nrBlocksW = image.getY().get(0).size();
        int nrBlocksH = image.getY().size();
        int width = nrBlocksW * 8;
        int height = nrBlocksH * 8;

        //build Y matrix
        int[][] y = new int[height][width];
        for (int i = 0; i < nrBlocksH; i++) {
            for (int j = 0; j < nrBlocksW; j++) {
                Block block = yBlocks.get(i).get(j);
                for (int n = 0; n < 8; n++) {
                    for (int m = 0; m < 8; m++) {
                        y[i*8+n][j*8+m] = block.getValues().get(n).get(m);
                    }
                }
            }
        }

        //build U matrix and do upsampling
        int[][] u = new int[height][width];
        for (int i = 0; i < nrBlocksH; i++) {
            for (int j = 0; j < nrBlocksW; j++) {
                Block block = uBlocks.get(i).get(j);
                for (int n = 0; n < 4; n++) {
                    for (int m = 0; m < 4; m++) {
                        int averageValue = block.getValues().get(n).get(m);
                        u[i*8+n*2][j*8+m*2] = averageValue;
                        u[i*8+n*2+1][j*8+m*2] = averageValue;
                        u[i*8+n*2][j*8+m*2+1] = averageValue;
                        u[i*8+n*2+1][j*8+m*2+1] = averageValue;
                    }
                }
            }
        }

        //build V matrix and do upsampling
        int[][] v = new int[height][width];
        for (int i = 0; i < nrBlocksH; i++) {
            for (int j = 0; j < nrBlocksW; j++) {
                Block block = vBlocks.get(i).get(j);
                for (int n = 0; n < 4; n++) {
                    for (int m = 0; m < 4; m++) {
                        int averageValue = block.getValues().get(n).get(m);
                        v[i*8+n*2][j*8+m*2] = averageValue;
                        v[i*8+n*2+1][j*8+m*2] = averageValue;
                        v[i*8+n*2][j*8+m*2+1] = averageValue;
                        v[i*8+n*2+1][j*8+m*2+1] = averageValue;
                    }
                }
            }
        }

        //build RGB matrix
        List<List<RGB>> rgbValues = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            List<RGB> line = new ArrayList<>();
            for (int j = 0; j < width; j++) {
                int r = (int) Math.round(1.164*(y[i][j]) + 1.596*(v[i][j] - 128));
                int g = (int) Math.round(1.164*(y[i][j]) - 0.813*(v[i][j] - 128) - 0.391*(u[i][j] - 128));
                int b = (int) Math.round(1.164*(y[i][j]) + 2.018*(u[i][j] - 128));

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
}

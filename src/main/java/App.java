import codec.Decoder;
import codec.Encoder;
import model.EncodedImage;
import model.PPMImage;

public class App {

    public static void main(String[] args) {
        PPMImage image = new PPMImage("nt-P3.ppm");

        Encoder encoder = new Encoder();
        EncodedImage encodedImage = encoder.encode(image);

        Decoder decoder = new Decoder();
        PPMImage decodedImage = decoder.decode(encodedImage);
        decodedImage.writeToFile();
    }
}

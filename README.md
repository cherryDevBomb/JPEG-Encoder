# JPEG Encoder
🎞️ Audio-Video Data Processing uni assignment

### The encoder part
* Dividing the image into blocks of 8x8 pixels
* Converting each pixel value from RGB to YUV
* Subsampling and Upsampling of the U and V block matrices
* Performing Forward DCT (Discrete Cosine Transform) and Quantization on each 8x8 pixels block
* Performing Entropy Encoding (zig-zag parsing and run-length encoding)

### The decoder part
* Performing Entropy Decoding (run-length decoding and forming an 8x8 block by zig-zag parsing)
* Performing DeQuantization and Inverse DCT (Discrete Cosine Transform) on each 8x8 pixels block
* Converting each pixel value from YUV to RGB
* Recreating the RGB matrix from the blocks
* Composing the final decoded PPM image

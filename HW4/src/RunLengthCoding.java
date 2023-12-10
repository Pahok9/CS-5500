import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RunLengthCoding {

    private static final int BIT_DEPTH = 8;

    public static class RLCResult {
        List<Integer> encodedData;
        long encodingTime;
        double compressionRatio;

        RLCResult(List<Integer> encodedData, long encodingTime, double compressionRatio) {
            this.encodedData = encodedData;
            this.encodingTime = encodingTime;
            this.compressionRatio = compressionRatio;
        }
    }

    public static class BitPlaneRLCResult {
        List<BitSet> encodedBitPlanes;
        long encodingTime;
        double compressionRatio;

        BitPlaneRLCResult(List<BitSet> encodedBitPlanes, long encodingTime, double compressionRatio) {
            this.encodedBitPlanes = encodedBitPlanes;
            this.encodingTime = encodingTime;
            this.compressionRatio = compressionRatio;
        }
    }

    public static RLCResult compressGrayscale(BufferedImage image) {
        long startTime = System.currentTimeMillis();
        ArrayList<Integer> encodedData = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();
        int imageSize = width * height;

        for (int y = 0; y < height; y++) {
            int count = 1;
            int currentPixel = new Color(image.getRGB(0, y)).getRed();
            for (int x = 1; x < width; x++) {
                int pixel = new Color(image.getRGB(x, y)).getRed();
                if (pixel == currentPixel) {
                    count++;
                } else {
                    encodedData.add(currentPixel);
                    encodedData.add(count);
                    currentPixel = pixel;
                    count = 1;
                }
            }
            encodedData.add(currentPixel);
            encodedData.add(count);
        }

        long endTime = System.currentTimeMillis();
        int compressedSizeInBytes = encodedData.size();
        double compressionRatio = (double) imageSize / compressedSizeInBytes;

        return new RLCResult(encodedData, endTime - startTime, compressionRatio);
    }

    public static BufferedImage decompressGrayscale(RLCResult rlcResult, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int x = 0, y = 0;

        for (int i = 0; i < rlcResult.encodedData.size(); i += 2) {
            int value = rlcResult.encodedData.get(i);
            int count = rlcResult.encodedData.get(i + 1);
            for (int j = 0; j < count; j++) {
                image.setRGB(x, y, new Color(value, value, value).getRGB());
                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            }
        }

        return image;
    }

    public static BitPlaneRLCResult compressBitPlanes(BufferedImage image) {
        long startTime = System.currentTimeMillis();
        List<BitSet> bitPlanes = extractBitPlanes(image);
        List<BitSet> encodedBitPlanes = new ArrayList<>(BIT_DEPTH);
        int width = image.getWidth();
        int height = image.getHeight();
        int totalEncodedBits = 0;

        for (BitSet plane : bitPlanes) {
            BitSet encodedPlane = encodeBitSet(plane, width * height);
            encodedBitPlanes.add(encodedPlane);
            totalEncodedBits += encodedPlane.length();
        }

        long endTime = System.currentTimeMillis();
        double compressionRatio = (double) (width * height * BIT_DEPTH) / totalEncodedBits;

        return new BitPlaneRLCResult(encodedBitPlanes, endTime - startTime, compressionRatio);
    }

    public static BufferedImage decompressBitPlanes(BitPlaneRLCResult result, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int[][] pixels = new int[height][width];

        for (int bit = 0; bit < BIT_DEPTH; bit++) {
            BitSet decodedPlane = decodeBitSet(result.encodedBitPlanes.get(bit));
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (decodedPlane.get(y * width + x)) {
                        pixels[y][x] |= 1 << bit;
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = pixels[y][x];
                image.setRGB(x, y, new Color(value, value, value).getRGB());
            }
        }

        return image;
    }

    private static List<BitSet> extractBitPlanes(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        List<BitSet> bitPlanes = new ArrayList<>();
        for (int i = 0; i < BIT_DEPTH; i++) {
            bitPlanes.add(new BitSet(width * height));
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x] & 0xFF;
                for (int bit = 0; bit < BIT_DEPTH; bit++) {
                    if ((pixel & (1 << bit)) != 0) {
                        bitPlanes.get(bit).set(y * width + x);
                    }
                }
            }
        }

        return bitPlanes;
    }

    private static BitSet encodeBitSet(BitSet bitSet, int size) {
        BitSet encoded = new BitSet();
        boolean lastBit = false;
        int count = 0;
        int encodedIndex = 0;

        for (int i = 0; i < size; i++) {
            boolean currentBit = bitSet.get(i);
            if (currentBit == lastBit) {
                count++;
            } else {
                encodedIndex = writeCountToBitSet(encoded, count, encodedIndex);
                count = 1;
                lastBit = currentBit;
            }
        }
        writeCountToBitSet(encoded, count, encodedIndex);

        return encoded;
    }

    private static BitSet decodeBitSet(BitSet encoded) {
        BitSet decoded = new BitSet();
        int decodedIndex = 0;
        boolean currentBit = false;

        for (int i = 0; encoded.nextSetBit(i) >= 0; i = encoded.nextClearBit(i + 1)) {
            int count = readCountFromBitSet(encoded, i);
            if (currentBit) {
                decoded.set(decodedIndex, decodedIndex + count);
            }
            decodedIndex += count;
            currentBit = !currentBit;
        }

        return decoded;
    }

    private static int writeCountToBitSet(BitSet encoded, int count, int startIndex) {
        for (int i = 0; i < Integer.SIZE; i++) {
            if ((count & (1 << i)) != 0) {
                encoded.set(startIndex + i);
            }
        }
        return startIndex + Integer.SIZE;
    }

    private static int readCountFromBitSet(BitSet encoded, int startIndex) {
        int count = 0;
        for (int i = 0; i < Integer.SIZE; i++) {
            if (encoded.get(startIndex + i)) {
                count |= 1 << i;
            }
        }
        return count;
    }

    public static double calculateRMSE(BufferedImage original, BufferedImage compressed) {
        double mse = 0;
        int width = original.getWidth();
        int height = original.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalPixel = new Color(original.getRGB(x, y)).getRed();
                int compressedPixel = new Color(compressed.getRGB(x, y)).getRed();
                mse += Math.pow(originalPixel - compressedPixel, 2);
            }
        }

        mse /= (width * height);
        return Math.sqrt(mse);
    }
}

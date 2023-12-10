import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LZW {
    public static class LZWResult {
        List<Integer> compressedData;
        double compressionRatio;
        double encodingTime;

        LZWResult(List<Integer> compressedData, double compressionRatio, double encodingTime) {
            this.compressedData = compressedData;
            this.compressionRatio = compressionRatio;
            this.encodingTime = encodingTime;
        }
    }

    public static LZWResult compress(BufferedImage image) {
        long startTime = System.currentTimeMillis();
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        List<Integer> compressedData = compress(pixels);

        long endTime = System.currentTimeMillis();
        double compressionRatio = (double) pixels.length / compressedData.size();
        double encodingTime = endTime - startTime;

        return new LZWResult(compressedData, compressionRatio, encodingTime);
    }

    public static BufferedImage decompress(LZWResult result, int width, int height, int type) {
        byte[] decompressedPixels = decompress(result.compressedData);
        BufferedImage decompressedImage = new BufferedImage(width, height, type);
        decompressedImage.getRaster().setDataElements(0, 0, width, height, decompressedPixels);
        return decompressedImage;
    }

    public static List<Integer> compress(byte[] data) {
        int dictSize = 256;
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        String w = "";
        List<Integer> result = new ArrayList<>();
        for (byte b : data) {
            String wb = w + (char) (b & 0xFF);
            if (dictionary.containsKey(wb)) {
                w = wb;
            } else {
                result.add(dictionary.get(w));
                dictionary.put(wb, dictSize++);
                w = "" + (char) (b & 0xFF);
            }
        }

        if (!w.isEmpty()) {
            result.add(dictionary.get(w));
        }
        return result;
    }

    public static byte[] decompress(List<Integer> compressedData) {
        int dictSize = 256;
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        String w = "" + (char) (int) compressedData.remove(0);
        StringBuilder result = new StringBuilder(w);
        for (int k : compressedData) {
            String entry;
            if (dictionary.containsKey(k)) {
                entry = dictionary.get(k);
            } else if (k == dictSize) {
                entry = w + w.charAt(0);
            } else {
                throw new IllegalArgumentException("Bad compressed k: " + k);
            }

            result.append(entry);
            dictionary.put(dictSize++, w + entry.charAt(0));
            w = entry;
        }

        byte[] decompressedData = new byte[result.length()];
        for (int i = 0; i < result.length(); i++) {
            decompressedData[i] = (byte) result.charAt(i);
        }
        return decompressedData;
    }
}
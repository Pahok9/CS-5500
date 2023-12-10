import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;

public class HuffmanCoding {

    static class HuffmanNode {
        int data;
        int frequency;
        HuffmanNode left, right;

        HuffmanNode(int data, int frequency) {
            this.data = data;
            this.frequency = frequency;
        }
    }

    static class HuffmanComparator implements Comparator<HuffmanNode> {
        @Override
        public int compare(HuffmanNode node1, HuffmanNode node2) {
            return node1.frequency - node2.frequency;
        }
    }

    public static class HuffmanResult {
        String encodedData;
        HuffmanNode root;
        int imageWidth;
        int imageHeight;
        double compressionRatio;
        long encodingTime;

        HuffmanResult(String encodedData, HuffmanNode root, int width, int height, double compressionRatio, long encodingTime) {
            this.encodedData = encodedData;
            this.root = root;
            this.imageWidth = width;
            this.imageHeight = height;
            this.compressionRatio = compressionRatio;
            this.encodingTime = encodingTime;
        }
    }

    public static HuffmanResult encode(int[] imageData, int width, int height) {
        long startTime = System.currentTimeMillis();

        // Frequency map and priority queue setup
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int key : imageData) {
            frequencyMap.put(key, frequencyMap.getOrDefault(key, 0) + 1);
        }
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>(new HuffmanComparator());
        frequencyMap.forEach((key, value) -> queue.add(new HuffmanNode(key, value)));

        // Building Huffman tree
        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();
            HuffmanNode parent = new HuffmanNode(-1, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            queue.add(parent);
        }
        HuffmanNode root = queue.poll();

        // Encoding
        Map<Integer, String> huffmanCodes = new HashMap<>();
        generateCodes(root, "", huffmanCodes);
        StringBuilder encodedImage = new StringBuilder();
        for (int key : imageData) {
            encodedImage.append(huffmanCodes.get(key));
        }

        // Compression ratio and encoding time calculation
        long endTime = System.currentTimeMillis();
        int originalSize = imageData.length * Integer.SIZE;
        int compressedSize = encodedImage.length();
        double compressionRatio = (double) originalSize / compressedSize;

        return new HuffmanResult(encodedImage.toString(), root, width, height, compressionRatio, endTime - startTime);
    }

    public static int[] decode(HuffmanResult result) {
        String encodedData = result.encodedData;
        HuffmanNode root = result.root;
        int[] decodedData = new int[result.imageWidth * result.imageHeight];
        int index = 0;
        HuffmanNode current = root;
        for (int i = 0; i < encodedData.length(); ) {
            while (current.left != null && current.right != null) {
                current = encodedData.charAt(i++) == '0' ? current.left : current.right;
            }
            decodedData[index++] = current.data;
            current = root;
        }
        return decodedData;
    }

    private static void generateCodes(HuffmanNode node, String code, Map<Integer, String> huffmanCodes) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.data, code);
        } else {
            generateCodes(node.left, code + "0", huffmanCodes);
            generateCodes(node.right, code + "1", huffmanCodes);
        }
    }

    public static BufferedImage reconstructImage(int[] decodedData, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int grayValue = decodedData[y * width + x];
                int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }
}

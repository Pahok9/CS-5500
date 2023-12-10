import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Load the image
//            BufferedImage originalImage = ImageIO.read(new File("Lena.jpg"));
            BufferedImage originalImage = ImageIO.read(new File("img1.jpg"));

            // Run length coding on the grayscale values
//            RunLengthCoding.RLCResult rlcResult = RunLengthCoding.compressGrayscale(originalImage);
//            System.out.println("Compression Ratio: " + rlcResult.compressionRatio);
//            System.out.println("Encoding Time: " + rlcResult.encodingTime + "ms");
//            BufferedImage processedImage = RunLengthCoding.decompressGrayscale(rlcResult, originalImage.getWidth(), originalImage.getHeight());

            // Run length coding on the Bit planes
//            RunLengthCoding.BitPlaneRLCResult bitPlaneRLCResult = RunLengthCoding.runLengthEncodeBitPlanes(originalImage);
//            System.out.println("Bit Plane Compression Ratio: " + bitPlaneRLCResult.compressionRatio);
//            System.out.println("Bit Plane Encoding Time: " + bitPlaneRLCResult.encodingTime + "ms");
//            BufferedImage processedImage = RunLengthCoding.runLengthDecodeBitPlanes(bitPlaneRLCResult.encodedBitPlanes, originalImage.getWidth(), originalImage.getHeight());


            // Huffman Coding
//            int[] imagePixels = getGrayscaleArray(originalImage);
//            HuffmanCoding.HuffmanResult result = HuffmanCoding.encode(imagePixels, originalImage.getWidth(), originalImage.getHeight());
//            System.out.println("Huffman Compression Ratio: " + result.compressionRatio);
//            System.out.println("Huffman Encoding Time: " + result.encodingTime + "ms");
//            int[] decodedPixels = HuffmanCoding.decode(result);
//            BufferedImage processedImage = HuffmanCoding.reconstructImage(decodedPixels, result.imageWidth, result.imageHeight);

            // LZW
            LZW.LZWResult compressed = LZW.compress(originalImage);
            System.out.println("Compression Ratio: " + compressed.compressionRatio);
            System.out.println("Encoding Time: " + compressed.encodingTime + "ms");
            BufferedImage processedImage = LZW.decompress(compressed, originalImage.getWidth(), originalImage.getHeight(), originalImage.getType());

            // Print RMSE
            double rmse = RunLengthCoding.calculateRMSE(originalImage, processedImage);
            System.out.println("Root Mean Square Error: " + rmse);

            // Save the processed image
            saveImage(processedImage, "output.jpg");

            // Display the original and processed images
            displayImages(originalImage, processedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] getGrayscaleArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] grayscale = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb & 0xFF); // Extracting the blue component as gray for grayscale image
                grayscale[y * width + x] = gray;
            }
        }
        return grayscale;
    }


    private static void displayImages(BufferedImage originalImage, BufferedImage processedImage) {
        JFrame frame = new JFrame("Image Processing Results");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // Add the original image
        frame.add(new JLabel(new ImageIcon(originalImage)));

        // Add the processed image
        frame.add(new JLabel(new ImageIcon(processedImage)));

        // Pack and display the frame
        frame.pack();
        frame.setVisible(true);
    }

    private static void saveImage(BufferedImage image, String filePath) {
        try {
            File outputFile = new File(filePath);
            ImageIO.write(image, "jpg", outputFile);
        } catch (IOException e) {
            System.out.println("Error saving the image: " + e.getMessage());
        }
    }
}

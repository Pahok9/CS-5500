import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

public class Filters {
    public static BufferedImage toGrayscale(BufferedImage input) {
        BufferedImage grayscaleImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayscaleImage.getGraphics();
        g.drawImage(input, 0, 0, null);
        g.dispose();
        return grayscaleImage;
    }

    public static BufferedImage globalHistogramEqualization(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage output = new BufferedImage(width, height, inputImage.getType());
        int imageSize = width * height;
        int[] histogram = new int[256];

        // Count the number of each pixel nk
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = inputImage.getRGB(x, y) & 0xFF; // getting grayscale pixel value
                histogram[pixelValue]++;
            }
        }

        // Cumulative sum of nk
        int[] sumOfPixels = new int[256];
        sumOfPixels[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            sumOfPixels[i] = sumOfPixels[i - 1] + histogram[i];
        }

        // Compute new values
        int[] newValues = new int[256];
        for (int i = 0; i < 256; i++) {
            newValues[i] = (int) Math.round(255.0 * sumOfPixels[i] / imageSize);
        }

        // Create new image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = inputImage.getRGB(x, y) & 0xFF;
                int newValue = newValues[value];
                output.setRGB(x, y, new Color(newValue, newValue, newValue).getRGB()); // RGB have same value due to grayscale
            }
        }
        return output;
    }

    // Overloading for local histogram equalization with 3x3 default mask size
    public static BufferedImage localHistogramEqualization(BufferedImage inputImage) {
        inputImage = localHistogramEqualization(inputImage, 3);
        return inputImage;
    }

    public static BufferedImage localHistogramEqualization(BufferedImage input, int maskSize) {
        int width = input.getWidth();
        int height = input.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, input.getType());

        int radius = maskSize / 2;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // Compute histogram values for local
                int[] localHistogram = new int[256];
                for (int i = -radius; i <= radius; i++) {
                    for (int j = -radius; j <= radius; j++) {
                        // Out of bound check
                        if ((x + j >= 0 && x + j < width) && (y + i >= 0 && y + i < height)) {
                            int pixelValue = input.getRGB(x + j, y + i) & 0xFF;
                            localHistogram[pixelValue]++;
                        }
                    }
                }

                // Compute cumulative histogram for local
                int[] sumOflocalHist = new int[256];
                sumOflocalHist[0] = localHistogram[0];
                for (int i = 1; i < 256; i++) {
                    sumOflocalHist[i] = sumOflocalHist[i - 1] + localHistogram[i];
                }

                // Getting size of mask
                int localTotal = maskSize * maskSize;
                int currentValue = input.getRGB(x, y) & 0xFF;
                // Calculate the new pixel value
                int newValue = (int) Math.round(255.0 * sumOflocalHist[currentValue] / localTotal);
                outputImage.setRGB(x, y, new Color(newValue, newValue, newValue).getRGB());
            }
        }
        return outputImage;
    }

    public static BufferedImage bitPlaneSlicing(BufferedImage inputImage, int bit) {
        toGrayscale(inputImage);
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage output = new BufferedImage(width, height, inputImage.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = inputImage.getRGB(x, y) & 0xFF; // getting grayscale pixel value
                String binaryPixelValue = Integer.toBinaryString(pixelValue);
                while (binaryPixelValue.length() < 8) {
                    binaryPixelValue = "0" + binaryPixelValue;
                }
                int newPixelValue;
                if (binaryPixelValue.charAt(7 - (bit - 1)) == '1') {
                    newPixelValue = 255;
                } else {
                    newPixelValue = 0;
                }
                output.setRGB(x, y, new Color(newPixelValue, newPixelValue, newPixelValue).getRGB());
            }
        }
        return output;
    }

    public static BufferedImage combineBitPlanes(BufferedImage... bitImages) {
        if (bitImages.length == 0) {
            return null;
        }

        int width = bitImages[0].getWidth();
        int height = bitImages[0].getHeight();
        BufferedImage combinedImages = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int combinedPixelValue = 0;
                for (BufferedImage image : bitImages) {
                    int pixelValue = image.getRGB(x, y) & 0xFF;
                    if (pixelValue == 255) {
                        combinedPixelValue = 255;
                        break;
                    }
                }
                combinedImages.setRGB(x, y, new Color(combinedPixelValue, combinedPixelValue, combinedPixelValue).getRGB());
            }
        }

        return combinedImages;
    }

    public static BufferedImage smoothingBoxFilter(BufferedImage inputImage, int maskSize) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImg = new BufferedImage(width, height, inputImage.getType());
        int subMask = maskSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sumPixelValue = 0;
                int count = 0;
                for (int yj = -subMask; yj <= maskSize; yj++) {
                    for (int xi = -subMask; xi <= maskSize; xi++) {
                        if ((x + xi >= 0 && x + xi < width) && (y + yj >= 0 && y + yj < height)) {
                            int pixelValue = inputImage.getRGB(x + xi, y + yj) & 0xFF;
                            sumPixelValue += pixelValue;
                            count++;
                        }
                    }
                }
                int newPixelValue = sumPixelValue / count;
                outputImg.setRGB(x, y, new Color(newPixelValue, newPixelValue, newPixelValue).getRGB());
            }
        }
        return outputImg;
    }

    private static BufferedImage smoothingWeightedAverageFilter(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImg = new BufferedImage(width, height, inputImage.getType());
        int[][] kernel = {
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sumPixelValue = 0;
                int count = 0;
                for (int yj = -1; yj <= 1; yj++) {
                    for (int xi = -1; xi <= 1; xi++) {
                        int pixelValue = inputImage.getRGB(x + xi, y + yj) & 0xFF;
                        sumPixelValue += pixelValue * kernel[yj + 1][xi + 1];
                    }
                }
                int newPixelValue = sumPixelValue / 16; // 16 due to the sum of the kernel pixel values
                outputImg.setRGB(x, y, new Color(newPixelValue, newPixelValue, newPixelValue).getRGB());
            }
        }
        return outputImg;
    }

    public static BufferedImage gaussianBlur(BufferedImage inputImage, int maskSize, double sigma) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage result = new BufferedImage(width, height, inputImage.getType());

        double[][] kernel = generateGaussianKernel(maskSize, sigma);
        double kernelSum = 0;

        for (int i = 0; i < maskSize; i++) {
            for (int j = 0; j < maskSize; j++) {
                kernelSum += kernel[i][j];
            }
        }

        int offset = maskSize / 2;

        for (int x = offset; x < width - offset; x++) {
            for (int y = offset; y < height - offset; y++) {
                double sumPixelValue = 0.0;

                for (int i = -offset; i <= offset; i++) {
                    for (int j = -offset; j <= offset; j++) {
                        int pixelValue = inputImage.getRGB(x + i, y + j) & 0xFF;
                        sumPixelValue += pixelValue * kernel[i + offset][j + offset];
                    }
                }

                int newPixelValue = (int)Math.round(sumPixelValue / kernelSum);
                result.setRGB(x, y, new Color(newPixelValue, newPixelValue, newPixelValue).getRGB());
            }
        }

        return result;
    }

    public static double[][] generateGaussianKernel(int size, double sigma) {
        double[][] kernel = new double[size][size];
        int offset = size / 2;
        double sigmaSquare = sigma * sigma;

        for (int x = -offset; x <= offset; x++) {
            for (int y = -offset; y <= offset; y++) {
                kernel[x + offset][y + offset] = (1.0 / (2.0 * Math.PI * sigmaSquare)) * Math.exp(-(x * x + y * y) / (2 * sigmaSquare));
            }
        }

        return kernel;
    }


    public static BufferedImage laplacianSharpeningFilter(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        // Create a new image with increased width and height for Laplacian calculation
        BufferedImage paddedImage = new BufferedImage(width + 2, height + 2, inputImage.getType());  // +2 for left and right, and top and bottom extra pixels

        // Copy the original image into the center of the padded image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                paddedImage.setRGB(x + 1, y + 1, inputImage.getRGB(x, y));
            }
        }

        // Provide pixel values to edges
        for (int x = 1; x <= width; x++) {
            paddedImage.setRGB(x, 0, inputImage.getRGB(x - 1, 0));
            paddedImage.setRGB(x, height + 1, inputImage.getRGB(x - 1, height - 1));
        }
        for (int y = 0; y <= height + 1; y++) {
            paddedImage.setRGB(0, y, paddedImage.getRGB(1, y));
            paddedImage.setRGB(width + 1, y, paddedImage.getRGB(width, y));
        }

        // Handle the corners
        paddedImage.setRGB(0, 0, inputImage.getRGB(0, 0));
        paddedImage.setRGB(width + 1, 0, inputImage.getRGB(width - 1, 0));
        paddedImage.setRGB(0, height + 1, inputImage.getRGB(0, height - 1));
        paddedImage.setRGB(width + 1, height + 1, inputImage.getRGB(width - 1, height - 1));

        // Now apply the Laplacian sharpening on the padded image
        BufferedImage output = new BufferedImage(width, height, inputImage.getType());
        int[][] filter = {
                {1, 1,  1},
                {1, -8, 1},
                {1, 1,  1}
        };

        for (int y = 1; y <= height; y++) {
            for (int x = 1; x <= width; x++) {
                int pixelSum = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = paddedImage.getRGB(x + kx, y + ky) & 0xFF;
                        pixelSum += pixel * filter[ky + 1][kx + 1];
                    }
                }

                int originalPixel = paddedImage.getRGB(x, y) & 0xFF;
                int newPixelValue = originalPixel - pixelSum;
                // Adjust the values to be between 0 and 255
                newPixelValue = Math.max(0, Math.min(255, newPixelValue));
                output.setRGB(x - 1, y - 1, new Color(newPixelValue, newPixelValue, newPixelValue).getRGB());
            }
        }

        return output;
    }

    public static BufferedImage highBoostFilter(BufferedImage inputImage, float k) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        BufferedImage output = new BufferedImage(width, height, inputImage.getType());

        // 1. Blur the original image using a simple average filter
        BufferedImage blurredImage = gaussianBlur(inputImage, 31, 5);

        // 2. Compute the mask: original - blurred
        BufferedImage maskImage = new BufferedImage(width, height, inputImage.getType());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int originalPixel = inputImage.getRGB(x, y) & 0xFF;
                int blurredPixel = blurredImage.getRGB(x, y) & 0xFF;
                int maskPixel = originalPixel - blurredPixel;
                maskPixel = Math.max(0, maskPixel);
                maskImage.setRGB(x, y, maskPixel);
            }
        }

        // 3. Add the weighted mask to the original image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int originalPixel = inputImage.getRGB(x, y) & 0xFF;
                int maskPixel = maskImage.getRGB(x, y) & 0xFF;
                int highBoostPixel = originalPixel + Math.round(k * maskPixel);

                // Clamp to the range [0, 255]
                highBoostPixel = Math.max(0, Math.min(255, highBoostPixel));

                output.setRGB(x, y, new Color(highBoostPixel, highBoostPixel, highBoostPixel).getRGB());
            }
        }

        return output;
    }

    public static BufferedImage medianFilter(BufferedImage inputImage, int maskSize) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        BufferedImage outputImg = new BufferedImage(width, height, inputImage.getType());
        int subMask = maskSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ArrayList<Integer> listOfSubMaskPixels = new ArrayList<Integer>();
                for (int i = -subMask; i <= subMask; i++) {
                    for (int j = -subMask; j <= subMask; j++) {
                        if ((x + j >= 0 && x + j < width) && (y + i >= 0 && y + i < height)) {
                            int pixelValue = inputImage.getRGB(x + j, y + i) & 0xFF;
                            listOfSubMaskPixels.add(pixelValue);
                        }
                    }
                }
                Collections.sort(listOfSubMaskPixels);
                int newPixelValue = listOfSubMaskPixels.get(listOfSubMaskPixels.size()/2);
                outputImg.setRGB(x, y, new Color(newPixelValue, newPixelValue, newPixelValue).getRGB());
            }
        }
        return outputImg;
    }
}

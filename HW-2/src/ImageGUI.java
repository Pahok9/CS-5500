import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageGUI {

    private static JLabel originalImageLabel;
    private static JLabel processedImageLabel;
    private JLabel currentFilterLabel = new JLabel();
    private int currentMaskSize = 3;
    private float currentA = 4.5f;
    private JCheckBox[] bitPlaneCheckboxes;
    private JLabel frame;

    public ImageGUI() {
        createGUI();
    }

    private void createGUI() {
        // Initialization of main frame
        JFrame frame = new JFrame("Homework 2");
        frame.setSize(1000, 500);

        // ---- Menu Bar Section ----
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadMenuItem = new JMenuItem("Load Image");
        fileMenu.add(loadMenuItem);
        menuBar.add(fileMenu);

        // Filter Menu
        JMenu filterMenu = new JMenu("Filter");
        String[] filterNames = {"Global Histogram Equalization", "Local Histogram Equalization", "Smoothing Box Filter", "Smoothing Average Weighted Filter", "Median Filter", "Laplacian Sharpening", "High Boost Filter"};

        currentFilterLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel aPanel = new JPanel(new FlowLayout()); // Initialization here

        for (String filter : filterNames) {
            JMenuItem filterItem = new JMenuItem(filter);
            filterItem.addActionListener(e -> {
                applyFilter(filter);
                currentFilterLabel.setText(filter);  // Display current filter
                aPanel.setVisible("High Boost Filter".equals(filter));
            });
            filterMenu.add(filterItem);
        }
        menuBar.add(filterMenu);
        frame.setJMenuBar(menuBar);

        // ---- Images Panel Section ----
        JPanel imagesPanel = new JPanel(new GridLayout(1, 2));

        originalImageLabel = new JLabel("", SwingConstants.CENTER);
        originalImageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        originalImageLabel.setPreferredSize(new Dimension(520, 520));

        processedImageLabel = new JLabel("", SwingConstants.CENTER);
        processedImageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        processedImageLabel.setPreferredSize(new Dimension(520, 520));

        imagesPanel.add(originalImageLabel);
        imagesPanel.add(processedImageLabel);
        frame.add(imagesPanel);

        // ---- Mask Size Input Section ----
        JPanel maskSizePanel = new JPanel(new FlowLayout());
        JLabel maskSizeLabel = new JLabel("Mask size:");
        JTextField maskSizeTextField = new JTextField(5);
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            try {
                currentMaskSize = Integer.parseInt(maskSizeTextField.getText());
                if (currentFilterLabel.getText().length() > 0) {
                    applyFilter(currentFilterLabel.getText());  // Reapply filter with the new mask size
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid number for mask size.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        maskSizePanel.add(maskSizeLabel);
        maskSizePanel.add(maskSizeTextField);
        maskSizePanel.add(applyButton);

        // ---- A Parameter Input Section ----
        JLabel aLabel = new JLabel("A:");
        JTextField aTextField = new JTextField(5);
        JButton applyAButton = new JButton("Apply");
        applyAButton.addActionListener(e -> {
            try {
                currentA = (float) Double.parseDouble(aTextField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid number for A.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        aPanel.add(aLabel);
        aPanel.add(aTextField);
        aPanel.add(applyAButton);
        aPanel.setVisible(false);  // Initially hidden

        // ---- Current Filter Display Section ----
        currentFilterLabel = new JLabel();
        currentFilterLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // ---- Combining Mask Size and A Parameter Sections ----
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(maskSizePanel, BorderLayout.WEST);
        inputPanel.add(currentFilterLabel, BorderLayout.CENTER);
        inputPanel.add(aPanel, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.NORTH);

        // ---- Action Listeners Section ----
        loadMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                BufferedImage img = null;
                try {
                    img = ImageIO.read(selectedFile);
                    originalImageLabel.setIcon(new ImageIcon(img));
                    frame.pack();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // ---- Bit Plane GUI Integration ----
        JPanel bitPlanePanel = createBitPlaneGUI();
        frame.add(bitPlanePanel, BorderLayout.SOUTH);

        // Setting frame default operations
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Setting frame default operations
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JPanel createBitPlaneGUI() {
        JPanel bitPlanePanel = new JPanel(new FlowLayout());

        JLabel bitPlaneLabel = new JLabel("Bit Planes: ");
        bitPlanePanel.add(bitPlaneLabel);

        bitPlaneCheckboxes = new JCheckBox[8];
        for (int i = 0; i < 8; i++) {
            bitPlaneCheckboxes[i] = new JCheckBox("Bit " + (i + 1));
            bitPlanePanel.add(bitPlaneCheckboxes[i]);
        }

        JButton applyBitPlanesButton = new JButton("Apply Bit Planes");
        applyBitPlanesButton.addActionListener(e -> {
            BufferedImage originalImage = (BufferedImage) ((ImageIcon) originalImageLabel.getIcon()).getImage();

            // List to store the selected bit positions
            ArrayList<Integer> selectedBits = new ArrayList<>();
            StringBuilder appliedBitPlanes = new StringBuilder("Selected Bit Planes: "); // For tracking

            for (int i = 0; i < bitPlaneCheckboxes.length; i++) {
                if (bitPlaneCheckboxes[i].isSelected()) {
                    selectedBits.add(i + 1); // Add the bit position to the list
                    appliedBitPlanes.append(i + 1).append(" ");
                }
            }

            System.out.println(appliedBitPlanes.toString());

            if (!selectedBits.isEmpty()) {
                BufferedImage combinedImage = Filters.combineBitPlanes(originalImage, selectedBits.stream().mapToInt(Integer::intValue).toArray());
                processedImageLabel.setIcon(new ImageIcon(combinedImage));
            } else {
                // Handle the case where no checkboxes are selected
                System.out.println("No bit planes selected.");
            }
        });

        bitPlanePanel.add(applyBitPlanesButton);

        return bitPlanePanel;
    }

    private void applyFilter(String filterName) {
        if (originalImageLabel.getIcon() == null) {
            return;
        }

        BufferedImage originalImage = (BufferedImage) ((ImageIcon) originalImageLabel.getIcon()).getImage();
        BufferedImage processedImage = null;

        switch (filterName) {
            case "Global Histogram Equalization":
                processedImage = Filters.globalHistogramEqualization(originalImage);
                break;
            case "Local Histogram Equalization":
                processedImage = Filters.localHistogramEqualization(originalImage, currentMaskSize);
                break;
            case "Smoothing Box Filter":
                processedImage = Filters.smoothingBoxFilter(originalImage, currentMaskSize);
                break;
            case "Smoothing Average Weighted Filter":
                processedImage = Filters.smoothingWeightedAverageFilter(originalImage);
                break;
            case "Laplacian Sharpening":
                processedImage = Filters.laplacianSharpeningFilter(originalImage);
                break;
            case "High Boost Filter":
                processedImage = Filters.highBoostFilter(originalImage, currentA);
                break;
            case "Median Filter":
                processedImage = Filters.medianFilter(originalImage, currentMaskSize);
                break;
        }

        if (processedImage != null) {
            processedImageLabel.setIcon(new ImageIcon(processedImage));
        }
    }
}

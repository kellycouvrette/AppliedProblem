/*
$env:JAVA_HOME="C:\Users\kelly\Java\jdk-23.0.2"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
javac TrustGUI.java
java TrustGui
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;


public class TrustGUI extends JFrame {
    private JLabel instructionLabel;
    private JPanel mapPanel;
    private JPanel keyPanel;
    private Point markerLocation;
    private long startTime;
    private int level = 1;
    private int levelTrials = 0;
    private Image shipIcon;
    private long[] totalTimes = new long[5]; 
    private int[] totalConfidences = new int[5]; 
    private int[] trialsCount = new int[5]; 
    private boolean taskStarted = false;

    public TrustGUI() {
        setTitle("Algorithm Trust Evaluation");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        shipIcon = new ImageIcon("ship_icon.png").getImage(); 

        // Instruction label on the top
        instructionLabel = new JLabel("Locate the ship on the map. Click anywhere to place your marker.", SwingConstants.CENTER);
        add(instructionLabel, BorderLayout.NORTH);

        // Map Panel
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // helps remove ship after each trial, redraws map after each trial
                if (markerLocation != null) {
                    g.drawImage(shipIcon, markerLocation.x - 15, markerLocation.y - 15, 30, 30, this); 
                }
                else {
                    super.paintComponent(g);
                    setBackground(Color.BLUE); 
                    drawLand(g);
                    drawHeatMap(g);
                }
                 
                
            }
        };
        mapPanel.setPreferredSize(new Dimension(800, 500)); 
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!taskStarted) return; // ignore clicks before Start is pressed to fix timer bug

                markerLocation = e.getPoint();
                long elapsedTime = System.currentTimeMillis() - startTime;
                repaint();

                // ask for confidence rating after ship click
                String confidenceStr = JOptionPane.showInputDialog("Rate your confidence (1-10):");
                int confidence = Integer.parseInt(confidenceStr);

                // Update total times + confidence for each trial
                totalTimes[level] += elapsedTime;
                totalConfidences[level] += confidence;
                trialsCount[level]++;

                // Log data
                System.out.println("Marker placed at: " + markerLocation);
                System.out.println("Time taken: " + elapsedTime + "ms");
                System.out.println("Confidence: " + confidence);

                repaint();
                markerLocation = null; // Hide ship for next trial so it doesn't get repainted

                // goto next trial or level
                levelTrials++;
                if (levelTrials < 3) {
                    instructionLabel.setText("Level " + level + ": Locate the ship with assistance.");
                    startTime = System.currentTimeMillis();
                } else {
                    nextLevel();
                }
            }
        });

        add(mapPanel, BorderLayout.CENTER);

        // Start button
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startTask());
        add(startButton, BorderLayout.SOUTH);

        // Key Panel
        keyPanel = new JPanel();
        keyPanel.setPreferredSize(new Dimension(175, 100)); 
        keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.Y_AXIS));
        add(keyPanel, BorderLayout.EAST);

        // Key content
        JLabel keyTitle = new JLabel("Key:");
        keyPanel.add(keyTitle);
        keyPanel.add(createKeyItem("Blue = Water", Color.BLUE));
        keyPanel.add(createKeyItem("Green = Land", Color.GREEN));
        keyPanel.add(createKeyItem("Red = Suggested Location", new Color(255, 0, 0, 100)));
    }

    private void startTask() {
        instructionLabel.setText("Task started. Click to place your marker.");
        startTime = System.currentTimeMillis(); // Start timer only now so it doens't get weird for first trial
        taskStarted = true; // Allow ship clicks now
    }


    private void nextLevel() {
        // Calculate and display average times and confidence for the current level when it ends (3 trials)
        if (levelTrials >= 3) {
            int averageTime = (int) (totalTimes[level] / trialsCount[level]);
            int averageConfidence = totalConfidences[level] / trialsCount[level];

            System.out.println("Level " + level + " completed!");
            System.out.println("Average time to decide: " + averageTime + "ms");
            System.out.println("Average confidence: " + averageConfidence + "/10");

            // Reset trial counter for the next level and go to the next level (if not done)
            levelTrials = 0;
            if (level < 4) {
                level++;
                instructionLabel.setText("Level " + level + ": Click to locate the ship.");
                startTime = System.currentTimeMillis();
            } else {
                showFinalResults();
            }
        }
    }

    private void showFinalResults() {
        // Output average times and confidence for all levels... cool popup!
        StringBuilder results = new StringBuilder("Experiment Complete! \n\n");
        for (int i = 1; i <= 4; i++) {
            int averageTime = (int) (totalTimes[i] / trialsCount[i]);
            int averageConfidence = totalConfidences[i] / trialsCount[i];
            results.append("Level " + i + ":\n");
            results.append("  Average time: " + averageTime + "ms\n");
            results.append("  Average confidence: " + averageConfidence + "%\n\n");
        }
        JOptionPane.showMessageDialog(this, results.toString());
        writeToCSV();
        System.exit(0);
    }

    private void drawHeatMap(Graphics g) {
        Random rand = new Random();
        if (level == 1) {
            g.setColor(Color.BLACK);
        } else if (level == 2) { // heatmaps start for level 2
            for (int i = 0; i < 4; i++) {
                int x = rand.nextInt(700) + 50;
                int y = rand.nextInt(400) + 50;
                int size = rand.nextInt(50) + 50;
                g.setColor(new Color(255, 0, 0, 100));
                g.fillOval(x, y, size, size);
            }
        } else if (level == 3 || level == 4) { // confidence ratings for elvels 3/4
            int highestConfidence = 0;
            Point highestPoint = new Point(0, 0);
            int highestSize = 0;

            for (int i = 0; i < 4; i++) {
                int x = rand.nextInt(700) + 50;
                int y = rand.nextInt(400) + 50;
                int size = rand.nextInt(50) + 50;
                int confidence = rand.nextInt(50) + 50;

                if (level == 3) {
                // Draw all hotspots normally in level 3
                g.setColor(new Color(255, 0, 0, 100));
                g.fillOval(x, y, size, size);
                g.setColor(Color.BLACK);
                g.drawString(confidence + "%", x + size / 2 - 10, y + size / 2);
            }

            // Track the highest confidence hotspot
            if (confidence > highestConfidence) {
                highestConfidence = confidence;
                highestPoint = new Point(x, y);
                highestSize = size;
            }
        }

        // In level 4, only display the highest confidence hotspot
        if (level == 4) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillOval(highestPoint.x, highestPoint.y, highestSize, highestSize);
            g.setColor(Color.BLACK);
            g.drawString(highestConfidence + "%", highestPoint.x + highestSize / 2 - 10, highestPoint.y + highestSize / 2);
            g.drawString("Algorithm suggests this area based on its historical data.", highestPoint.x + highestSize + 10, highestPoint.y + highestSize / 2);
        }
        }
    }

    private void drawLand(Graphics g) { // draws the green bubbles for the "land"
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            int x = rand.nextInt(700) + 50;
            int y = rand.nextInt(400) + 50;
            int size = rand.nextInt(100) + 50;
            g.setColor(Color.GREEN);
            g.fillOval(x, y, size, size);
        }
    }

    private void writeToCSV() { // How I'm exporting data
        try (FileWriter writer = new FileWriter("experiment_results.csv", true)) {
            // Write header if file is empty
            if (new java.io.File("experiment_results.csv").length() == 0) {
                writer.append("Level, Average Time (ms), Average Confidence\n");
            }
            
            for (int i = 1; i <= 4; i++) {
                if (trialsCount[i] > 0) {
                    int averageTime = (int) (totalTimes[i] / trialsCount[i]);
                    int averageConfidence = totalConfidences[i] / trialsCount[i];

                    writer.append(String.format("%d, %d, %d\n", i, averageTime, averageConfidence));
                }
            }
            
            System.out.println("Data successfully written to CSV.");
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }

    // styling for the green = land, blue = water, etc. 
    private JPanel createKeyItem(String text, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        JLabel colorLabel = new JLabel();
        colorLabel.setPreferredSize(new Dimension(20, 20));
        colorLabel.setBackground(color);
        colorLabel.setOpaque(true);
        panel.add(colorLabel);
        panel.add(new JLabel(text));
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrustGUI gui = new TrustGUI();
            gui.setVisible(true);
        });
    }
}

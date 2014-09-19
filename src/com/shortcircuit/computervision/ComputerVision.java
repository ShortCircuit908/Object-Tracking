package com.shortcircuit.computervision;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Toolkit;

/**
 * @author ShortCircuit908
 * 
 */
public class ComputerVision {
    private JFrame frame;
    private ImageAnalysis image_analysis;
    private Thread analysis_thread;
    private JLabel display_label;
    private int selected_camera;
    
    /**
     * Create the application.
     */
    public ComputerVision(int camera, Color color, double tolerance) {
        selected_camera = camera;
        initialize();
        image_analysis = new ImageAnalysis(this, selected_camera);
        //image_analysis.setColor(new Color(95, 255, 255));
        image_analysis.setColor(color);
        image_analysis.setGrabDelay(0);
        image_analysis.setTolerance(tolerance);
        analysis_thread = new Thread(image_analysis);
        analysis_thread.start();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
                ComputerVision.class.getResource("/com/shortcircuit/computervision/resources/Crosshair.png")));
        frame.setResizable(false);
        frame.setBounds(100, 100, 646, 508);
        frame.getContentPane().setLayout(null);

        display_label = new JLabel("");
        display_label.setBackground(Color.CYAN);
        display_label.setBounds(0, 0, 640, 480);
        frame.getContentPane().add(display_label);
        frame.setVisible(true);
    }
    public void setImage(BufferedImage image) {
        display_label.setIcon(new ImageIcon(image));
    }

    @SuppressWarnings("deprecation")
    public void close() {
        analysis_thread.stop();
        image_analysis.halt();
        frame.dispose();
    }
}

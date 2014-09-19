package com.shortcircuit.computervision;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import org.bytedeco.javacv.VideoInputFrameGrabber;

/**
 * @author ShortCircuit908
 * 
 */
public class CVMenu {

    private JFrame frmObjectTracking;
    private JComboBox<String> camera_selector;
    private JLabel snapshot;
    private JButton btn_refresh;
    private JButton btn_snap;
    private ComputerVision window;
    private BufferedImage image;
    private JPanel panel_snapshot;
    private Color color = new Color(0, 0, 0);
    private JButton btn_begin;
    private JPanel panel_color;
    private JLabel lbl_color;
    private JLabel lbl_tolerance;
    private JSlider slider_blue;
    private JSlider slider_red;
    private JSlider slider_green;
    private JLabel lbl_red;
    private JLabel lbl_green;
    private JLabel lbl_blue;
    private JSlider slider_tolerance;
    private JComboBox<String> size_selector;
    private int selected_camera = 0;
    private int cam_width = 640;
    private int cam_height = 480;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    CVMenu window = new CVMenu();
                    window.frmObjectTracking.setVisible(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public CVMenu() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    @SuppressWarnings("deprecation")
    private void initialize() {
        frmObjectTracking = new JFrame();
        frmObjectTracking.setTitle("Object Tracking");
        frmObjectTracking.getContentPane().setBackground(Color.BLACK);
        frmObjectTracking.setIconImage(Toolkit.getDefaultToolkit().getImage(
                CVMenu.class.getResource("/com/shortcircuit/computervision/resources/Crosshair.png")));
        frmObjectTracking.addWindowFocusListener(new WindowFocusListener() {
            public void windowGainedFocus(WindowEvent event) {
                if(window != null) {
                    window.close();
                }
            }
            public void windowLostFocus(WindowEvent event) {
            }
        });
        try {
            image = ImageIO.read(
                    CVMenu.class.getResource("/com/shortcircuit/computervision/resources/ColorBar.png"));
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        frmObjectTracking.setResizable(false);
        frmObjectTracking.setBounds(100, 100, 871, 508);
        frmObjectTracking.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmObjectTracking.getContentPane().setLayout(null);

        panel_snapshot = new JPanel();
        panel_snapshot.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if(event.getX() >= 0 && event.getX() < 640 && event.getY() >= 0 && event.getY() < 480) {
                    Color col = new Color(image.getRGB(event.getX(), event.getY()));
                    slider_red.setValue(col.getRed());
                    slider_green.setValue(col.getGreen());
                    slider_blue.setValue(col.getBlue());
                    refreshColor();
                    refreshColor();
                }
            }
        });
        panel_snapshot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                frmObjectTracking.setCursor(Cursor.CROSSHAIR_CURSOR);
            }
            @Override
            public void mouseExited(MouseEvent event) {
                frmObjectTracking.setCursor(Cursor.DEFAULT_CURSOR);
            }
            @Override
            public void mousePressed(MouseEvent event) {
                Color col = new Color(image.getRGB(event.getX(), event.getY()));
                slider_red.setValue(col.getRed());
                slider_green.setValue(col.getGreen());
                slider_blue.setValue(col.getBlue());
                refreshColor();
            }
        });
        panel_snapshot.setBounds(225, 0, 640, 480);
        frmObjectTracking.getContentPane().add(panel_snapshot);
        panel_snapshot.setLayout(null);

        snapshot = new JLabel("");
        snapshot.setBounds(0, 0, 640, 480);
        panel_snapshot.add(snapshot);

        btn_begin = new JButton("Begin tracking");
        btn_begin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                initializeCV();
            }
        });
        btn_begin.setEnabled(false);
        btn_begin.setBounds(10, 446, 205, 23);
        frmObjectTracking.getContentPane().add(btn_begin);

        JPanel panel_camera = new JPanel();
        panel_camera.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panel_camera.setBounds(10, 11, 205, 131);
        frmObjectTracking.getContentPane().add(panel_camera);
        panel_camera.setLayout(null);

        btn_refresh = new JButton("Refresh list");
        btn_refresh.setToolTipText("Update list of available cameras");
        btn_refresh.setBounds(10, 66, 185, 23);
        panel_camera.add(btn_refresh);

        camera_selector = new JComboBox<String>();
        camera_selector.setBounds(10, 35, 185, 20);
        panel_camera.add(camera_selector);
        camera_selector.addItem("");
        camera_selector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                selected_camera = camera_selector.getSelectedIndex() -1;
                btn_snap.setEnabled(selected_camera != -1);
                btn_begin.setEnabled(selected_camera != -1);
            }
        });
        camera_selector.setToolTipText("Select a camera to use");

        JLabel lbl_settings = new JLabel("Camera settings");
        lbl_settings.setLabelFor(panel_camera);
        lbl_settings.setBounds(10, 11, 185, 14);
        panel_camera.add(lbl_settings);
        
        size_selector = new JComboBox<String>();
        size_selector.setToolTipText("<html>Select a camera resolution<br>WARNING: This is slightly broken<br>Change at your own risk</html>");
        size_selector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                cam_width = Integer.parseInt(((String)size_selector.getSelectedItem()).split(" X ")[0]);
                cam_height = Integer.parseInt(((String)size_selector.getSelectedItem()).split(" X ")[1]);
                System.out.println(cam_width + ", " + cam_height);
            }
        });
        size_selector.setModel(new DefaultComboBoxModel<String>(new String[] {"320 X 240", "352 X 288",
                "424 X 240", "640 X 360", "640 X 480", "800 X 448", "960 X 540", "1280 X 720"}));
        size_selector.setSelectedIndex(4);
        size_selector.setBounds(10, 100, 185, 20);
        panel_camera.add(size_selector);

        panel_color = new JPanel();
        panel_color.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panel_color.setBounds(10, 153, 205, 282);
        frmObjectTracking.getContentPane().add(panel_color);
        panel_color.setLayout(null);

        lbl_color = new JLabel("Color");
        lbl_color.setBounds(11, 10, 185, 14);
        panel_color.add(lbl_color);
        lbl_color.setLabelFor(panel_color);

        lbl_red = new JLabel("R");
        lbl_red.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_red.setBounds(182, 35, 14, 23);
        panel_color.add(lbl_red);

        lbl_green = new JLabel("G");
        lbl_green.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_green.setBounds(182, 70, 14, 23);
        panel_color.add(lbl_green);

        lbl_blue = new JLabel("B");
        lbl_blue.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_blue.setBounds(182, 105, 14, 23);
        panel_color.add(lbl_blue);
        
        slider_tolerance = new JSlider();
        slider_tolerance.setValue(10);
        slider_tolerance.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                lbl_tolerance.setText("Tolerance: " + slider_tolerance.getValue() + "%");
            }
        });
        slider_tolerance.setToolTipText("<html>Slide to adjust tolerance when searching for color matches"
                + "<br>A tolerance below 10% is suggested to begin with</html>");
        slider_tolerance.setBounds(11, 214, 185, 23);
        panel_color.add(slider_tolerance);
        
        lbl_tolerance = new JLabel("Tolerance: " + slider_tolerance.getValue() + "%");
        lbl_tolerance.setHorizontalAlignment(SwingConstants.LEFT);
        lbl_tolerance.setBounds(11, 199, 185, 14);
        panel_color.add(lbl_tolerance);

        btn_snap = new JButton("Take snapshot");
        btn_snap.setToolTipText("Take a picture with the camera to select a color");
        btn_snap.setBounds(11, 248, 185, 23);
        panel_color.add(btn_snap);
        btn_snap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                takeSnapshot();
            }
        });
        btn_snap.setEnabled(false);

        slider_blue = new JSlider();
        slider_blue.setToolTipText("Slide to adjust blue content");
        slider_blue.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                refreshColor();
            }
        });
        slider_blue.setValue(0);
        slider_blue.setMaximum(255);
        slider_blue.setBounds(10, 105, 172, 23);
        panel_color.add(slider_blue);

        slider_red = new JSlider();
        slider_red.setToolTipText("Slide to adjust red content");
        slider_red.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                refreshColor();
            }
        });
        slider_red.setValue(0);
        slider_red.setMaximum(255);
        slider_red.setBounds(10, 35, 172, 23);
        panel_color.add(slider_red);

        slider_green = new JSlider();
        slider_green.setToolTipText("Slide to adjust green content");
        slider_green.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                refreshColor();
            }
        });
        slider_green.setValue(0);
        slider_green.setMaximum(255);
        slider_green.setBounds(10, 70, 172, 23);
        panel_color.add(slider_green);
        btn_refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refreshCameraList();
            }
        });
        /*
         * TODO: Move these
         */
        slider_red.setFocusable(false);
        slider_green.setFocusable(false);
        slider_blue.setFocusable(false);
        slider_tolerance.setFocusable(false);

        refreshCameraList();
        refreshSnapshot();
    }
    private void refreshCameraList() {
        try {
            while(camera_selector.getItemCount() > 1) {
                camera_selector.removeItemAt(1);
            }
            for(String camera : VideoInputFrameGrabber.getDeviceDescriptions()) {
                camera_selector.addItem(camera);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    private void takeSnapshot() {
        try {
            VideoInputFrameGrabber grabber = new VideoInputFrameGrabber(selected_camera);
            grabber.setImageWidth(cam_width);
            grabber.setImageHeight(cam_height);
            grabber.start();
            image = resizeImage(grabber.grab().getBufferedImage(), 640, 480);
            grabber.release();
            grabber.stop();
            refreshSnapshot();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    public void initializeCV() {
        if(window != null) {
            window.close();
        }
        window = new ComputerVision(selected_camera, color, (double)slider_tolerance.getValue() / 100.0);
    }
    private void refreshSnapshot() {
        snapshot.setIcon(new ImageIcon(image));
    }
    private void refreshColor() {
        color = new Color(slider_red.getValue(), slider_green.getValue(), slider_blue.getValue());
        frmObjectTracking.getContentPane().setBackground(color);
    }
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage final_image = new BufferedImage(width, height, image.getType());
        Image sized = null;
        int offset_x = 0;
        int offset_y = 0;
        if((double)image.getWidth() / (double)image.getHeight() > 4.0/3.0) {
            sized = image.getScaledInstance(width, -1, Image.SCALE_FAST);
            offset_y = (height - sized.getHeight(null)) / 2;
        }
        else if((double)image.getWidth() / (double)image.getHeight() < 4.0/3.0) {
            sized = image.getScaledInstance(-1, height, Image.SCALE_FAST);
            offset_x = (width - sized.getHeight(null)) / 2;
        }
        else {
            sized = image.getScaledInstance(width, height, Image.SCALE_FAST);
        }
        final_image.getGraphics().drawImage(sized, offset_x, offset_y, null);
        return final_image;
    }
}

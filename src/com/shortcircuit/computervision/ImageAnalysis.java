package com.shortcircuit.computervision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.bytedeco.javacv.VideoInputFrameGrabber;

/**
 * @author ShortCircuit908
 * 
 */
public class ImageAnalysis implements Runnable{
    private BufferedImage image;
    private Color color;
    private int grab_delay = 100;
    private ComputerVision computer_vision;
    private VideoInputFrameGrabber grabber;
    private double tolerance;
    public ImageAnalysis(ComputerVision computer_vision, int camera) {
        this.computer_vision = computer_vision;
        grabber = new VideoInputFrameGrabber(camera);
    }
    @Override
    public void run() {
        try {
            grabber.start();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        while(true) {
            try {
                image = grabber.grab().getBufferedImage();
                checkImage(image);
                computer_vision.setImage(image);
                Thread.sleep(grab_delay);
            }
            catch(Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
    private void checkImage(BufferedImage image) {
        try {
            double x_avg = 0;
            double y_avg = 0;
            double total = 0;
            /*
            Mat before_blur = new Mat(image.getWidth(), image.getHeight(), CvType.CV_16UC1);
            Mat after_blur = before_blur.clone();
            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            before_blur.put(0, 0, pixels);
            Imgproc.GaussianBlur(before_blur, after_blur, new Size(5, 5), 5);
            MatOfByte bytemat = new MatOfByte();
            Highgui.imencode(".jpg", after_blur, bytemat);
            byte[] bytes = bytemat.toArray();
            InputStream in = new ByteArrayInputStream(bytes);
            BufferedImage check_image = ImageIO.read(in);
             */
            BufferedImage check_image = image;
            for(int x = 0; x < check_image.getWidth(); x++) {
                for(int y = 0; y < check_image.getHeight(); y++) {
                    Color check_color = new Color(check_image.getRGB(x, y));
                    int diffRed   = Math.abs(color.getRed()   - check_color.getRed());
                    int diffGreen = Math.abs(color.getGreen() - check_color.getGreen());
                    int diffBlue  = Math.abs(color.getBlue()  - check_color.getBlue());
                    double pctDiffRed   = (double)diffRed   / 255.0;
                    double pctDiffGreen = (double)diffGreen / 255.0;
                    double pctDiffBlue   = (double)diffBlue  / 255.0;
                    double difference = (pctDiffRed + pctDiffGreen + pctDiffBlue) / 3.0;
                    if(difference <= tolerance) {
                        x_avg += x;
                        y_avg += y;
                        total++;
                    }
                    /*
                    float[] hsv_color = new float[3];
                    float[] hsv_check = new float[3];
                    hsv_color = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv_color);
                    hsv_check = Color.RGBtoHSB(check_color.getRed(), check_color.getGreen(),
                            check_color.getBlue(), hsv_check);
                    float color_hue = hsv_color[0];
                    float color_sat = hsv_color[1];
                    float check_hue = hsv_check[0];
                    float check_sat = hsv_color[1];
                    if(color_hue/check_hue == 1.0 && color_sat/check_sat == 1.0) {
                        x_avg += x;
                        y_avg += y;
                        total++;
                    }
                    */
                }
            }
            image = check_image;
            x_avg /= total;
            y_avg /= total;
            if(total > 0) {
                Graphics graphics = image.getGraphics();
                graphics.setColor(Color.RED);
                graphics.fillOval((int)x_avg, (int)y_avg, 40, 40);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public BufferedImage getImage() {
        return image;
    }
    public void setGrabDelay(int grab_delay) {
        this.grab_delay = grab_delay;
    }
    public void halt() {
        try {
            grabber.stop();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }
}

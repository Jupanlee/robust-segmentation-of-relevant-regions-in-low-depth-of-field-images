package others.doanloaded;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.image.ColorModel;

public class SSIM_index
        implements PlugIn
{
    protected ImagePlus image_1_imp;
    protected ImagePlus image_2_imp;
    protected ImageProcessor image_1_p;
    protected ImageProcessor image_2_p;

    public void run(String arg)
    {
        int[] wList = WindowManager.getIDList();
        if (wList == null) {
            IJ.error("There is no image open");
            return;
        }
        int a = WindowManager.getImageCount();
        if (a != 2) {
            IJ.error("There must be two images open to calculate SSIM index");
            return;
        }
        this.image_1_imp = WindowManager.getImage(wList[0]);
        this.image_2_imp = WindowManager.getImage(wList[1]);
        int image_height = this.image_1_imp.getHeight();
        a = this.image_2_imp.getHeight();
        if (a != image_height) {
            IJ.error("Both images must have the same height");
            return;
        }
        int image_width = this.image_1_imp.getWidth();
        a = this.image_2_imp.getWidth();
        if (a != image_width) {
            IJ.error("Both images must have the same width");
            return;
        }
        int bits_per_pixel_1 = this.image_1_imp.getBitDepth();
        int bits_per_pixel_2 = this.image_2_imp.getBitDepth();
        if (bits_per_pixel_1 != bits_per_pixel_2) {
            IJ.error("Both images must have the same number of bits per pixel");
            return;
        }
        if (bits_per_pixel_1 == 24) {
            IJ.error("RGB images are not supportedl");
            return;
        }

        double sigma_gauss = 1.5D;
        int filter_width = 11;
        int filter_scale = 20;
        double K1 = 0.01D;
        double K2 = 0.03D;
        double downsampled = image_height / 256;
        double downsampled_backup = downsampled;
        boolean gaussian_window = true;
        String[] window_type = { "Gaussian", "Same weight" };
        String window_selection = window_type[0];
        boolean out = false;
        boolean show_downsampled_images = false;
        boolean show_gaussian_filter = false;
        boolean show_ssim_map = false;

        while (!out) {
            out = true;
            GenericDialog gd = new GenericDialog("Filter parameters");
            gd.addNumericField("Standard deviation:", sigma_gauss, 1);
            gd.addChoice("Window type:", window_type, window_selection);
            gd.addNumericField("Filter width:", filter_width, 0);
            gd.addNumericField("K1:", K1, 2);
            gd.addNumericField("K2:", K2, 2);
            gd.addNumericField("View scale (downsampled by):", downsampled, 0);
            gd.addNumericField("Filter scale (for viewing gaussian filter):", filter_scale, 0);
            gd.addCheckbox("Show downsampled images", show_downsampled_images);
            gd.addCheckbox("Show SSIM map", show_ssim_map);
            gd.addCheckbox("Show gaussian filter", show_gaussian_filter);
            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
            }
            sigma_gauss = gd.getNextNumber();
            window_selection = gd.getNextChoice();
            filter_width = (int)gd.getNextNumber();
            K1 = gd.getNextNumber();
            K2 = gd.getNextNumber();
            downsampled = (int)gd.getNextNumber();
            filter_scale = (int)gd.getNextNumber();
            show_downsampled_images = gd.getNextBoolean();
            show_ssim_map = gd.getNextBoolean();
            show_gaussian_filter = gd.getNextBoolean();

            a = filter_width / 2;
            double d = filter_width - a * 2;
            if (window_selection != "Gaussian") {
                gaussian_window = false;
            }
            if (d == 0.0D) {
                IJ.error("Filter width and heigth must be odd");
                out = false;
            }
            if ((gaussian_window & sigma_gauss <= 0.0D)) {
                IJ.error("Sigma must be greater than 0");
                out = false;
            }
            if ((gaussian_window & filter_scale <= 0)) {
                IJ.error("Filter scale must be greater than 0");
                out = false;
            }
            if (downsampled > downsampled_backup) {
                IJ.error("Miminum height must be 256 pixels (review Viewing scale)");
                out = false;
            }
            if (downsampled < 1.0D) {
                IJ.error("Minimun value of Viewing scale must be 1");
                out = false;
            }
            gd.dispose();
        }
        double C1 = (Math.pow(2.0D, bits_per_pixel_1) - 1.0D) * K1;
        C1 *= C1;
        double C2 = (Math.pow(2.0D, bits_per_pixel_1) - 1.0D) * K2;
        C2 *= C2;

        int filter_length = filter_width * filter_width;
        float[] window_weights = new float[filter_length];
        double[] array_gauss_window = new double[filter_length];

        if (gaussian_window)
        {
            double distance = 0.0D;
            int center = filter_width / 2;
            double total = 0.0D;
            double sigma_sq = sigma_gauss * sigma_gauss;

            for (int y = 0; y < filter_width; y++) {
                for (int x = 0; x < filter_width; x++) {
                    distance = Math.abs(x - center) * Math.abs(x - center) + Math.abs(y - center) * Math.abs(y - center);
                    int pointer = y * filter_width + x;
                    array_gauss_window[pointer] = Math.exp(-0.5D * distance / sigma_sq);
                    total += array_gauss_window[pointer];
                }
            }
            for (int pointer = 0; pointer < filter_length; pointer++) {
                array_gauss_window[pointer] /= total;
                window_weights[pointer] = (float)array_gauss_window[pointer];
            }
        } else {
            for (int pointer = 0; pointer < filter_length; pointer++) {
                array_gauss_window[pointer] = (1.0D / filter_length);
                window_weights[pointer] = (float)array_gauss_window[pointer];
            }
        }
        if (show_gaussian_filter) {
            ColorModel cm = null;
            ImageProcessor gauss_window_ip = new FloatProcessor(filter_width, filter_width, window_weights, cm);
            gauss_window_ip = gauss_window_ip.resize(filter_width * filter_scale);
            String title_filtro_1 = "Sigma: " + sigma_gauss + " Width: " + filter_width + " p?xeles";
            ImagePlus gauss_window_imp = new ImagePlus(title_filtro_1, gauss_window_ip);
            gauss_window_imp.show();
            gauss_window_imp.updateAndDraw();
        }

        ImageProcessor image_1_original_p = this.image_1_imp.getProcessor();
        ImageProcessor image_2_original_p = this.image_2_imp.getProcessor();

        image_width = image_1_original_p.getWidth();
        image_width = (int)(image_width / downsampled);
        image_1_original_p.setInterpolate(true);
        image_2_original_p.setInterpolate(true);
        this.image_1_p = image_1_original_p.resize(image_width);
        this.image_2_p = image_2_original_p.resize(image_width);

        image_height = this.image_1_p.getHeight();
        image_width = this.image_1_p.getWidth();
        int image_dimension = image_width * image_height;

        ImageProcessor mu1_ip = new FloatProcessor(image_width, image_height);
        ImageProcessor mu2_ip = new FloatProcessor(image_width, image_height);
        float[] array_mu1_ip = (float[])(float[])mu1_ip.getPixels();
        float[] array_mu2_ip = (float[])(float[])mu2_ip.getPixels();

        float[] array_mu1_ip_copy = new float[image_dimension];
        float[] array_mu2_ip_copy = new float[image_dimension];
        int b;
        a = b = 0;
        for (int pointer = 0; pointer < image_dimension; pointer++)
        {
            if (bits_per_pixel_1 == 8) {
                a = 0xFF & this.image_1_p.get(pointer);
                b = 0xFF & this.image_2_p.get(pointer);
            }
            if (bits_per_pixel_1 == 16) {
                a = 0xFFFF & this.image_1_p.get(pointer);
                b = 0xFFFF & this.image_2_p.get(pointer);
            }
            if (bits_per_pixel_1 == 32) {
                a = this.image_1_p.get(pointer);
                b = this.image_2_p.get(pointer);
            }
            int tmp1229_1226 = a; array_mu1_ip_copy[pointer] = tmp1229_1226; array_mu1_ip[pointer] = tmp1229_1226;
            int tmp1243_1240 = b; array_mu2_ip_copy[pointer] = tmp1243_1240; array_mu2_ip[pointer] = tmp1243_1240;
        }
        mu1_ip.convolve(window_weights, filter_width, filter_width);
        mu2_ip.convolve(window_weights, filter_width, filter_width);

        double[] mu1_sq = new double[image_dimension];
        double[] mu2_sq = new double[image_dimension];
        double[] mu1_mu2 = new double[image_dimension];

        for (int pointer = 0; pointer < image_dimension; pointer++) {
            array_mu1_ip[pointer] *= array_mu1_ip[pointer];
            array_mu2_ip[pointer] *= array_mu2_ip[pointer];
            array_mu1_ip[pointer] *= array_mu2_ip[pointer];
        }

        double[] sigma1_sq = new double[image_dimension];
        double[] sigma2_sq = new double[image_dimension];
        double[] sigma12 = new double[image_dimension];

        for (int pointer = 0; pointer < image_dimension; pointer++)
        {
            array_mu1_ip_copy[pointer] *= array_mu1_ip_copy[pointer];
            array_mu2_ip_copy[pointer] *= array_mu2_ip_copy[pointer];
            array_mu1_ip_copy[pointer] *= array_mu2_ip_copy[pointer];
        }

        ImageProcessor soporte_1_ip = new FloatProcessor(image_width, image_height);
        ImageProcessor soporte_2_ip = new FloatProcessor(image_width, image_height);
        ImageProcessor soporte_3_ip = new FloatProcessor(image_width, image_height);
        float[] array_soporte_1 = (float[])(float[])soporte_1_ip.getPixels();
        float[] array_soporte_2 = (float[])(float[])soporte_2_ip.getPixels();
        float[] array_soporte_3 = (float[])(float[])soporte_3_ip.getPixels();

        for (int pointer = 0; pointer < image_dimension; pointer++) {
            array_soporte_1[pointer] = (float)sigma1_sq[pointer];
            array_soporte_2[pointer] = (float)sigma2_sq[pointer];
            array_soporte_3[pointer] = (float)sigma12[pointer];
        }
        soporte_1_ip.convolve(window_weights, filter_width, filter_width);
        soporte_2_ip.convolve(window_weights, filter_width, filter_width);
        soporte_3_ip.convolve(window_weights, filter_width, filter_width);

        for (int pointer = 0; pointer < image_dimension; pointer++) {
            array_soporte_1[pointer] -= mu1_sq[pointer];
            array_soporte_2[pointer] -= mu2_sq[pointer];
            array_soporte_3[pointer] -= mu1_mu2[pointer];
        }
        double[] ssim_map = new double[image_dimension];
        double suma = 0.0D;
        for (int pointer = 0; pointer < image_dimension; pointer++) {
            ssim_map[pointer] = ((2.0D * mu1_mu2[pointer] + C1) * (2.0D * sigma12[pointer] + C2) / ((mu1_sq[pointer] + mu2_sq[pointer] + C1) * (sigma1_sq[pointer] + sigma2_sq[pointer] + C2)));
            suma += ssim_map[pointer];
        }
        double ssim_index = suma / image_dimension;
        String message_1 = " ";
        if (show_ssim_map) {
            ImageProcessor ssim_map_ip = new FloatProcessor(image_width, image_height, ssim_map);
            message_1 = "SSIM Index:   " + ssim_index;
            ImagePlus ssim_map_imp = new ImagePlus(message_1, ssim_map_ip);
            ssim_map_imp.show();
            ssim_map_imp.updateAndDraw();
        }
        if (show_downsampled_images) {
            String title_1 = this.image_1_imp.getTitle();
            String title_2 = this.image_2_imp.getTitle();
            title_1 = title_1 + " down scaled " + downsampled + " times";
            title_2 = title_2 + " down scaled " + downsampled + " times";
            ImagePlus image_1_final_imp = new ImagePlus(title_1, this.image_1_p);
            image_1_final_imp.show();
            image_1_final_imp.updateAndDraw();
            ImagePlus image_2_final_imp = new ImagePlus(title_2, this.image_2_p);
            image_2_final_imp.show();
            image_2_final_imp.updateAndDraw();
        }
        message_1 = " ";
        String message_2 = "ssim_index:  " + ssim_index;
        IJ.showProgress(1.0D);
        IJ.showMessage(message_1, message_2);
    }
}
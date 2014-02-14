package edu.clarkson.cs.wpcomp.img.desc;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.telmomenezes.jfastemd.JFastEMD;
import com.telmomenezes.jfastemd.Signature;

import edu.clarkson.cs.wpcomp.img.accessor.ImageAccessor;
import edu.clarkson.cs.wpcomp.img.desc.descriptor.GradientEMDDescriptor;
import edu.clarkson.cs.wpcomp.img.desc.descriptor.HogSVMDescriptor;
import edu.clarkson.cs.wpcomp.img.transform.ImageTransformer;

public class ImageDescriptor {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BufferedImage bw = ImageIO
				.read(new File(
						"/home/harper/Research/webpage-comparison/imageset_test/12014939095_0c6dd934d6_b.jpg"));
		System.out.println(bw.getType());
		System.out.println(bw.getColorModel().getNumComponents());
		System.out.println(bw.getSampleModel().getNumBands());
		System.out.println(bw.getSampleModel().getDataType());
		BufferedImage scaled = ImageTransformer.scale(bw, 500, 500);
		ImageIO.write(scaled, "png", new File("test.png"));
	}

	protected static void descripeHog() throws Exception {
		BufferedImage img = ImageIO.read(new File("res/sample.jpg"));
		ImageAccessor accessor = new ImageAccessor(img);

		System.out.println(System.currentTimeMillis());
		HogSVMDescriptor descriptor = new HogSVMDescriptor();
		Feature result = descriptor.describe(accessor);
		System.out.println(result.size());
		System.out.println(System.currentTimeMillis());
	}

	protected static void describeGradient() throws Exception {
		BufferedImage ebay1 = ImageTransformer.scale(
				ImageIO.read(new File("res/image/ebay_1.jpg")), 300, 300);
		BufferedImage ebay1_trans = ImageTransformer.scale(
				ImageTransformer.transform(ebay1,
						AffineTransform.getTranslateInstance(1, 1)), 300, 300);
		BufferedImage ebay2 = ImageTransformer.scale(
				ImageIO.read(new File("res/image/ebay_2.jpg")), 300, 300);
		BufferedImage ebay3 = ImageTransformer.scale(
				ImageIO.read(new File("res/image/ebay_3.png")), 300, 300);
		BufferedImage google = ImageTransformer.scale(
				ImageIO.read(new File("res/image/logo11w.png")), 300, 300);

		// ImageIO.write(GradientHelper.gradientImage(ebay1), "png", new File(
		// "res/output/ebay1.png"));
		// ImageIO.write(GradientHelper.gradientImage(ebay1_trans), "png",
		// new File("res/output/ebay1_trans.png"));
		// ImageIO.write(GradientHelper.gradientImage(ebay2), "png", new File(
		// "res/output/ebay2.png"));
		// ImageIO.write(GradientHelper.gradientImage(ebay3), "png", new File(
		// "res/output/ebay3.png"));
		// ImageIO.write(GradientHelper.gradientImage(google), "png", new File(
		// "res/output/google.png"));

		GradientEMDDescriptor gradesc = new GradientEMDDescriptor(10, 5);

		System.out.println(System.currentTimeMillis());
		Signature ebay1sig = gradesc.describe(new ImageAccessor(ebay1));
		Signature ebay1_trans_sig = gradesc.describe(new ImageAccessor(
				ebay1_trans));
		Signature ebay2sig = gradesc.describe(new ImageAccessor(ebay2));
		Signature ebay3sig = gradesc.describe(new ImageAccessor(ebay3));
		Signature googlesig = gradesc.describe(new ImageAccessor(google));
		System.out.println(System.currentTimeMillis());

		System.out.println(JFastEMD.distance(ebay1sig, ebay1_trans_sig, 1));
		System.out.println(System.currentTimeMillis());

		System.out.println(JFastEMD.distance(ebay1sig, ebay2sig, 1));
		System.out.println(System.currentTimeMillis());

		System.out.println(JFastEMD.distance(ebay1sig, ebay3sig, 1));
		System.out.println(System.currentTimeMillis());

		System.out.println(JFastEMD.distance(ebay1sig, googlesig, 1));
		System.out.println(System.currentTimeMillis());
	}
}

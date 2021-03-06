package edu.clarkson.cs.leo;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import edu.clarkson.cs.leo.img.ImageHelper;
import edu.clarkson.cs.leo.img.splitcombine.Combine;
import edu.clarkson.cs.leo.img.splitcombine.Split;
import edu.clarkson.cs.leo.svm.Classifier;
import edu.clarkson.cs.leo.svm.DataSet;
import edu.clarkson.cs.leo.svm.FileDataSet;
import edu.clarkson.cs.leo.svm.FileModel;
import edu.clarkson.cs.leo.svm.Model;
import edu.clarkson.cs.leo.svm.DataSet.Row;
import edu.clarkson.cs.leo.svm.libsvm.LibSVMClassifier;
import edu.clarkson.cs.leo.task.GenTestSet;
import edu.clarkson.cs.leo.task.Input;
import edu.clarkson.cs.leo.task.Input.ImageInput;

public class PhishMain {

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		File workdir = new File("workdir");

		BufferedImage input = ImageIO.read(new File(
				"res/image/split/phishing.png"));

		Split split = new Split();
		Combine combine = new Combine();

		List<Rectangle> ranges = split.split(input);
		List<Rectangle> combined = combine.combine(ranges);

		Classifier classifier = new LibSVMClassifier();
		Model model = new FileModel(new File("workdir/model/logo_train.model"));

		List<Input> inputImages = new ArrayList<Input>();
		File test = new File(workdir.getAbsolutePath() + File.separator
				+ "test");
		for (Rectangle rect : combined) {
			BufferedImage part = ImageHelper.crop(input, rect);
			inputImages.add(new ImageInput(part, 0));
		}

		GenTestSet.generate(inputImages, test);

		DataSet output = classifier.classify(model, new FileDataSet(test));
		for(Row row: output) {
			if(Integer.valueOf(String.valueOf(row.get(0))) != 0) {
				System.err.println("Phishing Match found!");
			}
		}
	}

}

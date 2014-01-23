package edu.clarkson.cs.wpcomp.svm.libsvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.AfterClass;
import org.junit.Test;

import edu.clarkson.cs.wpcomp.svm.DataSet;
import edu.clarkson.cs.wpcomp.svm.FileDataSet;
import edu.clarkson.cs.wpcomp.svm.Model;

public class LibSVMClassifierTest extends LibSVMClassifier {

	@Test
	public void testClassify() throws IOException {
		LibSVMTrainer trainer = new LibSVMTrainer();
		Model model = trainer.train(new FileDataSet(new File("res/svm/train")));
		LibSVMClassifier classifier = new LibSVMClassifier();
		DataSet result = classifier.classify(model, new FileDataSet(new File(
				"res/svm/test")));
		assertEquals(result.getClass(), FileDataSet.class);
		FileDataSet resultds = (FileDataSet) result;
		assertEquals(resultds.getFile().getAbsolutePath(), new File(
				"res/svm/test.output").getAbsolutePath());
		assertTrue(resultds.getFile().exists());

		// There should be 90 positive and 90 negative
		BufferedReader read = new BufferedReader(new InputStreamReader(
				new FileInputStream(resultds.getFile())));

		String line = null;
		int count = 0;
		while (null != (line = read.readLine())) {
			int value = Integer.parseInt(line);
			assertTrue((count < 90 && value == 1)
					|| (count >= 90 && value == 0));
			count++;
		}

		read.close();
	}

	@AfterClass
	public static void clear() {
		new File("res/svm/test.output").delete();
		new File("res/svm/train.model").delete();
	}
}

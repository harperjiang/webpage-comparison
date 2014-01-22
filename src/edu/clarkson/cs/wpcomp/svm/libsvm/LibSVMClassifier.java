package edu.clarkson.cs.wpcomp.svm.libsvm;

import java.io.File;

import org.slf4j.LoggerFactory;

import edu.clarkson.cs.wpcomp.common.proc.OutputHandler;
import edu.clarkson.cs.wpcomp.common.proc.ProcessRunner;
import edu.clarkson.cs.wpcomp.svm.Classifier;
import edu.clarkson.cs.wpcomp.svm.DataSet;
import edu.clarkson.cs.wpcomp.svm.FileDataSet;
import edu.clarkson.cs.wpcomp.svm.FileModel;
import edu.clarkson.cs.wpcomp.svm.Model;

public class LibSVMClassifier implements Classifier {

	private static final String SVM_PREDICT = "svm-predict";

	@Override
	public DataSet classify(Model model, DataSet input) {
		if (!(input instanceof FileDataSet)) {
			throw new IllegalArgumentException();
		}
		FileDataSet inputFds = (FileDataSet) input;
		if (!(model instanceof FileModel)) {
			throw new IllegalArgumentException();
		}
		FileModel fModel = (FileModel) model;

		File output = new File(inputFds.getFile().getAbsolutePath() + ".output");

		ProcessRunner runner = new ProcessRunner(
				Configuration.ROOT_FOLDER.getAbsolutePath() + File.separator
						+ SVM_PREDICT, inputFds.getFile().getAbsolutePath(),
				fModel.getFile().getAbsolutePath(), output.getAbsolutePath());

		runner.setHandler(new OutputHandler() {
			@Override
			public void output(String input) {
				// TODO Handle error input
			}
		});
		try {
			runner.runAndWait();
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error(
					"Exception while executing predict", e);
			throw new RuntimeException(e);
		}

		return new FileDataSet(output);
	}

}
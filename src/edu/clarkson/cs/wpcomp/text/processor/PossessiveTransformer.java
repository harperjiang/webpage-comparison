package edu.clarkson.cs.wpcomp.text.processor;

public class PossessiveTransformer implements Transformer {

	@Override
	public String transform(String input) {
		if (input.endsWith("'s")) {
			String remain = input.substring(0, input.length() - 2);
			if (WordDict.getInstance().isWord(remain)) {
				return remain;
			}
		}
		if (input.endsWith("'")) {
			String remain = input.substring(0, input.length() - 1);
			if (WordDict.getInstance().isWord(remain)) {
				return remain;
			}
		}
		return null;
	}

}

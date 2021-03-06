package edu.clarkson.cs.leo.img;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.imageio.ImageIO;

import org.junit.Test;

import edu.clarkson.cs.leo.tools.RandomPictureGenerator;

public class RandomPictureGeneratorTest {

	@Test
	public void testGenerate() throws IOException {
		for (int i = 0; i < 1000; i++) {
			BufferedImage random = RandomPictureGenerator
					.generate(new Dimension(500, 500));
			ImageIO.write(
					random,
					"png",
					new File(MessageFormat
							.format("res/image/set/img{0}.png", i)));
		}
	}

}

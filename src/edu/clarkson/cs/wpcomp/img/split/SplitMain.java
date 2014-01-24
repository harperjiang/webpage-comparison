package edu.clarkson.cs.wpcomp.img.split;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import edu.clarkson.cs.wpcomp.img.GradientHelper;
import edu.clarkson.cs.wpcomp.img.MarkHelper;
import edu.clarkson.cs.wpcomp.img.accessor.ColorAccessor;
import edu.clarkson.cs.wpcomp.img.accessor.ImageAccessor;

public class SplitMain {

	public static void main(String[] args) throws IOException {
		BufferedImage input = ImageIO.read(new File(
				"res/image/split/bordered.png"));

		BufferedImage gradient = GradientHelper.gradientImage(input, 0);

		ColorAccessor accessor = new ImageAccessor(gradient);

		RectangleSplitter rect = new RectangleSplitter(accessor);
		LineSplitter line = new LineSplitter(accessor);

		List<Rectangle> source = new ArrayList<Rectangle>();
		List<Rectangle> result = new ArrayList<Rectangle>();
		source.add(new Rectangle(0, 0, accessor.getWidth(), accessor
				.getHeight()));

		int depth = 4;

		for (int i = 0; i < depth; i++) {
			for (Rectangle r : source) {
				Rectangle fence = rect.lowerbound(r);
				MarkHelper.redrect(fence, accessor);
				LineSegment lc = line.maxmarginsplit(fence);
				if (lc != null) {
					if (lc.isHorizontal()) {
						Rectangle top = rect.lowerbound(new Rectangle(fence.x,
								fence.y, fence.width, lc.from.y - fence.y));
						if (null != top)
							result.add(top);
						Rectangle bottom = rect.lowerbound(new Rectangle(
								fence.x, lc.from.y, fence.width, fence.y
										+ fence.height - lc.from.y));
						if (null != bottom)
							result.add(bottom);
					}
					if (lc.isVertical()) {
						Rectangle left = rect.lowerbound(new Rectangle(fence.x,
								fence.y, lc.from.x - fence.x, fence.height));
						if (null != left)
							result.add(left);
						Rectangle right = rect.lowerbound(new Rectangle(
								lc.from.x, fence.y, fence.x + fence.width
										- lc.from.x, fence.height));
						if (null != right)
							result.add(right);
					}
					MarkHelper.redline(lc, accessor);
				} else {
					// Nonsplittable
					result.add(fence);
				}
			}
			source = result;
			result = new ArrayList<Rectangle>();
		}

		ImageIO.write(gradient, "png", new File(
				"res/image/split/phishing_split.png"));
	}
}

package edu.clarkson.cs.wpcomp.img;

import java.awt.Rectangle;

import edu.clarkson.cs.wpcomp.img.splitcombine.LineSegment;

public class GeometryHelper {

	public static Rectangle[] split(Rectangle r, LineSegment split) {
		if (split.isVertical()) {
			return new Rectangle[] {
					new Rectangle(r.x, r.y, split.from.x - r.x, r.height),
					new Rectangle(split.from.x, r.y, r.width + r.x
							- split.from.x, r.height) };
		} else if (split.isHorizontal()) {
			return new Rectangle[] {
					new Rectangle(r.x, r.y, r.width, split.from.y - r.y),
					new Rectangle(r.x, split.from.y, r.width, r.y + r.height
							- split.from.y) };
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static Integer area(Rectangle r) {
		if (null == r)
			return 0;
		return r.width * r.height;
	}

	public static boolean contains(Rectangle parent, Rectangle child) {
		return parent.x <= child.x && parent.y <= child.y
				&& parent.width >= child.width && parent.height >= child.height;
	}

	public static Rectangle cover(Rectangle... rects) {
		if (rects == null || rects.length == 0)
			return null;
		if (rects.length == 1)
			return rects[0];
		if (rects.length == 2) {
			Rectangle a = rects[0];
			Rectangle b = rects[1];
			int x = Math.min(a.x, b.x);
			int y = Math.min(a.y, b.y);
			return new Rectangle(x, y, Math.max(a.x + a.width, b.x + b.width)
					- x, Math.max(a.y + a.height, b.y + b.height) - y);
		}
		Rectangle[] newarray = new Rectangle[rects.length - 1];
		System.arraycopy(rects, 0, newarray, 0, rects.length - 2);
		newarray[newarray.length - 1] = cover(rects[rects.length - 2],
				rects[rects.length - 1]);
		return cover(newarray);
	}
}

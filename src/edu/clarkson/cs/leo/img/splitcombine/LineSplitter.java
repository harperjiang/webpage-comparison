package edu.clarkson.cs.leo.img.splitcombine;

import java.awt.Point;
import java.awt.Rectangle;

import edu.clarkson.cs.leo.img.accessor.ColorAccessor;

public class LineSplitter extends AbstractSplitter {

	public LineSplitter(ColorAccessor accessor) {
		super(new SplitCore(accessor));
	}

	public LineSplitter(SplitCore core) {
		super(core);
	}

	public LineSegment centralSplit(Rectangle range) {
		return split(range, new CentralSplitCondition(), null);
	}

	public LineSegment maxMarginSplit(Rectangle range) {
		return split(range, new MaxMarginSplitCondition(), null);
	}

	public LineSegment maxMarginSplit(Rectangle range, boolean preferH) {
		return split(range, new MaxMarginSplitCondition(), preferH);
	}

	public LineSegment firstSplit(Rectangle range) {
		return split(range, new FirstSplitCondition(), null);
	}

	public LineSegment split(Rectangle range, SplitCondition condition,
			Boolean preferH) {
		if (null == range) {
			range = new Rectangle(0, 0, getAccessor().getWidth(), getAccessor()
					.getHeight());
		}
		condition.setRange(range);
		LineSegment vs = vsplit(range, condition);
		LineSegment hs = hsplit(range, condition);
		if (preferH != null) {
			return preferH ? hs : vs;
		}
		if (vs == null || hs == null) {
			return vs == null ? hs : vs;
		}
		condition.setHorizontal(false);
		int vratio = condition.bias(vs.from.x);
		condition.setHorizontal(true);
		int hratio = condition.bias(hs.from.y);
		return vratio > hratio ? hs : vs;
	}

	protected LineSegment vsplit(Rectangle range, SplitCondition condition) {
		condition.setHorizontal(false);
		int bias = Integer.MAX_VALUE;
		int record = -1;
		for (int i = range.x + 1; i < range.x + range.width - 1; i++) {
			int fb = condition.fastbreak(i, bias);
			if (fb != i) {
				i = fb;
			}
			if (condition.satisfy(i)) {
				// Candidate
				int thisbias = condition.bias(i);
				if (thisbias < bias) {
					bias = thisbias;
					record = i;
				}
			}
		}
		if (record == -1)
			return null;
		record = condition.postprocess(record);
		return new LineSegment(new Point(record, range.y), new Point(record,
				range.y + range.height));
	}

	protected LineSegment hsplit(Rectangle range, SplitCondition condition) {
		condition.setHorizontal(true);
		int bias = Integer.MAX_VALUE;
		int record = -1;
		for (int i = range.y + 1; i < range.y + range.height - 1; i++) {
			int fb = condition.fastbreak(i, bias);
			if (fb != i) {
				i = fb;
			}
			if (condition.satisfy(i)) {
				// Candidate
				int thisbias = condition.bias(i);
				if (thisbias < bias) {
					bias = thisbias;
					record = i;
				}
			}
		}
		if (-1 == record) {
			return null;
		}
		record = condition.postprocess(record);
		return new LineSegment(new Point(range.x, record), new Point(range.x
				+ range.width, record));
	}

	protected abstract class SplitCondition {

		protected Rectangle range;

		protected boolean horizontal;

		public SplitCondition() {
		}

		public boolean isHorizontal() {
			return horizontal;
		}

		public void setHorizontal(boolean horizontal) {
			this.horizontal = horizontal;
		}

		public Rectangle getRange() {
			return range;
		}

		public void setRange(Rectangle range) {
			this.range = range;
		}

		public abstract boolean satisfy(int point);

		public abstract int bias(int point);

		public abstract int fastbreak(int point, int maxbias);

		public abstract int postprocess(int point);
	}

	protected abstract class MarginSplitCondition extends SplitCondition {
		protected int[] margin(int point) {
			if (isHorizontal()) {
				int top, bottom;
				for (top = point; top > range.y
						&& core.preprocess[range.x][top].width >= core.preprocess[range.x][point].width; top--)
					;
				for (bottom = point; bottom < range.y + range.height - 1
						&& core.preprocess[range.x][bottom].width >= core.preprocess[range.x][point].width; bottom++)
					;
				return new int[] { top, bottom };
			} else {
				int left, right;
				for (left = point; left > range.x
						&& core.preprocess[left][range.y].height >= core.preprocess[point][range.y].height; left--)
					;
				for (right = point; right < range.x + range.width - 1
						&& core.preprocess[right][range.y].height >= core.preprocess[point][range.y].height; right++)
					;
				return new int[] { left, right };
			}
		}
	}

	public class CentralSplitCondition extends SplitCondition {
		@Override
		public boolean satisfy(int point) {
			if (isHorizontal()) {
				return core.preprocess[range.x][point].width >= range.width;
			} else {
				return core.preprocess[point][range.y].height >= range.height;
			}
		}

		@Override
		public int bias(int point) {
			if (isHorizontal()) {
				return Math.abs(point - (range.y + range.height / 2));
			} else {
				return Math.abs(point - (range.x + range.width / 2));
			}
		}

		@Override
		public int fastbreak(int point, int maxbias) {
			if (isHorizontal()) {
				return (point - (range.y + range.height / 2)) > maxbias ? (range.y
						+ range.height - 1)
						: point;
			} else {
				return (point - (range.x + range.width / 2)) > maxbias ? (range.x
						+ range.width - 1)
						: point;
			}
		}

		@Override
		public int postprocess(int point) {
			if (isHorizontal()) {
				int top, bottom;
				for (top = point; top > 0
						&& core.preprocess[range.x][top].width >= core.preprocess[range.x][point].width; top--)
					;
				for (bottom = point; bottom < range.y + range.height - 1
						&& core.preprocess[range.x][bottom].width >= core.preprocess[range.x][point].width; bottom++)
					;
				return (top + bottom) / 2;
			} else {
				int left, right;
				for (left = point; left > 0
						&& core.preprocess[left][range.y].height >= core.preprocess[point][range.y].height; left--)
					;
				for (right = point; right < range.x + range.width - 1
						&& core.preprocess[right][range.y].height >= core.preprocess[point][range.y].height; right++)
					;
				return (left + right) / 2;
			}
		}
	}

	public class MaxMarginSplitCondition extends MarginSplitCondition {

		@Override
		public boolean satisfy(int point) {
			if (isHorizontal()) {
				return core.preprocess[range.x][point].width >= range.width;
			} else {
				return core.preprocess[point][range.y].height >= range.height;
			}
		}

		@Override
		public int bias(int point) {
			int[] margin = margin(point);
			return -(margin[1] - margin[0] - 2);
		}

		@Override
		public int fastbreak(int point, int maxbias) {
			if (!satisfy(point))
				return point;
			int[] margin = margin(point);
			return margin[1] - 1;
		}

		@Override
		public int postprocess(int point) {
			int[] margin = margin(point);
			return (margin[0] + margin[1]) / 2;
		}
	}

	public class FirstSplitCondition extends MarginSplitCondition {

		private int firstPoint = -1;

		@Override
		public boolean satisfy(int point) {
			if (firstPoint != -1) {
				boolean satisfy = false;
				if (isHorizontal()) {
					satisfy = core.preprocess[range.x][point].width >= range.width;
				} else {
					satisfy = core.preprocess[point][range.y].height >= range.height;
				}
				firstPoint = point;
				return satisfy;
			} else {
				return false;
			}
		}

		@Override
		public int bias(int point) {
			return firstPoint == point ? 0 : Integer.MAX_VALUE;
		}

		@Override
		public int fastbreak(int point, int maxbias) {
			if (firstPoint != -1) {
				// Jump to end
				return isHorizontal() ? range.y + range.height - 1 : range.x
						+ range.width - 1;
			}
			return point;
		}

		@Override
		public int postprocess(int point) {
			int[] margin = margin(point);
			return (margin[0] + margin[1]) / 2;
		}

	}
}

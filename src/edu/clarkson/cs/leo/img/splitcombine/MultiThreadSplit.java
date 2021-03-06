package edu.clarkson.cs.leo.img.splitcombine;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import edu.clarkson.cs.leo.img.GeometryHelper;
import edu.clarkson.cs.leo.img.GradientHelper;
import edu.clarkson.cs.leo.img.accessor.ColorAccessor;
import edu.clarkson.cs.leo.img.accessor.ImageAccessor;
import edu.clarkson.cs.leo.img.splitcombine.filter.Filter;
import edu.clarkson.cs.leo.img.splitcombine.filter.FilterResult;
import edu.clarkson.cs.leo.img.splitcombine.filter.SizeFilter;
import edu.clarkson.cs.leo.img.splitcombine.filter.TextFilter;

public class MultiThreadSplit {

	private List<Filter> filters;

	private RectangleSplitter rect;

	private LineSplitter line;

	private SplitEnv cenv;

	private ExecutorService threadPool;

	public MultiThreadSplit() {
		super();
		filters = new ArrayList<Filter>();
		filters.add(new SizeFilter());
		filters.add(new TextFilter());

		threadPool = Executors.newFixedThreadPool(4, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
	}

	public List<Rectangle> split(BufferedImage input) throws Exception {
		BufferedImage gradient = GradientHelper.gradientImage(input, 20);
		ColorAccessor accessor = new ImageAccessor(gradient);

		rect = new RectangleSplitter(accessor);
		line = new LineSplitter(accessor);

		cenv = new SplitEnv();
		cenv.lineSplitter = line;
		cenv.rectSplitter = rect;
		cenv.sourceImage = gradient;

		List<Rectangle> source = new ArrayList<Rectangle>();
		List<Rectangle> mature = new Vector<Rectangle>();
		source.add(new Rectangle(0, 0, accessor.getWidth(), accessor
				.getHeight()));

		while (!source.isEmpty()) {
			List<Task> tasks = new ArrayList<Task>();
			for (Rectangle r : source) {
				tasks.add(new Task(cenv, r, mature));
			}
			List<Future<List<Rectangle>>> results = threadPool.invokeAll(tasks);
			source = new ArrayList<Rectangle>();
			for (Future<List<Rectangle>> future : results) {
				source.addAll(future.get());
			}
		}
		return mature;
	}

	private class Task implements Callable<List<Rectangle>> {

		SplitEnv cenv;

		Rectangle rect;

		List<Rectangle> mature;

		public Task(SplitEnv cenv, Rectangle rect, List<Rectangle> mature) {
			this.cenv = cenv;
			this.rect = rect;
			this.mature = mature;
		}

		@Override
		public List<Rectangle> call() throws Exception {
			List<Rectangle> result = new ArrayList<Rectangle>();
			Rectangle fence = cenv.rectSplitter.lowerBound(rect);
			if (fence == null)
				// Blank Rectangle
				return result;
			// Linear split
			LineSegment lc = line.maxMarginSplit(fence);
			if (null != lc) {
				Rectangle[] split = GeometryHelper.split(fence, lc);
				Rectangle top = cenv.rectSplitter.lowerBound(split[0]);
				Rectangle bottom = cenv.rectSplitter.lowerBound(split[1]);

				if (null != top)
					addResult(result, top);
				if (null != bottom)
					addResult(result, bottom);
				return result;
			}

			MaxSplitProcessor msp = new MaxSplitProcessor(fence, cenv);
			// Border Removal
			Rectangle removed = msp.removeBorder();
			if (!removed.equals(fence)) {
				addResult(result, removed);
				return result;
			}
			// Rectangle Split, applied to big image
			List<Rectangle> maxSplitResult = msp.process();
			if (maxSplitResult != null) {
				for (Rectangle msr : maxSplitResult) {
					addResult(result, msr);
				}
				return result;
			}

			// Rectangle split, applied to images like search box
			if (fence.height >= searchBoxRange[0]
					&& fence.height <= searchBoxRange[1]
					&& (fence.getWidth() / fence.getHeight()) >= searchBoxRatio) {
				Rectangle rectsplit = cenv.rectSplitter.maxSplit(fence);
				if (null != rectsplit) {
					if (rectsplit.x - fence.x <= searchBoxBorder
							&& rectsplit.y - fence.y <= searchBoxBorder
							&& fence.y + fence.height - rectsplit.x
									- rectsplit.height <= searchBoxBorder
							&& fence.x + fence.width - rectsplit.x
									- rectsplit.width < fence.width
									* searchBoxButtonRatio) {
						addResult(result, rectsplit);
						return result;
					}
				}
			}

			// All methods tried, this area is thought to be non-splittable
			mature.add(fence);
			return result;
		}

		protected void addResult(List<Rectangle> result, Rectangle range) {
			FilterResult fresult = new FilterResult();
			fresult.getAccepted().add(range);
			for (Filter filter : filters) {
				List<Rectangle> output = new ArrayList<Rectangle>();
				for (Rectangle r : fresult.getAccepted()) {
					FilterResult fr = filter.filter(r, cenv);
					output.addAll(fr.getAccepted());
					fresult.getMatured().addAll(fr.getMatured());
				}
				fresult.getAccepted().clear();
				fresult.getAccepted().addAll(output);
			}
			mature.addAll(fresult.getMatured());
			result.addAll(fresult.getAccepted());
		}

	}

	public SplitEnv getCenv() {
		return cenv;
	}

	private int[] searchBoxRange = { 15, 40 };

	private double searchBoxRatio = 5;

	private int searchBoxBorder = 5;

	private double searchBoxButtonRatio = 0.3d;
}

package tintor.apps.traveling_salesman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import tintor.util.UnionFind;

public class TSP_23opt {
	static int n;
	static double[] x, y;

	static int[] tour;
	static double length;

	static Random rand = new Random(3);

	public static void main(final String[] args) {
		//load("d:/ar9152.tsp");
		//load("d:/nu3496.tsp");
		loadRand(5000);
		//loadMatrix(8, 8, 15);
		//load("d:/mona-lisa100K.tsp");

		init(true, false, false);
		//output(true);
		improve();
	}

	static long timeOfLastOutput = Long.MIN_VALUE;
	static int iter = 0;

	static void info(final int run) {
		if (run > 0) {
			iter += 1;
		}
		System.out.printf("[%s] %s %s", run, iter, length);
		output(run == 0);
		System.out.println();
	}

	static void improve() {
		boolean again;
		do {
			again = false;
			do {
				again = false;
				for (int z = 2; z < 1 + n / 2; z++) {
					for (int a = 0; a < n; a++)
						if (try2opt(a, (a + z) % n)) {
							info(2);
							again = true;
						}
				}
			} while (again);

			//			for (int w = 4; w < n; w++)
			//				for (int z = 2; z < w - 1; z++)
			//					for (int a = (w == n - 1) ? 1 : 0; a < n - w; a++) {
			//						if (try3opt(a, a + z, a + w)) {
			//							info(3);
			//							again = true;
			//						}
			//					}
		} while (again);
		info(0);
	}

	static boolean try2opt(final int a, final int b) {
		final int ap = (a + 1) % n;
		final int bp = (b + 1) % n;

		final double q = distance(a, ap) + distance(b, bp);
		final double ab = distance(a, b) + distance(ap, bp) - q;
		if (ab >= 0)
			return false;

		length += ab;
		flip(ap, b);
		return true;
	}

	static double[] abc = new double[4];

	static boolean try3opt(final int a, final int b, final int c) {
		final int ap = a + 1;
		final int bp = b + 1;
		final int cp = (c + 1) % n;

		int min = 0;

		// ab_bc_ca
		abc[0] = distance(a, bp) + distance(b, cp) + distance(c, ap);
		// ab_bc
		abc[1] = distance(a, b) + distance(c, ap) + distance(bp, cp);
		// bc_ca
		abc[2] = distance(a, bp) + distance(ap, cp) + distance(b, c);
		// ab_ca
		abc[3] = distance(a, c) + distance(ap, bp) + distance(b, cp);

		for (int i = 1; i <= 3; i++)
			if (abc[i] < abc[min]) {
				min = i;
			}

		final double q = distance(a, ap) + distance(b, bp) + distance(c, cp);
		if (abc[min] >= q)
			return false;

		length += abc[min] - q;

		switch (min) {
		case 0:
			flip(ap, b);
			flip(bp, c);
			flip(cp, a);
			break;
		case 1:
			flip(ap, b);
			flip(bp, c);
			break;
		case 2:
			flip(bp, c);
			flip(cp, a);
			break;
		case 3:
			flip(ap, b);
			flip(cp, a);
			break;
		}

		return true;
	}

	static void flip(final int a, final int b) {
		final int z = (b - a + 1 + n) % n / 2;
		for (int i = 0; i < z; i++) {
			swap((a + i) % n, (b + n - i) % n);
		}
	}

	static void swap(final int a, final int b) {
		final int t = tour[a];
		tour[a] = tour[b];
		tour[b] = t;
	}

	static void output(final boolean end) {
		// Don't output more than once in each minute
		final long time = System.currentTimeMillis();
		if (!end && time < timeOfLastOutput + 30000)
			return;

		timeOfLastOutput = time;
		System.out.print(" saved");

		double xmin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < n; i++) {
			xmin = Math.min(xmin, x[i]);
			xmax = Math.max(xmax, x[i]);
			ymin = Math.min(ymin, y[i]);
			ymax = Math.max(ymax, y[i]);
		}

		final double strokeWidth = Math.max((xmax - xmin) / 1650, (ymax - ymin) / 1024);

		try {
			final BufferedWriter w = new BufferedWriter(new FileWriter("c:/out.svg"));
			w.append("<?xml version=\"1.0\" standalone=\"no\"?>\n");
			w.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"");
			w.append(" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
			final String box = xmin + " " + ymin + " " + (xmax - xmin) + " " + (ymax - ymin);
			w.append("<svg viewBox=\"" + box + "\" version=\"1.1\">\n<path d=\"M ");
			w.append(x[tour[n - 1]] + " " + y[tour[n - 1]]);
			for (int i = 0; i < n; i++) {
				w.append("L" + x[tour[i]] + " " + y[tour[i]]);
			}
			w.append("\" stroke=\"black\" stroke-width=\"" + strokeWidth + "\" fill=\"yellow\"/>\n</svg>");
			w.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	static double[] dist;

	static class Edge implements Comparable<Edge> {
		int a, b;
		double dist;

		public int compareTo(final Edge e) {
			return Double.compare(dist, e.dist);
		}
	}

	static void zsort(final int offset, final int size, final int depth) {
		if (size < 2 || depth == 0)
			return;

		double xmin = Double.POSITIVE_INFINITY, xmax = Double.NEGATIVE_INFINITY;
		double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
		for (int i = offset; i < offset + size; i++) {
			xmin = Math.min(xmin, x[tour[i]]);
			xmax = Math.max(xmax, x[tour[i]]);
			ymin = Math.min(ymin, y[tour[i]]);
			ymax = Math.max(ymax, y[tour[i]]);
		}

		final double mx = (xmin + xmax) / 2, my = (ymin + ymax) / 2;

		int a = 0;
		for (int i = offset; i < offset + size; i++)
			if (x[tour[i]] <= mx && y[tour[i]] <= my) {
				swap(i, offset + a++);
			}
		for (int i = offset; i < offset + a; i++) {
			final double xx = x[tour[i]], yy = y[tour[i]];
			x[tour[i]] = yy;
			y[tour[i]] = xx;
		}
		zsort(offset, a, depth - 1);

		int b = 0;
		for (int i = offset + a; i < offset + size; i++)
			if (x[tour[i]] <= mx && y[tour[i]] > my) {
				swap(i, offset + a + b++);
			}
		zsort(offset + a, b, depth - 1);

		int c = 0;
		for (int i = offset + a + b; i < offset + size; i++)
			if (y[tour[i]] > my) {
				swap(i, offset + a + b + c++);
			}
		zsort(offset + a + b, c, depth - 1);
		for (int i = offset + a + b + c; i < offset + size; i++) {
			final double xx = x[tour[i]], yy = y[tour[i]];
			x[tour[i]] = ymax - my - yy;
			y[tour[i]] = xmax - mx - xx;
		}
		zsort(offset + a + b + c, size - a - b - c, depth - 1);
	}

	static void init(final boolean zsort, final boolean tree, final boolean sort) {
		dist = new double[n * n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < i; j++) {
				final double dx = x[i] - x[j];
				final double dy = y[i] - y[j];
				dist[i * n + j] = dist[j * n + i] = Math.sqrt(dx * dx + dy * dy);
			}
		}

		if (zsort) {
			tour = new int[n];
			for (int i = 0; i < n; i++) {
				tour[i] = i;
			}

			final double[] xx = Arrays.copyOf(x, x.length);
			final double[] yy = Arrays.copyOf(y, y.length);
			zsort(0, n, 12);
			x = xx;
			y = yy;
		} else if (tree) {
			int e = 0;
			Edge[] allEdges = new Edge[n * (n - 1) / 2];
			for (int b = 0; b < n; b++) {
				for (int a = 0; a < b; a++) {
					final Edge q = new Edge();
					q.a = a;
					q.b = b;
					q.dist = dist[a * n + b];
					allEdges[e++] = q;
				}
			}
			Arrays.sort(allEdges);

			e = 0;
			UnionFind[] union = new UnionFind[n];
			for (int i = 0; i < n; i++) {
				union[i] = new UnionFind();
			}

			final List<Edge> edges = new ArrayList<Edge>(n);
			int[] map = new int[n];
			final int[][] link = new int[n][2];
			while (edges.size() < n) {
				final Edge q = allEdges[e++];
				if (map[q.a] < 2 && map[q.b] < 2
						&& (union[q.a].group() != union[q.b].group() || edges.size() + 1 == n)) {
					union[q.a].union(union[q.b]);
					edges.add(q);
					link[q.a][map[q.a]] = q.b;
					link[q.b][map[q.b]] = q.a;
					map[q.a] += 1;
					map[q.b] += 1;
				}
			}
			union = null;
			allEdges = null;
			map = null;

			tour = new int[n];
			tour[1] = link[0][0];
			for (int i = 2; i < n; i++) {
				tour[i] = tour[i - 2] == link[tour[i - 1]][0] ? link[tour[i - 1]][1] : link[tour[i - 1]][0];
			}
		} else if (sort) {
			final Integer[] tourI = new Integer[n];
			for (int i = 0; i < n; i++) {
				tourI[i] = i;
			}

			Arrays.sort(tourI, new Comparator<Integer>() {
				@Override
				public int compare(final Integer a, final Integer b) {
					final int aa = a, bb = b;
					if (y[aa] > y[bb])
						return 1;
					if (y[aa] < y[bb])
						return -1;
					if (x[aa] > x[bb])
						return 1;
					if (x[aa] < x[bb])
						return -1;
					return 0;
				}
			});

			tour = new int[n];
			for (int i = 0; i < n; i++) {
				tour[i] = tourI[i];
			}
		} else {
			tour = new int[n];
			for (int i = 0; i < n; i++) {
				tour[i] = i;
			}
		}

		length = distance(0, n - 1);
		for (int i = 1; i < n; i++) {
			length += distance(i - 1, i);
		}
	}

	static double distance(final int i, final int j) {
		return dist[tour[i] * n + tour[j]];
	}

	static void load(final String file) {
		try {
			final BufferedReader r = new BufferedReader(new FileReader(file));
			r.readLine();
			r.readLine();
			r.readLine();
			r.readLine();
			n = Integer.parseInt(r.readLine().split(" ")[2]);
			r.readLine();
			r.readLine();

			x = new double[n];
			y = new double[n];

			for (int i = 0; i < n; i++) {
				final String[] words = r.readLine().split(" ");
				y[i] = Double.parseDouble(words[1]);
				x[i] = Double.parseDouble(words[2]);
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	static void loadMatrix(final int w, final int h, final double d) {
		n = w * h;
		x = new double[n];
		y = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = i / w * d;
			y[i] = i % w * d;
		}
	}

	static void loadRand(final int nn) {
		n = nn;
		x = new double[n];
		y = new double[n];
		for (int i = 0; i < n; i++) {
			x[i] = rand.nextDouble() * 1500;
			y[i] = rand.nextDouble() * 925;
		}
	}

	static void loadRand2(final int nn) {
		n = nn - nn % 2;
		x = new double[n];
		y = new double[n];
		for (int i = 0; i < n; i += 2) {
			x[i] = rand.nextDouble() * 1500;
			y[i] = rand.nextDouble() * 925;
			x[i + 1] = -x[i];
			y[i + 1] = -y[i];
		}
	}

	static void loadRand3(final int nn) {
		n = nn - nn % 3;
		x = new double[n];
		y = new double[n];
		final double cosa = Math.cos(Math.PI * 2.0 / 3.0);
		final double sina = Math.sin(Math.PI * 2.0 / 3.0);
		for (int i = 0; i < n; i += 3) {
			x[i] = rand.nextDouble() * 925;
			y[i] = rand.nextDouble() * 925;
			x[i + 1] = x[i] * cosa - y[i] * sina;
			y[i + 1] = x[i] * sina + y[i] * cosa;
			x[i + 2] = x[i] * cosa - y[i] * -sina;
			y[i + 2] = x[i] * -sina + y[i] * cosa;
		}
	}
}
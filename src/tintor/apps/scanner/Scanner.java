package tintor.apps.scanner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import tintor.util.BigRational;

public class Scanner {
	private final static Logger log = Logger.getLogger(Scanner.class.getName());

	private final PriorityQueue<Event> queue = new PriorityQueue<Event>();
	private final List<Segment> activeSegments = new ArrayList<Segment>();

	private final List<Segment> before = new ArrayList<Segment>();
	private final List<Segment> after = new ArrayList<Segment>();

	void addRing(final Object attribute, final Point... points) {
		for (int i = 1; i < points.length; i++) {
			addEdge(attribute, points[i - 1], points[i]);
		}
		addEdge(attribute, points[0], points[points.length - 1]);
	}

	void addEdge(final Object attribute, final Point a, final Point b) {
		if (a.equals(b))
			throw new IllegalArgumentException("zero length edge");

		final Segment s = a.compareTo(b) < 0 ? new Segment(attribute, a, b) : new Segment(attribute, b, a);
		queue.add(new Event(s, 0));
		queue.add(new Event(s, 1));
	}

	protected Object combineAttributes(final Segment a, final Segment b) {
		if (a.attribute == b.attribute || b.attribute == null)
			return a.attribute;
		if (a.attribute == null)
			return b.attribute;
		return null;
	}

	public void scan() {
		while (!queue.isEmpty()) {
			final Event event = queue.remove();
			Segment eventSegment = event.segment;

			if (event.index == 0) {
				// start of a new segment

				final List<Segment> toRemove = new ArrayList<Segment>();
				final List<Segment> toAdd = new ArrayList<Segment>();

				for (final Segment segment : activeSegments) {
					log.fine("checking " + segment + " and " + eventSegment);

					if (segment.disjoint(eventSegment)) {
						log.fine(" -> disjoint");
						continue;
					}

					if (segment.simpleIntersection(eventSegment)) {
						log.fine(" -> simple intersection");
						continue;
					}

					if (!segment.parallel(event.segment)) {
						log.fine(" -> lines intersect");

						final Segment.PointIntersection ipoint = segment.getPointIntersection(eventSegment);
						if (ipoint != null) {
							log.fine(" -> segments intersect at (" + ipoint.x + " " + ipoint.y + ")");

							final RationalPoint point = new RationalPoint(ipoint.x, ipoint.y);
							toRemove.add(segment);
							Segment newEventSegment = null;

							// make a split at intersection point
							final Segment a = new Segment(segment.attribute, segment.a, point);
							toAdd.add(a);
							queue.add(new Event(a, 1));

							if (!point.equals(segment.b)) {
								final Segment b = new Segment(segment.attribute, point, segment.b);
								queue.add(new Event(b, 0));
								queue.add(new Event(b, 1));
							}

							if (!eventSegment.a.equals(point)) {
								final Segment c = new Segment(eventSegment.attribute, eventSegment.a,
										point);
								newEventSegment = c;
								queue.add(new Event(c, 1));
							}

							if (!point.equals(eventSegment.b)) {
								final Segment d = new Segment(eventSegment.attribute, point,
										eventSegment.b);
								if (newEventSegment == null) {
									newEventSegment = d;
								} else {
									queue.add(new Event(d, 0));
								}
								queue.add(new Event(d, 1));
							}

							eventSegment = newEventSegment;
						}
					} else {
						log.fine(" -> lines parallel");
						final Segment.SegmentOverlap overlap = segment.getSegmentOverlap(eventSegment);
						if (overlap != null) {
							log.fine(" -> segments overlap");
							toRemove.add(segment);

							assert overlap.a == segment.a;
							if (!overlap.a.equals(overlap.b)) {
								assert overlap.a == segment.a;
								final Segment a = new Segment(segment.attribute, overlap.a, overlap.b);
								toAdd.add(a);
								queue.add(new Event(a, 1));
							}

							assert overlap.b == event.point;
							final Segment b = new Segment(combineAttributes(segment, eventSegment),
									overlap.b, overlap.c);
							final Segment newEventSegment = b;
							queue.add(new Event(b, 0));
							queue.add(new Event(b, 1));

							if (!overlap.c.equals(overlap.d)) {
								final Segment c = new Segment(
										overlap.d == segment.b ? segment.attribute
												: eventSegment.attribute, overlap.c, overlap.d);
								queue.add(new Event(c, 0));
								queue.add(new Event(c, 1));
							}

							eventSegment = newEventSegment;
						}
					}
				}

				after.add(eventSegment);

				activeSegments.removeAll(toRemove);
				activeSegments.add(eventSegment);
				activeSegments.addAll(toAdd);
			} else {
				// end of active segment
				if (activeSegments.remove(event.segment)) {
					before.add(event.segment);
				}
			}

			if (queue.isEmpty() || event.point.compareTo(queue.peek().point) < 0) {
				beforeTheJunction(event.point);
				processTheJunction(event.point, before, after, queue.isEmpty()
						|| !event.point.x.equals(queue.peek().point.x));
				before.clear();
				after.clear();
			}
		}
	}

	private void beforeTheJunction(final RationalPoint junction) {
		Collections.sort(before, new Comparator<Segment>() {
			@Override
			public int compare(final Segment p, final Segment q) {
				assert p.b.equals(q.b);
				return RationalPoint.compareAngles(q.a, p.a, p.b);
			}
		});
		Collections.sort(after, new Comparator<Segment>() {
			@Override
			public int compare(final Segment p, final Segment q) {
				assert p.a.equals(q.a);
				return RationalPoint.compareAngles(p.b, q.b, p.a);
			}
		});

		if (log.isLoggable(Level.INFO)) {
			final StringBuilder sb = new StringBuilder("junction ");
			sb.append(junction);
			if (before.size() > 0) {
				sb.append(" before");
				for (final Segment segment : before) {
					sb.append(" " + segment);
				}
			}
			if (after.size() > 0) {
				sb.append(" after");
				for (final Segment segment : after) {
					sb.append(" " + segment);
				}
			}
			log.info(sb.toString());
		}
	}

	protected void processTheJunction(final RationalPoint junction, final List<Segment> before,
			final List<Segment> after, final boolean endOfLine) {
	}
}

class Point implements Comparable<Point> {
	public final long x;
	public final long y;

	public Point(final long x, final long y) {
		assert Longs.fitsIn(x, 63) && Longs.fitsIn(y, 63);
		this.x = x;
		this.y = y;
	}

	@Override
	public int compareTo(final Point e) {
		if (x > e.x)
			return 1;
		if (x < e.x)
			return -1;
		if (y > e.y)
			return 1;
		if (y < e.y)
			return -1;
		return 0;
	}

	// returns 1 if A is clockwise from B
	// returns 0 if A is over B
	// returns -1 if A is counterclockwise from B
	static int compareAngles(final Point a, final Point b, final Point zero) {
		return Longs.detSign(a.y - zero.y, b.x - zero.x, a.x - zero.x, b.y - zero.y);
	}

	boolean equals(final Point e) {
		return x == e.x && y == e.y;
	}

	@Override
	public String toString() {
		return "(" + x + " " + y + ")";
	}
}

class RationalPoint implements Comparable<RationalPoint> {
	final long xInt;
	final BigRational xFrac;
	final long yInt;
	final BigRational yFrac;

	final BigRational x, y;
	final Point original;

	public RationalPoint(final Point p) {
		xInt = p.x;
		xFrac = BigRational.ZERO;
		yInt = p.y;
		yFrac = BigRational.ZERO;
		x = BigRational.create(p.x);
		y = BigRational.create(p.y);
		original = p;
	}

	public RationalPoint(final BigRational x, final BigRational y) {
		xInt = longValue(x.integer());
		xFrac = x.fraction();
		yInt = longValue(y.integer());
		yFrac = y.fraction();
		this.x = x;
		this.y = y;
		original = null;
		final TreeMap<Integer, Integer> a;
	}

	private static long longValue(final BigInteger v) {
		if (v.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
				|| v.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
			throw new RuntimeException();
		return v.longValue();
	}

	@Override
	public int compareTo(final RationalPoint e) {
		if (xInt > e.xInt)
			return 1;
		if (xInt < e.xInt)
			return -1;
		final int cmpX = xFrac.compareTo(e.xFrac);
		if (cmpX != 0)
			return cmpX;

		if (yInt > e.yInt)
			return 1;
		if (yInt < e.yInt)
			return -1;
		return yFrac.compareTo(e.yFrac);
	}

	// returns 1 if A is clockwise from B
	// returns 0 if A is over B
	// returns -1 if A is counterclockwise from B
	static int compareAngles(final RationalPoint a, final RationalPoint b, final RationalPoint zero) {
		return Longs
				.detSign(a.y.subtract(zero.y), b.x.subtract(zero.x), a.x.subtract(zero.x), b.y.subtract(zero.y));
	}

	@Override
	public String toString() {
		return "(" + xInt + "[" + xFrac + "] " + yInt + "[" + yFrac + "])";
	}
}

class Longs {
	// Can "value" fit into singed integer with given number of bits? 
	static boolean fitsIn(final long value, final int bits) {
		assert 1 <= bits && bits <= 64;
		return value >> bits - 1 == value >> 63;
	}

	private static BigInteger mul(final long a, final long b) {
		return BigInteger.valueOf(a).multiply(BigInteger.valueOf(b));
	}

	static BigInteger det(final long a, final long b, final long c, final long d) {
		return mul(a, b).subtract(mul(c, d));
	}

	static BigRational det(final BigRational a, final BigRational b, final BigRational c, final BigRational d) {
		return a.multiply(b).subtract(c.multiply(d));
	}

	static int detSign(final long a, final long b, final long c, final long d) {
		return mul(a, b).compareTo(mul(c, d));
	}

	static int detSign(final BigRational a, final BigRational b, final BigRational c, final BigRational d) {
		return a.multiply(b).compareTo(c.multiply(d));
	}

	static long divide(final BigInteger a, final BigInteger b) {
		return new BigDecimal(a).divide(new BigDecimal(b), 0, RoundingMode.HALF_EVEN).longValue();
	}

	static long gcd(long a, long b) {
		while (b != 0) {
			final long r = a % b;
			a = b;
			b = r;
		}
		return a;
	}
}

class Segment {
	private final static Logger log = Logger.getLogger(Segment.class.getName());

	final Object attribute;
	final RationalPoint a;
	final RationalPoint b;
	final BigRational dir; // Normalized direction vector

	Segment(final Object attribute, final Point a, final Point b) {
		this(attribute, new RationalPoint(a), new RationalPoint(b));
	}

	Segment(final Object attribute, final RationalPoint a, final RationalPoint b) {
		assert a.compareTo(b) < 0 : a + " must be less than " + b;
		this.attribute = attribute;
		this.a = a;
		this.b = b;
		dir = b.y.subtract(a.y).divide(b.x.subtract(a.x));
	}

	@Override
	public String toString() {
		return "Segment[a=" + a + " b=" + b + " dir=" + dir + " attr=" + attribute + "]";
	}

	boolean parallel(final Segment e) {
		assert a.compareTo(e.a) <= 0;
		return dir.equals(e.dir);
	}

	boolean disjoint(final Segment e) {
		assert a.compareTo(e.a) <= 0;
		return max(a.y, b.y).compareTo(min(e.a.y, e.b.y)) < 0 || max(e.a.y, e.b.y).compareTo(min(a.y, b.y)) < 0;
	}

	private static BigRational max(final BigRational a, final BigRational b) {
		return a.max(b);
	}

	private static BigRational min(final BigRational a, final BigRational b) {
		return a.min(b);
	}

	static class PointIntersection {
		final BigRational x, y;

		PointIntersection(final BigRational x, final BigRational y) {
			this.x = x;
			this.y = y;
		}
	}

	static class SegmentOverlap {
		// All input points are on the same line.
		// Here they are sorted by position.
		final RationalPoint a, b, c, d;

		SegmentOverlap(final RationalPoint a, final RationalPoint b, final RationalPoint c, final RationalPoint d) {
			assert a.compareTo(b) <= 0;
			assert b.compareTo(c) < 0;
			assert c.compareTo(d) <= 0;
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}
	}

	// If segments are touching at endpoints, but not overlapping!
	boolean simpleIntersection(final Segment q) {
		final RationalPoint c = q.a, d = q.b;
		assert a.compareTo(b) < 0;
		assert c.compareTo(d) < 0;
		assert a.compareTo(c) <= 0;
		assert c.compareTo(b) <= 0;

		return a.equals(c) && !parallel(q) || c.equals(b) || b.equals(d) && !parallel(q);
	}

	PointIntersection getPointIntersection(final Segment q) {
		final RationalPoint c = q.a, d = q.b;
		assert a.compareTo(b) < 0;
		assert c.compareTo(d) < 0;
		assert a.compareTo(c) <= 0;
		assert c.compareTo(b) <= 0;
		assert !simpleIntersection(q);
		assert !disjoint(q) : this + " and " + q + " must not be verticaly disjoint";
		assert !parallel(q);

		final BigRational bax = b.x.subtract(a.x), bay = b.y.subtract(a.y);
		final BigRational acx = a.x.subtract(c.x), acy = a.y.subtract(c.y);
		final BigRational dcx = d.x.subtract(c.x), dcy = d.y.subtract(c.y);

		final BigRational det = Longs.det(dcx, bay, dcy, bax);
		assert det.signum() != 0;

		// Lines intersect in a single point
		final BigRational m = Longs.det(acx, dcy, acy, dcx).divide(det);
		if (m.signum() < 0 || m.compareTo(BigRational.ONE) > 1) {
			log.finer("m=" + m);
			return null;
		}

		final BigRational n = Longs.det(acx, bay, acy, bax).divide(det);
		if (n.signum() < 0 || n.compareTo(BigRational.ONE) > 1) {
			log.finer("n=" + n);
			return null;
		}

		// Found point intersection
		final BigRational x = m.multiply(bax).add(a.x);
		final BigRational y = m.multiply(bay).add(a.y);

		// Sanity checks
		assert x.equals(n.multiply(dcx).add(c.x));
		assert y.equals(n.multiply(dcy).add(c.y));

		return new PointIntersection(x, y);
	}

	SegmentOverlap getSegmentOverlap(final Segment q) {
		final RationalPoint c = q.a, d = q.b;
		assert a.compareTo(b) < 0;
		assert c.compareTo(d) < 0;
		assert a.compareTo(c) <= 0;
		assert c.compareTo(b) <= 0;
		assert !simpleIntersection(q);
		assert !disjoint(q);
		assert parallel(q) : this + " and " + q + " must be parallel";
		assert !b.equals(c); // because it would be a simple intersection

		// Check if lines are disjoint
		if (!a.equals(c) && !parallel(new Segment(null, a, c)))
			return null;
		return b.compareTo(d) < 0 ? new SegmentOverlap(a, c, b, d) : new SegmentOverlap(a, c, d, b);
	}
}

class Event implements Comparable<Event> {
	public final Segment segment;
	public final RationalPoint point;
	public final int index;

	Event(final Segment segment, final int index) {
		assert index == 0 || index == 1;
		this.segment = segment;
		this.index = index;
		point = index == 0 ? segment.a : segment.b;
	}

	@Override
	public int compareTo(final Event e) {
		final int cmp = point.compareTo(e.point);
		if (cmp != 0)
			return cmp;
		if (index < e.index)
			return -1;
		if (index > e.index)
			return 1;
		return 0;
	}
}
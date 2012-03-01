package tintor.apps.scanner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

class OutlineScanner extends Scanner {
	Set<Object> insideBefore = new HashSet<Object>();
	Set<Object> insideAfter = new HashSet<Object>();

	@Override
	protected void processTheJunction(final RationalPoint junction, final List<Segment> before,
			final List<Segment> after, final boolean endOfLine) {
		for (final Segment segment : before) {
			assert segment.attribute != null;
			if (segment.a.x == segment.b.x) {
				if (insideBefore.isEmpty() != insideAfter.isEmpty()) {
					emit(segment);
				}
			} else {
				if (insideBefore.contains(segment.attribute)) {
					insideBefore.remove(segment.attribute);
					if (insideBefore.isEmpty()) {
						emit(segment);
					}
				} else {
					if (insideBefore.isEmpty()) {
						emit(segment);
					}
					insideBefore.add(segment.attribute);
				}
			}
		}

		for (final Segment segment : after) {
			assert segment.attribute != null;
			if (segment.a.x != segment.b.x) {
				if (insideAfter.contains(segment.attribute)) {
					insideAfter.remove(segment.attribute);
				} else {
					insideAfter.add(segment.attribute);
				}
			}
		}

		if (endOfLine) {
			final Set<Object> set = insideBefore;
			insideBefore = insideAfter;
			insideAfter = set;
			set.clear();
		}

		throw new NotImplementedException();
	}

	void emit(final Segment segment) {
		System.out.println("outline " + segment);
	}
}

public class ScannerTest {
	private final static Logger log = Logger.getLogger(ScannerTest.class.getName());

	@Test
	public void pointCompareTo() {
		Assert.assertEquals(-1, (int) (10L - 20L >> 63));
		Assert.assertEquals(-1, new Point(10, 10).compareTo(new Point(10, 20)));
		Assert.assertEquals(1, new Point(10, 20).compareTo(new Point(10, 10)));
		Assert.assertEquals(0, new Point(-10, -20).compareTo(new Point(-10, -20)));
	}

	public static void main(final String[] args) {
		Logger.getLogger(ScannerTest.class.getPackage().getName()).setLevel(Level.WARNING);

		final Scanner sc = new OutlineScanner();
		sc.addRing(1, new Point(0, 0), new Point(2000, 0), new Point(2000, 2000), new Point(0, 2000));
		sc.addRing(2, new Point(1000, 1000), new Point(3000, 1000), new Point(3000, 3000), new Point(1000, 3000));
		sc.scan();
	}
}
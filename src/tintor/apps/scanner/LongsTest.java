package tintor.apps.scanner;

import org.junit.Assert;
import org.junit.Test;

public class LongsTest {
	@Test
	public void fitsIn() {
		Assert.assertTrue(Longs.fitsIn(0L, 1));
		Assert.assertTrue(Longs.fitsIn(-1L, 1));
		Assert.assertFalse(Longs.fitsIn(1L, 1));

		Assert.assertTrue(Longs.fitsIn(7L, 4));
		Assert.assertFalse(Longs.fitsIn(8L, 4));
		Assert.assertTrue(Longs.fitsIn(-8L, 4));
		Assert.assertFalse(Longs.fitsIn(-9L, 4));

		Assert.assertTrue(Longs.fitsIn(Long.MAX_VALUE, 64));
		Assert.assertTrue(Longs.fitsIn(Long.MIN_VALUE, 64));
	}

	@Test
	public void gcd() {
		Assert.assertEquals(-2, Longs.gcd(-10, -2));
		Assert.assertEquals(2, Longs.gcd(-10, 2));
		Assert.assertEquals(-2, Longs.gcd(10, -2));
		Assert.assertEquals(2, Longs.gcd(10, 2));
		Assert.assertEquals(0, Longs.gcd(0, 0));
		Assert.assertEquals(1, Longs.gcd(1, 0));
		Assert.assertEquals(1, Longs.gcd(0, 1));
	}
}
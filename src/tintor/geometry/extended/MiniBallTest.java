package tintor.geometry.extended;

import java.util.Random;

public class MiniBallTest {
	public static void main(final String[] args) {
		final MiniBall m = new MiniBall();
		final Random rand = new Random();
		for (int i = 0; i < 1000; i++) {
			m.add(new double[] { rand.nextDouble(), rand.nextDouble(), rand.nextDouble() });
		}
		m.build();
		System.out.println(Math.sqrt(m.squared_radius()));
		System.out.println(MiniBall.pointToString(m.center()));
		System.out.println(m.slack());
		System.out.println(m.accuracy());
		System.out.println(m.is_valid());
	}
}
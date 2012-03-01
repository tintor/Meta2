package tintor.problems.robots;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class Generator {
	public static void main(final String[] args) throws Exception {
		final BufferedWriter w = new BufferedWriter(new FileWriter("src/tintor/problems/robots/robots.gen.in"));
		final int count = 200000;
		w.write(1 + "\n");
		w.write(count + " " + count + "\n");
		final Random rand = new Random();
		for (int i = 0; i < count; i++) {
			w.write(String.format("%.3f %.3f %.3f\n", rand.nextDouble() * count,
					rand.nextDouble() * count / 2000, rand.nextDouble() * count));
		}
		w.close();
	}
}
package tintor.heap;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import tintor.util.Timer;

public class Test {
	public static void main(final String[] args) {
		test(new AuxTwoPassPairingHeap<Integer>());
		test(new ArrayDeque<Integer>());
	}

	static final Random r = new Random();
	static final int n = 1100000;

	private static void test(final Queue<Integer> queue) {
		final Timer time = new Timer();
		time.start();
		for (int i = 0; i < n; i++)
			queue.offer(r.nextInt(Integer.MAX_VALUE));
		while (!queue.isEmpty())
			queue.poll();
		time.stop();
		System.out.println(time);
		System.gc();
	}
}
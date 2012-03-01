package tintor.problems.robots;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Comparator;

public class Robots {
	static class Robot {
		double position;
		double cost;

		int iLeft, iRight;
		double pLeft, pRight;
	}

	static Robot[] robotsByPosition;
	static Robot[] robotsByCost;

	static ReadableByteChannel ch;
	static final ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);

	public static void main(final String[] args) throws Exception {
		ch = Channels.newChannel(new FileInputStream("src/tintor/problems/robots/robots.gen.in"));
		buffer.clear();
		ch.read(buffer);
		buffer.flip();

		final int cases = nextInt();
		main: for (int c = 1; c <= cases; c++) {
			final int count = nextInt();
			final int needed = nextInt();

			// Load robots
			robotsByPosition = new Robot[count];
			robotsByCost = new Robot[count];

			for (int i = 0; i < count; i++) {
				final Robot robot = new Robot();

				robot.position = nextDouble();
				final double range = nextDouble();
				robot.cost = nextDouble();

				robot.pLeft = robot.position - range;
				robot.pRight = robot.position + range;
				robotsByPosition[i] = robotsByCost[i] = robot;
			}

			// Sort by position and cost
			Arrays.sort(robotsByPosition, new Comparator<Robot>() {
				@Override
				public int compare(final Robot a, final Robot b) {
					return Double.compare(a.position, b.position);
				}
			});
			Arrays.sort(robotsByCost, new Comparator<Robot>() {
				@Override
				public int compare(final Robot a, final Robot b) {
					return Double.compare(a.cost, b.cost);
				}
			});

			for (int i = 0; i < count; i++) {
				robotsByPosition[i].iLeft = robotsByPosition[i].iRight = i;
			}

			// Compute
			for (final Robot robot : robotsByCost) {
				Robot repaired = robot;
				while (true) {
					robot.iLeft = Math.min(robot.iLeft, repaired.iLeft);
					robot.iRight = Math.max(robot.iRight, repaired.iRight);
					robot.pLeft = Math.min(robot.pLeft, repaired.pLeft);
					robot.pRight = Math.max(robot.pRight, repaired.pRight);

					if (robot.iRight - robot.iLeft + 1 >= needed) {
						System.out.printf("Case %d: %.6f\n", c, robot.cost);
						continue main;
					}

					if (robot.iLeft > 0) {
						final Robot left = robotsByPosition[robot.iLeft - 1];
						if (robot.pLeft <= left.position) {
							repaired = left;
							continue;
						}
					}
					if (robot.iRight < count - 1) {
						final Robot right = robotsByPosition[robot.iRight + 1];
						if (right.position <= robot.pRight) {
							repaired = right;
							continue;
						}
					}
					break;
				}
			}
			System.out.printf("Case %d: No solution\n", c);
		}
	}

	static double nextDouble() throws IOException {
		int result = 0;
		byte b = prepare();
		while (true) {
			if ((byte) '0' <= b && b <= (byte) '9') {
				result = result * 10 + b - (byte) '0';
				b = buffer.get();
				continue;
			}
			if ((byte) '.' == b) {
				break;
			}
			return result;
		}

		int den = 1;
		b = buffer.get();
		while ((byte) '0' <= b && b <= (byte) '9') {
			result = result * 10 + b - (byte) '0';
			den *= 10;
			b = buffer.get();
		}
		return (double) result / (double) den;
	}

	static int nextInt() throws IOException {
		int result = 0;
		byte b = prepare();
		while ((byte) '0' <= b && b <= (byte) '9') {
			result = result * 10 + b - (byte) '0';
			b = buffer.get();
		}
		return result;
	}

	static byte prepare() throws IOException {
		if (buffer.remaining() <= 50) {
			buffer.compact();
			ch.read(buffer);
			buffer.flip();
		}

		while (true) {
			final byte b = buffer.get();
			if (b > 32)
				return b;
		}
	}
}
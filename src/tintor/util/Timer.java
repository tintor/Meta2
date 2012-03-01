package tintor.util;

public final class Timer {
	private long time;
	private long start;

	public void start() {
		start = System.nanoTime();
	}

	public void stop() {
		time += System.nanoTime() - start;
	}

	public double seconds() {
		return time * 1e-9;
	}

	public void reset() {
		time = 0;
	}

	@Override
	public String toString() {
		return format(time);
	}

	private static final long micro = 1000L;
	private static final long mili = 1000L * micro;
	private static final long sec = 1000L * mili;
	private static final long min = 60L * sec;
	private static final long hour = 60L * min;
	private static final long day = 24L * hour;

	private static String format(final long time) {
		if (time < 20 * micro) return time + "ns";
		if (time < 20 * mili) return (time + micro / 2) / micro + "us";
		if (time < 20 * sec) return (time + mili / 2) / mili + "ms";
		if (time < 10 * min) return (time + sec / 2) / sec + "sec";
		if (time < 10 * hour) return (time + min / 2) / min + "min";
		if (time < 10 * day) return (time + hour / 2) / hour + "hour";
		return (time + day / 2) / day + "day";
	}
}
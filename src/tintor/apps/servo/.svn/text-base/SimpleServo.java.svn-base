package tintor.apps.servo;

public class SimpleServo extends Force {
	@Override
	public double eval(final double r, final double v) {
		if (r <= 0 && v < 0) return 1;
		if (r >= 0 && v > 0) return -1;
		return v * v < 2 * Math.abs(r) ? -Math.signum(r) : Math.signum(r);
	}
}
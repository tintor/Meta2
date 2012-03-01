package tintor.apps.servo;

abstract class Force {
	abstract double eval(double r, double v);

	void reset(@SuppressWarnings("unused") final double r, @SuppressWarnings("unused") final double v) {
	}
}

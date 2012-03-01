package tintor.apps.servo;

import java.io.IOException;

class Simulator {
	static final double dt = 0.01;
	static final int maxT = 10000;
	static final double zeroR = 1e-3, zeroV = 1e-3;

	static double simulate(double r, double v, final double mass, final Force servo, final Force external) {
		servo.reset(r, v);
		external.reset(r, v);

		double s = 0;
		int t = 0;
		while (t < maxT) {
			final double a = (servo.eval(r, v) + external.eval(r, v)) / mass;
			r += dt * dt / 2 * a + dt * v;
			v += dt * a;
			s += (Math.abs(r) + v * v) * t / maxT;
			t += 1;
		}
		return s;
	}

	static Force zeroForce = new Force() {
		@Override
		public double eval(final double r, final double v) {
			return 0;
		}
	};

	static Force periodicForce(final double mag, final double freq) {
		return new Force() {
			double t;

			@Override
			public double eval(final double r, final double v) {
				return Math.cos(t += freq) * mag;
			}
		};
	}

	static Force force(final double f, final double linR, final double lin, final double quad) {
		return new Force() {
			@Override
			public double eval(final double r, final double v) {
				return f + linR * r + lin * v + Math.signum(v) * quad * v * v;
			}
		};
	}

	static void simulate2(double r, double v, final double mass, final Force servo, final Force external) {
		int t = 0;
		while (t < maxT) {
			final double f = servo.eval(r, v);
			final double a = (f + external.eval(r, v)) / mass;
			r += dt * dt / 2 * a + dt * v;
			v += dt * a;
			t += 1;
			System.out.printf("t:%s r:%.3f v:%.3f f:%.3f\n", t, r, v, f);
		}
	}

	public static void main(final String[] args) throws IOException {
		compare(0, 0.001);
		compare(0.001, 0.001);
		compare(10, 0);
		compare(0, 5);
		compare(5, 1);
		compare(5, -1);
		System.out.println(Trainer.evaluate(new SimpleServo()));
	}

	static void compare(final double r, final double v) throws IOException {
		final double a = simulate(r, v, 100, new SimpleServo(), zeroForce);
		final double b = simulate(r, v, 1, new SimpleServo(), zeroForce);
		final double c = simulate(r, v, 0.01, new SimpleServo(), zeroForce);

		final double a1 = simulate(r, v, 100, new SimpleServo(), force(0.9, 0, 0, 0));
		final double b1 = simulate(r, v, 1, new SimpleServo(), force(0.9, 0, 0, 0));
		final double c1 = simulate(r, v, 0.01, new SimpleServo(), force(0.9, 0, 0, 0));

		final double a2 = simulate(r, v, 100, new SimpleServo(), force(-0.9, 0, 0, 0));
		final double b2 = simulate(r, v, 1, new SimpleServo(), force(-0.9, 0, 0, 0));
		final double c2 = simulate(r, v, 0.01, new SimpleServo(), force(-0.9, 0, 0, 0));

		System.out.printf("%.0f %.0f %.0f\t| %.0f %.0f %.0f\t| %.0f %.0f %.0f\n", a, b, c, a1, b1, c1, a2, b2, c2);
	}
}

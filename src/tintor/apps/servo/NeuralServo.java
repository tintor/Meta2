package tintor.apps.servo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import tintor.neural.NeuralNetwork;

// 5165692598 inputs 27, gen 22
public class NeuralServo extends Force {
	private final double[] inputs = new double[9];
	NeuralNetwork net = new NeuralNetwork(inputs, 40, 1);

	double ir1, iv1; // integrals
	double ir2, iv2; // integrals
	double pr, pv, pa; // prev values

	@Override
	public double eval(final double r, final double v) {
		ir1 += r;
		iv1 += v;
		ir2 = ir2 * 0.9 + r * 0.1;
		iv2 = iv2 * 0.9 + v * 0.1;

		inputs[0] = ir1;
		inputs[1] = iv1;
		inputs[2] = ir2;
		inputs[3] = iv2;
		inputs[4] = r;
		inputs[5] = v;
		inputs[6] = pr;
		inputs[7] = pv;
		inputs[8] = pa;

		final double out = net.compute()[0];
		final double a = Math.max(-1, Math.min(1, (out - 0.5) / 0.4));

		pr = r;
		pv = v;
		pa = a;

		return a;
	}

	@Override
	public void reset(final double r, final double v) {
		ir1 = iv1 = 0;
		ir2 = iv2 = 0;
		pr = r;
		pv = v;
		pa = 0;
	}

	void save(final String name) throws IOException {
		final DataOutputStream out = new DataOutputStream(new FileOutputStream("c:/servo/" + name));
		try {
			net.visit(new NeuralNetwork.Visitor() {
				@Override
				public double visit(final double a) {
					try {
						out.writeDouble(a);
					} catch (final IOException e) {
						throw new RuntimeException(e);
					}
					return a;
				}
			});
		} finally {
			out.close();
		}
	}

	void load(final String name) throws IOException {
		final DataInputStream in = new DataInputStream(new FileInputStream("c:/servo/" + name));
		try {
			net.visit(new NeuralNetwork.Visitor() {
				@Override
				public double visit(final double a) {
					try {
						return in.readDouble();
					} catch (final IOException e) {
						throw new RuntimeException(e);
					}
				}
			});
		} finally {
			in.close();
		}
	}
}
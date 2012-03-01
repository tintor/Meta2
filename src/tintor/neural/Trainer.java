package tintor.neural;

import java.util.Random;

public class Trainer {
	static double[][] inputs = { { 0, 0 }, { 0, 1 }, { 1, 0 }, { 1, 1 } };
	static double[][] outputs = { { 0.1 }, { 0.9 }, { 0.9 }, { 0.1 } };
	static final NeuralNetwork net = new NeuralNetwork(inputs[0].length, 2, outputs[0].length);

	public static void main(final String[] args) {
		final Random rand = new Random();
		net.visit(new NeuralNetwork.Visitor() {
			@Override
			public double visit(final double a) {
				return rand.nextDouble() - 0.5;
			}
		});

		net.learningRate = 0.1;
		net.useMomentum = false;
		net.momentumFactor = 0;
		train();
	}

	static double error() {
		double error = 0;
		for (int i = 0; i < inputs.length; i++) {
			net.setInputs(inputs[i]);
			final double[] output = net.compute();
			for (int j = 0; j < output.length; j++) {
				final double e = output[j] - outputs[i][j];
				error += e * e;
			}
		}
		return Math.sqrt(error / inputs.length);
	}

	static void train() {
		int generation = 0;
		while (true) {
			for (int j = 0; j < 100000; j++) {
				for (int i = 0; i < inputs.length; i++) {
					net.setInputs(inputs[i]);
					net.compute();
					net.train(outputs[i]);
				}
				generation += 1;
			}

			final double error = error();
			System.out.printf("Generation %s: error %s\n", generation, error);
			System.out.flush();
			if (error < 1e-10) break;
		}
	}
}

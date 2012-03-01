package tintor.apps.servo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import tintor.neural.NeuralNetwork;

class Trainer {
	int gensize = 20;
	int offspring = 6;
	double mutation0 = 0.005;
	double mutationQ = 2;

	static class UnitServo extends NeuralServo implements Comparable<UnitServo> {
		double fitness;
		double mutation;

		@Override
		public int compareTo(final UnitServo other) {
			return Double.compare(fitness, other.fitness);
		}
	}

	UnitServo[] pool = new UnitServo[gensize * (1 + offspring)];

	final Random rand = new Random();

	final NeuralNetwork.Visitor randomize = new NeuralNetwork.Visitor() {
		@Override
		public double visit(final double a) {
			return rand.nextDouble() - 0.5;
		}
	};

	int wpos = 0;
	double[] temp = new double[1000];

	final NeuralNetwork.Visitor writer = new NeuralNetwork.Visitor() {
		@Override
		public double visit(final double a) {
			return temp[wpos++] = a;
		}
	};

	int rpos = 0;
	double mutation;

	final NeuralNetwork.Visitor reader = new NeuralNetwork.Visitor() {
		@Override
		public double visit(final double a) {
			return temp[rpos++];
		}
	};

	final NeuralNetwork.Visitor mutator = new NeuralNetwork.Visitor() {
		@Override
		public double visit(final double a) {
			return temp[rpos++] + rand.nextGaussian() * mutation;
		}
	};

	static double evaluate(final Force servo) {
		return sim(servo, -10, 0) + sim(servo, 10, 0) + sim(servo, 5, 1) + sim(servo, 5, -1) + sim(servo, -5, 1)
				+ sim(servo, -5, -1) + sim(servo, 0, 5) + sim(servo, 0, -5);
	}

	static double sim(final Force servo, final double r, final double v) {
		final double s = Simulator.simulate(r, v, 1, servo, Simulator.zeroForce);
		return s * s;
	}

	Trainer() {
		for (int i = 0; i < pool.length; i++) {
			pool[i] = new UnitServo();
			pool[i].net.visit(randomize);
		}
	}

	void evolve() throws IOException {
		for (final UnitServo element : pool)
			element.fitness = evaluate(element);
		Arrays.sort(pool);
		int generation = 0;

		while (true) {
			System.out.printf("gen %s, fitness %.0f %.0f, best mutation %f\n", generation, pool[0].fitness,
					pool[gensize - 1].fitness, pool[0].mutation);

			// save the best net
			pool[0].save("genx." + generation);

			// create new generation
			for (int i = 0; i < gensize; i++) {
				wpos = 0;
				pool[i].net.visit(writer);

				mutation = mutation0;
				for (int j = 0; j < offspring; j++) {
					final UnitServo b = pool[gensize + i * offspring + j];

					rpos = 0;
					b.net.visit(mutator);
					b.mutation = mutation;
					mutation *= mutationQ;
					b.fitness = evaluate(b);
				}
			}
			Arrays.sort(pool);
			generation += 1;
		}
	}

	public static void main(final String[] args) throws IOException {
		final Trainer trainer = new Trainer();
		//for (int i = 0; i < trainer.pool.length; i++)
		//	trainer.pool[i].load("gen." + (139 - i));
		trainer.evolve();
	}
}

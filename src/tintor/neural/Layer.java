package tintor.neural;

class Layer {
	final double[][] weights;
	final double[] bias;

	double[] inputs;
	final double[] outputs;
	final double[] errors;

	final double[][] weightChanges;

	Layer(final double[] inputs, final int outputSize) {
		assert inputs.length >= 2;
		assert outputSize >= 1;

		weights = new double[outputSize][inputs.length];
		bias = new double[outputSize];

		this.inputs = inputs;
		outputs = new double[outputSize];
		errors = new double[outputSize];

		weightChanges = new double[outputSize][inputs.length];
	}

	void computeOutputs() {
		assert weights.length == outputs.length;
		for (int i = 0; i < outputs.length; i++)
			outputs[i] = neuron(inputs, weights[i], bias[i]);
	}

	void computeErrors(final double[] expectedOutputs) {
		for (int i = 0; i < outputs.length; i++)
			errors[i] = (expectedOutputs[i] - outputs[i]) * outputs[i] * (1.0 - outputs[i]);
	}

	void computeErrors(final Layer layer) {
		for (int i = 0; i < outputs.length; i++) {
			double delta = 0;
			for (int j = 0; j < layer.outputs.length; j++)
				delta += layer.errors[j] * layer.weights[j][i];
			errors[i] = delta * outputs[i] * (1.0 - outputs[i]);
		}
	}

	void adjustWeights(final double learningRate, final boolean useMomentum, final double momentumFactor) {
		for (int i = 0; i < inputs.length; i++)
			for (int j = 0; j < outputs.length; j++) {
				final double dw = learningRate * errors[j] * inputs[i];
				weights[j][i] += dw;

				if (useMomentum) {
					weights[j][i] += momentumFactor * weightChanges[j][i];
					weightChanges[j][i] = dw;
				}
			}

		for (int i = 0; i < outputs.length; i++)
			bias[i] += learningRate * errors[i] * bias[i];
	}

	void visit(final NeuralNetwork.Visitor visitor) {
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++)
				weights[i][j] = visitor.visit(weights[i][j]);
			bias[i] = visitor.visit(bias[i]);
		}
	}

	static double neuron(final double[] inputs, final double[] weights, final double bias) {
		assert inputs.length == weights.length;
		double x = bias;
		for (int i = 0; i < weights.length; i++)
			x += weights[i] * inputs[i];
		return 1.0 / (1.0 + Math.exp(-x));
	}
}

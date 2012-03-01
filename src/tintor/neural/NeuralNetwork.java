package tintor.neural;

public class NeuralNetwork {
	public interface Visitor {
		double visit(double a);
	}

	private final Layer hidden, output;

	public double learningRate = 0.1;
	public double momentumFactor = 0.1;
	public boolean useMomentum = true;

	public NeuralNetwork(final double[] inputs, final int hiddenSize, final int outputSize) {
		hidden = new Layer(inputs, hiddenSize);
		output = new Layer(hidden.outputs, outputSize);
	}

	public NeuralNetwork(final int inputSize, final int hiddenSize, final int outputSize) {
		this(new double[inputSize], hiddenSize, outputSize);
	}

	public void setInputs(final double[] inputs) {
		assert inputs.length == hidden.inputs.length;
		hidden.inputs = inputs;
	}

	public double[] compute() {
		hidden.computeOutputs();
		output.computeOutputs();
		return output.outputs;
	}

	// expects compute call before this one
	public void train(final double[] expectedOutputs) {
		output.computeErrors(expectedOutputs);
		hidden.computeErrors(output);

		output.adjustWeights(learningRate, useMomentum, momentumFactor);
		hidden.adjustWeights(learningRate, useMomentum, momentumFactor);
	}

	public void visit(final Visitor visitor) {
		hidden.visit(visitor);
		output.visit(visitor);
	}
}

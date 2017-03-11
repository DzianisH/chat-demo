package org.chatbot.model;

import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer.PoolingType;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

import static org.deeplearning4j.nn.api.OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT;

/**
 * Created by DzianisH on 25.02.2017.
 */
public class ModelFactory {
	private final int numOfClasses = 16;
	// input shape
	private final int width = 300, height = 32, depth = 1;

	public MultiLayerConfiguration createConfiguration(){
		return createHelloWorldConvolutional();
	}

	private MultiLayerConfiguration createHelloWorldConvolutional(){
		return new NeuralNetConfiguration.Builder()
				.iterations(2)
				.updater(Updater.ADAGRAD).learningRate(0.5).epsilon(1e-6)
				.optimizationAlgo(STOCHASTIC_GRADIENT_DESCENT)
				.miniBatch(true)
				.regularization(true).l2(0.7)
//				.useDropConnect(true).dropOut(0.99)
				.list()
				.layer(0, new ConvolutionLayer.Builder(3, 100)
						.name("Convolution-layer")
						.nIn(1)
						.nOut(16)
						.stride(1, 100)
						.activation(Activation.RELU)
						.weightInit(WeightInit.RELU)
						.build())
				.layer(1, new SubsamplingLayer.Builder()
						.name("Pooling-layer")
						.stride(100, 1)
						.kernelSize(30, 1)
						.poolingType(PoolingType.MAX)
						.build())
				.layer(2, new GravesLSTM.Builder()
						.name("LSTM-layer")
						.nIn(10)
						.nOut(10)
						.activation(Activation.RELU)
						.weightInit(WeightInit.RELU)
						.build())
				.layer(3, new RnnOutputLayer.Builder(LossFunction.MSE)
						.name("RNN-output-layer")
						.nOut(numOfClasses)
						.activation(Activation.SOFTMAX)
						.weightInit(WeightInit.XAVIER)
						.build())
				.setInputType(InputType.convolutionalFlat(height, width, depth))
				.pretrain(false)
				.backprop(true).backpropType(BackpropType.TruncatedBPTT)
				.tBPTTForwardLength(height)
				.tBPTTBackwardLength(height)
				.build();
	}

	public int getNumOfClasses() {
		return numOfClasses;
	}
}

package com.kdcloud.engine.embedded.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import com.kdcloud.engine.embedded.BufferedInstances;
import com.kdcloud.engine.embedded.NodeAdapter;
import com.kdcloud.engine.embedded.WorkerConfiguration;
import com.kdcloud.engine.embedded.WrongConfigurationException;
import com.kdcloud.engine.embedded.WrongInputException;

public class QRS extends NodeAdapter {

	public static final int M = 5;
	public static final int windowsSize = 15;
	public static final int sampling_rate_ms = 10;

	public static final Attribute OUTPUT_ATTRIBUTE = new Attribute("rr");

	private BufferedInstances input;
	private BufferedInstances output;
	private int index = 0;
	
	public Logger logger = Logger.getLogger(getClass().getSimpleName());

	public static double[] highPass(double[] sig0, int nsamp) {
		double[] highPass = new double[nsamp];
		double constant = (double) 1 / M;

		for (int i = 0; i < sig0.length; i++) {
			double y1 = 0;
			double y2 = 0;

			int y2_index = i - ((M + 1) / 2);
			if (y2_index < 0) {
				y2_index = nsamp + y2_index;
			}
			y2 = sig0[y2_index];

			double y1_sum = 0;
			for (int j = i; j > i - M; j--) {
				int x_index = i - (i - j);
				if (x_index < 0) {
					x_index = nsamp + x_index;
				}
				y1_sum += sig0[x_index];
			}
			y1 = constant * y1_sum;
			highPass[i] = y2 - y1;
		}
		return highPass;
	}

	public static double[] lowPass(double[] sig0, int nsamp) {
		double[] lowPass = new double[nsamp];
		for (int i = 0; i < sig0.length; i++) {
			double sum = 0;
			if (i + windowsSize < sig0.length) {
				for (int j = i; j < i + windowsSize; j++) {
					double current = sig0[j] * sig0[j];
					sum += current;
				}
			} else if (i + windowsSize >= sig0.length) {
				int over = i + windowsSize - sig0.length;
				for (int j = i; j < sig0.length; j++) {
					double current = sig0[j] * sig0[j];
					sum += current;
				}
				for (int j = 0; j < over; j++) {
					double current = sig0[j] * sig0[j];
					sum += current;
				}
			}
			lowPass[i] = sum;
		}
		return lowPass;
	}

	public static double average(double[] array) {
		double sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum / array.length;
	}

	public static int[] QRS_RC(double[] lowPass, int nsamp) {
		int[] QRS = new int[nsamp];

		int cut_length = 200;
		double[] sort_array = Arrays.copyOf(lowPass, cut_length);
		Arrays.sort(sort_array);
		int percent = 50 * cut_length / 100;
		double[] mean_array = Arrays.copyOfRange(sort_array,
				(sort_array.length - percent), sort_array.length);
		double treshold = average(mean_array);

		/*
		 * int cut_length=100; double max_value=getMax(lowPass,0,cut_length);
		 * double treshold =max_value - (max_value*80/100);
		 */

		int frame = 300;// 400 is good too
		int i = 0;

		while (i < lowPass.length) {
			double max = 0;
			int index = 0;

			if (i + frame > lowPass.length) {
				index = lowPass.length;
			} else {
				index = i + frame;
			}

			int offset = 0;

			if (lowPass[index - 1] > treshold && index < lowPass.length) {
				double low_th = (treshold * 90) / 100;
				while (lowPass[index - 1] > (treshold - low_th)
						&& index < lowPass.length) {
					index += 1;
					offset += 1;
				}
			}

			for (int j = i; j < index; j++) {
				if (lowPass[j] > max)
					max = lowPass[j];
			}
			double last_max = 0;
			int index_last_max = -1;
			for (int j = i; j < index; j++) {
				if (lowPass[j] >= treshold && lowPass[j] > last_max) {
					last_max = lowPass[j];
					index_last_max = j;
				} else if (lowPass[j] < treshold && index_last_max != -1) {
					QRS[index_last_max] = 1;
					last_max = 0;
					index_last_max = -1;
				}
			}

			double gama = (Math.random() > 0.5) ? 0.15 : 0.20;
			double alpha = 0.01 + (Math.random() * ((0.1 - 0.01)));

			treshold = alpha * gama * max + (1 - alpha) * treshold;

			i += frame + offset;
		}

		return QRS;
	}

	public static double[] readData(Instances data) {
		Vector<Double> sign = new Vector<Double>();
		for (Instance i : data) {
			sign.add(i.value(0));
		}
		double[] res = new double[sign.size()];
		for (int i = 0; i < sign.size(); i++) {
			res[i] = sign.get(i);
		}
		return res;
	}
	
	public QRS() {
		ArrayList<Attribute> attrs = new ArrayList<Attribute>(1);
		attrs.add(OUTPUT_ATTRIBUTE);
		Instances result = new Instances("rr", attrs, 1000);
		output = new BufferedInstances(result);
	}

	public Instances ecg() {
		Instances data = input.getInstances();
		Instances result = output.getInstances();
		double[] sign = readData(data);
		int nsamp = sign.length;
		double[] highPass = highPass(sign, nsamp);
		double[] lowPass = lowPass(highPass, nsamp);
		int[] QRS = QRS_RC(lowPass, nsamp);
		int time = 0;
		boolean first_peak = true;
		int j = 0;
		for (; j < QRS.length; j++) {
			if (QRS[j] == 1) {
				if (first_peak) {
					first_peak = false;
				} else {
					time += sampling_rate_ms;
					double[] row = { time };
					result.add(new DenseInstance(0, row));
				}
				time = 0;
			} else {
				time += sampling_rate_ms;
			}
		}
		return result;
	}
	
	@Override
	public void configure(WorkerConfiguration config)
			throws WrongConfigurationException {
		if (config.containsKey("index")) {
			index = (Integer) config.get("index");
		}
	}

	@Override
	public void setInput(BufferedInstances input) throws WrongInputException {
		String msg = null;
		if (input instanceof BufferedInstances) {
			BufferedInstances candidate = (BufferedInstances) input;
			if (candidate.getInstances() == null)
				msg = "no data to work on";
			else if (candidate.getInstances().numAttributes() <= index)
				msg = "invalid instances header";
			else
				this.input = candidate;
		} else {
			msg = "invalid input type";
		}
		if (msg != null)
			throw new WrongInputException(msg);
	}

	@Override
	public BufferedInstances getOutput() {
		return output;
	}

	@Override
	public void run() {
		Instances instances = input.getInstances();
		if (instances.size() < 2) {
			logger.info("input size is too small, doing nothing");
		} else {
			String msg = "qrs input size: " + instances.size() + "    mean: "
					+ instances.meanOrMode(index);
			logger.info(msg);
			ecg();
		}
	}

}

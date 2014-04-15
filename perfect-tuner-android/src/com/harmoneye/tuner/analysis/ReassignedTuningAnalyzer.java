package com.harmoneye.tuner.analysis;

import org.apache.commons.math3.util.FastMath;

import com.harmoneye.audio.DoubleRingBuffer;
import com.harmoneye.audio.SoundConsumer;
import com.harmoneye.math.MaxNorm;
import com.harmoneye.math.Modulo;
import com.harmoneye.math.Norm;
import com.harmoneye.tuner.analysis.StreamingReassignedSpectrograph.OutputFrame;

public class ReassignedTuningAnalyzer implements SoundConsumer,
	Analyzer<AnalyzedFrame> {

	private static final int WINDOW_SIZE = 4096;

	private int overlapFactor = 1;
	// private double historyInSecs = 3.0;

	private DoubleRingBuffer samplesRingBuffer;
	private StreamingReassignedSpectrograph spectrograph;
	private Norm maxNorm = new MaxNorm();

	private double sampleRate;
	private int windowSize;
	private int hopSize;
	// private int historySize;

	private OutputFrame outputFrame;
	private double[] wrappedChromagram;
	// private double[] pitchHistory;
	// private double[] errorHistory;
	private double[] sampleWindow;

	public ReassignedTuningAnalyzer(int windowSize, double sampleRate) {
		this.windowSize = windowSize;
		this.sampleRate = sampleRate;
		spectrograph = new StreamingReassignedSpectrograph(windowSize,
			sampleRate);
		samplesRingBuffer = new DoubleRingBuffer(4 * windowSize);
		sampleWindow = new double[windowSize];

		hopSize = windowSize / overlapFactor;

		// historySize = (int) (historyInSecs * sampleRate / hopSize);

		// pitchHistory = new double[historySize];
		// errorHistory = new double[historySize];
	}

	@Override
	public void consume(double[] samples) {
		samplesRingBuffer.write(samples);
	}

	public AnalyzedFrame analyze() {
		awaitData();

		if (!canProcess()) {
			return null;
		}

		samplesRingBuffer.read(windowSize, sampleWindow);
		samplesRingBuffer.incrementReadIndex(hopSize);

		if (maxNorm.norm(sampleWindow) < 1e-4) {
			// continue;
			return null;
		}

		outputFrame = spectrograph.computeChromagram(sampleWindow);
		wrappedChromagram = outputFrame.getWrappedChromagram();
		double[] chromagram = outputFrame.getChromagram();

		boolean pitchDetected = false;
		double pitch = 0;
		double nearestTone = 0;
		double distToNearestTone = 0;
		int maxChromaBin = findMaxBin(chromagram);
		if (maxChromaBin == 0) {
			return null;
		}
		// too quiet
		if (chromagram[maxChromaBin] < 0.01) {
			return null;
		}
		double binFreq = spectrograph.frequencyForMusicalBin(maxChromaBin);
		int linearBin = (int) FastMath.floor(spectrograph
			.linearBinByFrequency(binFreq));
		double secondD = outputFrame.getSecondDerivatives()[linearBin];
		if (Math.abs(secondD) > 0.1) {
			return null;
		}
		double[] instantFrequencies = outputFrame.getFrequencies();
		double preciseFreq = instantFrequencies[linearBin];
		if (preciseFreq <= 0) {
			return null;
		}
		// System.out.println(preciseFreq*sampleRate);
		double frequency = preciseFreq * sampleRate;
		pitch = spectrograph.wrapMusicalBin(spectrograph
			.musicalBinByFrequency(preciseFreq))
			/ wrappedChromagram.length
			* 12;
		nearestTone = Modulo.modulo(Math.round(pitch), 12);
		distToNearestTone = phaseUnwrappedDiff(pitch, nearestTone);
		pitchDetected = true;

		// if (pitchDetected) {
		// shift(pitchHistory);
		// pitchHistory[pitchHistory.length - 1] = pitch;
		// shift(errorHistory);
		// errorHistory[errorHistory.length - 1] = distToNearestTone;
		// }

		return new AnalyzedFrame(pitchDetected, pitch, nearestTone,
			distToNearestTone, frequency);
	}

	private void awaitData() {
		while (!canProcess()) {
			try {
				samplesRingBuffer.awaitNotEmpty();
			} catch (InterruptedException e) {
				continue;
			}
		}
	}

	private boolean canProcess() {
		return samplesRingBuffer.getCapacityForRead() >= windowSize;
	}

	// private void shift(double[] values) {
	// System.arraycopy(values, 1, values, 0, values.length - 1);
	// }

	private int findMaxBin(double[] values) {
		double max = maxNorm.norm(values);
		if (max > 1e-6) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] == max) {
					return i;
				}
			}
		}
		return 0;
	}

	double phaseUnwrappedDiff(double u, double v) {
		double diff = u - v;
		// simple phase unwrapping
		if (diff > 6 && u > v) {
			diff -= 12;
		} else if (diff < -6 && u < v) {
			diff += 12;
		}
		return diff;
	}
}

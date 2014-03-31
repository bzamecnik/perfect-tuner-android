package com.harmoneye.tuner.analysis;

import com.harmoneye.tuner.viz.Visualizer;

public class AnalysisThread extends Thread {

	private ReassignedTuningAnalyzer tuningAnalyzer;
	private Visualizer<AnalyzedFrame> visualizer;

	public AnalysisThread(ReassignedTuningAnalyzer tuningAnalyzer,
		Visualizer<AnalyzedFrame> visualizer) {
		this.tuningAnalyzer = tuningAnalyzer;
		this.visualizer = visualizer;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			// this will wait until enough data is available
			tuningAnalyzer.update();
			visualizer.update(tuningAnalyzer.getAnalyzedFrame());
		}
	}
}

package com.harmoneye.tuner.analysis;

import android.util.Log;

import com.harmoneye.tuner.viz.Visualizer;

public class AnalysisThread extends Thread {

	private static final String LOG_TAG = AnalysisThread.class.getSimpleName();

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
			tuningAnalyzer.update();
			visualizer.update(tuningAnalyzer.getAnalyzedFrame());
			// TODO: It would be great to be notified from the SoundCapture
			// thread that there are some data to be analyzed. This is neither
			// efficient not responsive the best.
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Log.i(LOG_TAG, "thread interrupted");
			}
		}
	}
}

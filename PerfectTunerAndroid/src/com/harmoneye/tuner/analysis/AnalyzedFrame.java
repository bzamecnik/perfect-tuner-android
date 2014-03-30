package com.harmoneye.tuner.analysis;

public class AnalyzedFrame {
	private boolean pitchDetected;
	private double pitch;
	private double nearestTone;
	private double distToNearestTone;
	private double frequency;

	public AnalyzedFrame(boolean pitchDetected, double pitch,
		double nearestTone, double distToNearestTone, double frequency) {
		this.pitchDetected = pitchDetected;
		this.pitch = pitch;
		this.nearestTone = nearestTone;
		this.distToNearestTone = distToNearestTone;
		this.frequency = frequency;
	}

	public boolean isPitchDetected() {
		return pitchDetected;
	}

	public double getPitch() {
		return pitch;
	}

	public double getNearestTone() {
		return nearestTone;
	}

	public double getDistToNearestTone() {
		return distToNearestTone;
	}

	public double getFrequency() {
		return frequency;
	}

}

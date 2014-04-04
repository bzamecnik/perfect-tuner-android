package com.harmoneye.tuner.viz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceHolder;

import com.harmoneye.tuner.analysis.AnalyzedFrame;
import com.harmoneye.tuner.math.Modulo;
import com.harmoneye.tuner.math.filter.ScalarExpSmoother;

public class SpiralTunerGameView extends GameView implements Visualizer<AnalyzedFrame> {

	private static final double TWO_PI = 2 * Math.PI;
	private static final double HALF_PI = 0.5 * Math.PI;

	private static final String[] TONE_NAMES = { "C", "Db", "D", "Eb", "E",
		"F", "Gb", "G", "Ab", "A", "Bb", "B" };

	private ScalarExpSmoother errorSmoother = new ScalarExpSmoother(0.2);
	
	private Paint textPaint;
	private Rect textRect = new Rect();
	private Paint beadPaint;
	private RectF beadRect = new RectF();
	private long startTime;
	private float[] hsvColor;
	private float error;
	private String toneName;
	private float secsSinceStart;
	private AnalyzedFrame frame;
	private int selectedTone;
	private boolean pitchDetected;

	public SpiralTunerGameView(Context context) {
		super(context);

		textPaint = new Paint();
		textPaint.setStrokeWidth(3);
		textPaint.setColor(0xFF097286);
		textPaint.setTextAlign(Align.CENTER);
		//textPaint.setTextSize(100);
		textPaint.setAntiAlias(true);

		beadPaint = new Paint();
		beadPaint.setStrokeWidth(0);
		beadPaint.setColor(0xFF000000);
		beadPaint.setAntiAlias(true);

		hsvColor = new float[] { 0f, 0.25f, 0.85f };
	}

	@Override
	protected void onUpdate() {
		secsSinceStart = (getTime() - startTime) * 0.001f;

		// StopWatch watch = new StopWatch();
		// watch.start();

		if (frame != null) {
			error = (float) frame.getDistToNearestTone();
			pitchDetected = frame.isPitchDetected();
			selectedTone = (int) frame.getNearestTone();
			toneName = (pitchDetected) ? TONE_NAMES[selectedTone] : "";
		} else {
			error = 0;
			pitchDetected = false;
			selectedTone = 0;
			toneName = "";
		}
		error = (float)errorSmoother.smooth(error);
		
//		error = 0.12f;
//		toneName = "Gb";
	}

	@Override
	protected void onRender(Canvas canvas) {
		canvas.drawColor(Color.WHITE);

		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
		//canvas.drawLine(0, 0, (secsSinceStart % 1.0f) * width, 0, textPaint);
		
		canvas.save();

		canvas.translate(0.5f * width, 0.5f * height);

		hsvColor[0] = 360 * errorHue(error);
		hsvColor[2] = 0.85f;
		int color = Color.HSVToColor(hsvColor);
		beadPaint.setColor(color);
		hsvColor[2] = 0.6f;
		int selectedColor = Color.HSVToColor(hsvColor);
		textPaint.setColor(selectedColor);

		// 12, 0.7, 1.5
		int count = 12;
		float bigRadius = (float) (0.7 * 0.5 * Math.min(width, height));
		float size = 0.25f * bigRadius;
		
		textPaint.setTextSize(0.5f * bigRadius);
		
		float t = secsSinceStart * 2;
		float freq = 2 * error;
		float aFreq = Math.abs(freq);
		for (int i = 0; i < count; i++) {
			float p = i / (float) count;
			float s = (float) Modulo.modulo((p + (error > 0 ? 1 : -1) * t), 1);
			s = aFreq * s + (1 - aFreq);
			float r = bigRadius * (0.5f + 0.5f * s);
			float re = s * size;
			double angle = p * TWO_PI - HALF_PI;
			float x = r * (float) Math.cos(angle);
			float y = r * (float) Math.sin(angle);
			beadRect.set(x - re, y - re, x + re, y + re);
			beadPaint.setColor(pitchDetected && (i == selectedTone) ? selectedColor : color);
			canvas.drawCircle(x, y, re, beadPaint);
		}

		if (toneName != null && toneName.length() > 0) {
			textPaint.getTextBounds(toneName, 0, toneName.length(), textRect);
			canvas.drawText(toneName, 0, textRect.height() * 0.5f, textPaint);
		}

		canvas.restore();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		startTime = System.currentTimeMillis();

		super.surfaceCreated(holder);
	}

	private float errorHue(double error) {
		return 0.25f * (float) (1 - 2 * Math.abs(error));
	}

	public void update(AnalyzedFrame frame) {
		this.frame = frame;
	}
}

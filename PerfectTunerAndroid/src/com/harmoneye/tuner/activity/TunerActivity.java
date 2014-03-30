package com.harmoneye.tuner.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.harmoneye.tuner.R;
import com.harmoneye.tuner.analysis.ReassignedTuningAnalyzer;
import com.harmoneye.tuner.audio.android.Capture;
import com.harmoneye.tuner.viz.SpiralTunerGameView;

public class TunerActivity extends Activity {

	public static final String LOG_TAG = "PerfectTuner";

	private static final int WINDOW_SIZE = 4096;

	private Capture soundCapture;
	private ReassignedTuningAnalyzer tuningAnalyzer;
	private SpiralTunerGameView gameView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main);

		tuningAnalyzer = new ReassignedTuningAnalyzer(WINDOW_SIZE,
			Capture.AUDIO_SAMPLE_RATE);

//		TextView fpsTextView = (TextView) findViewById(R.id.fpsTextView);
//		FpsCounter fpsCounter = new FpsCounter(fpsTextView);

		LinearLayout layout = (LinearLayout) findViewById(R.id.frame);

		gameView = new SpiralTunerGameView(getApplicationContext());
//		gameView.setFpsCounter(fpsCounter);
		gameView.setTuningAnalyzer(tuningAnalyzer);

		layout.addView(gameView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (soundCapture == null) {
			soundCapture = new Capture(tuningAnalyzer);
		}
		Thread thread = new Thread(soundCapture);
		thread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		gameView.pause();

		if (soundCapture != null) {
			soundCapture.stop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (soundCapture != null) {
			soundCapture.stop();
		}
	}
}
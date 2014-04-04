package com.harmoneye.tuner.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.harmoneye.tuner.R;
import com.harmoneye.tuner.analysis.AnalysisThread;
import com.harmoneye.tuner.analysis.ReassignedTuningAnalyzer;
import com.harmoneye.tuner.audio.android.SoundCapture;
import com.harmoneye.tuner.viz.SpiralTunerGameView;

public class TunerActivity extends Activity {

	public static final String LOG_TAG = "PerfectTuner";

	private static final int WINDOW_SIZE = 4096;

	private SoundCapture soundCapture;
	private ReassignedTuningAnalyzer tuningAnalyzer;
	private SpiralTunerGameView gameView;

	private AnalysisThread analysisThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableFullScreen();

		setContentView(R.layout.main);
		
		tuningAnalyzer = new ReassignedTuningAnalyzer(WINDOW_SIZE,
			SoundCapture.AUDIO_SAMPLE_RATE);

		gameView = (SpiralTunerGameView) findViewById(R.id.spiralTunerView);

		// TextView fpsTextView = (TextView) findViewById(R.id.fpsTextView);
		// FpsCounter fpsCounter = new FpsCounter(fpsTextView);
		// gameView.setFpsCounter(fpsCounter);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (soundCapture == null) {
			soundCapture = new SoundCapture(tuningAnalyzer);
		}
		Thread thread = new Thread(soundCapture);
		thread.start();

		analysisThread = new AnalysisThread(tuningAnalyzer, gameView);
		analysisThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		gameView.pause();

		if (soundCapture != null) {
			soundCapture.stop();
		}
		if (analysisThread != null) {
			analysisThread.interrupt();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (soundCapture != null) {
			soundCapture.stop();
		}
		if (analysisThread != null) {
			analysisThread.interrupt();
		}
	}

	/*
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_settings:
			openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	
	*/
	
	private void enableFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
}
package com.harmoneye.tuner.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.harmoneye.audio.android.AudioRecordDiscovery;
import com.harmoneye.audio.android.AudioRecordDiscovery.AudioRecordParams;
import com.harmoneye.audio.android.SoundCapture;
import com.harmoneye.tuner.R;
import com.harmoneye.tuner.analysis.AnalysisThread;
import com.harmoneye.tuner.analysis.ReassignedTuningAnalyzer;
import com.harmoneye.tuner.viz.SpiralTunerGameView;

public class TunerActivity extends Activity {

	public static final String LOG_TAG = "PerfectTuner";

	private SoundCapture soundCapture;
	private ReassignedTuningAnalyzer tuningAnalyzer;
	private SpiralTunerGameView gameView;

	private AnalysisThread analysisThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableFullScreen();

		setContentView(R.layout.main);

		AudioRecordParams audioRecordParams = new AudioRecordDiscovery()
			.findParams();

		tuningAnalyzer = new ReassignedTuningAnalyzer(
			audioRecordParams.getSampleRate());

		soundCapture = new SoundCapture(tuningAnalyzer, audioRecordParams);

		gameView = (SpiralTunerGameView) findViewById(R.id.spiralTunerView);

		// TextView fpsTextView = (TextView) findViewById(R.id.fpsTextView);
		// FpsCounter fpsCounter = new FpsCounter(fpsTextView);
		// gameView.setFpsCounter(fpsCounter);

		prepareAd();
	}

	private void prepareAd() {
		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = createAdRequest();
		adView.loadAd(adRequest);
	}

	private AdRequest createAdRequest() {
		Builder builder = new AdRequest.Builder();
		builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
		String[] admobTestDeviceIds = getResources()
			.getStringArray(R.array.admob_test_device_ids);
		for (String id : admobTestDeviceIds) {
			builder.addTestDevice(id);
		}
		AdRequest adRequest = builder.build();
		// TODO: explore better targetting the ads via keywords
		// builder.addKeyword("music guitar")
		// - eg. provide the selected instrument, etc.
		return adRequest;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Thread thread = new Thread(soundCapture);
		thread.start();

		analysisThread = new AnalysisThread(tuningAnalyzer, gameView);
		analysisThread.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		gameView.pause();
		if (soundCapture.isRunning()) {
			soundCapture.stop();
		}
		if (analysisThread != null) {
			analysisThread.interrupt();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (soundCapture.isRunning()) {
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
	}
}
package com.example.timesync;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import ibt.ortc.api.*;
import ibt.ortc.extensibility.*;

public class CountDownActivity extends AppCompatActivity {

	private TextView theCounterView;
	private FloatingActionButton theButton;
	private TextView theButtonCaption;
	private int theCounter = 10;
	private OrtcClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_count_down);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		theCounterView = (TextView) findViewById(R.id.theCounter);
		theButton = (FloatingActionButton) findViewById(R.id.theButton);
		theButton.setEnabled(false);
		theButtonCaption = (TextView) findViewById(R.id.theButtonCaption);
		startRealtimeClient();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_count_down, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_reset) {
			resetButton();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void resetButton() {
		theCounter = 10;
		theCounterView.setText("");
		theButton.setEnabled(true);
		theButton.setBackgroundTintList(ColorStateList.valueOf(
				getResources().getColor(R.color.colorAccent)));
		theButtonCaption.setText(R.string.start);
	}

	private Runnable countDownAction = new Runnable() {
	@Override
		public void run() {
			theButton.setEnabled(false);
			theButton.setBackgroundTintList(ColorStateList.valueOf(
					getResources().getColor(R.color.colorRunning)));
			theButtonCaption.setText(R.string.running);
			theCounterView.setText(Integer.toString(theCounter));
			if (theCounter-- > 0) {
				theCounterView.postDelayed(this, 1000);
			}
			else {
				theButton.setBackgroundTintList(ColorStateList.valueOf(
						getResources().getColor(R.color.colorFinished)));
			}
		};
	};

	public void startCountDown(View view) {
		client.send("Blinks", "START");
	}

	private boolean startRealtimeClient() {
		OrtcFactory factory = null;

		Ortc ortc = new Ortc();
		try {
			factory = ortc.loadOrtcFactory("IbtRealtimeSJ");
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (factory == null) {
			return false;
		}
		client = factory.createClient();

		client.setClusterUrl("http://ortc-developers.realtime.co/server/2.1/");
		client.setConnectionMetadata("TimeSyncAndroidApp");

		client.onConnected = new OnConnected() {
			@Override
			public void run(final OrtcClient sender) {
				client.subscribe("Blinks", true,
						new OnMessage() {
							public void run(OrtcClient sender, String channel, String message) {
								if (message.equals("START")) {
									theCounterView.post(countDownAction);
								}
							};
						}
				);
			}
		};

		client.onSubscribed = new OnSubscribed() {
			@Override
			public void run(OrtcClient sender, String channel) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						theButton.setEnabled(true);
					}
				});
			}
		};

		client.onException = new OnException() {
			@Override
			public void run(OrtcClient send, Exception e) {
				e.printStackTrace();
			}
		};
		client.connect(getString(R.string.api_key), "my_authentication_token");
		return true;
	}
}

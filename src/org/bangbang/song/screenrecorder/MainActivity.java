package org.bangbang.song.screenrecorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void onStart(View view) {
		new RecordProcess(getExternalFilesDir(null), "media%s", 1 * 60).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static class RecordProcess {
		private File mDir;
		private int mLimit;
		private Thread mWorkThread;
		private String mStem;

		public RecordProcess(File mediaDir, String nameStem, int limit){
			mDir = mediaDir;
			mStem = nameStem;
			mLimit = limit;
		}
		
		public void start(){
			final int androidLimit = 3 * 60;
			final int loopCount = mLimit / androidLimit + 1;
			mWorkThread = new Thread("screen record") {
				@Override
				public void run() {
					super.run();
					
					for (int i = 0; i < loopCount; i++) {
						String file = mDir.getPath() + "/" + mStem.replace("%s", i + "");
						String[] command = new String[]{
								"screenrecord",
								"--verbose",
								"--time-limit",
								((i == (loopCount - 1)) ? mLimit - (i * androidLimit) : androidLimit) + "",
								file								
						};
						
						Process process = null;
	                    try {
	                    	Log.i(TAG, "exec: " + MainActivity.toString(command));
	                        process = new ProcessBuilder(command).start();               
	                        BufferedReader in = new BufferedReader(new InputStreamReader(
	                        		process.getInputStream()));         
	                        BufferedReader err = new BufferedReader(new InputStreamReader(
	                        		process.getErrorStream()));
	                        String inLine = null;
	                        String errLine = null;
	                        while ((errLine = err.readLine()) != null
	                        		||(inLine = in.readLine()) != null) {
	                        	if (!TextUtils.isEmpty(inLine)) {
	                        		Log.d(TAG, "out: " + inLine);
	                        	}
	                        	if (!TextUtils.isEmpty(errLine)) {
	                        		Log.d(TAG, "err: " + errLine);
	                        	}
	                        }
	                    } catch (Exception e) {
	                        // ignore this, it's safe.
	                        Log.e(TAG, "Exception", e);
	                    } finally {
	                        if (null != process) {
	                            process.destroy();
	                        }
	                    }
					}
				}
			};
			mWorkThread.start();
		}
	}
	
	public static String toString(String[] strings) {
		String string = "";
		for (String s : strings) {
			string  += s  + " ";
		}
		
		return string;
	}
}

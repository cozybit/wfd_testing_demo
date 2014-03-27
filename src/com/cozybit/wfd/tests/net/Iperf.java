package com.cozybit.wfd.tests.net;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cozybit.wfd.tests.utils.Log;

import android.content.Context;
import android.os.AsyncTask;

/*The main class for executing iperf instances. This class extends the class 
 * AsyncTask which is used to perform long background tasks.*/
public class Iperf extends AsyncTask<Void, String, String>  {
	
	private static String TAG = Iperf.class.getName();
	//TODO: do not hardcode "/data/"; use Context.getFilesDir().getPath() instead
	private static final String IPERF_PATH = "/data/data/com.cozybit.wfd.tests/iperf";
	
	private Context mContext;
	private Process mProcess = null;
	private String mIperfOptions;
	
	public Iperf(Context context, int interval) {
		mContext = context;
		mIperfOptions = String.format("-s -u -i %d", interval);
	}
	
	public Iperf(Context context, String clientIP, String bandwith, int duration, int interval) {
		mContext = context;
		mIperfOptions = String.format("-u -c %s -b %s -t %d -i %d", clientIP, bandwith, duration, interval);
	}
	
	//This function is used to implement the main task that runs on the background.
	@Override
	protected String doInBackground(Void... voids) {
		
		if( !isIperfAvailable() )
			if(! copyIperfInternalMemory() )
				return null;
		
		try {
			List<String> commandList = new ArrayList<String>(Arrays.asList(mIperfOptions.split(" ")));
			//The execution command is added first in the list for the shell interface.
			commandList.add(0, IPERF_PATH);
			//The process is now being run with the verified parameters.
			mProcess = new ProcessBuilder().command(commandList).redirectErrorStream(true).start();
			//A buffered output of the stdout is being initialized so the iperf output could be displayed on the screen.
			BufferedReader reader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
			int read;
			//The output text is accumulated into a string buffer and published to the GUI
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			String str = null;
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
				str = output.toString();
				Log.d(TAG, "%s", str );
				//This is used to pass the output to the thread running the GUI, since this is separate thread.
				//publishProgress(output.toString());
				output.delete(0, output.length());
			}
			reader.close();
			mProcess.destroy();
			return str;
		}
		catch (IOException e) {
			publishProgress("Error occurred while accessing system resources, please reboot and try again.");
			e.printStackTrace();
		}
		
		return null;
	}

	
	//This function is called by AsyncTask when publishProgress is called.
	//This function runs on the main GUI thread so it can publish changes to it, while not 
	//getting in the way of the main task.
	/*@Override
	public void onProgressUpdate(String... strings) {
	}*/

	//This function is called by the AsyncTask class when IperfTask.cancel is called.
	//It is used to terminate an already running task.
	@Override
	public void onCancelled() {
		//The running process is destroyed and system resources are freed.
		if (mProcess != null) {
			mProcess.destroy();
			try {
				mProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mProcess = null;
		}

		Log.d(TAG, "Iperf aborted!!");
	}

	@Override
	public void onPostExecute(String result) {
		
		//TODO parse results:
		//[  3] Server Report:
		//[ ID] Interval       Transfer     Bandwidth       Jitter   Lost/Total Datagrams
		//[  3]  0.0-15.0 sec  8.94 MBytes  5.00 Mbits/sec  1.159 ms    0/ 6379 (0%)

		
		//The running process is destroyed and system resources are freed.
		if (mProcess != null) {
			mProcess.destroy();
			try {
				mProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mProcess = null;
			Log.d(TAG, "Iperf finished!");
		}
	}
	
	//This function is used to copy the iperf executable to a directory which execute permissions for this application, and then gives it execute permissions.
	//It runs on every initiation of an iperf test, but copies the file only if it's needed.
	public boolean isIperfAvailable() {
		
		InputStream in;
		//The asset "iperf" (from assets folder) inside the activity is opened for reading.
		try {
			in = mContext.getResources().getAssets().open("iperf");
		} catch (IOException e) {
			Log.e(TAG, "Error occurred while accessing iperf in the application resources");
			e.printStackTrace();
			return false;			
		}
		
		//Checks if the file already exists, if not copies it.
		try {
			new FileInputStream(IPERF_PATH);
		} catch (FileNotFoundException e) {
			Log.d(TAG, "Iperf is not available in the internal memory: " + IPERF_PATH);
			return false;
		} 

		Log.d(TAG, "Iperf is installed in: %s", IPERF_PATH);
		return true;
	}
	
	private boolean copyIperfInternalMemory() {
		Log.d(TAG, "Copying Iperf to internal memory. Dst: %s", IPERF_PATH );
		
		InputStream in;
		try {
			in = mContext.getResources().getAssets().open("iperf");
			//The file named "iperf" is created in a system designated folder for this application.
			OutputStream out = new FileOutputStream(IPERF_PATH, false); 
			// Transfer bytes from "in" to "out"
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			Log.d(TAG, "Iperf copied succesfully!!");
			
			Log.d(TAG, "Changing Iperf execute permissions...");
			//After the copy operation is finished, we give execute permissions to the "iperf" executable using shell commands.
			Process processChmod = Runtime.getRuntime().exec("/system/bin/chmod 744 " + IPERF_PATH); 
			// Executes the command and waits untill it finishes.
			processChmod.waitFor();
			Log.d(TAG, "Iperf permissions changed succesfully!!");
		} catch (Exception e) {
			Log.e(TAG, "Error occurred while accessing system resources, please reboot and try again.");
			e.printStackTrace();
			return false;
		}
		
		Log.d(TAG, "Copying Iperf succesfully!!");
		return true;
	}
}

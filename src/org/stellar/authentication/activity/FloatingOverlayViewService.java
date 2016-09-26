package org.stellar.authentication.activity;

import java.util.List;

import org.stellar.authentication.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;

public class FloatingOverlayViewService extends Service implements OnTouchListener, OnClickListener {

	
	private static final String TAG = FloatingOverlayViewService.class.getSimpleName();

	private View topLeftView;
	private Button overlayedButton;
	private WindowManager wm;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		overlayedButton = new Button(this);
		overlayedButton.setBackgroundResource(R.drawable.power_off);
		overlayedButton.setOnTouchListener(this);

		overlayedButton.setOnClickListener(this);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(50, 50,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.RIGHT | Gravity.TOP;

		params.x = 15;
		params.y = 15;
		wm.addView(overlayedButton, params);

		topLeftView = new View(this);
		WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(50, 50,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		topLeftParams.gravity = Gravity.RIGHT | Gravity.TOP;
		topLeftParams.x = 15;
		topLeftParams.y = 15;
		topLeftParams.width = 0;
		topLeftParams.height = 0;
		wm.addView(topLeftView, topLeftParams);	
		

	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");
	
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (overlayedButton != null) {
			wm.removeView(overlayedButton);
			wm.removeView(topLeftView);
			overlayedButton = null;
			topLeftView = null;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setComponent(new ComponentName("org.stellar.pes.wslhd","org.stellar.pes.wslhd.MainActivity"));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);		
		this.stopSelf();
		killBackgroundProcess();
		return false;
	}

	@Override
	public void onClick(View v) {
		
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setComponent(new ComponentName("org.stellar.pes.wslhd","org.stellar.pes.wslhd.MainActivity"));
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		this.stopSelf();
		killBackgroundProcess();

	}
	
	private void killBackgroundProcess(){
		
		Context context = getApplicationContext();
		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		
		List<RunningAppProcessInfo> activityes = ((ActivityManager)am).getRunningAppProcesses();
		
		 for (int iCnt = 0; iCnt < activityes.size(); iCnt++){

             Log.e("List of running process","APP: "+iCnt +" "+ activityes.get(iCnt).processName);

             if (activityes.get(iCnt).processName.contains("com.citrix.Receiver")){
             	
             	 Log.e("Service ","Found CITRIX.... Kill it");
             	
                 android.os.Process.sendSignal(activityes.get(iCnt).pid, android.os.Process.SIGNAL_KILL);
                 android.os.Process.killProcess(activityes.get(iCnt).pid);
                 am.killBackgroundProcesses("com.citrix.Receiver");

                 System.out.println("Killed Citrix");
             }
             
            if (activityes.get(iCnt).processName.contains("org.mozilla.fennec_root")){
             	
             	System.out.println("Found Firefox.... Kill it");
             	
                 android.os.Process.sendSignal(activityes.get(iCnt).pid, android.os.Process.SIGNAL_KILL);
                 android.os.Process.killProcess(activityes.get(iCnt).pid);
                 am.killBackgroundProcesses("org.mozilla.fennec_root");
                                         
                 Log.e("Service","Killed Firefox....");
             }
         }
		
	}
}

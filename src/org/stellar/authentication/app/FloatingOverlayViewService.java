package org.stellar.authentication.app;


import org.stellar.authentication.R;
import org.stellar.authentication.activity.MainActivity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
/*public class FloatingFaceBubbleService extends Service {
    private WindowManager windowManager;
    private ImageView floatingFaceBubble;
    
    public void onCreate() {
    	//Log.i("Bubble", "floating bubble");
        super.onCreate();
        floatingFaceBubble = new ImageView(this);
        //a face floating bubble as imageView
        floatingFaceBubble.setImageResource(R.drawable.back_arrow);  

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        //here is all the science of params
        final LayoutParams myParams = new WindowManager.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.TYPE_PHONE,
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

        myParams.gravity = Gravity.TOP | Gravity.LEFT;
        myParams.x=0;
        myParams.y=100;
        // add a floatingfacebubble icon in window
        windowManager.addView(floatingFaceBubble, myParams);
        
       
        try{
        	//for moving the picture on touch and slide
        	floatingFaceBubble.setOnTouchListener(new View.OnTouchListener() {
                WindowManager.LayoutParams paramsT = myParams;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                private long touchStartTime = 0;
                
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                		
                	String packageName = "org.stellar.pes.ent";
                    if (IsAppInstalled(packageName)) {
                        Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
                        startActivity(i);
                    } else {
                        Log.i("Test","App is not installed");
                    }
                	
                	//Intent intent = new Intent(FloatingFaceBubbleService.this ,RegisterActivity.class);
                	//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);	
                	//startActivity(intent);
                     
					//remove bubble on long press
                   	if(System.currentTimeMillis()-touchStartTime>ViewConfiguration.getLongPressTimeout() && initialTouchX== event.getX()){
                    //if(isApplicationBroughtToBackground()){
                		windowManager.removeView(floatingFaceBubble);
                		stopSelf();
                		return false;
                		
                	}
                	
                	switch(event.getAction()){
                          
                    case MotionEvent.ACTION_DOWN:
                    	touchStartTime = System.currentTimeMillis();
                        initialX = myParams.x;
                        initialY = myParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                    	
                        myParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        myParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(v, myParams);
                        break;
                    }
                    return false;
                
                }
            });
        	    	
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
  
    
    private boolean IsAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean isAppInstalled = false;
        try {
            pm.getPackageInfo(packageName,
                    PackageManager.GET_ACTIVITIES);
            isAppInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            isAppInstalled = false;
        }
        return isAppInstalled;
    }
   
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    
}*/


public class FloatingOverlayViewService extends Service implements OnTouchListener, OnClickListener {
    
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
	overlayedButton.setBackgroundResource(R.drawable.cross);
	overlayedButton.setOnTouchListener(this);

	overlayedButton.setOnClickListener(this);
	
	WindowManager.LayoutParams params = new WindowManager.LayoutParams(50, 50, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
	params.gravity = Gravity.RIGHT | Gravity.TOP;
	
	params.x = 15;
	params.y = 15;
	wm.addView(overlayedButton, params);

	topLeftView = new View(this);
	WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(50,50, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
	topLeftParams.gravity = Gravity.RIGHT | Gravity.TOP;
	topLeftParams.x = 15;
	topLeftParams.y = 15;
	topLeftParams.width = 0;
	topLeftParams.height = 0;
	wm.addView(topLeftView, topLeftParams);

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
    	//Toast.makeText(this, "Overlay button click event", Toast.LENGTH_SHORT).show();
    	Intent dialogIntent = new Intent(this,MainActivity.class);
    	dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	this.startActivity(dialogIntent);
    	this.stopSelf();


	return false;
    }

    @Override
    public void onClick(View v) {
	
	Intent dialogIntent = new Intent(this,MainActivity.class);
	dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	this.startActivity(dialogIntent);
	this.stopSelf();

    } 
    }


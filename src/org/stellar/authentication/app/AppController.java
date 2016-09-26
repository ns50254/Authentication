package org.stellar.authentication.app;

import android.app.Application;
import android.text.TextUtils;

import java.util.HashMap;

import org.stellar.authentication.R;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

public class AppController extends Application {

	public static final String TAG = AppController.class.getSimpleName();

	private RequestQueue mRequestQueue;
	
	// The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-73844712-1";

    public static int GENERAL_TRACKER = 0;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. 
        ECOMMERCE_TRACKER, // Tracker used by all commerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	private static AppController mInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}
	
	 public synchronized Tracker getTracker(TrackerName trackerId) {
	        if (!mTrackers.containsKey(trackerId)) {

	            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
	            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
	            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
	                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(
	                            R.xml.global_tracker)
	                            : analytics.newTracker(R.xml.ecommerce_tracker);
	            t.enableAdvertisingIdCollection(true);
	            mTrackers.put(trackerId, t);
	        }
	        return mTrackers.get(trackerId);
	    }

}
package net.bible.android.view.activity.page;

import net.bible.android.BibleApplication;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;

public class TiltScrollManager {

	private WebView mWebView;
	
	private boolean mIsTiltScrollEnabled;

	private Handler mScrollHandler = new Handler();
	
	// the pitch at which a user views the text stationary
	// this changes dynamically when the screen is touched
	// both angles are degrees
	private int mNoScrollViewingPitch = 57;
	private static final int NO_SCROLL_VIEWING_TOLERANCE = 7;
	private static final int NO_SPEED_INCREASE_VIEWING_TOLERANCE = 7;
	
	// this is decreased (subtracted from) to speed up scrolling
	private static int BASE_TIME_BETWEEN_SCROLLS = 50;
	
	private float[] mAccelerometerValues;
	private float[] mMagneticFieldValues;
	private int mPitch;
	private int mTempPrevPitch;
	
	private static final String TAG = "TiltScrollManager";
	
	public TiltScrollManager(WebView webView) {
		this.mWebView = webView;
	}
	
	/** start or stop tilt to scroll functionality
	 */
	public void enableTiltScroll(boolean enable) {
		if (mIsTiltScrollEnabled != enable) {
			mIsTiltScrollEnabled = enable;
			if (enable) {
				connectAMListeners();
				kickOffScrollHandler();
			} else {
				disconnectAMListeners();
			}
		}
	}
	
	/** 
	 * Scroll screen at a certain speed
	 */

	/** start scrolling handler
	 */
	private void kickOffScrollHandler() {
       mScrollHandler.post(mScrollTask);
	}
	
	/** cause content of attached WebView to scroll
	 */
	private Runnable mScrollTask = new Runnable() {
		public void run() {
			if (mPitch!=mTempPrevPitch) {
				Log.d(TAG, "Pitch:" + Math.round(mPitch));
			}
			int speedUp = 0;
			int devianceFromViewingAngle = Math.abs(mPitch-mNoScrollViewingPitch);
			if (devianceFromViewingAngle > NO_SCROLL_VIEWING_TOLERANCE) {

				// speedUp if tilt screen beyond a certain amount
				speedUp = Math.max(0, devianceFromViewingAngle-NO_SCROLL_VIEWING_TOLERANCE-NO_SPEED_INCREASE_VIEWING_TOLERANCE);

				// speedup is initially done by decreasing time between scrolls and then by increasing scroll amount
				int scrollAmount = 1+speedUp;
				Log.d(TAG,  "scroll amount:"+scrollAmount);
				
				boolean isTiltedForward = mPitch<mNoScrollViewingPitch; 
				// TODO - do not allow scroll off end
				if (isTiltedForward) {
					// scroll down/forward
					mWebView.scrollBy(0, scrollAmount);
				} else if (mWebView.getScrollY() > 0) {
					// scroll up/back
					mWebView.scrollBy(0, -scrollAmount);
				}
				
			}

			if (mIsTiltScrollEnabled) {
				mScrollHandler.postDelayed(mScrollTask, BASE_TIME_BETWEEN_SCROLLS);
			}
			mTempPrevPitch = mPitch; 
		}
	};

	/**
	 * Orientation monitor (see Professional Android 2 App Dev Meier pg 469)
	 */
	
	private void connectAMListeners() {
		SensorManager sm = (SensorManager) BibleApplication.getApplication().getSystemService(Context.SENSOR_SERVICE);
		Sensor aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor mfSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		sm.registerListener(myAccelerometerListener, aSensor, SensorManager.SENSOR_DELAY_UI);

		sm.registerListener(myMagneticFieldListener, mfSensor, SensorManager.SENSOR_DELAY_UI);
	}
    private void disconnectAMListeners() {
		SensorManager sm = (SensorManager) BibleApplication.getApplication().getSystemService(Context.SENSOR_SERVICE);
    	sm.unregisterListener(myAccelerometerListener);
    	sm.unregisterListener(myMagneticFieldListener);
    }

	final SensorEventListener myAccelerometerListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				mAccelerometerValues = sensorEvent.values;
				calcOrientation();
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	final SensorEventListener myMagneticFieldListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mMagneticFieldValues = sensorEvent.values;
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	private void calcOrientation() {
		if (mMagneticFieldValues !=null && mAccelerometerValues!=null) {
			float[] values = new float[3];
			float[] R = new float[9];
			SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticFieldValues);
	
			float[] outR = new float[9];
			SensorManager.remapCoordinateSystem(R,
			                                    SensorManager.AXIS_X,
			                                    SensorManager.AXIS_Z,
			                                    outR);
			SensorManager.getOrientation(outR, values);
	
			// Convert from radians to degrees.
//			values[0] = (float) Math.toDegrees(values[0]);
			mPitch = (int)Math.toDegrees(values[1]);
//			values[2] = (float) Math.toDegrees(values[2]);
		}
	}

}

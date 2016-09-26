package org.stellar.authentication.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.smartcardio.ATR;
import android.smartcardio.Card;
import android.smartcardio.CardChannel;
import android.smartcardio.CardException;
import android.smartcardio.CardTerminal;
import android.smartcardio.CommandAPDU;
import android.smartcardio.ResponseAPDU;
import android.smartcardio.TerminalFactory;
import android.smartcardio.ipc.CardService;
import android.smartcardio.ipc.ICardService;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HeterogeneousExpandableList;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import org.json.JSONException;
import org.json.JSONObject;
import org.stellar.authentication.R;
import org.stellar.authentication.app.AppConfig;
import org.stellar.authentication.app.AppController;
import org.stellar.authentication.helper.SessionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("NewApi")
public class LoginActivity extends Activity {
	private static final String TAG = LoginActivity.class.getSimpleName();
	protected static final String NULL = null;
	private Button btnLogin;
	private Button btnLoginWithUsernamePassword;
	private Button btnLoginWithCardIdPIN;
	private Button btnAuthwithCardIdPIN;
	private Button btnAuthwithUsernamePass;
	private Button btnLinkToRegister;
	private Button btnLinkMainLogin;
	private Button btnSendLog;
	private EditText inputUserName;
	private EditText inputPassword;
	private EditText inputPIN;
	//private EditText inputCardId;
	private ProgressDialog pDialog;
	private SessionManager session;
	//private SQLiteHandler db;
	
	private String name;

	private String cardStatus = "0";
	
	private static final String MANAGEMENT_PACKAGE = "com.theobroma.cardreadermanager";
	private static final String MANAGEMENT_APP = "CardReaderManager.apk";
	private static final int REQUEST_APP_INSTALL = 0xbeef;
	
	private ICardService mService = null;
	private TerminalFactory mFactory = null;
	private CardTerminal mReader = null;
	
	private AsyncTask<Void,String,String> mGetCardStatusTask = null;
	
	private String mAPDU = "";
	
	public static final String auth_tag_username_password = "password";
	public static final String auth_tag_cardId_pin = "PIN";
	
	private String CitrixUrl = "http://188.165.1.55/apes/autologin.php";

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//		Toast.makeText(getApplicationContext(),
//				"Please hold the card near the reader and enter PIN for secure authentication.After that click on Authenticate with card button",
//				Toast.LENGTH_LONG).show();
		
		
//		com.google.android.gms.analytics.Tracker t = ((AppController) getApplication())
//				.getTracker(org.stellar.authentication.app.AppController.TrackerName.APP_TRACKER);
//
//		// Enable Display Features so you can see demographics in Google
//		// Analytics
//		t.enableAdvertisingIdCollection(true);
//
//		// Set screen name.
//		t.setScreenName("Login Activity");
//
//		// Send a screen view.
//		t.send(new HitBuilders.AppViewBuilder().build());

		//inputCardId = (EditText) findViewById(R.id.cardid);
		inputUserName = (EditText) findViewById(R.id.username);
	    inputPassword = (EditText) findViewById(R.id.password);
	    inputPIN = (EditText) findViewById(R.id.PIN);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnAuthwithUsernamePass = (Button) findViewById(R.id.btnAuthwithUsernamePass);
		btnAuthwithCardIdPIN = (Button) findViewById(R.id.btnAuthwithCardIdPIN);
		btnLoginWithUsernamePassword = (Button) findViewById(R.id.btnLoginWithUsernamePassword);
		btnLoginWithCardIdPIN = (Button) findViewById(R.id.btnLoginWithCardIdPIN);
		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
		btnLinkMainLogin  = (Button) findViewById(R.id.btnLinkMainLogin);
		btnSendLog = (Button) findViewById(R.id.btnSendLog);

		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		
		try{
			if (alreadyInstalled(MANAGEMENT_PACKAGE)) {
				mService = CardService.getInstance(this);	
			} else {
				/* If the management App cannot be installed, further processing
				 * is impossible. */
				if (!installManagementApp()) {
					 showToast("Error: unable to install the management App");
					this.finish();			  
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		// SQLite database handler
		//db = new SQLiteHandler(getApplicationContext());

		// Session manager
//		session = new SessionManager(getApplicationContext());
//
//		// Check if user is already logged in or not
//		if (session.isLoggedIn()) {
//
//			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//			startActivity(intent);
//
//			
//		}
		
		
		try{
			
		mGetCardStatusTask = new GetCardStatusTask();
		mGetCardStatusTask.execute();			
			
		//Toast.makeText(getApplicationContext(), "UID:" +mAPDU, Toast.LENGTH_LONG).show();
		
		if(cardStatus.equals("1")){
			Intent in = new Intent(getApplicationContext(),
                    LoginActivity.class);
               startActivity(in);
               finish();
		}else{
			if(!(mAPDU.isEmpty()) && cardStatus.equals("0"))
			checkCardEnroll(mAPDU);
		}
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		
		

		// Login button Click Event
		btnLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				try {
					try {
						mGetCardStatusTask = new GetCardStatusTask();
						mGetCardStatusTask.execute();
						// Toast.makeText(getApplicationContext(), "UID:"
						// +mAPDU, Toast.LENGTH_LONG).show();
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
//					String cardId = mAPDU;
//					String PIN = inputPIN.getText().toString().trim();
					//if (!cardId.isEmpty()) {
					//Log.e("Card ID", ""+cardId);
					btnLogin.setVisibility(View.INVISIBLE);
					btnAuthwithUsernamePass.setVisibility(View.GONE);
					btnAuthwithCardIdPIN.setVisibility(View.VISIBLE);
					inputUserName.setVisibility(View.INVISIBLE);
					inputPassword.setVisibility(View.GONE);
					inputPIN.setVisibility(View.VISIBLE);
					btnLoginWithUsernamePassword.setVisibility(View.GONE);
					btnLoginWithCardIdPIN.setVisibility(View.VISIBLE);
					btnLinkToRegister.setVisibility(View.GONE);
					btnLinkMainLogin.setVisibility(View.VISIBLE);
					
					//}
//					 else {
//						// Prompt user to enter credentials
//						Toast.makeText(getApplicationContext(), "Please tap on your card and try again!", Toast.LENGTH_LONG)
//								.show();
//					}
					
//					try {
//
//						mGetCardStatusTask = new GetCardStatusTask();
//						mGetCardStatusTask.execute();
//
//						// Toast.makeText(getApplicationContext(), "UID:"
//						// +mAPDU, Toast.LENGTH_LONG).show();
//
//					} catch (NullPointerException e) {
//						e.printStackTrace();
//					}
				} catch (NullPointerException e) {
					e.printStackTrace();					 
				}
			}

		});
		
		btnLoginWithCardIdPIN.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				try {
					
					String cardId = mAPDU;
					String PIN = inputPIN.getText().toString().trim();
					
					pDialog.setMessage("Please hold your card  near the device for authentication for 15 seconds and enter PIN...");

					if (!cardId.isEmpty()&& !PIN.isEmpty()) {
						checkLogin(cardId,PIN);
						//Toast.makeText(getApplicationContext(), "UID:" +mAPDU, Toast.LENGTH_LONG).show();
					} else {
						// Prompt user to enter credentials
						Toast.makeText(getApplicationContext(), "Please enter the credentials!", Toast.LENGTH_LONG)
								.show();
					}
				} catch (NullPointerException e) {
					e.printStackTrace();					 
				}
			}

		});
		
		btnLinkMainLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				try {
					btnLogin.setVisibility(View.VISIBLE);
					btnAuthwithUsernamePass.setVisibility(View.VISIBLE);
					btnAuthwithCardIdPIN.setVisibility(View.GONE);
					inputUserName.setVisibility(View.VISIBLE);
					inputPassword.setVisibility(View.VISIBLE);
					inputPIN.setVisibility(View.GONE);
					btnLoginWithUsernamePassword.setVisibility(View.VISIBLE);
					btnLoginWithCardIdPIN.setVisibility(View.GONE);
					btnLinkToRegister.setVisibility(View.VISIBLE);
					btnLinkMainLogin.setVisibility(View.GONE);
					inputPIN.setText("");
					//mAPDU = "";

				} catch (NullPointerException e) {
					e.printStackTrace();					 
				}
			}

		});
		
		// Login with User Name and Password button Click Event
		btnLoginWithUsernamePassword.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {

				// String cardid = inputCardId.getText().toString().trim();
				String username = inputUserName.getText().toString().trim();
				String password = inputPassword.getText().toString().trim();

				try {
					if (!username.isEmpty() && !password.isEmpty()) {
						checkLoginWithUsernamePassword(username, password);
					} else {
						// Prompt user to enter credentials
						Toast.makeText(getApplicationContext(), "Please enter the credentials!", Toast.LENGTH_LONG)
								.show();
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				// Intent i = new Intent(Intent.ACTION_MAIN);
				// i.setComponent(new
				// ComponentName("org.stellar.pes.wslhd","org.stellar.pes.wslhd.MainActivity"));
				// startActivity(i); finish();

			}

		});
		
		

		// Link to Register Screen
		btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
				startActivity(i);
				finish();
			}
		});
		
		btnSendLog.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				sendLogFile ();
			}
		});

	}
	
	public void sendLogFile ()
	{
	  String fullName = extractLogToFile();
	  if (fullName == null)
	    return;

	  Intent intent = new Intent (Intent.ACTION_SEND);
	  intent.setType ("plain/text");
	  intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"ns.50254@gmail.com"});
	  intent.putExtra (Intent.EXTRA_SUBJECT, "MyApp log file");
	  intent.putExtra (Intent.EXTRA_STREAM, Uri.parse ("file://" + fullName));
	  intent.putExtra (Intent.EXTRA_TEXT, "Log file attached."); // do this so some email clients don't complain about empty body.
	  startActivity (intent);
	}
	
	private String extractLogToFile()
	{
	  PackageManager manager = this.getPackageManager();
	  PackageInfo info = null;
	  try {
	    info = manager.getPackageInfo (this.getPackageName(), 0);
	  } catch (NameNotFoundException e2) {
	  }
	  String model = Build.MODEL;
	  if (!model.startsWith(Build.MANUFACTURER))
	    model = Build.MANUFACTURER + " " + model;

	  // Make file name - file must be saved to external storage or it wont be readable by
	  // the email app.
	  String path = Environment.getExternalStorageDirectory() + "/" + "MyApp/";
	  String fullName = path + "log";
	  if(!new File(path).exists()){
		  new File(path).mkdir();
	  }
	  
	  // Extract to file.
	  File file = new File (fullName);
	  InputStreamReader reader = null;
	  FileWriter writer = null;
	  try
	  {
	    // For Android 4.0 and earlier, you will get all app's log output, so filter it to
	    // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
	    String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
	                  "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
	                  "logcat -d -v time";

	    // get input stream
	    Process process = Runtime.getRuntime().exec(cmd);
	    reader = new InputStreamReader (process.getInputStream());

	    // write output stream
	    writer = new FileWriter (file);
	    writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
	    writer.write ("Device: " + model + "\n");
	    writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

	    char[] buffer = new char[10000];
	    do 
	    {
	      int n = reader.read (buffer, 0, buffer.length);
	      if (n == -1)
	        break;
	      writer.write (buffer, 0, n);
	    } while (true);

	    reader.close();
	    writer.close();
	  }
	  catch (IOException e)
	  {
	    if (writer != null)
	      try {
	        writer.close();
	      } catch (IOException e1) {
	      }
	    if (reader != null)
	      try {
	        reader.close();
	      } catch (IOException e1) {
	      }

	    // You might want to write a failure message to the log here.
	    return null;
	  }

	  return fullName;
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		inputUserName.setText("");
		inputPassword.setText("");
		inputPIN.setText("");
		//mAPDU = "";
//		inputCardId.setVisibility(View.INVISIBLE);
//		inputPassword.setVisibility(View.INVISIBLE);
		
	}

	@Override
	public void onResume() {
		super.onResume();
		//mAPDU = "";
		//if(mAPDU.isEmpty()){
			try{
			mGetCardStatusTask = new GetCardStatusTask();
			mGetCardStatusTask.execute();
			}catch(Exception e){
				e.printStackTrace();
			}
		//}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		
		/* Cancel task if not already done by button click. */
		if (mGetCardStatusTask != null) {
			mGetCardStatusTask.cancel(true);
		}
		inputUserName.setText("");
		inputPassword.setText("");
		inputPIN.setText("");
		//mAPDU = "";
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mService != null) {
			mService.releaseService();
		}
		inputUserName.setText("");
		inputPassword.setText("");
		inputPIN.setText("");
		mAPDU = "";
//		inputCardId.setVisibility(View.INVISIBLE);
//		inputPassword.setVisibility(View.INVISIBLE);
		
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_APP_INSTALL) {
			mService = CardService.getInstance(this);
		}
	}
	
	private boolean alreadyInstalled(String packageName) {
		try {
			PackageManager pm = getPackageManager();
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}
	
	private boolean installManagementApp() {
		String cachePath = null;
		if (!alreadyInstalled(MANAGEMENT_PACKAGE)) {
			try {
				/*
				 * Copy the .apk file from the assets directory to the external
				 * cache, from where it can be installed.
				 */
				File temp = File.createTempFile("CardReaderManager", "apk", getExternalCacheDir());
				temp.setWritable(true);
				FileOutputStream out = new FileOutputStream(temp);
				InputStream in = getResources().getAssets().open(MANAGEMENT_APP);
				byte[] buffer = new byte[1024];
				int bytes = 0;

				while ((bytes = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytes);
				}
				in.close();
				out.close();
				cachePath = temp.getPath();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				return false;
			}

			/*
			 * Actual installation, calls external Activity that is shown to the
			 * user and returns with call to onActivityResult() to this
			 * Activity.
			 */

			Intent promptInstall = new Intent(Intent.ACTION_VIEW);
			promptInstall.setDataAndType(Uri.fromFile(new File(cachePath)), "application/vnd.android.package-archive");
			// startActivityForResult(promptInstall, REQUEST_APP_INSTALL);
			promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(promptInstall);
			return true;
		}
		return false;
	}
	
	private void showToast(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	
	
	private CardTerminal getFirstReader() {
		if (mFactory == null) {
			try {
				mFactory = mService.getTerminalFactory(); //entry point for JSR268 API methods
			} catch (Exception e) {
				Log.e(TAG, "unable to get terminal factory");
				//showToast("Error: unable to get terminal factory");
				return null;
			}
		}

		CardTerminal firstReader = null;
		try {
			/* Get the available card readers as list. */
			List<CardTerminal> readerList = mFactory.terminals().list();
			if (readerList.size() == 0) {
				return null;
			}
			/* Establish a connection with the first reader from the list. */
			firstReader = readerList.get(0);

		} catch (CardException e) {
			Log.e(TAG, e.toString());
			//showToast("Error: " + e.toString());
		}
		return firstReader;
	}
	
	private String byteArrayToString(byte[] array) {
		String hex = "";
		for (int i = 0; i < array.length-2; i++) { //strip last 2 byte
//			//hex += "0x" + Integer.toHexString(array[i] & 0x000000ff) + " ";			
			hex +=  Integer.toHexString(array[i] & 0x000000FF).toUpperCase();
//			//hex += Integer.toString(array[i]  & 0x000000ff );			
		}
		
		return hex;
	}
	
	private class GetCardStatusTask extends AsyncTask<Void, String, String> {
		String hexval = "";
		@Override
		public String doInBackground(Void... params) {
			
			/* Wait until we have the reader instance. */
			while (mReader == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mReader = getFirstReader();
			}

			/* This is done until the button is clicked, which cancels this
			 * AsyncTask. */
			for (; !isCancelled();) {
				try {
					if (mReader.isCardPresent()) {
						/* Connect to the reader. This returns a card object.
						 * "*" indicates that either protocol T=0 or T=1 can be
						 * used. */
						Card card = mReader.connect("*");
						//ATR atr = card.getATR();  //answer to reset bytes,ATR object can be obtained
						CardChannel channel = card.getBasicChannel();
                        //byte[] command = {(byte) 0xff,(byte) 0xca, 0, 0, 0 }; // command for getting card ID (UID)
						byte[] command = {(byte) 0xff,(byte) 0xb0, 0, 0, 0 };
						ResponseAPDU response = channel.transmit(new CommandAPDU(command));	
						hexval = byteArrayToString(response.getBytes()); //gets the string representation of response byte in Integer format with last 2 (status) byte removed

//						try {
//							apduString = new String(hexval.getBytes(), Charset.forName("UTF-8"));
//							publishProgress("Card APDU",
//									"response: " + apduString);
//						}catch(NumberFormatException e){
//							e.printStackTrace();
//						}
						card.disconnect(true);
//						publishProgress("Card present",
//								"ATR: " + byteArrayToString(atr.getBytes()) );
                        publishProgress("Card APDU",
                                "response: " + byteArrayToString(response.getBytes()));
						
					} else {
						publishProgress("No card present",
								"Waiting for card...");
						Log.i(TAG, "No card present ,waiting for card");
					}
					try {
						/* Don't overtax the USB/Bluetooth connection.*/
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				} catch (CardException e) {
					Log.e(TAG, e.toString());
					publishProgress("Error: " + e.toString(), "");
				}
			}
		    
			return null;
		}

		@Override
		public void onProgressUpdate(String... params) {
			//setStatusString(params[0]);
			setAPDUString(params[1]);
		}
		
		 protected void onPostExecute(String result) {
	         setAPDUString(result);
			 
	     }
		
//		@Override
//		public void onCancelled(Void unused) {
//			setStatusString("");
//			setAPDUString("");
//		}
	}
	
//	private void setStatusString(String string) {
//		mCardStatus.setText(string);
//	}
	
	private void setAPDUString(String string) {
		mAPDU = string;
	}
	
	private void checkCardEnroll(final String cardId) {

		String tag_string_req = "req_cardid_exists";

		pDialog.setMessage("Checking whether card ID exists in database ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_CHECK_CARD,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "Check card Response: " + response.toString());
						hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);

							JSONObject enrolment = jObj.getJSONObject("enrolment");
							cardStatus = enrolment.getString("status");
							Log.i("status:", ":" + cardStatus);

							if (cardStatus.equals("0")) {
								// Launch Register activity
								Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
								startActivity(intent);
								finish();
							}

						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
							Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG)
									.show();
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Card check Error: " + error.getMessage());
						Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
						hideDialog();
					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to card check url
				Map<String, String> params = new HashMap<String, String>();
				params.put("cardid", cardId);
				return params;
			}
		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

	}

	@SuppressLint({ "NewApi", "SetJavaScriptEnabled", "WorldReadableFiles" })

	private void checkLogin(final String cardId ,final String PIN) {
		// Tag used to cancel the request
		String tag_string_req = "req_login";

		pDialog.setMessage("Authenticating ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {

			@SuppressWarnings("deprecation")
			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Login Response: " + response.toString());
				hideDialog();

				try {
					JSONObject jObj = new JSONObject(response);
					JSONObject auth = jObj.getJSONObject("auth");
					String status = auth.getString("status");
					String sessionid = auth.getString("sessionid");
//                    String encryptPIN = auth.getString("encryptPIN");
//                    String salt = auth.getString("salt");

					// Check for error node in json
					//if (!encryptPIN.isEmpty() && !salt.isEmpty()) {
					if (status.equals("1")) {
						
//						Log.i("EncryptPIN :", ""+encryptPIN);
//						Log.i("salt :", ""+salt);
//						
//						String decryptPIN = null;
//						String strkey = "u7mzqw2";
//						decryptPIN = RegisterActivity.decrypt(strkey, encryptPIN ,salt);
//						
//		                Log.i("decryptPIN ", "After decryption :" + decryptPIN);
                        Toast.makeText(getApplicationContext(), "Login Success ", Toast.LENGTH_SHORT).show();
                       // Toast.makeText(getApplicationContext(), "Session ID "+sessionid, Toast.LENGTH_SHORT).show();
                        
//                        SharedPreferences prefs = getSharedPreferences("authpref",
//                                Context.MODE_WORLD_READABLE);
//                        SharedPreferences.Editor editor = prefs.edit();
//                        editor.putString("cardId", mAPDU);
//                        editor.putString("PIN", decryptPIN);
//                        editor.commit();
                        
                        SharedPreferences prefs = getSharedPreferences("authpref",
                              Context.MODE_WORLD_READABLE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("sessionid", sessionid);
                        editor.commit();
//                        try{
//                        Intent i = new Intent(Intent.ACTION_MAIN);
//                		i.setComponent(new ComponentName("org.stellar.pes.clinical_services","org.stellar.pes.clinical_services.ClinicalApplicationsActivity"));
//                		startActivity(i);
//                        }catch(ActivityNotFoundException e){
//                        	e.printStackTrace();
//                        }
                        
						try {
							Intent intent = getPackageManager().getLaunchIntentForPackage("org.mozilla.fennec_root");
							Intent serviceIntent = new Intent(getApplicationContext(),
									FloatingOverlayViewService.class);
							intent.setData(Uri.parse(CitrixUrl));
							startActivity(intent);
							startService(serviceIntent);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
//                        ContentValues values = new ContentValues();
//                		values.put(MyProvider.cardId,cardId );
//                		values.put(MyProvider.PIN,decryptPIN );
//                		Uri uri = getContentResolver().insert(MyProvider.CONTENT_URI, values);
//                		Toast.makeText(getBaseContext(), "Credentials inserted", Toast.LENGTH_LONG)
//                				.show();
                                       
					} else {
						// Error in login. Get the error message
						String errorMsg = jObj.getString("error_msg");
						Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
						Intent intent = new Intent(getApplicationContext(),
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
					}
				} catch (JSONException e) {
					// JSON error
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Login Error: " + error.getMessage());
				Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
				hideDialog();
			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag",auth_tag_cardId_pin);
				 params.put("cardid", cardId);
				//params.put("password", password);
				 params.put("PIN", PIN);
				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}
	
	private void checkLoginWithUsernamePassword(final String Username ,final String Password) {
		// Tag used to cancel the request
		String tag_string_req = "req_login_username_password";

		pDialog.setMessage("Authenticating ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {

			@SuppressWarnings("deprecation")
			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Login Response: " + response.toString());
				hideDialog();

				try {
//					JSONObject jObj = new JSONObject(response);
//					JSONObject auth = jObj.getJSONObject("auth");
//                    String encryptPIN = auth.getString("encryptPIN");
//                    String salt = auth.getString("salt");
                    
                    JSONObject jObj = new JSONObject(response);
					JSONObject auth = jObj.getJSONObject("auth");
					String status = auth.getString("status");
					String sessionid = auth.getString("sessionid");

					// Check for error node in json
					//if (!encryptPIN.isEmpty() && !salt.isEmpty()) {
						if (status.equals("1")) {
						// user successfully logged in
//						inputCardId.setVisibility(View.INVISIBLE);
//						inputPassword.setVisibility(View.INVISIBLE);
						
//						Log.i("EncryptPIN :", ""+encryptPIN);
//						Log.i("salt :", ""+salt);
//						
//						String decryptPIN = null;
//						String strkey = "u7mzqw2";
//						decryptPIN = RegisterActivity.decrypt(strkey, encryptPIN ,salt);
//						
//		                Log.i("decryptPIN ", "After decryption :" + decryptPIN);
						
						 SharedPreferences prefs = getSharedPreferences("authpref",
	                              Context.MODE_WORLD_READABLE);
	                     SharedPreferences.Editor editor = prefs.edit();
	                     editor.putString("sessionid", sessionid);
	                     editor.commit();
                        
		               // if(decryptPIN.equals(PIN)){
		                	Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
//		                	try{
//		                	Intent i = new Intent(Intent.ACTION_MAIN);
//		                    i.setComponent(new ComponentName("org.stellar.pes.clinical_services","org.stellar.pes.clinical_services.ClinicalApplicationsActivity"));
//		                	startActivity(i);
//		                	}catch(ActivityNotFoundException e){
//		                		e.printStackTrace();
//		                	}
		              //  }
		                	
						try {
							Intent intent = getPackageManager().getLaunchIntentForPackage("org.mozilla.fennec_root");
							Intent serviceIntent = new Intent(getApplicationContext(),
									FloatingOverlayViewService.class);
							intent.setData(Uri.parse(CitrixUrl));
							startActivity(intent);
							startService(serviceIntent);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
											
					} else {
						// Error in login. Get the error message
//						inputCardId.setVisibility(View.INVISIBLE);
//						inputPassword.setVisibility(View.INVISIBLE);
						String errorMsg = jObj.getString("error_msg");
						Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();						
						Intent intent = new Intent(getApplicationContext(),
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
					}
				} catch (JSONException e) {
					// JSON error
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				try{
				Log.e(TAG, "Login Error: " + error.getMessage());
				Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
				hideDialog();
				}catch(NullPointerException e){
					e.printStackTrace();
				}
			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting parameters to login url
				Map<String, String> params = new HashMap<String, String>();	
				params.put("tag", auth_tag_username_password);
				params.put("username", Username);
				params.put("password", Password);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
}

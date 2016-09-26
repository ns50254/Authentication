
package org.stellar.authentication.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.stellar.authentication.R;

import org.stellar.authentication.app.AppConfig;
import org.stellar.authentication.app.AppController;
import org.stellar.authentication.helper.SQLiteHandler;
import org.stellar.authentication.helper.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

@SuppressLint("NewApi")
public class RegisterActivity extends Activity {
	private static final String TAG = RegisterActivity.class.getSimpleName();
	private Button btnRegister;
	private Button btnLinkToLogin;
	private EditText inputPassword;

	private ProgressDialog pDialog;
	private SessionManager session;
//	private SQLiteHandler db;

	String appname1;
	String appname2;
	String appname3;

	String etUsername1;
	String etUsername2;
	String etUsername3;

	String etconfirmPassword1;
	String etconfirmPassword2;
	String etconfirmPassword3;

	String getSalt1;
	String getSalt2;
	String getSalt3;

	private static final String MANAGEMENT_PACKAGE = "com.theobroma.cardreadermanager";
	private static final String MANAGEMENT_APP = "CardReaderManager.apk";
	private static final int REQUEST_APP_INSTALL = 0xbeef;
	
	private ICardService mService = null;
	private TerminalFactory mFactory = null;
	private CardTerminal mReader = null;
	
	private AsyncTask<Void,String,String> mGetCardStatusTask = null;
	
	private String mAPDU = "";
	String apduString;

	protected static final String emptyUID = null;
	SharedPreferences pref;

	static String HEX_STRING = "0123456789ABCDEF";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		Toast.makeText(this,
				"Please hold the card near to the card reader for collecting card informaton for secure registration.",
				Toast.LENGTH_LONG).show();

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
		
		try {
			mGetCardStatusTask = new GetCardStatusTask();
			mGetCardStatusTask.execute();
			Toast.makeText(getApplicationContext(), "UID:" + mAPDU, Toast.LENGTH_LONG).show();

		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		inputPassword = (EditText) findViewById(R.id.password);

		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

		Button userApps = (Button) findViewById(R.id.multi_choice_btn);
		userApps.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(), ApplistActivity.class);
				startActivity(i);
			}
		});

		// Progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);

		// Session manager
		session = new SessionManager(getApplicationContext());

		// SQLite database handler
		//db = new SQLiteHandler(getApplicationContext());

		// Check if user is already logged in or not
		if (session.isLoggedIn()) {

			Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}

		// Register Button Click event
		btnRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				String PIN = inputPassword.getText().toString().trim();
				// String spinnerusertype =
				// spinner.getSelectedItem().toString();
				String cardid = mAPDU;
				String strkey = "u7mzqw2";

				String encryptPIN = null;
				String getSalt = generateSalt();
				if (PIN != null) {
					String newPIN = PIN.concat(getSalt);
					encryptPIN = encrypt(strkey, newPIN);

					Log.i("encryptPIN ", "After encryption :" + encryptPIN);

				}
				String decryptPIN = null;
				if (encryptPIN != null && getSalt != null) {
					decryptPIN = decrypt(strkey, encryptPIN, getSalt);

					Log.i("decryptPIN ", "After decryption :" + decryptPIN);
				}

				// Toast.makeText
				// (getApplicationContext(), "cardid : " + cardid,
				// Toast.LENGTH_SHORT).show();

				Log.i("Register: ", "" + encryptPIN + "" + mAPDU);

				if (!encryptPIN.isEmpty() && mAPDU != emptyUID
						&& ((!appname1.isEmpty()) || (!appname2.isEmpty()) || (!appname3.isEmpty()))) {

					registerUser(encryptPIN, appname1, etUsername1, etconfirmPassword1, getSalt1, appname2, etUsername2,
							etconfirmPassword2, getSalt2, appname3, etUsername3, etconfirmPassword3, getSalt3, cardid,
							getSalt);

				} else {
					Toast.makeText(getApplicationContext(), "Please enter your details!", Toast.LENGTH_LONG).show();
				}
			}
		});

		// Link to Login Screen
		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(i);
				finish();
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();

		pref = getApplication().getSharedPreferences("Options", MODE_PRIVATE);
		appname1 = pref.getString("app1", "");
		appname2 = pref.getString("app2", "");
		appname3 = pref.getString("app3", "");
		etUsername1 = pref.getString("etUsername1", "");
		etconfirmPassword1 = pref.getString("etconfirmPassword1", "");
		getSalt1 = pref.getString("getSalt1", "");
		etUsername2 = pref.getString("etUsername2", "");
		etconfirmPassword2 = pref.getString("etconfirmPassword2", "");
		getSalt2 = pref.getString("getSalt2", "");
		etUsername3 = pref.getString("etUsername3", "");
		etconfirmPassword3 = pref.getString("etconfirmPassword3", "");
		getSalt3 = pref.getString("getSalt3", "");

		// Toast.makeText (getApplicationContext(), "TAG_APP1 : " +appname1
		// +"TAG_APP2 : " +appname2 +"TAG_APP3 : " +appname3 +
		// "username1: " +etUsername1 + "password1: " +etconfirmPassword1 +
		// "salt1: " +getSalt1 +
		// "username2: " +etUsername2 + "password2: " +etconfirmPassword2 +
		// "salt2: " +getSalt2 +
		// "username3: " +etUsername3 + "password3: " +etconfirmPassword3 +
		// "salt3: " +getSalt3 , Toast.LENGTH_LONG).show();

	}
	
	@Override
	protected void onStop() {
		super.onStop();
		/* Cancel task if not already done by button click. */
		if (mGetCardStatusTask != null) {
			mGetCardStatusTask.cancel(true);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			mService.releaseService();
		}		
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
	

	private void setAPDUString(String string) {
		mAPDU = string;
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
	}
	
	
	@Override
	public void onBackPressed() {

		Intent in = new Intent(Intent.ACTION_MAIN);
		in.addCategory(Intent.CATEGORY_HOME);
		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(in);
	}


	/**
	 * Function to store user in MySQL database will post params to register url
	 */

	private void registerUser(final String encryptPIN, final String appname1, final String username1,
			final String password1, final String salt1, final String appname2, final String username2,
			final String password2, final String salt2, final String appname3, final String username3,
			final String password3, final String salt3, final String cardid, final String salt) {
		// Tag used to cancel the request
		String tag_string_req = "req_register";

		pDialog.setMessage("Registering ...");
		showDialog();

		StringRequest strReq = new StringRequest(Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				Log.d(TAG, "Register Response: " + response.toString());
				hideDialog();

				try {

					JSONObject jObj = new JSONObject(response);

					JSONObject enrollment = jObj.getJSONObject("registration");
					String status = enrollment.getString("status");
					Log.i("status:", ":" + status);

					Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!",
							Toast.LENGTH_LONG).show();

					if (status.equals("1")) {
						// Launch Register activity
						Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
						//Toast.makeText(getApplicationContext(), "UID inside register:" + mAPDU, Toast.LENGTH_LONG).show();
						startActivity(intent);
						finish();
					} else {
						Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
						startActivity(intent);
						finish();
					}
				} catch (JSONException e) {

					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "Registration Error: " + error.getMessage());
				Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
				hideDialog();
			}
		}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();

				params.put("encryptPIN", encryptPIN);
				params.put("appname1", appname1);
				params.put("username1", username1);
				params.put("password1", password1);
				params.put("salt1", salt1);
				params.put("appname2", appname2);
				params.put("username2", username2);
				params.put("password2", password2);
				params.put("salt2", salt2);
				params.put("appname3", appname3);
				params.put("username3", username3);
				params.put("password3", password3);
				params.put("salt3", salt3);
				params.put("cardid", cardid);
				params.put("salt", salt);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	public static String encrypt(String key, String plain) {
		// Security.insertProviderAt(new
		// org.bouncycastle.jce.provider.BouncyCastleProvider(), 3);
		byte[] plainText = plain.getBytes();
		String cipher = null;

		try {
			SecretKeySpec blowfishKey = new SecretKeySpec(key.getBytes(), "Blowfish");
			Cipher blowfishCipher = Cipher.getInstance("Blowfish");
			blowfishCipher.init(Cipher.ENCRYPT_MODE, (Key) blowfishKey);
			byte[] cipherText = blowfishCipher.doFinal(plainText);
			cipher = convertBinary2Hexadecimal(cipherText);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipher;
	}

	public static String decrypt(String key, String cipher, String salt) {
		// Security.insertProviderAt(new
		// org.bouncycastle.jce.provider.BouncyCastleProvider(), 3);
		String plain = null;
		String plain1 = null;
		byte[] cipherText = convertHexadecimal2Binary(cipher.getBytes());

		try {
			SecretKeySpec blowfishKey = new SecretKeySpec(key.getBytes(), "Blowfish");
			Cipher blowfishCipher = Cipher.getInstance("Blowfish");
			blowfishCipher.init(Cipher.DECRYPT_MODE, (Key) blowfishKey);
			byte[] plainText = blowfishCipher.doFinal(cipherText);
			plain = new String(plainText);
			plain1 = plain.replace(salt, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return plain1;

	}

	private static String convertBinary2Hexadecimal(byte[] binary) {
		StringBuffer buf = new StringBuffer();
		int block = 0;

		for (int i = 0; i < binary.length; i++) {
			block = binary[i] & 0xFF;
			buf.append(HEX_STRING.charAt(block >> 4));
			buf.append(HEX_STRING.charAt(binary[i] & 0x0F));
		}

		return buf.toString();
	}

	public static byte[] convertHexadecimal2Binary(byte[] hex) {
		int block = 0;
		byte[] data = new byte[hex.length / 2];
		int index = 0;
		boolean next = false;

		for (int i = 0; i < hex.length; i++) {
			block <<= 4;
			int pos = HEX_STRING.indexOf(Character.toUpperCase((char) hex[i]));
			if (pos > -1)
				block += pos;

			if (next) {
				data[index] = (byte) (block & 0xff);
				index++;
				next = false;
			} else
				next = true;
		}

		return data;
	}

	public String generateSalt() {
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[20];
		random.nextBytes(bytes);
		Log.i("Salt:", "" + bytes.toString());
		return bytes.toString();
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

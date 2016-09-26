package org.stellar.authentication.activity;

import java.security.Key;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.stellar.authentication.R;
import org.stellar.authentication.helper.ServiceHandler;

@SuppressLint("WorldReadableFiles")
public class ApplistActivity extends ListActivity {

	private ProgressDialog pDialog;

	// URL to get applist JSON
	private static String url = "http://188.165.1.55/wbh/getapplist.php";
	
	// JSON Node names
	private static final String TAG_APPS = "apps";
	private static final String TAG_APP1 = "app1";
	private static final String TAG_APP2 = "app2";
	private static final String TAG_APP3 = "app3";
	
	protected static final String TAG = "ApplistActivity";

	JSONObject appinfo;

	// Hashmap for ListView
	ArrayList<HashMap<String, String>> ApplicationList;
	final Context context = this;
	String appname1 = "";
	String appname2 = "";
	String appname3 = "";
	String item;

	static String HEX_STRING = "0123456789ABCDEF";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_applist);

		ApplicationList = new ArrayList<HashMap<String, String>>();

		ListView lv = getListView();

		// Listview on item click listener
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				// setTitle(parent.getItemAtPosition(position).toString());
				item = parent.getItemAtPosition(position).toString();

				// Toast.makeText(context, item, Toast.LENGTH_LONG).show();
				// getting values from selected ListItem
				// String appname1 = ((TextView)
				// view.findViewById(R.id.appname1))
				// .getText().toString();
				// String appname2 = ((TextView)
				// view.findViewById(R.id.appname2))
				// .getText().toString();
				// String appname3 = ((TextView)
				// view.findViewById(R.id.appname3))
				// .getText().toString();

				SharedPreferences pref = getApplicationContext().getSharedPreferences("Options", MODE_PRIVATE);
				Editor editor = pref.edit();

				editor.putString("app1", appname1);
				editor.putString("app2", appname2);
				editor.putString("app3", appname3);

				editor.apply();
				editor.commit();

				displayAlertDialog();

				Button buttonComplete = (Button) findViewById(R.id.ButtonA);
				buttonComplete.setOnClickListener(new Button.OnClickListener() {
					public void onClick(View v) {
						Intent in = new Intent(ApplistActivity.this, RegisterActivity.class);
						// in.putExtra(TAG_APP1, appname1);
						// in.putExtra(TAG_APP2, appname2);
						// in.putExtra(TAG_APP3, appname3);
						startActivity(in);

					}
				});

			}
		});
		
		// Calling async task to get json
		new GetAppInfo().execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
	}

	private class GetAppInfo extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new ProgressDialog(ApplistActivity.this);
			pDialog.setMessage("Please wait...");
			pDialog.setCancelable(false);
			pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			// Creating service handler class instance
			ServiceHandler sh = new ServiceHandler();

			String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

			Log.d("Response: ", ": " + jsonStr);

			if (jsonStr != null) {
				try {
					JSONObject jsonObj = new JSONObject(jsonStr);

					// Getting JSON object node
					appinfo = jsonObj.getJSONObject(TAG_APPS);
					HashMap<String, String> applists = new HashMap<String, String>();
					HashMap<String, String> applists1 = new HashMap<String, String>();
					HashMap<String, String> applists2 = new HashMap<String, String>();

					for (int i = 0; i < appinfo.length(); i++) {
						// JSONObject c = appinfo.optJSONObject(TAG_APPS);

						Log.d("appinfo lenght", ":" + appinfo.length());

						appname1 = appinfo.getString(TAG_APP1);
						appname2 = appinfo.getString(TAG_APP2);
						appname3 = appinfo.getString(TAG_APP3);

						// adding each child node to HashMap key => value
						applists.put(TAG_APP1, appname1);
						applists1.put(TAG_APP2, appname2);
						applists2.put(TAG_APP3, appname3);
						// applists.put(TAG_APPTYPE, apptype);
						// applists.put(TAG_USERNAME, username);
						// applists.put(TAG_POSTURL, status );
						// ApplicationList.add(applists);
					}
					ApplicationList.add(applists);
					ApplicationList.add(applists1);
					ApplicationList.add(applists2);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				Log.e("ServiceHandler", "Couldn't get any data from the url");
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Dismiss the progress dialog
			if (pDialog.isShowing())
				pDialog.dismiss();

			/**
			 * Updating parsed JSON data into ListView
			 */
			ListAdapter adapter = new SimpleAdapter(ApplistActivity.this, ApplicationList, R.layout.applist_item,
					new String[] { TAG_APP1, TAG_APP2, TAG_APP3 },
					new int[] { R.id.appname1, R.id.appname2, R.id.appname3 });
			setListAdapter(adapter);

		}
	}

	public void displayAlertDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View alertLayout = inflater.inflate(R.layout.layout_custom_dialog, null);
		final EditText etUsername = (EditText) alertLayout.findViewById(R.id.et_Username);
		final EditText etPassword = (EditText) alertLayout.findViewById(R.id.et_Password);
		final EditText etconfirmPassword = (EditText) alertLayout.findViewById(R.id.et_RTPassword);
		final CheckBox cbShowPassword = (CheckBox) alertLayout.findViewById(R.id.cb_ShowPassword);
		final TextView error = (TextView) alertLayout.findViewById(R.id.TextView_PwdProblem);

		cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					etPassword.setTransformationMethod(null);
				else
					etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
		});

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Enter User Credential below:");
		alert.setView(alertLayout);
		alert.setCancelable(false);

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Toast.makeText(getBaseContext(), "Cancel clicked",
				// Toast.LENGTH_SHORT).show();
			}
		});

		alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				String user = etUsername.getText().toString();
				String pass = etPassword.getText().toString();
				String strkey = "u7mzqw2";

				String encryptPass = null;
				String getSalt = generateSalt();
				if (pass != null) {
					String newPass = pass.concat(getSalt);
					encryptPass = encrypt(strkey, newPass);

					Log.i("encryptPassword: ", "" + encryptPass);

				}
				String decryptPass = null;
				if (encryptPass != null && getSalt != null) {
					decryptPass = decrypt(strkey, encryptPass, getSalt);

					Log.i("decryptPassword ", "" + decryptPass);
				}

				SharedPreferences pref = getApplicationContext().getSharedPreferences("Options", MODE_PRIVATE);
				Editor editor = pref.edit();

				if (item.startsWith("{app1=trackcare")) {
					editor.putString("etUsername1", user);
					editor.putString("etconfirmPassword1", encryptPass);
					editor.putString("getSalt1", getSalt);
					editor.apply();
					editor.commit();
				} else if (item.startsWith("{app2=citrix")) {
					editor.putString("etUsername2", user);
					editor.putString("etconfirmPassword2", encryptPass);
					editor.putString("getSalt2", getSalt);
					editor.apply();
					editor.commit();
				} else if (item.startsWith("{app3=mydicare")) {
					editor.putString("etUsername3", user);
					editor.putString("etconfirmPassword3", encryptPass);
					editor.putString("getSalt3", getSalt);
					editor.apply();
					editor.commit();
				}

				// Toast.makeText(getBaseContext(), "Username: " + user + "
				// Password: " + pass, Toast.LENGTH_SHORT).show();
			}
		});
		final AlertDialog dialog = alert.create();
		dialog.show();
		// Initially disable the button
		((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

		etconfirmPassword.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				String strPass1 = etPassword.getText().toString();
				String strPass2 = etconfirmPassword.getText().toString();
				if (strPass1.equals(strPass2)) {
					error.setText(R.string.settings_pwd_equal);
				} else {
					error.setText(R.string.settings_pwd_not_equal);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String strPassword1 = etPassword.getText().toString().trim();
				String strPassword2 = etconfirmPassword.getText().toString().trim();

				if (strPassword1.length() > 0 && strPassword1.equals(strPassword2)) {
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				} else
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			}
		});

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
		// bytes[0] = 0x63;
		return bytes.toString();
	}
}

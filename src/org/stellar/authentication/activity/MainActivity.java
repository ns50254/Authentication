package org.stellar.authentication.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.stellar.authentication.R;
import org.stellar.authentication.helper.SQLiteHandler;
import org.stellar.authentication.helper.SessionManager;

import java.util.HashMap;

public class MainActivity extends Activity {

	private TextView txtName;
	private TextView txtEmail;
	private Button btnLogout;

	private SQLiteHandler db;
	private SessionManager session;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txtName = (TextView) findViewById(R.id.name);
		//txtEmail = (TextView) findViewById(R.id.email);
		btnLogout = (Button) findViewById(R.id.btnLogout);

		// SqLite database handler
		db = new SQLiteHandler(getApplicationContext());

		// session manager
		session = new SessionManager(getApplicationContext());

		if (!session.isLoggedIn()) {
			//String url = "http://188.165.1.45/mgmtportal/public/user/login";
			//Intent intent = new Intent(Intent.ACTION_VIEW);
			//intent.setData(Uri.parse(url));
			//startActivity(intent);
			logoutUser();
		}

		// Fetching user details from SQLite
		//HashMap<String, String> user = db.getUserDetails();

		//String name = user.get("name");
		//String email = user.get("email");

		// Displaying the user details on the screen
		//txtName.setText(name);
		//txtEmail.setText(email);

		// Logout button click event
		btnLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				logoutUser();
			}
		});
	}


	/**
	 * Logging out the user. Will set isLoggedIn flag to false in shared
	 * preferences Clears the user data from sqlite users table
	 * */
	private void logoutUser() {
		session.setLogin(false);

		db.deleteUsers();

		// Launching the login activity
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
		//Intent i = new Intent(Intent.ACTION_MAIN);
		//i.setComponent(new ComponentName("org.stellar.pes.wslhd","org.stellar.pes.wslhd.MainActivity"));
		//startActivity(i);
		//finish();
	}
}

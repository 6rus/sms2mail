package fr.rezvani.sms2mail;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	
	Button mStartButton = null;
	Context mContext = this;
	EditText mEditTextMail = null;
	EditText mEditTextPass = null;
	private ProgressDialog mProgressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /**  handle url */
        TextView tv = ( TextView ) findViewById( R.id.link );  
        tv.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(getResources().getString(R.string.link_github_href)));
				startActivity(browserIntent);

				
			}
		});
        
        mStartButton = (Button) findViewById(R.id.start_button);        
        mEditTextMail = (EditText) findViewById(R.id.user_mail);
        mEditTextPass = (EditText) findViewById(R.id.user_pass);
        
        
        SharedPreferences settings = getSharedPreferences(
				Utils.APP_NAME, Context.MODE_PRIVATE);
        Boolean serviceStarted = settings.getBoolean(
				Utils.PREFS_SERVICE_STATUS, false);
        
        if(serviceStarted) {
			mStartButton.setText(getResources().getString(R.string.stop));
			
		} else {
			mStartButton.setText(getResources().getString(R.string.start));
			
		}
        
        String password = settings.getString(Utils.PREFS_USER_PWD, "");
        mEditTextPass.setText(password);
        
        
       String mail = settings.getString(Utils.PREFS_USER_MAIL, null);
       if(mail==null){
    	   mail = Utils.getEmail(mContext);
       }
        if(mail!=null){
        	mEditTextMail.setText(mail);
        }
        
        
       
        
        
        mStartButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				 mProgressDialog = ProgressDialog.show(MainActivity.this,
			                "", "Please wait...", true);   
			       
				 
				new Thread() {
					@Override
					public void run() {
						String mail = mEditTextMail.getText().toString();
						String password = mEditTextPass.getText().toString();

						// TODO regex mail
						if (!mail.contains("@")) {
							Toast.makeText(mContext,
									Utils.APP_NAME + " :Invalid mail ",
									Toast.LENGTH_LONG).show();

						} else if ("".equals(password)) {
							Toast.makeText(mContext,
									Utils.APP_NAME + " :Empty password",
									Toast.LENGTH_LONG).show();
						} else {

							SharedPreferences settings = getSharedPreferences(
									Utils.APP_NAME, Context.MODE_PRIVATE);
							Boolean serviceStarted = settings.getBoolean(
									Utils.PREFS_SERVICE_STATUS, false);
							Editor editor = settings.edit();
							editor.putBoolean(Utils.PREFS_SERVICE_STATUS,
									!serviceStarted);

							editor.putString(Utils.PREFS_USER_MAIL, mail);
							editor.putString(Utils.PREFS_USER_PWD, password);

							editor.commit();
							if (!serviceStarted) {
								Utils.activationMail(mContext);
							} else {
								Utils.desactivationMail(mContext);
							}

						}
						 mProgressDialog.dismiss();
					}
				}.start();
				
				
				SharedPreferences settings = getSharedPreferences(
						Utils.APP_NAME, Context.MODE_PRIVATE);
				Boolean serviceStarted = settings.getBoolean(
						Utils.PREFS_SERVICE_STATUS, false);
				if (!serviceStarted) {
					mStartButton.setText(getResources().getString(
							R.string.stop));
				} else {
					mStartButton.setText(getResources().getString(
							R.string.start));
				}

				
			}
       });
        
    }
}
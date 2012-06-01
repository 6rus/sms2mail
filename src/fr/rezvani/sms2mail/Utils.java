package fr.rezvani.sms2mail;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.mail.AuthenticationFailedException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

public class Utils {

	static String APP_NAME = "sms2mail";
	static String PREFS_SERVICE_STATUS = "0";
	static String PREFS_USER_MAIL = "1";
	static String PREFS_USER_PWD = "2";

	public static String getContactDisplayNameByNumber(Context ctx,
			String number) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String name = "?";

		ContentResolver contentResolver = ctx.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {
				BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
				null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return name;
	}
	public static void activationMail(Context context){
		sendMail(context, "activation", "Service sms2mail is active, to stop send sms 'sms2mail off'");
		
	}
	
	public static void desactivationMail(Context context){
		sendMail(context, "service stopped", "Service sms2mail is inactive, to start send sms 'sms2mail on'");
		
	}
	

	public static void sendMail(Context ctx, String object, String body) {

		SharedPreferences settings = ctx.getSharedPreferences(Utils.APP_NAME,
				Context.MODE_PRIVATE);
		String mail = settings.getString(Utils.PREFS_USER_MAIL, "?");
		if("?".equals(mail)){
			mail = getEmail(ctx);
		}
		String subject = "["+APP_NAME+"] " + object;
		String password = settings.getString(Utils.PREFS_USER_PWD, "");
		
		try {
			GMailSender sender = new GMailSender(mail, password);
			sender.sendMail(subject, body, mail, mail);
		} catch (Exception e) {
			if( e instanceof AuthenticationFailedException){
				Toast.makeText(ctx, APP_NAME + " : Authentication Failed : mail = " + mail + " & password = " +password ,Toast.LENGTH_LONG).show();
				Log.e(APP_NAME, " : Authentication Failed : mail ='" + mail + "' & password = '" +password+"'" );
			} else {
				Log.e("APP_NAME", e.getMessage(), e);
			}
			
			
		}

	}

	static String getEmail(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account account = getAccount(accountManager);

		if (account == null) {
			return null;
		} else {
			return account.name;
		}
	}

	private static Account getAccount(AccountManager accountManager) {
		Account[] accounts = accountManager.getAccountsByType("com.google");
		Account account;
		if (accounts.length > 0) {
			account = accounts[0];
		} else {
			account = null;
		}
		return account;

	}
	
	public static void sendUnreadMessages(Context context){
		ContentResolver  cr = context.getContentResolver();
		Uri uri = Uri.parse("content://sms/inbox");
		Cursor c = cr.query(uri, null, "read = 0", null, "date ASC");
		
		final Calendar cal = Calendar.getInstance();
	     
	    DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
	    		DateFormat.SHORT,
	    		DateFormat.SHORT);
		
		while (c.moveToNext()) {
		
			
			
			String phoneNumber = c.getString(c.getColumnIndex("address"));
			String date_string = c.getString(c.getColumnIndex("date"));
			//String subject = c.getString(c.getColumnIndex("subject"));
			String body  = c.getString(c.getColumnIndex("body"));
		  
			String contact = Utils.getContactDisplayNameByNumber(
					context, phoneNumber);
			String from =  '"'+contact.trim() +'"'+" <" + phoneNumber + ">" ;
		
		   cal.setTimeInMillis(new Long(date_string));
		  
           if(!body.contains("sms2mail")){
        	   String object = shortDateFormat.format(cal.getTime()) +" "+from;
        	   
        	   Log.v(APP_NAME, phoneNumber + "  " + shortDateFormat.format(cal.getTime()) + " "+body);
        	   sendMail(context, object, body);
           }
             
						
			
		}
		
		
		
	}
	

}

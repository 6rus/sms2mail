package fr.rezvani.sms2mail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

	private final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION_RECEIVE_SMS)) {

			SharedPreferences settings = context.getSharedPreferences(
					Utils.APP_NAME, Context.MODE_PRIVATE);
			Boolean serviceStarted = settings.getBoolean(
					Utils.PREFS_SERVICE_STATUS, false);

			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");

				final SmsMessage[] messages = new SmsMessage[pdus.length];
				
				String messageBody = "";
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					messageBody+=messages[i].getDisplayMessageBody();
				}
				if (messages.length > -1) {
					//final String messageBody = messages[0].getMessageBody();
					
					
					
						String[] words= messageBody.split("\\s+");
						if(words[0]!=null && "sms2mail".equals(words[0])){
							Editor editor = settings.edit();
							if(words[1]!=null && "on".equals(words[1])){
								editor.putBoolean(Utils.PREFS_SERVICE_STATUS,true);
								Utils.activationMail(context);
								Utils.sendUnreadMessages(context);
								
							} else if (words[1]!=null && "off".equals(words[1])){
								editor.putBoolean(Utils.PREFS_SERVICE_STATUS,false);
								Utils.desactivationMail(context);
							} else if (words[1]!=null && "config".equals(words[1])){
								if (words[2]!=null){
									editor.putString(Utils.PREFS_USER_PWD, words[2]);
									editor.putBoolean(Utils.PREFS_SERVICE_STATUS,true);
									Toast.makeText(context, Utils.APP_NAME + " :Changing Password, service starting  " +words[2].trim(),
											Toast.LENGTH_LONG).show();
									editor.commit();
									
								}
								
							}
							editor.commit();
							return;
						}
					
							
						
					
					
					if (serviceStarted) {
						final String phoneNumber = messages[0]
								.getDisplayOriginatingAddress();

						Uri uri = Uri.withAppendedPath(
								PhoneLookup.CONTENT_FILTER_URI,
								Uri.encode(phoneNumber));

						String contact = Utils.getContactDisplayNameByNumber(
								context, phoneNumber);
						String from =  '"'+contact.trim() +'"'+" <" + phoneNumber + ">" ;

//						Toast.makeText(context, Utils.APP_NAME + " :Message : " + messageBody,
//								Toast.LENGTH_LONG).show();

						Utils.sendMail(context, from, messageBody);

					} 
						
				}

			}		
			
		}
	}

}
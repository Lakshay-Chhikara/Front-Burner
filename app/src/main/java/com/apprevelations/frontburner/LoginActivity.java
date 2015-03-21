/*
 * Copyright 2014 IBM Corp. All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apprevelations.frontburner;


import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Properties;

public class LoginActivity extends Activity implements OnClickListener{

	/* Request code sent to the "AccountPicker" Activity. Returned when it finishes. */
	private static final int ACCOUNT_PICKER_REQUEST_CODE = 17;

	/* Request code sent to the "UserRecoverableAuthorizationException" Activity. Returned when it finishes. */
	private static final int AUTH_REQUEST_CODE = 18;

	/*
	 * This is the key for the "Android Application Client ID" for a given Android Client, whose value is found 
	 * in the Google Developers Console.
	 */
	private static final String GOOGLE_ANDROID_APP_CLIENT_ID_KEY = "androidAppClientID";
	
	/*
	 * This is the key for the "Web Application Client ID" field for a given Web Application Client, whose value 
	 * is found in the Google Developers Console.
	 */
	private static final String GOOGLE_WEB_APP_CLIENT_ID_KEY = "webAppClientID";

	private static final String PROPS_FILE = "bluelist.properties";
	private static final String CLASS_NAME = LoginActivity.class.getSimpleName();

	private com.google.android.gms.common.SignInButton mGetGoogleTokenButton;
	private String googleAccessToken;
	private TextView mStatus;
	private TextView statusWindow;

	private String selectedAccountName = null;
	private String googleEmail = null;
	private String googleFirstName = null;
	private String googleLastName = null;
	private String googlePicture = null;
	private String statusMessage = "";

	private Activity thisActivity = this;

	/*
	 * This is the "Android Application Client ID" field for a given Android Client, whose value is found 
	 * in the Google Developers Console.
	 */
	private String androidAppClientIdValue = null;
	
	/*
	 * This is the "Web Application Client ID" field for a given Web Application Client, whose value is found
	 * in the Google Developers Console.
	 */
	private String webAppClientIdValue = null;

	/* Assume the id token is valid until proven otherwise during token validation. */
	private boolean googleTokenFailedVerification = false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	    mGetGoogleTokenButton = (com.google.android.gms.common.SignInButton) findViewById(R.id.get_google_token_button);
	    mGetGoogleTokenButton.setOnClickListener(this);

	    mStatus = (TextView) findViewById(R.id.connection_msg);
	    statusWindow = (TextView) findViewById(R.id.status_msg);
	    
		// Read from properties file.
		Properties props = new Properties();
		Context context = getApplicationContext();
		try {
			AssetManager assetManager = context.getAssets();					
			props.load(assetManager.open(PROPS_FILE));
			Log.i(CLASS_NAME, "Found configuration file: " + PROPS_FILE);
			androidAppClientIdValue = props.getProperty(GOOGLE_ANDROID_APP_CLIENT_ID_KEY);
			webAppClientIdValue = props.getProperty(GOOGLE_WEB_APP_CLIENT_ID_KEY);
		} catch (FileNotFoundException e) {
			Log.e(CLASS_NAME, "The bluelist.properties file was not found.", e);
		} catch (IOException e) {
			Log.e(CLASS_NAME, "The bluelist.properties file could not be read properly.", e);
		}
		Log.i(CLASS_NAME, "Android Application Client ID is: " + androidAppClientIdValue);
		Log.i(CLASS_NAME, "Web Application Client ID is: " + webAppClientIdValue);
	}

	protected void onActivityResult(final int requestCode, final int resultCode,
	         final Intent data) {
	     
		mStatus.setText("Loading User Details...");
		if (requestCode == ACCOUNT_PICKER_REQUEST_CODE && resultCode == RESULT_OK) 
		{
	    	 selectedAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
	    	 
	    	 /* Start the getToken task to get the ID token. */
	         new GetTokenTask().execute();
	    } else {
		     if (requestCode == AUTH_REQUEST_CODE && resultCode == RESULT_OK) {	
		    	 new GetTokenTask().execute();
		     }
	    }
		
	}
	
	protected void onStart() {
		super.onStart();
	}

	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
			case R.id.get_google_token_button:
				statusMessage = "";
				getIdToken();
				break;
			default:
				break;
		}
	}
	
	/**
	 * Start the process for acquiring an ID token. The process starts by picking the account to user. 
	 */
	void getIdToken() {
	    Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
	            false, null, null, null, null);
	    
		/*
	     * Start the choose account intent. The result is received in onActivityResult().
	     * Note that if there's exactly one Google account registered, onActivityResult is called immediately
	     * without launching the choose account dialogue.
		 */
	    startActivityForResult(intent, ACCOUNT_PICKER_REQUEST_CODE);
	}

	
	/**
	 * An async task for getting the ID token. It is needed because GoogleAuthUtil.getToken cannot be
	 * called from the main thread.
	 *
	 */
	public class GetTokenTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			
			/*
			 * If the user has not selected an account then 
			 * do nothing.
			 */
			if( selectedAccountName == null )
				return null;
			
			String idToken = "Failed";
			try {
				// USE THIS SCOPE TO GET THE GOOGLE ID_TOKEN.
				String clientIdScope = "audience:server:client_id:" + webAppClientIdValue;
				// USE THIS SCOPE TO GET THE GOOGLE ACCESS_TOKEN.
				String oAuthScopes = "oauth2:";
				oAuthScopes += " https://www.googleapis.com/auth/userinfo.profile"; 
				oAuthScopes += " https://www.googleapis.com/auth/userinfo.email"; 
				oAuthScopes += " https://www.googleapis.com/auth/plus.login";
                statusMessage += selectedAccountName +" now signed in.\n";
                statusMessage += "Loading Blue List...\n";
                idToken = GoogleAuthUtil.getToken(getApplicationContext(), selectedAccountName, clientIdScope);		
                System.out.println("Google ID Token: "+idToken);
                googleAccessToken = GoogleAuthUtil.getToken(getApplicationContext(), selectedAccountName, oAuthScopes);				
                System.out.println("Google Access Token: "+googleAccessToken);
                
			} catch (UserRecoverableAuthException userAuthEx) {			
			    /*
				 * Launch the intent to open the UI dialog for resolving the error (e.g. enter correct password).
				 * The result (success / failure in case the user hit the Cancel button) is returned in onActivityResult().
				 */
				startActivityForResult(
		                  userAuthEx.getIntent(),
		                  AUTH_REQUEST_CODE);

			} catch (IOException e) {
				statusMessage += "IOException "+e.getMessage() +"\n";
				e.printStackTrace();
			} catch (GoogleAuthException e) {
				Log.e(CLASS_NAME, "A GoogleAuthException occurred. Please check your account credentials. "
						+ "Ensure your device's debug keystore SHA-1 fingerprint is configured for your 'Client ID for Android application' ");
				statusMessage += "GoogleAuthException "+e.getStackTrace()[0] +"\n";
				e.printStackTrace();
			}
			
			/*
			 * Get some useful information about the currently selected user.
			 * We will populate the Blue List form with these details later.
			 */
			getAccountDetails(googleAccessToken);
			
			if(!validateToken(idToken,true))
            {
            	return null;
            }

			
			return idToken;
		}
		
		private boolean validateToken(String token, boolean production)
		{
			String details = null;
			
			if(token == null)
				return false;
			
			if(production)
				details = validateTokenForProduction(token);
			else
				details = validateTokenForTesting(token);
				
			if(details == null)
				return false;
			
			return true;
		}

		/*
		 * This code is provided as an example of to verify client side.  
		 * Note that Performing token verification client side is not as 
		 * secure as performing it server side. 
		 */
		private String validateTokenForProduction(String token)
		{
			
			String details = null;
			
		    JsonFactory factory = new JacksonFactory();
		    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier(
		    /* Http Transport is needed to fetch Google's latest public key. */
		        new ApacheHttpTransport(), factory);
		    GoogleIdToken idToken;
			try {
				idToken = GoogleIdToken.parse(factory, token);
			    if (idToken == null) {
			      System.out.println("Token cannot be parsed");
			      googleTokenFailedVerification = true;
			      return null;
			    }
	
			    details = idToken.getPayload().toPrettyString();
			    System.out.println("ID Token details:");
			    System.out.println(idToken.getPayload().toPrettyString());
			    
			    // ***************************************************************************
			    // IMPORTANT: Security Issue - Make sure the email_verified flag is TRUE
			    //            Otherwise, FAIL validation (googleTokenFailedVerification=true)
			    // ***************************************************************************
			    if (idToken.getPayload().getEmailVerified() != null && 
			    	idToken.getPayload().getEmailVerified()) {
			    	System.out.println("email_verified is TRUE");   /* SAFE */
			    } else {
			    	System.out.println("Invalid token - email_verified is FALSE");
				    googleTokenFailedVerification = true;
				    return null;
			    }
			    
			    /* Verify valid token, signed by google.com, intended for a third party. */
			    if (!verifier.verify(idToken)
				    || !idToken.verifyAudience(Collections.singletonList(webAppClientIdValue))
			        || !idToken.getPayload().getAuthorizedParty().equals(androidAppClientIdValue)) {
			      System.out.println("Invalid token");
			      googleTokenFailedVerification = true;
			      return null;
			    }
			    
			    /* Token originates from Google and is targeted to a specific client. */
			    System.out.println("The token is valid");	
			    
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		    
		    
			return details;
		}
		
		/*
		 * This form of validation is for development purposes only.
		 * We are simply sending this token to google and asking for
		 * user details.
		 */
		private String validateTokenForTesting(String token)
		{
			String details = token;
			
	        try {
	        	
	        	/* Use this url to get details from  id_token. */
	            URL url = new URL("https://www.googleapis.com/oauth2/v1/tokeninfo?alt=json&id_token="+token);

	            
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpGet pageGet = new HttpGet(url.toURI());
	            org.apache.http.HttpResponse hResponse = httpClient.execute(pageGet);
	            
	            InputStreamReader isr = new InputStreamReader(
	            		hResponse.getEntity().getContent());
	            /* Read in the data from input stream, this can be done a variety of ways. */
	            BufferedReader reader = new BufferedReader(isr);
	            StringBuilder sb = new StringBuilder();
	            String line = "";
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }

	            // Get the string version of the response data.
	            details = sb.toString();
			    System.out.println("ID Token details:");
			    System.out.println(details);
	            
	        } catch (IOException e) {
	        	statusMessage += "HTTP GET: "+ e.toString();
	        } catch (URISyntaxException e) {
	        	statusMessage += e.toString();
			}
			
			
			return details;
		}

		/*
		 * Use the access token to get user details displayed in the 
		 * BlueList Application MainActivity.
		 */
		private String getAccountDetails(String accessToken)
		{
			String details = accessToken;
			
	        try {
	        	
	            URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+accessToken);
	            
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpGet pageGet = new HttpGet(url.toURI());
	            org.apache.http.HttpResponse hResponse = httpClient.execute(pageGet);
	            
	            InputStreamReader isr = new InputStreamReader(
	            		hResponse.getEntity().getContent());

	            /* Read in the data from input stream, this can be done a variety of ways. */
	            BufferedReader reader = new BufferedReader(isr);
	            StringBuilder sb = new StringBuilder();
	            String line = "";
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }

	            // Get the string version of the response data.
	            details = sb.toString();
			    System.out.println("Access Token details:");
			    System.out.println(details);
	            
				/*
	             * Details about user comes back in the following form:
	             * Details: {
	             * "id": "112835395680253021869",
	             * "email": "myibmaccount@gmail.com",
	             * "verified_email": true,
	             * "name": "Morgan Johnson",
	             * "given_name": "Morgan",
	             * "family_name": "Johnson",
	             * "link": "https://plus.google.com/112835395680253021869",
	             * "picture": "https://lh4.googleusercontent.com/-sJf3tjZ8cu0/AAAAAAAAAAI/AAAAAAAAANs/zlKMXyB6d80/photo.jpg",
	             * "gender": "male",
	             * "locale": "en"
	             * }
	             * Here we pull out some of those details.
	             */
	            if(details != null)
	            {
	     	       	JSONObject jO = new JSONObject(details);
	            	googleFirstName = (String)jO.get("given_name");
	            	googleLastName = (String)jO.get("family_name");
	            	googleEmail = (String)jO.get("email");
	            	googlePicture = (String)jO.get("picture");            	
	            }

	            
	        } catch (IOException e) {
	        	statusMessage += "HTTP GET: "+ e.toString();
	        } catch (URISyntaxException e) {
	        	statusMessage += e.toString();
			} catch (JSONException e) {
				statusMessage += e.toString();
			}
			
			
			return details;
		}
		
		

		@Override
		protected void onPostExecute(String token) {
			startMainActivity(null, token, googleAccessToken);
		}					
	} // GetToken class.
	
	void startMainActivity(String wlAccessToken, String googleIdToken, String googleAccessToken) 
	{

		statusWindow.setText(statusMessage);
		
		if( googleIdToken != null && !googleIdToken.isEmpty() )
		{
		    /*
			 * The token has been found and validated so lets pass user onto the 
			 * Bluelist main activity.
			 *
			 * If user has not been signed in then don't go onto MainActivity.
			 */
			if(	googleEmail == null || googleEmail.isEmpty() )
			{
				mStatus.setText("Error: User Not Signed in");
				return;
			}
						
			final Context context = thisActivity;
		    Intent intent = new Intent(context, MainNavigationActivity.class);
		    intent.putExtra("GOOGLE_ID_TOKEN",googleIdToken);
		    intent.putExtra("GOOGLE_OAUTH_TOKEN",googleAccessToken);
		    intent.putExtra("GOOGLE_NAME", googleFirstName+" "+googleLastName);
		    intent.putExtra("GOOGLE_EMAIL", googleEmail);
		    intent.putExtra("GOOGLE_PICTURE", googlePicture);
			statusMessage = "";
			mStatus.setText("");
			System.out.println("Opening Main Activity and passing Google ID Token: "+googleIdToken);
            startActivity(intent); 
            
			selectedAccountName = null;
            finish();
		}
		else if(googleTokenFailedVerification)
			mStatus.setText("Error: ID Token failed verification.");
		else	
			mStatus.setText("Error: ID Token was not found.");
		
	}
}

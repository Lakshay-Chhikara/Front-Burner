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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import bolts.Continuation;
import bolts.Task;

import com.ibm.mobile.services.cloudcode.IBMCloudCode;
import com.ibm.mobile.services.core.IBMBluemix;
import com.ibm.mobile.services.core.IBMBluemix.IBMSecurityProvider;
import com.ibm.mobile.services.core.IBMCurrentUser;
import com.ibm.mobile.services.core.http.IBMHttpResponse;
import com.ibm.mobile.services.data.IBMData;
import com.ibm.mobile.services.data.IBMDataException;
import com.ibm.mobile.services.data.IBMDataObject;
import com.ibm.mobile.services.data.IBMQuery;
import com.ibm.mobile.services.push.IBMPush;

public class MainActivity extends Activity {

	public List<Item> itemList;
	public BlueListApplication blApplication = null;
	public ArrayAdapter<Item> lvArrayAdapter = null;
	public ListView itemsLV = null;
	public ActionMode mActionMode = null;
	int listItemPosition = 0;
	public static final String CLASS_NAME = "MainActivity";
	public final Activity mainActivity = this;
	public IBMPush push;
	public IBMCloudCode myCloudCodeService;
	public String deviceAlias = "TargetDevice";
	public String consumerID = "MBaaSListApp";
	private String uUserID = null;
	
	@Override
	/**
	 * onCreate called when main activity is created.
	 * 
	 * Sets up the itemList, application, and sets listeners.
	 *
	 * @param savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		Bundle extras = getIntent().getExtras();
		
		// start loading a blank main activity before authentication with IBM Bluemix
		super.onCreate(savedInstanceState);							
		mainActivity.setContentView(R.layout.activity_main);
		
		/* Use application class to maintain global state. */
		blApplication = (BlueListApplication) mainActivity.getApplication();
		itemList = blApplication.getItemList();
		
		/* Set up the array adapter for items list view. */
		itemsLV = (ListView) mainActivity.findViewById(R.id.itemsList);
		
		/* Set up the array adapter for items list view. */
		lvArrayAdapter = new ArrayAdapter<Item>(mainActivity, R.layout.list_item_1, itemList);
		itemsLV.setAdapter(lvArrayAdapter);
		
		/* Set long click listener. */
		itemsLV.setOnItemLongClickListener(new OnItemLongClickListener() {
		    /* Called when the user long clicks on the textview in the list. */
		    public boolean onItemLongClick(AdapterView<?> adapter, View view, int position,
	                long rowId) {
		    	listItemPosition = position;
				if (mActionMode != null) {
		            return false;
		        }

		        /* Start the contextual action bar using the ActionMode.Callback. */
		        mActionMode = MainActivity.this.startActionMode(mActionModeCallback);
		        return true;
		    }
		});

		/* Set up edit action listener in the edit view widget */
		EditText itemToAdd = (EditText) mainActivity.findViewById(R.id.itemToAdd);
		/* Set key listener for edittext (done key to accept item to list). */
		itemToAdd.setOnEditorActionListener(new OnEditorActionListener() {
			/* Called when user "long" clicks an item already added to the list. One can then edit or delete the entry. */
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE){
                    createItem(v);
                    return true;
                }
                return false;
            }
        });		
		
		if (extras != null) {
			UserAccount.setIdToken(extras.getString("GOOGLE_ID_TOKEN"));
			UserAccount.setAccessToken(extras.getString("GOOGLE_OAUTH_TOKEN"));
			UserAccount.setName(extras.getString("GOOGLE_NAME"));
			UserAccount.setEmail(extras.getString("GOOGLE_EMAIL"));
			UserAccount.setPicture(extras.getString("GOOGLE_PICTURE"));

			/* Set ID TOKEN so that all subsequent Service calls will contain the ID TOKEN in the header. */
	    	Log.d(CLASS_NAME, "Setting the GOOGLE ID TOKEN for all future IBM Bluemix Mobile Cloud Service calls");
	    	
		    /* ID is overridden by applicationID set in assets/configuration.json. */
			IBMBluemix.setSecurityToken(IBMSecurityProvider.GOOGLE, UserAccount.getAccessToken()).continueWithTask(
					new Continuation<IBMCurrentUser, Task<String>>() {
                        @Override
                        public Task<String> then(Task<IBMCurrentUser> user) throws Exception {

                            if (user.isFaulted()) {
                                Log.e(CLASS_NAME, "There was an error setting security token. Stack trace: ");
                                user.getError().printStackTrace();
                                return null;
                            }

                            Log.i(CLASS_NAME, "Set security token successfully. Initializing services.");
                            Log.i(CLASS_NAME, "The successfully returned IBMCurrentUser: " + user.getResult().getUuid());

                            IBMCurrentUser cUser = user.getResult();
                            uUserID = cUser.getUuid();

                            // Initialize the IBM Data Service
                            IBMData.initializeService();
                            // Register Item Specialization
                            Item.registerSpecialization(Item.class);
                            // Initialize the IBM Cloud Code Service
                            IBMCloudCode.initializeService();
                            // Instantiate the cloud code service instance
                            myCloudCodeService = IBMCloudCode.getService();
                            // Initialize IBM Push service
                            IBMPush.initializeService();
                            // Retrieve instance of the IBM Push service
                            push = IBMPush.getService();
                            /* Refresh the list. */
                            listItems();
                            // Register the device with the IBM Push service.
                            return push.register(deviceAlias, consumerID);
                        }
                    }).continueWith(new Continuation<String, Void>() {
                                @Override
                                public Void then(Task<String> task) throws Exception {
                                    if (task.isCancelled()) {
                                        Log.e(CLASS_NAME, "Exception : Task " + task.toString() + " was cancelled.");
                                    } else if (task.isFaulted()) {
                                        Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                                        Log.e(CLASS_NAME, "There was an error initializing Push service. Stack trace: ");
                                        task.getError().printStackTrace();
                                    } else {
                                        Log.i(CLASS_NAME, "Device Successfully Registered with the Push service with deivceID: "
                                                + task.getResult());
                                    }
                                    return null;
                                }
                    });
		}
	}
	/**
	 * Removes text on click of x button.
	 *
	 * @param  v the edittext view.
	 */
	public void clearText(View v) {
		EditText itemToAdd = (EditText) findViewById(R.id.itemToAdd);
		itemToAdd.setText("");
	}
	
	/**
	 * Refreshes itemList from data service.
	 * 
	 * An IBMQuery is used to find all the list items.
	 */
	public void listItems() {
		try {
			IBMQuery<Item> query = IBMQuery.queryForClass(Item.class);
			/**
			 * IBMQueryResult is used to receive array of objects from server.
			 * 
			 * onResult is called when it successfully retrieves the objects associated with the 
			 * query, and will reorder these items based on creation time.
			 * 
			 * onError is called when an error occurs during the query.
			 */
			Log.i(CLASS_NAME, "Retreiving objects from MobileData ");
			
			query.find().continueWith(new Continuation<List<Item>, Void>() {

                @Override
                public Void then(Task<List<Item>> task) throws Exception {
                    // Log error message if the save was cancelled.
                    if (task.isCancelled()) {
                        Log.e(CLASS_NAME, "Exception getting ItemList from MobileData: Task " + task.toString() + " was cancelled.");
                    }

                    // Log error message, if the save task fails.
                    else if (task.isFaulted()) {
                        Log.e(CLASS_NAME, "Exception getting ItemList from MobileData: " + task.getError().getMessage());
                        task.getError().printStackTrace();
                    }

                    // If the result succeeds, load the list.
                    else {
                        Log.i(CLASS_NAME, "Successfully retrieved ItemList from MobileData. Getting objects.... ");

                        final List<Item> objects = task.getResult();

                        Log.i(CLASS_NAME, "Successfully retrieved ItemList from MobileData: " + objects);

                        // Clear local itemList, as we'll be reordering & repopulating from DataService.
                        itemList.clear();
                        Log.i(CLASS_NAME, "Clearing the locally-kept list. List is now: " + itemList);
                        for (Item item : objects) {
                            if (item.getUserId() != null && item.getUserId().equals(uUserID)) {
                                itemList.add(item);
                            }
                        }
                        sortItems(itemList);

                        // tells the view to refresh itself, since the underlying data has changed.
                        lvArrayAdapter.notifyDataSetChanged();
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
			
		}  catch (Exception e) {
			Log.e(CLASS_NAME, "Exception : " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Send a notification to all devices whenever the BlueList is modified (create, update, or delete)
	 */
	private void updateOtherDevices() {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("key1", "value1");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		/*
		 * Call the node.js application hosted in the IBM Cloud Code service
		 * with a POST call, passing in a non-essential JSONObject.
		 * The URI is relative to, appended to, the BlueMix context root.
		 */
		
		myCloudCodeService.post("notifyOtherDevices", jsonObj).continueWith(new Continuation<IBMHttpResponse, Void>() {
            @Override
            public Void then(Task<IBMHttpResponse> task) throws Exception {
                int responseCode;
                if (task.isCancelled()) {
                    Log.e(CLASS_NAME, "Exception : Task " + task.toString() + " was cancelled.");
                } else if (task.isFaulted()) {
                    Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                }
                else {


                    responseCode = task.getResult().getHttpResponseCode();
                    InputStream is = task.getResult().getInputStream();
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(is));
                        String responseString = "";
                        String myString = "";
                        while ((myString = in.readLine()) != null)
                            responseString += myString;

                        in.close();
                        Log.i(CLASS_NAME, "Response Body: " + responseString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(CLASS_NAME, "Response Status from notifyOtherDevices: " + responseCode);
                    if (responseCode == 401) {
                        Intent intent = new Intent(mainActivity, LoginActivity.class);
                        mainActivity.startActivity(intent);
                        IBMBluemix.clearSecurityToken().continueWith(new Continuation<IBMCurrentUser, Void>() {
                            @Override
                            public Void then(Task<IBMCurrentUser> userTask)
                                    throws Exception {
                                if (userTask.isCancelled()) {
                                    Log.e(CLASS_NAME, "Exception during logout : Task " + userTask.toString() + " was cancelled.");
                                } else if (userTask.isFaulted()) {
                                    Log.e(CLASS_NAME, "Exception during logout : " + userTask.getError().getMessage());
                                    userTask.getError().printStackTrace();
                                } else {
                                    Log.i(CLASS_NAME, "Successfully logged out of the BlueList app. Finishing Main activity.");
                                    mainActivity.finish();
                                }
                                return null;
                            }
                        });
                    }
                }
                return null;
            }
        });

	}

	/**
	 * On return from other activity, check result code to determine behavior.
	 */
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		switch (resultCode)
		{
		/* If an edit has been made, notify that the data set has changed. */
		case BlueListApplication.EDIT_ACTIVITY_RC:
			updateOtherDevices();
			sortItems(itemList);
			lvArrayAdapter.notifyDataSetChanged();
    		break;
		}
    }
	
	/**
	 * Called on done and will add item to list.
	 *
	 * @param  v edittext View to get item from.
	 * @throws IBMDataException 
	 */
	public void createItem(View v) {
		EditText itemToAdd = (EditText) findViewById(R.id.itemToAdd);
		String toAdd = itemToAdd.getText().toString();
		Item item = new Item();
		if (!toAdd.equals("")) {
			item.setName(toAdd);
			item.setUserId(uUserID);
			/**
			 * IBMObjectResult is used to handle the response from the server after 
			 * either creating or saving an object.
			 * 
			 * onResult is called if the object was successfully saved.
			 * onError is called if an error occurred saving the object. 
			 */
			item.save().continueWith(new Continuation<IBMDataObject, Void>() {

				@Override
				public Void then(Task<IBMDataObject> task) throws Exception {
                    // Log error message, if the save task is cancelled.
                    if (task.isCancelled()) {
                        Log.e(CLASS_NAME, "Exception : Task " + task.toString() + " was cancelled.");
                    }

					 // Log error message, if the save task fails.
					else if (task.isFaulted()) {
						Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
					}

					 // If the result succeeds, load the list.
					else {
						listItems();
						updateOtherDevices();
					}
					return null;
				}

			}, Task.UI_THREAD_EXECUTOR);

			// Set text field back to empty after item is added.
			itemToAdd.setText("");
		}
	}
	
	/**
	 * Will delete an item from the list.
	 *
	 * @param  Item item to be deleted
	 */
	public void deleteItem(Item item) {
		itemList.remove(listItemPosition);
		// This will attempt to delete the item on the server.
		item.delete().continueWith(new Continuation<IBMDataObject, Void>() {
			// Called if the object is successfully deleted.
			@Override
			public Void then(Task<IBMDataObject> task) throws Exception {
                // Log error message, if the delete task is cancelled.
                if (task.isCancelled()) {
                    Log.e(CLASS_NAME, "Exception : Task " + task.toString() + " was cancelled.");
                }

				 // Log error message, if the delete task fails.
				else if (task.isFaulted()) {
					Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
				}

				 // If the result succeeds, reload the list.
				else {
					updateOtherDevices();
					lvArrayAdapter.notifyDataSetChanged();
				}
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
		
		lvArrayAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Will call new activity for editing item on list.
	 * @parm String name - name of the item.
	 */
	public void updateItem(String name) {
		Intent editIntent = new Intent(getBaseContext(), EditActivity.class);
    	editIntent.putExtra("userId", uUserID);
    	editIntent.putExtra("ItemText", name);
    	editIntent.putExtra("ItemLocation", listItemPosition);
    	startActivityForResult(editIntent, BlueListApplication.EDIT_ACTIVITY_RC);
	}
	
	/**
	 * Sort a list of Items.
	 * @param List<Item> theList
	 */
	private void sortItems(List<Item> theList) {
		// Sort collection by case insensitive alphabetical order.
		Collections.sort(theList, new Comparator<Item>() {
			public int compare(Item lhs,
					Item rhs) {
				String lhsName = lhs.getName();
				String rhsName = rhs.getName();
				return lhsName.compareToIgnoreCase(rhsName);
			}
		});
	}
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        /* Inflate a menu resource with context menu items. */
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.editaction, menu);
	        return true;
	    }

	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false;
	    }

		/**
		 * Called when user clicks on contextual action bar menu item.
		 * 
		 * Determined which item was clicked, and then determine behavior appropriately.
		 *
		 * @param  item menu item clicked
		 */
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	    	Item lItem = itemList.get(listItemPosition);
	    	/* Switch dependent on which action item was clicked. */
	    	switch (item.getItemId()) {
	    		/* On edit, get all info needed & send to new, edit activity. */
	            case R.id.action_edit:
	            	updateItem(lItem.getName());
	                mode.finish(); /* Action picked, so close the CAB. */
	                return true;
	            /* On delete, remove list item & update. */
	            case R.id.action_delete:
	            	deleteItem(lItem);
	                mode.finish(); /* Action picked, so close the CAB. */
	            default:
	                return false;
	        }
	    }

	    /* Called on exit of action mode. */
	    public void onDestroyActionMode(ActionMode mode) {
	        mActionMode = null;
	    }
	};
}

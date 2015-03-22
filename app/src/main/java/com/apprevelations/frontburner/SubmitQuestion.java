package com.apprevelations.frontburner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ibm.mobile.services.cloudcode.IBMCloudCode;
import com.ibm.mobile.services.core.IBMBluemix;
import com.ibm.mobile.services.core.IBMCurrentUser;
import com.ibm.mobile.services.data.IBMData;
import com.ibm.mobile.services.data.IBMDataObject;
import com.ibm.mobile.services.data.IBMQuery;
import com.ibm.mobile.services.push.IBMPush;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Altair on 22-03-2015.
 */
public class SubmitQuestion extends Fragment {

    private View view;
    private ListView listView;

    private String CLASS_NAME = "SubmitQuestion";
    private String uUserId;

    private IBMPush push;

    public String deviceAlias = "TargetDevice";
    public String consumerID = "MBaaSListApp";
    public String userId = null;

    private List<Item> itemList;
    private CustomQuesAdapter lvArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            listView = (ListView) view.findViewById(R.id.listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // open comment window

                }
            });
        } else {
            // If we are returning from a configuration change:
            // "view" is still attached to the previous view hierarchy
            // so we need to remove it and re-attach it to the current one
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
        }

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newQuestionActivity = new Intent(getParentFragment().getActivity(),
                        NewQuestionActivity.class);
                newQuestionActivity.putExtra("uUserId", uUserId);
                startActivity(newQuestionActivity);
            }
        });

        itemList = new ArrayList<Item>();
        lvArrayAdapter = new CustomQuesAdapter(getParentFragment().getActivity(), itemList);

        /*
         * Asynchronous bluemix data fetching
         * new DownloadXML().execute(URL);
        */

        /* ID is overridden by applicationID set in assets/configuration.json. */
        IBMBluemix.setSecurityToken(IBMBluemix.IBMSecurityProvider.GOOGLE, UserAccount.getAccessToken()).continueWithTask(
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
                        uUserId = cUser.getUuid();

                        // Initialize the IBM Data Service
                        IBMData.initializeService();
                        // Register Item Specialization
                        Item.registerSpecialization(Item.class);
                        // Initialize the IBM Cloud Code Service
                        IBMCloudCode.initializeService();
                        // Instantiate the cloud code service instance
                        IBMCloudCode myCloudCodeService = IBMCloudCode.getService();
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

        return view;
    }

    /*
     * Will delete an item from the list.
     *
     * @param  Item item to be deleted
     */
    public void deleteQues(int listItemPosition) {
        if ((itemList.get(listItemPosition).getUserId()).equals(uUserId)) {
            itemList.remove(listItemPosition);
            // This will attempt to delete the item on the server.
            itemList.get(listItemPosition).delete().continueWith(new Continuation<IBMDataObject, Void>() {
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
//                        updateOtherDevices();
                        lvArrayAdapter.notifyDataSetChanged();
                    }
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);

            lvArrayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Will call new activity for editing item on list.
     * @parm String name - name of the item.
     */
    /*public void updateQues(int listItemPosition) {
        if ((item.getUserId()).equals(user.getResult().getUuid())) {
            Intent editIntent = new Intent(getActivity().getBaseContext(), EditActivity.class);
            editIntent.putExtra("userId", uUserId);
            editIntent.putExtra("ItemText", name);
            editIntent.putExtra("ItemLocation", listItemPosition);
            startActivityForResult(editIntent, BlueListApplication.EDIT_ACTIVITY_RC);
        }
    }*/

    private void showToast(String message) {
        Toast.makeText(getParentFragment().getActivity(), message, Toast.LENGTH_SHORT).show();
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
                            if (item.getUserId() != null && item.getUserId().equals(uUserId)) {
                                itemList.add(item);
                            }
                        }
//                        sortItems(itemList);

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
}

package com.apprevelations.frontburner;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ibm.mobile.services.data.IBMDataObject;

import org.json.JSONArray;
import org.json.JSONException;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by appyware on 22/03/15.
 */
public class NewQuestionActivity extends ActionBarActivity {

    private EditText itemToAdd;
    private Spinner categorySpinner;

    private String CLASS_NAME = "NewQuestionActivity";
    private String uUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_ques);

        if (getIntent().getExtras() != null) {
            uUserId = getIntent().getExtras().getString("uUserId");
        }

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar_post);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // activity.getSupportActionBar().setTitle("");

        findViewById(R.id.addNewQues).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeQues();
            }
        });

        itemToAdd = (EditText) findViewById(R.id.newQues);
        categorySpinner = (Spinner) findViewById(R.id.post_topics);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        categorySpinner.setAdapter(adapter);

    }

    public void storeQues() {

        String quesBody = itemToAdd.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        Item item = new Item();
        if (!quesBody.equals("")) {
            item.setName(quesBody);
            item.setUserId(uUserId);
            try {
                item.setObject("category", category);
                item.setObject("comments", new JSONArray("[]"));
                item.setObject("upvotes", new JSONArray("[]"));
                item.setObject("downvote", new JSONArray("[]"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                        showToast("Cancelled");
                    }

                    // Log error message, if the save task fails.
                    else if (task.isFaulted()) {
                        Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                        showToast("Error");
                    }

                    // If the result succeeds, load the list.
                    else {
//                       listItems();
//                        updateOtherDevices();
                    }
                    return null;
                }

            }, Task.UI_THREAD_EXECUTOR);

            // Set text field back to empty after item is added.
            itemToAdd.setText("");
        }
    }

    private void showToast(String message) {
        Toast.makeText(NewQuestionActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

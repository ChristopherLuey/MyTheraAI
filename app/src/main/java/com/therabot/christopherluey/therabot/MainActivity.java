package com.therabot.christopherluey.therabot;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    static RecyclerView mMessageRecycler;
    static RecyclerView.Adapter mMessageAdapter;

    Button settingsButton;

    static Boolean showmessagetimemain = false;
    static Boolean iconbothidemain = false;

    static String TextInput;
    static int textsizenumbermain = 6;

    static List listMessage;

    Conversation service;
    private com.ibm.watson.developer_cloud.conversation.v1.model.Context context = null;

    InputData inputData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsButton = findViewById(R.id.settings);

        mMessageRecycler = findViewById(R.id.message_list);
        mMessageRecycler.setHasFixedSize(true);

        listMessage = new ArrayList<String>();

        mMessageAdapter = new MessageListAdapter(listMessage);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

        final EditText input = findViewById(R.id.input);

        //Settings button that leads to Settings Activity
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });


        //Initial bot message
        listMessage.add(getString(R.string.chat_intro));
        mMessageAdapter.notifyDataSetChanged();


        //Edit text onEditorActionListener
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final String inputText;

                    if (input.getText().toString().equals("")) {
                        return false;

                    } else {
                        inputText = input.getText().toString();
                        MainActivity.TextInput = inputText;
                        listMessage.add(Html.fromHtml("0 " + inputText));
                    }

                    input.setText("");
                    watson();
                }
                return false;
            }
        });
    }

    //Getting conversation service and receiving the response
    private void watson() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    service = new Conversation(Conversation.VERSION_DATE_2017_05_26);
                    service.setUsernameAndPassword(getString(R.string.username), getString(R.string.password));
                    inputData = new InputData.Builder(MainActivity.TextInput).build();
                    MessageOptions options = new MessageOptions.Builder(getString(R.string.workspace)).input(inputData).context(context).build();
                    MessageResponse response = service.message(options).execute();

                    if (response.getContext() != null) {
                    //context.clear();
                    context = response.getContext();

                    }

                    final String outMessage;

                    if (response != null) {
                        if (response.getOutput() != null && response.getOutput().containsKey("text")) {

                            ArrayList responseList = (ArrayList) response.getOutput().get("text");
                            if (null != responseList && responseList.size() > 0) {
                                outMessage = String.valueOf(responseList.get(0));

                                listMessage.add("1 " + outMessage);
                                runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMessageAdapter.notifyDataSetChanged();
                                        }
                            });
                        }
                    }

                } else if (!checkInternetConnection()) {
                    listMessage.add("1 Oh No! I require an internet connection to work. Please connect to any available network.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMessageAdapter.notifyDataSetChanged();
                        }
                    });
                }
                } catch (Exception e){
                    if (!checkInternetConnection()) {
                        listMessage.add("1 Oh No! I require an internet connection to work. Please connect to any available network.");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMessageAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });

        thread.start();
    }

    //Boolean to check internet connection
    boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            // Check for network connections
            if (isConnected){
                return true;
            }
            else {
                return false;
            }
        } catch (Exception e){
            return false;
        }
    }
}

package com.example.zhangxinwei.smartwatch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends Activity {
    private static final String TAG = "VoiceRecognition";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private TextView textView;
    private Button button;
    private String command;
    ESP8266Client client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidget();

        // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startVoiceRecognitionActivity();
                }
            });
        } else {
            button.setEnabled(false);
            button.setText("Recognizer not present");
        }
    }

    private void initWidget()
    {
        textView = findViewById(R.id.resultText2);
        button = findViewById(R.id.button1);
        client = new ESP8266Client();
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            StringBuilder stringBuilder = new StringBuilder();
            command = matches.get(0);
            int Size = matches.size();
            for(int i=0;i<Size;++i)
            {
                stringBuilder.append(matches.get(i));
                stringBuilder.append("\n");
            }
            textView.setText(stringBuilder);
            System.out.println("Voice recognized!");
            postToESP8266();
            System.out.println("Sent Post to ESP8266 server");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void postToESP8266() {
        RequestParams params = new RequestParams();
        params.put("", command);
        client.post("", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println("Successfully sent to ESP8266"+responseBody.toString());
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println(statusCode);
                error.printStackTrace(System.out);
            }
        });
    }
}

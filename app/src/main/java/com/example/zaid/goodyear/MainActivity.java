package com.example.zaid.goodyear;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private ProgressBar spinner;
    private String userInput;
    private EditText TermtoAnalyze;
    private TextView Result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        TermtoAnalyze = (EditText) findViewById(R.id.TermtoAnalyze);
        Result = (TextView) findViewById(R.id.result);
    }

    public void searchMethod(View v) {
        Uri builtUri2 = Uri.parse("https://api.meaningcloud.com/sentiment-2.1").buildUpon()
                .appendQueryParameter("key", "5cefecfee765dde6c9c943db4c891c88")
                .appendQueryParameter("txt", TermtoAnalyze.getText().toString())
                .appendQueryParameter("lang", "en")
                .build();
        new FindSentimentTask().execute(builtUri2.toString());
        spinner.setVisibility(View.VISIBLE);

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String formattedDate = df.format(c.getTime());
        //get one year ago
        c.add(Calendar.YEAR, -1);
        String yearAgoDate = df.format(c.getTime());

        Uri builtUri3 = Uri.parse("https://api.nytimes.com/svc/search/v2/articlesearch.json").buildUpon()
                .appendQueryParameter("api-key", "4bc349cc561c4e5d8e7023eff1bf7019")
                .appendQueryParameter("begin_date", yearAgoDate)
                .appendQueryParameter("end_date", formattedDate)
                .build();
        new yearEvaluation().execute(builtUri3.toString());
        spinner.setVisibility(View.VISIBLE);
    }


    public class FindSentimentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {

            String toreturn = "Did not work";
            try {
                toreturn = getResponseFromHttpUrl(url[0]);
            } catch (Exception e) {
                Log.d("ErrorInApp", "exception on get Response from HTTP call" + e.getMessage());
            }
            return toreturn;
        }

        protected void onPostExecute(String sentimentData) {
            /*P+: strong positive
            P: positive
            NEU: neutral
            N: negative
            N+: strong negative
            NONE: without sentiment*/
            try {
                JSONObject sentimentJSON = new JSONObject(sentimentData);
                //int x=5;
                //((TextView)findViewById(R.id.textView2)).setText(sentimentJSON.toString());
                //JSONObject status= sentimentJSON.getJSONObject("model");
                String scoreTag = sentimentJSON.get("score_tag").toString();
                String confidenceTag = sentimentJSON.get("confidence").toString();
                String ironyTag = sentimentJSON.get("irony").toString();



                Result.setText("Score Tag: " + scoreTag + "\n" + "Confidence: " + confidenceTag + "\n" + "Irony: " + ironyTag);
                spinner.setVisibility(View.GONE);

            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(sentimentData);
        }
    }


    public class yearEvaluation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            android.os.Debug.waitForDebugger();
            String toreturn = "Did not work";
            try {
                toreturn = getResponseFromHttpUrl(url[0]);
            } catch (Exception e) {
                Log.d("ErrorInApp", "exception on get Response from HTTP call" + e.getMessage());
            }
            return toreturn;
        }

        protected void onPostExecute(String sentimentData) {
            try {
                JSONObject sentimentJSON = new JSONObject(sentimentData);

                JSONObject answer = sentimentJSON.getJSONObject("response");
                JSONArray Jarray = answer.getJSONArray("docs");
                JSONObject index = Jarray.getJSONObject(0);

                for(int i=0; i< Jarray.length(); i++){
                    index = Jarray.getJSONObject(i);
                    sentimentData = index.toString();
                }

                TermtoAnalyze.setText(sentimentData);
                spinner.setVisibility(View.GONE);

            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(sentimentData);
        }
    }


    public static String getResponseFromHttpUrl(String url) throws IOException {
        URL theURL = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) theURL.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
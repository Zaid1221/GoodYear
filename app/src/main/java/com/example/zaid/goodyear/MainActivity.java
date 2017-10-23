package com.example.zaid.goodyear;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
    private String article;
    private TextView dateFrom;
    private TextView dateTo;
    private EditText inputQuery;
    private TextView Result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = (ProgressBar) findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        dateFrom = (TextView) findViewById(R.id.DateFrom);
        dateTo = (TextView) findViewById(R.id.DateTo);
        inputQuery = (EditText) findViewById(R.id.Query);
        Result = (TextView) findViewById(R.id.result);

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String formattedDate = df.format(c.getTime());
        //get one year ago
        c.add(Calendar.YEAR, -1);
        String yearAgoDate = df.format(c.getTime());

        dateFrom.setText(yearAgoDate);
        dateTo.setText(formattedDate);
    }

    public void searchMethod(View v) {
        BuildUriTimes();
    }

    public void BuildUriTimes(){
        Uri builtUri = Uri.parse("https://api.nytimes.com/svc/search/v2/articlesearch.json").buildUpon()
            .appendQueryParameter("api-key", "05cb6024971840939205aaa75206ee2a")
            .appendQueryParameter("q", inputQuery.getText().toString())
            .appendQueryParameter("begin_date", dateFrom.getText().toString())
            .appendQueryParameter("end_date", dateTo.getText().toString())
            .build();
        BuildNYtimes(builtUri);
    }

    public void BuildNYtimes(Uri builtUri){
        new yearEvaluation().execute(builtUri.toString());
        BuildUriSentiment();
    }

    public class yearEvaluation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            // android.os.Debug.waitForDebugger();
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
                JSONObject Jarticle = new JSONObject(sentimentData);
                JSONObject answer = Jarticle.getJSONObject("response");
                JSONArray Jarray = answer.getJSONArray("docs");

                String temp = "";
                for (int i = 0; i < Jarray.length(); i++) {
                    JSONObject index = Jarray.getJSONObject(i);
                    temp += index.getString("headline");
                }

                String[] headlines = temp.split("\"main\":\"");
                String ALLHEADLINES = "";

                for (int i = 1; i < headlines.length; i++) {
                    String[] headlines2 = headlines[i].split("\"");
                    ALLHEADLINES += headlines2[0].toString() + " ";
                }
                //Result.setText(ALLHEADLINES);
                article = ALLHEADLINES; //Set Variable to Be Analyzed in the Sentiment API

                spinner.setVisibility(View.GONE);

            } catch (Exception e) {
                e.printStackTrace();
            }
            Result.setText(article);
            super.onPostExecute(sentimentData);
        }
    }

    public void BuildUriSentiment(){
        Uri builtUri2 = Uri.parse("https://api.meaningcloud.com/sentiment-2.1").buildUpon()
            .appendQueryParameter("key", "f9d923ead87a27f37ff8bfb485feb895")
            .appendQueryParameter("txt", article)
            .appendQueryParameter("lang", "en")
            .build();
        BuildSentiment(builtUri2);
    }

    public void BuildSentiment(Uri builtUri2){
        new FindSentimentTask().execute(builtUri2.toString());
        spinner.setVisibility(View.VISIBLE);
    }


    public class FindSentimentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {

            String toreturn = "No response";
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

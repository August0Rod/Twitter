 package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

 public class TimelineActivity extends AppCompatActivity {

     private SwipeRefreshLayout swipeContainer;

     public static final String TAG = "TimelineActivity";
     TwitterClient client;
     RecyclerView rvTweets;
     List<Tweet> tweets;
     TweetsAdapter adapter;
     MenuItem miActionProgressItem;

     private final int REQUEST_CODE = 20;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_timeline);

         client = TwitterApp.getRestClient(this);

         rvTweets = findViewById(R.id.rvTweets);

         tweets = new ArrayList<>();
         adapter = new TweetsAdapter(this, tweets);

         rvTweets.setLayoutManager(new LinearLayoutManager(this));
         rvTweets.setAdapter(adapter);

         LinearLayoutManager layoutManager = new LinearLayoutManager(this);

         DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvTweets.getContext(),
                 layoutManager.getOrientation());
         rvTweets.addItemDecoration(dividerItemDecoration);

         populateHomeTimeline();

         swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
         // Setup refresh listener which triggers new data loading
         swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 // Your code to refresh the list here.
                 // Make sure you call swipeContainer.setRefreshing(false)
                 // once the network request has completed successfully.
                 fetchTimelineAsync(0);
             }
         });

     }

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {

         getMenuInflater().inflate(R.menu.menu_main, menu);
         return true;
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         if (item.getItemId() == R.id.Compose) {

             //Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(this, ComposeActivity.class);
             startActivityForResult(intent, REQUEST_CODE);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }


     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
             Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

             tweets.add(0, tweet);

             adapter.notifyItemInserted(0);
             rvTweets.smoothScrollToPosition(0);

         }
         super.onActivityResult(requestCode, resultCode, data);
     }

     private void populateHomeTimeline () {
         client.getHomeTimeline(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.i(TAG, "OnSuccess!"+json.toString());
                    JSONArray jsonArray = json.jsonArray;

                    miActionProgressItem.setVisible(true);

                    try {
                        tweets.addAll(Tweet.fromJsonArray(jsonArray));
                        adapter.notifyDataSetChanged();
                        miActionProgressItem.setVisible(false);

                    } catch (JSONException e) {
                        Log.e(TAG, "Json exception", e);
                        e.printStackTrace();

                    }

                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "OnFailure!"+ response, throwable);


                }
            }
         );
     }

     public void onLogoutButton(View view) {

         TwitterApp.getRestClient(this).clearAccessToken();

         Intent i = new Intent(this, LoginActivity.class);
         i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(i);

     }

     public void fetchTimelineAsync(int page) {
         // Send the network request to fetch the updated data
         // `client` here is an instance of Android Async HTTP
         // getHomeTimeline is an example endpoint.
         client.getHomeTimeline(new JsonHttpResponseHandler() {
             @Override
             public void onSuccess(int statusCode, Headers headers, JSON json) {
                 JSONArray jsonArray = json.jsonArray;

                 // Remember to CLEAR OUT old items before appending in the new ones
                 adapter.clear();
                 // ...the data has come back, add new items to your adapter...
                 try {
                     tweets.addAll(Tweet.fromJsonArray(jsonArray));
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 // Now we call setRefreshing(false) to signal refresh has finished
                 swipeContainer.setRefreshing(false);
             }

             @Override
             public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                 Log.d("DEBUG", "Fetch timeline error: " + throwable.toString());
             }
         });
     }

         @Override
         public boolean onPrepareOptionsMenu(Menu menu) {
             // Store instance of the menu item containing progress
             miActionProgressItem = menu.findItem(R.id.miActionProgress);

             // Return to finish
             return super.onPrepareOptionsMenu(menu);

     }

 }
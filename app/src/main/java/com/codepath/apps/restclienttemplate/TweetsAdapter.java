package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

       Context context;
       List<Tweet>tweets;


       public TweetsAdapter(Context context, List<Tweet> tweets) {
              this.context = context;
              this.tweets = tweets;
       }

       @NonNull
       @Override
       public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

              View view = LayoutInflater.from(context).inflate(R.layout.item_tweet,parent, false);
              return new ViewHolder(view);
       }

       @Override
       public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


              holder.bind(position);

       }

       @Override
       public int getItemCount() {
              return tweets.size();
       }

       public class ViewHolder extends RecyclerView.ViewHolder {

              ImageView ivProfileImage;
              TextView tvBody;
              TextView tvScreenName;
              ImageView rlImage;
              TextView rlTime;
              ImageButton clLikeButton;
              ImageButton clRetweetButton;
              ImageButton clCommentButton;

              public ViewHolder(@NonNull View itemView) {
                     super(itemView);
                     ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
                     tvBody = itemView.findViewById((R.id.tvBody));
                     tvScreenName = itemView.findViewById((R.id.tvScreenName));
                     rlImage = itemView.findViewById(R.id.rlImage);
                     rlTime = itemView.findViewById(R.id.rlTime);
                     clLikeButton = itemView.findViewById(R.id.LikeButton);
                     clCommentButton = itemView.findViewById(R.id.CommentButton);
                     clRetweetButton = itemView.findViewById(R.id.RetweetButton);
                     Log.i("amdamd", "OnSuccess");
              }
              public void bind (int position) {
                     Tweet tweet = tweets.get(position);

                     rlTime.setText(getRelativeTimeAgo(tweet.createdAt));
                     tvBody.setText(tweet.body);
                     tvScreenName.setText(tweet.user.screenName);
                     Glide.with(context).load(tweet.user.profileImageUrl).transform(new RoundedCorners(100)).into(ivProfileImage);

                     if (tweet.imageUrl != null) {
                            rlImage.setVisibility(View.VISIBLE);
                           Glide.with(context).load(tweet.imageUrl).transform(new RoundedCorners(50)).into(rlImage);
                    }
                     else   {
                            rlImage.setVisibility(View.GONE);
                     }
                     TwitterClient client = TwitterApp.getRestClient(context);

                     if (tweet.isLiked){
                            // set icon to red
                            clLikeButton.setImageResource(R.drawable.ic_vector_heart);
                     } else {
                            // set icon to plain
                            clLikeButton.setImageResource(R.drawable.ic_vector_heart_stroke);
                     }

                     if (tweet.isRetweeted){
                            // set icon to red
                            clRetweetButton.setImageResource(R.drawable.ic_vector_retweet);
                     } else {
                            // set icon to plain
                            clRetweetButton.setImageResource(R.drawable.ic_vector_retweet_stroke);
                     }

                     clLikeButton.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                                if (tweet.isLiked) {
                                       // unlike tweet
                                       tweet.isLiked = false;
                                       notifyItemChanged(position);


                                       // make api call to unlike tweet
                                       client.unfavoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                                              @Override
                                              public void onSuccess(int statusCode, Headers headers, JSON json) {
                                                     // do nothing
                                              }

                                              @Override
                                              public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                                     tweet.isLiked = true;
                                                     notifyItemChanged(position);
                                              }
                                       });
                                } else {
                                       //like tweet
                                       tweet.isLiked = true;
                                       notifyItemChanged(position);

                                       // make api call to like tweet
                                       // make api call to unlike tweet
                                       client.favoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                                              @Override
                                              public void onSuccess(int statusCode, Headers headers, JSON json) {
                                                     // do nothing
                                              }

                                              @Override
                                              public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                                     tweet.isLiked = false;
                                                     notifyItemChanged(position);
                                              }
                                       });
                                }
                         }
                  });


                     clRetweetButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                   if (tweet.isRetweeted){
                            // unlike tweet
                            tweet.isRetweeted = false;
                            notifyItemChanged(position);


                            // make api call to unlike tweet
                            client.unreTweet(tweet.id, new JsonHttpResponseHandler() {
                                   @Override
                                   public void onSuccess(int statusCode, Headers headers, JSON json) {
                                          // do nothing
                                   }

                                   @Override
                                   public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                          tweet.isRetweeted = true;
                                          notifyItemChanged(position);
                                   }
                            });
                     } else {
                            //like tweet
                            tweet.isRetweeted = true;
                            notifyItemChanged(position);

                            // make api call to like tweet
                            // make api call to unlike tweet
                            client.reTweet(tweet.id, new JsonHttpResponseHandler() {
                                   @Override
                                   public void onSuccess(int statusCode, Headers headers, JSON json) {
                                          // do nothing
                                   }

                                   @Override
                                   public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                          tweet.isRetweeted = false;
                                          notifyItemChanged(position);
                                   }
                            });
                     }
                            }
                     });
                   }
              }

       // Clean all elements of the recycler
       public void clear() {
              tweets.clear();
              notifyDataSetChanged();
       }

       // Add a list of items -- change to type used
       public void addAll(List<Tweet> list) {
              tweets.addAll(list);
              notifyDataSetChanged();
       }
       public String getRelativeTimeAgo(String rawJsonDate) {
              String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
              SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
              sf.setLenient(true);

              String relativeDate = "";
              try {
                     long dateMillis = sf.parse(rawJsonDate).getTime();
                     relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                             System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
              } catch (ParseException e) {
                     e.printStackTrace();
              }

              return relativeDate;
       }


}

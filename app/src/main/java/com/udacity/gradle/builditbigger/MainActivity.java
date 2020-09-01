package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.udacity.gradle.builditbigger.IdlingResource.SimpleIdlingResource;

import net.sunyounglee.javajokes.Joker;
import net.sunyounglee.mylibrary.JokeActivity;

import static com.udacity.gradle.builditbigger.MessageDelayer.processMessage;

public class MainActivity extends AppCompatActivity implements EndpointsAsyncTask.AsyncCallback, MessageDelayer.DelayerCallback {
    private static String TAG = MainActivity.class.getSimpleName();
    private ProgressBar mLoadingIndicator;
    private TextView mTextViewInstruction;
    private Button mButtonJokeJavaLibrary;
    private Button mButtonJokeGCE;
    private InterstitialAd mInterstitialAd;
    private EndpointsAsyncTask.AsyncCallback mAsyncCallback;
    private Context mContext;

    @Nullable
    private SimpleIdlingResource mIdlingResource;
    public String jokeGCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAsyncCallback = this;
        mContext = this;
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        mTextViewInstruction = findViewById(R.id.instructions_text_view);
        mButtonJokeJavaLibrary = findViewById(R.id.btn_joke_gce);
        mButtonJokeGCE = findViewById(R.id.btn_joke_java_library);

        //InterestitialAd is displaying only for free version app
        if (!BuildConfig.PAID_VERSION) {
            initializeInterstitialAd();
            loadInterstitialAd();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void tellJoke(View view) {
        Joker myJoker = new Joker();
        String todayJoke = myJoker.getJoke();
        Toast.makeText(this, todayJoke, Toast.LENGTH_SHORT).show();
    }

    //Get today's joke from java library.
    public void launchJokeActivityJavaLibary(View view) {
        Joker jokeSource = new Joker();
        final String todayJoke = jokeSource.getJoke();

        if (!BuildConfig.PAID_VERSION) {
            if (mInterstitialAd.isLoaded()) {
                showInterstitialAd();
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        if (todayJoke != null && !todayJoke.isEmpty()) {
                            launchJokeActivity(todayJoke);
                        }
                    }
                });
            } else {
                launchJokeActivity(todayJoke);
            }
        } else {
            launchJokeActivity(todayJoke);
        }
    }

    //get today's joke from GCE backend server.
    public void launchJokeActivityGCE(View view) {
        if (!BuildConfig.PAID_VERSION) {
            if (mInterstitialAd.isLoaded()) {
                showInterstitialAd();
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        new EndpointsAsyncTask(mAsyncCallback).execute();
                        getIdlingResource();
                        showLoadingIndicator();
                    }
                });
            } else {
                new EndpointsAsyncTask(mAsyncCallback).execute();
                getIdlingResource();
                showLoadingIndicator();
            }

        } else {
            new EndpointsAsyncTask(mAsyncCallback).execute();
            getIdlingResource();
            showLoadingIndicator();
        }
    }

    /* When EndpointsAsyncTask is done, this method is called to display joke in
    the JokeActivity */
    @Override
    public void onCompleteTask(String output) {
        //for asynchronous test purpose, process message after idling.
        processMessage(output, this, mIdlingResource);

        if (output != null && !output.isEmpty()) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            launchJokeActivity(output);

        }
    }

    public SimpleIdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }


    @Override
    public void onDone(String joke) {
        jokeGCE = joke;
    }

    //show loading indicator
    private void showLoadingIndicator() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mTextViewInstruction.setVisibility(View.INVISIBLE);
        mButtonJokeJavaLibrary.setVisibility(View.INVISIBLE);
        mButtonJokeGCE.setVisibility(View.INVISIBLE);
    }

    //show the inital view
    private void setupInitialView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mTextViewInstruction.setVisibility(View.VISIBLE);
        mButtonJokeJavaLibrary.setVisibility(View.VISIBLE);
        mButtonJokeGCE.setVisibility(View.VISIBLE);
    }

    //Initalize interstitial ad.
    private void initializeInterstitialAd() {
        Log.d(TAG, "initializeInterstitialAd called");
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitialAd_id));
    }

    //Load interstitial ad.
    private void loadInterstitialAd() {
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    //show interstitial ad if ad is loaded.
    private void showInterstitialAd() {
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d(TAG, "The interstitial wasn't loaded yet.");
        }
    }

    private void launchJokeActivity(String joke) {
        Intent intent = new Intent(this, JokeActivity.class);
        intent.putExtra(JokeActivity.JOKE_KEY, joke);
        startActivity(intent);
    }

    //Load interstitial ad when back button is pressed from JokeActivity.
    @Override
    protected void onResume() {
        super.onResume();
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            Log.d(TAG, "ad is loaded");
        } else {
            Log.d(TAG, "ad is not loaded");
            loadInterstitialAd();
        }
    }

    //Recover initial view to go back to the MainActivity from JokeActivity.
    @Override
    protected void onStop() {
        super.onStop();
        setupInitialView();
        Log.d(TAG, "onStop called");

    }
}

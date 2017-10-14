package ovh.szarasowa.currencyrates;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {
    private static final String REQUEST_URL_BTC = "https://bitbay.net/API/Public/BTCPLN/ticker.json";
    private static final String REQUEST_URL_ETH = "https://bitbay.net/API/Public/ETHPLN/ticker.json";
    private static final String REQUEST_URL_CHF = "http://api.nbp.pl/api/exchangerates/rates/c/chf/?format=json";

    static ProgressBar progressBar;
    static Animation animationAlpha;
    private final String TAG = MainActivity.class.getSimpleName();
    public SoundPool soundPool;
    public int beepSoundId;
    TextView btcMinView, btcMaxView, btcLastView, ethMinView, ethMaxView, ethLastView, chfAskView, chfBidView, disconnectedTextView, timerTextView;
    LinearLayout containerLayout, refreshLayout;
    CountDownTimer countDownTimer;
    boolean isCounting;
    SoundPool.Builder soundPoolBuilder;
    AudioAttributes attributes;
    AudioAttributes.Builder attributesBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        animationAlpha = new AlphaAnimation(0.0f, 1.0f);
        animationAlpha.setDuration(400);
        animationAlpha.setRepeatCount(0);

        containerLayout = (LinearLayout) findViewById(R.id.container_layout);
        refreshLayout = (LinearLayout) findViewById(R.id.refresh_layout);

        btcMinView = (TextView) findViewById(R.id.btc_min_textView);
        btcMaxView = (TextView) findViewById(R.id.btc_max_textView);
        btcLastView = (TextView) findViewById(R.id.btc_last_textView);

        ethMinView = (TextView) findViewById(R.id.eth_min_textView);
        ethMaxView = (TextView) findViewById(R.id.eth_max_textView);
        ethLastView = (TextView) findViewById(R.id.eth_last_textView);

        chfAskView = (TextView) findViewById(R.id.chf_ask_textView);
        chfBidView = (TextView) findViewById(R.id.chf_bid_textView);

        disconnectedTextView = (TextView) findViewById(R.id.disconnected_textView);
        timerTextView = (TextView) findViewById(R.id.timerTextView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // SoundPool setters:
        attributesBuilder = new AudioAttributes.Builder();
        attributesBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
        attributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
        attributes = attributesBuilder.build();

        soundPoolBuilder = new SoundPool.Builder();
        soundPoolBuilder.setAudioAttributes(attributes);
        soundPool = soundPoolBuilder.build();

        beepSoundId = soundPool.load(this, R.raw.beep, 1);

        checkInternetConnection();

        refreshLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.cancel();
                checkInternetConnection();
            }
        });

    }   // onCreate end.

    // check connection.
    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            Toast.makeText(this, "status: CONNECTED", Toast.LENGTH_SHORT).show();
            containerLayout.setAlpha(1.0f);
            new GetCurrency().execute();
            disconnectedTextView.setVisibility(View.GONE);
            timerTextView.setText("...");
            return true;
        } else {
            Toast.makeText(this, "status: DISCONNECTED", Toast.LENGTH_LONG).show();
            containerLayout.setAlpha(0.4f);
            if (disconnectedTextView.getVisibility() == View.GONE) {
                disconnectedTextView.setVisibility(View.VISIBLE);
            }
            timerTextView.setText("..");
            countTimer(9000);
            return false;
        }
    }

    public void countTimer(long time) {
        if (isCounting) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                timerTextView.setText(String.format("REFRESH: %d", millisUntilFinished / 1000));
                isCounting = true;
            }

            public void onFinish() {
                timerTextView.setText(R.string.on_finish_timer);
                isCounting = false;
                checkInternetConnection();
            }
        }.start();
    }

    public void bitbayNetButton(View v) {
        String url = "https://bitbay.net/";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void bitcoinchartsButton(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bitcoincharts.com/markets/"));
        startActivity(intent);
    }

    public void setNewCryptoValue(TextView textView, int newValue) {

        progressBar.setProgress(+10);

        CharSequence charSequence = textView.getText();

        if (Character.isDigit(charSequence.charAt(0))) {
            if (parseInt(charSequence.toString()) != newValue) {
                if (parseInt(charSequence.toString()) < newValue) {
                    textView.setBackgroundResource(R.color.priceUp);
                } else {
                    textView.setBackgroundResource(R.color.priceDrop);
                }
            } else {
                textView.setBackgroundResource(android.R.color.transparent);
            }
        }

        textView.startAnimation(animationAlpha);
        textView.setText(String.valueOf(newValue));
    }

    public void setNewForexValue(TextView textView, double newValue) {
        CharSequence charSequence = textView.getText();

        if (Character.isDigit(charSequence.charAt(0))) {
            if (Double.parseDouble(charSequence.toString()) < newValue) {
                textView.setBackgroundResource(R.color.priceUp);
            } else if (Double.parseDouble(charSequence.toString()) > newValue) {
                textView.setBackgroundResource(R.color.priceDrop);
            } else {
                textView.setBackgroundResource(android.R.color.transparent);
            }
        }

        textView.startAnimation(animationAlpha);
        textView.setText(String.valueOf(newValue));
    }

    private class GetCurrency extends AsyncTask<Void, Void, Void> {

        Integer lastBtcJson, maxBtcJson, minBtcJson;
        Integer lastEthJson, maxEthJson, minEthJson;
        double bidCHFJson, askCHFJson;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
            progressBar.setProgress(1);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // JSON

            // BTC
            HttpHandler httpHandlerBtc = new HttpHandler();
            String jsonString = httpHandlerBtc.makeServiceCall(REQUEST_URL_BTC);

            Log.e(TAG, "Response from BTC url: " + jsonString);
            if (jsonString != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    lastBtcJson = jsonObject.getInt("average");
                    maxBtcJson = jsonObject.getInt("max");
                    minBtcJson = jsonObject.getInt("min");
                } catch (JSONException e) {
                    Log.e(TAG, "(btc) JSONException: " + e);
                }
            }

            // ETH
            HttpHandler httpHandlerEth = new HttpHandler();
            String jsonStringEth = httpHandlerEth.makeServiceCall(REQUEST_URL_ETH);

            Log.e(TAG, "Response from ETH url: " + jsonStringEth);
            if (jsonStringEth != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStringEth);
                    lastEthJson = jsonObject.getInt("average");
                    maxEthJson = jsonObject.getInt("max");
                    minEthJson = jsonObject.getInt("min");
                } catch (JSONException e) {
                    Log.e(TAG, "(eth) JSONException: " + e);
                }
            }

            // CHF
            HttpHandler httpHandlerChf = new HttpHandler();
            String jsonStringCHF = httpHandlerChf.makeServiceCall(REQUEST_URL_CHF);

            Log.e(TAG, "Response from CHF url: " + jsonStringCHF);
            if (jsonStringCHF != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStringCHF);
                    JSONArray rates = jsonObject.getJSONArray("rates");
                    JSONObject object = rates.getJSONObject(0);
                    bidCHFJson = object.getDouble("bid");
                    askCHFJson = object.getDouble("ask");
                    return null;
                } catch (JSONException e) {
                    Log.e(TAG, "(chf) JSONException: " + e);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            setNewCryptoValue(btcLastView, lastBtcJson);
            setNewCryptoValue(btcMinView, minBtcJson);
            setNewCryptoValue(btcMaxView, maxBtcJson);

            setNewCryptoValue(ethLastView, lastEthJson);
            setNewCryptoValue(ethMaxView, maxEthJson);
            setNewCryptoValue(ethMinView, minEthJson);

            setNewForexValue(chfBidView, bidCHFJson);
            setNewForexValue(chfAskView, askCHFJson);

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressBar.setProgress(100);

            Animation alphaToZero = new AlphaAnimation(1.0f, 0.0f);
            alphaToZero.setDuration(400);
            alphaToZero.setFillAfter(true);
            progressBar.startAnimation(alphaToZero);

            countTimer(300000);

            soundPool.play(beepSoundId, 0.2f, 0.2f, 0, 0, 1);

        }
    }
}

package com.joggingtrackerapp.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.joggingtrackerapp.Objects.Time;
import com.joggingtrackerapp.ui.MainActivityForManagers;
import com.joggingtrackerapp.ui.MainActivityForUsers;
import com.joggingtrackerapp.ui.MainActivityForUsers_AdminsView;
import com.joggingtrackerapp.ui.TimesFragment;
import com.joggingtrackerapp.utils.Checks;
import com.joggingtrackerapp.utils.Constants;
import com.joggingtrackerapp.utils.InternetConnectionsTimeout;
import com.joggingtrackerapp.utils.MyPreferences;
import com.joggingtrackerapp.utils.Parse;
import com.joggingtrackerapp.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import static com.joggingtrackerapp.utils.Constants.API_URL;
import static com.joggingtrackerapp.utils.Constants.TAG_ERROR;

/**
 * Created by ibrahimradwan on 9/3/15.
 */
public class ReadTimes extends AsyncTask<String, Void, String> {
    private ProgressDialog pd;
    private Context context;
    private TimesFragment timesFragment;
    private String userID = "0";

    public ReadTimes (Context context) {
        this.context = context;
    }

    public ReadTimes (Context context, TimesFragment timesFragment) {
        this.context = context;
        this.timesFragment = timesFragment;
    }

    public ReadTimes (Context context, String userID) {
        this.context = context;
        this.userID = userID;
    }

    @Override
    protected void onPreExecute () {
        super.onPreExecute();
        pd = new ProgressDialog(context);
        pd.setIndeterminate(true);
        pd.setMessage("Please Wait...");
        pd.setCancelable(false);
        pd.show();

        InternetConnectionsTimeout.startTimesStopWatch(this, 10000);

    }

    @Override
    protected String doInBackground (String... params) {
        if (!Checks.isNetworkAvailable(context)) return null;
        try {
            URL url = null;
            if (userID.trim().equals("0")) {
                url = new URL(API_URL + "times");
            } else {
                url = new URL(API_URL + "times/" + userID);
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Authorization", MyPreferences.getString(context, Constants.PREF_API_KEY));

            conn.connect();

            InputStream in = conn.getInputStream();

            return Utils.convertStreamToString(in);

        } catch (MalformedURLException e) {
            Log.e(TAG_ERROR, "Error: " + e.getMessage());
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute (String result) {
        InternetConnectionsTimeout.stopTimesStopWatch();
        pd.dismiss();

        if (result == null || result.trim().length() <= 0) {
            Toast.makeText(context, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
        } else {

            ArrayList<Time> allTimes = Parse.parseTimes(context, result);
            if (context instanceof MainActivityForUsers) {
                MainActivityForUsers.fillTimesListView(allTimes, false);
            } else if (context instanceof MainActivityForManagers) {
                TimesFragment.fillTimesListView(allTimes, false);
            } else if (context instanceof MainActivityForUsers_AdminsView) {
                MainActivityForUsers_AdminsView.fillTimesListView(allTimes, false);
            }
        }

        super.onPostExecute(result);
    }

    public void stop () {
        onPostExecute(null);
        this.cancel(true);

    }
}

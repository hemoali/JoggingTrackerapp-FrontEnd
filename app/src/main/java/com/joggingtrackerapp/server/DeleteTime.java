package com.joggingtrackerapp.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

import static com.joggingtrackerapp.utils.Constants.API_URL;
import static com.joggingtrackerapp.utils.Constants.TAG_ERROR;

/**
 * Created by ibrahimradwan on 9/3/15.
 */
public class DeleteTime extends AsyncTask<String, Void, String> {
    private ProgressDialog pd;
    private Context context;
    private int position;

    public DeleteTime (Context context, int position) {
        this.context = context;
        this.position = position;
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
            URL url = new URL(API_URL + "times/" + params[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
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

            String[] deleteData = Parse.parseDeleteTimeData(result);
            if (deleteData[0].trim().equals("200")) {
                Toast.makeText(context, "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
                if (context instanceof MainActivityForUsers) {

                    MainActivityForUsers.removeRecordFromLV(position);
                } else if (context instanceof MainActivityForManagers) {
                    TimesFragment.removeRecordFromLV(position);
                } else if (context instanceof MainActivityForUsers_AdminsView) {
                    MainActivityForUsers_AdminsView.removeRecordFromLV(position);
                }
            } else {
                Toast.makeText(context, deleteData[1], Toast.LENGTH_SHORT).show();
            }
        }

        super.onPostExecute(result);
    }

    public void stop () {
        onPostExecute(null);
        this.cancel(true);
    }
}

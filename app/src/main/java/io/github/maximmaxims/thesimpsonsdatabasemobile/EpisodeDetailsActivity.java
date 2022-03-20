package io.github.maximmaxims.thesimpsonsdatabasemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONException;

public class EpisodeDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_details);

        Intent intent = getIntent();
        int episodeId = intent.getIntExtra(EpisodeActivity.EPISODEID, 0);

        RequestQueue queue = Volley.newRequestQueue(this);
        View view = findViewById(android.R.id.content);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = preferences.getString("lang", "en");
        String root = preferences.getString("address", "");

        if (root.equals("")) {
            Snackbar.make(view, R.string.no_api, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!root.endsWith("/")) {
            root += "/";
        }
        String episodeUrl = root + "extra/description/" + lang + "/" + episodeId;

        JsonObjectRequest episodeRequest = new JsonObjectRequest(Request.Method.GET, episodeUrl, null, response -> {
            // On response:
            try {
                String plot = response.getString("description");
                TextView textViewPlot = findViewById(R.id.textViewPlot);
                textViewPlot.setMovementMethod(new ScrollingMovementMethod());
                textViewPlot.setText(plot);
            } catch (JSONException e) {
                Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
            // On error:
            // If network response was obtained
            String errMessage;
            if (error.networkResponse != null) {
                switch (error.networkResponse.statusCode) {
                    case 429:
                        errMessage = getResources().getString(R.string.error_429);
                        break;
                    case 404:
                        errMessage = getResources().getString(R.string.error_404);
                        break;
                    case 500:
                        errMessage = getResources().getString(R.string.error_500);
                        break;
                    case 400:
                        errMessage = getResources().getString(R.string.error_400);
                        break;
                    default:
                        errMessage = getResources().getString(R.string.error) + " (" + error.networkResponse.statusCode + ")";
                }
            } else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                errMessage = getResources().getString(R.string.no_internet);
            } else {
                errMessage = error.getMessage();
                if (errMessage == null) {
                    errMessage = getResources().getString(R.string.error);
                }
            }
            Snackbar.make(view, errMessage, Snackbar.LENGTH_SHORT).show();
        });
        queue.add(episodeRequest);
    }

    public void close(View view) {
        finish();
    }
}
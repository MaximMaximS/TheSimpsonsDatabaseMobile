package io.github.maximmaxims.thesimpsonsdatabasemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
            if (error.networkResponse != null) {
                int code = error.networkResponse.statusCode;
                switch (code) {
                    case 404:
                        Snackbar.make(view, R.string.error_404, Snackbar.LENGTH_SHORT).show();
                        break;
                    case 500:
                        Snackbar.make(view, R.string.error_500, Snackbar.LENGTH_SHORT).show();
                        break;
                    case 400:
                        Snackbar.make(view, R.string.error_400, Snackbar.LENGTH_SHORT).show();
                        break;
                    default:
                        Snackbar.make(view, getResources().getString(R.string.error) + "(" + code + ")", Snackbar.LENGTH_SHORT).show();
                }
            }
            Snackbar.make(view, getResources().getString(R.string.no_internet), Snackbar.LENGTH_SHORT).show();
        });
        queue.add(episodeRequest);
    }
}
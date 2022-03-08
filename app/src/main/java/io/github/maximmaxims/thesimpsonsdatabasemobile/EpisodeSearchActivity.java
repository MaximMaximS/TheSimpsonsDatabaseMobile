package io.github.maximmaxims.thesimpsonsdatabasemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EpisodeSearchActivity extends AppCompatActivity {

    public static final String NAMERESPONSE = "io.github.maximmaxims.thesimpsonsdatabasemobile.NAMERESPONSE";
    private JSONArray episodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_search);
    }


    public void searchEpisode(View view) {
        // Hide button and show progress indicator
        Button buttonSearchEpisode = findViewById(R.id.buttonSearchEpisode);
        CircularProgressIndicator circularProgressIndicatorSearch = findViewById(R.id.circularProgressIndicatorSearch);
        buttonSearchEpisode.setVisibility(View.INVISIBLE);
        circularProgressIndicatorSearch.setVisibility(View.VISIBLE);

        // Input field
        EditText editTextEpisodeName = findViewById(R.id.editTextEpisodeName);
        // Settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // URL Builder
        String url = preferences.getString("address", "");
        if (url.equals("")) {
            Snackbar.make(view, R.string.no_api, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "search/" + preferences.getString("lang", "en") + "/" + editTextEpisodeName.getText();

        // Request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            // On response:
            // Hide progress and show button
            circularProgressIndicatorSearch.setVisibility(View.INVISIBLE);
            buttonSearchEpisode.setVisibility(View.VISIBLE);

            // Populate spinner
            Spinner spinner = findViewById(R.id.spinnerEpisodes);

            try {
                // Parse response into JSONArray
                episodes = new JSONObject(response).getJSONArray("episodes");
                List<String> names = new ArrayList<>();
                for (int i = 0; i < episodes.length(); i++) {
                    JSONObject namesObject = episodes.optJSONObject(i).optJSONObject("names");
                    if (namesObject != null) {
                        String name = namesObject.optString(preferences.getString("lang", "en"));
                        names.add(name);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinner.setAdapter(adapter);


            } catch (JSONException e) {
                Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }, error -> {
            // On error:
            // Hide progress and show button
            circularProgressIndicatorSearch.setVisibility(View.INVISIBLE);
            buttonSearchEpisode.setVisibility(View.VISIBLE);
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
                    default:
                        Snackbar.make(view, getResources().getString(R.string.error) + "(" + code + ")", Snackbar.LENGTH_SHORT).show();
                }

            } else {
                Snackbar.make(view, getResources().getString(R.string.no_internet), Snackbar.LENGTH_SHORT).show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }

    public void openEpisode(View view) {
        Spinner spinnerEpisodes = findViewById(R.id.spinnerEpisodes);
        int position = spinnerEpisodes.getSelectedItemPosition();
        Intent intent = new Intent(this, EpisodeActivity.class);
        try {
            JSONObject episode = episodes.getJSONObject(position);
            intent.putExtra(EpisodeActivity.EPISODEID, episode.getInt("overallId"));
            startActivity(intent);
        } catch (JSONException e) {
            Snackbar.make(view, R.string.json_error, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
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
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
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

    private JSONArray episodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_search);
    }

    private void enableButtons(boolean enable) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(findViewById(R.id.buttonSearchEpisodeByName));
        buttons.add(findViewById(R.id.buttonSearchEpisodeByNumber));
        buttons.add(findViewById(R.id.buttonOpenEpisodeByName));

        for (Button btn : buttons) {
            btn.setEnabled(enable);
        }
    }

    public void searchEpisodeByName(View view) {
        // Hide button and show progress indicator
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        EditText editTextEpisodeName = findViewById(R.id.editTextEpisodeName);
        Button buttonSearchEpisode = findViewById(R.id.buttonSearchEpisodeByName);
        CircularProgressIndicator circularProgressIndicatorSearch = findViewById(R.id.circularProgressIndicatorSearchByName);

        buttonSearchEpisode.setVisibility(View.INVISIBLE);
        circularProgressIndicatorSearch.setVisibility(View.VISIBLE);
        enableButtons(false);
        // URL Builder
        String url = preferences.getString("address", "");
        if (url.equals("")) {
            Snackbar.make(view, R.string.no_api, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!url.endsWith("/")) {
            url += "/";
        }
        String query = editTextEpisodeName.getText().toString();
        if (query.equals("")) {
            Snackbar.make(view, R.string.not_filled, Snackbar.LENGTH_SHORT).show();
            return;
        }


        url += "search/" + preferences.getString("lang", "en") + "/" + query;

        // Request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            enableButtons(true);
            circularProgressIndicatorSearch.setVisibility(View.INVISIBLE);
            buttonSearchEpisode.setVisibility(View.VISIBLE);
            // On response:

            // Populate spinner
            Spinner spinner = findViewById(R.id.spinnerEpisodes);
            Button buttonSubmit = findViewById(R.id.buttonOpenEpisodeByName);

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
                adapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setVisibility(View.VISIBLE);
                buttonSubmit.setVisibility(View.VISIBLE);


            } catch (JSONException e) {
                Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }, error -> {
            enableButtons(true);
            circularProgressIndicatorSearch.setVisibility(View.INVISIBLE);
            buttonSearchEpisode.setVisibility(View.VISIBLE);
            // On error:

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
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);


    }

    public void openEpisode(View view) {
        Spinner spinnerEpisodes = findViewById(R.id.spinnerEpisodes);
        int position = spinnerEpisodes.getSelectedItemPosition();
        try {
            JSONObject episode = episodes.getJSONObject(position);
            Intent intent = new Intent(this, EpisodeActivity.class);
            intent.putExtra(EpisodeActivity.EPISODEID, episode.getInt("overallId"));
            startActivity(intent);
        } catch (JSONException e) {
            Snackbar.make(view, R.string.json_error, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void searchEpisodeByNumber(View view) {
        Button buttonSearchEpisode = findViewById(R.id.buttonSearchEpisodeByNumber);
        CircularProgressIndicator circularProgressIndicatorSearch = findViewById(R.id.circularProgressIndicatorSearchByNumber);


        buttonSearchEpisode.setVisibility(View.INVISIBLE);
        circularProgressIndicatorSearch.setVisibility(View.VISIBLE);
        enableButtons(false);
        // Get values
        EditText seasonIdField = findViewById(R.id.editTextSeasonNumber);
        EditText episodeIdField = findViewById(R.id.editTextEpisodeNumber);
        if (seasonIdField.getText().toString().equals("") || episodeIdField.getText().toString().equals("")) {
            Snackbar.make(view, R.string.not_filled, Snackbar.LENGTH_SHORT).show();
            return;
        }
        int seasonId = Integer.parseInt(seasonIdField.getText().toString());
        int episodeId = Integer.parseInt(episodeIdField.getText().toString());


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

        url += "id/" + seasonId + "/" + episodeId;
        // Request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            enableButtons(true);
            circularProgressIndicatorSearch.setVisibility(View.INVISIBLE);
            buttonSearchEpisode.setVisibility(View.VISIBLE);
            // On response:
            try {
                // Parse response
                Intent intent = new Intent(this, EpisodeActivity.class);
                intent.putExtra(EpisodeActivity.EPISODEID, new JSONObject(response).getInt("id"));
                startActivity(intent);

            } catch (JSONException e) {
                Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        }, error -> {
            enableButtons(true);
            circularProgressIndicatorSearch.setVisibility(View.INVISIBLE);
            buttonSearchEpisode.setVisibility(View.VISIBLE);
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
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }
}
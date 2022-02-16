package io.github.maximmaxims.thesimpsonsdatabasemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class EpisodeActivity extends AppCompatActivity {

    public static final String EPISODEDETAILS = "io.github.maximmaxims.thesimpsonsdatabasemobile.EPISODEDETAILS";
    private int episodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);

        Intent intent = getIntent();
        episodeId = intent.getIntExtra(EpisodePickerActivity.EPISODE, 0);

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
        String episodeUrl = root + "episode/" + episodeId;

        JsonObjectRequest episodeRequest = new JsonObjectRequest(Request.Method.GET, episodeUrl, null, response -> {
            try {
                JSONObject episode = response.getJSONObject("episode");
                TextView textViewName = findViewById(R.id.textViewName);
                TextView textViewPremiere = findViewById(R.id.textViewPremiere);
                TextView textViewDirection = findViewById(R.id.textViewDirection);
                TextView textViewScreenplay = findViewById(R.id.textViewScreenplay);
                String idString = "S" + String.format(Locale.US, "%02d", episode.getInt("seasonId")) + "E" + String.format(Locale.US, "%02d", episode.getInt("inSeasonId"));
                setTitle(idString);
                textViewName.setText(episode.getJSONObject("names").getString(lang));
                textViewPremiere.setText(episode.getJSONObject("premieres").getString(lang));
                textViewDirection.setText(episode.getString("direction"));
                textViewScreenplay.setText(episode.getString("screenplay"));
            } catch (JSONException e) {
                Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
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
        });
        queue.add(episodeRequest);

        String key = preferences.getString("key", "");
        if (key.equals("")) {
            Snackbar.make(view, R.string.no_key, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String watchedUrl = root + "watched/" + episodeId + "/?api_key=" + key;

        queue.add(getWatched(view, watchedUrl));
    }

    public void markEpisode(View view) {
        SwitchMaterial switchMaterial = findViewById(R.id.switchWatched);
        switchMaterial.setEnabled(false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RequestQueue queue = Volley.newRequestQueue(this);
        boolean action = switchMaterial.isChecked();
        JSONObject request = new JSONObject();
        try {
            request.put("watched", action);
            String root = preferences.getString("address", "");

            if (root.equals("")) {
                Snackbar.make(view, R.string.no_api, Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (!root.endsWith("/")) {
                root += "/";
            }
            String key = preferences.getString("key", "");
            if (key.equals("")) {
                Snackbar.make(view, R.string.no_key, Snackbar.LENGTH_SHORT).show();
                return;
            }
            String watchedUrl = root + "watched/" + episodeId + "/?api_key=" + key;

            JsonObjectRequest postWatched = new JsonObjectRequest(Request.Method.POST, watchedUrl, request, response -> {
                try {
                    boolean watched = response.getBoolean("watched");
                    switchMaterial.setEnabled(true);
                    switchMaterial.setChecked(watched);
                } catch (JSONException e) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }, error -> {
                int code = error.networkResponse.statusCode;
                switch (code) {
                    case 404:
                        Snackbar.make(view, R.string.error_404, Snackbar.LENGTH_SHORT).show();
                        break;
                    case 500:
                        Snackbar.make(view, R.string.error_500, Snackbar.LENGTH_SHORT).show();
                        break;
                    case 401:
                        Snackbar.make(view, R.string.error_401, Snackbar.LENGTH_SHORT).show();
                        break;
                    case 400:
                        Snackbar.make(view, R.string.error_400, Snackbar.LENGTH_SHORT).show();
                        break;
                    default:
                        Snackbar.make(view, getResources().getString(R.string.error) + "(" + code + ")", Snackbar.LENGTH_SHORT).show();
                }
                queue.add(getWatched(view, watchedUrl));
            });
            queue.add(postWatched);
        } catch (JSONException e) {
            Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Contract("_, _ -> new")
    private @NotNull JsonObjectRequest getWatched(View view, String watchedUrl) {
        return new JsonObjectRequest(Request.Method.GET, watchedUrl, null, response -> {
            try {
                boolean watched = response.getBoolean("watched");
                SwitchMaterial switchMaterial = findViewById(R.id.switchWatched);
                switchMaterial.setEnabled(true);
                switchMaterial.setChecked(watched);
            } catch (JSONException e) {
                Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }, error -> {
            int code = error.networkResponse.statusCode;
            switch (code) {
                case 404:
                    Snackbar.make(view, R.string.error_404, Snackbar.LENGTH_SHORT).show();
                    break;
                case 500:
                    Snackbar.make(view, R.string.error_500, Snackbar.LENGTH_SHORT).show();
                    break;
                case 401:
                    Snackbar.make(view, R.string.error_401, Snackbar.LENGTH_SHORT).show();
                    break;
                default:
                    Snackbar.make(view, getResources().getString(R.string.error) + "(" + code + ")", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void openDetails(View view) {
        Intent intent = new Intent(this, EpisodeDetailsActivity.class);

        intent.putExtra(EPISODEDETAILS, getIntent().getIntExtra(EpisodePickerActivity.EPISODE, 0));
        startActivity(intent);
    }

}
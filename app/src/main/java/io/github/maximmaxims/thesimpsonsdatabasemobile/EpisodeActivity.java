package io.github.maximmaxims.thesimpsonsdatabasemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class EpisodeActivity extends AppCompatActivity {
    public static final String EPISODEID = "io.github.maximmaxims.thesimpsonsdatabasemobile.EPISODEID";
    private int episodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);

        Intent intent = getIntent();
        episodeId = intent.getIntExtra(EpisodeActivity.EPISODEID, 0);

        updateData(findViewById(android.R.id.content));
    }

    private void enableButtons(boolean enable) {
        if (!enable) {
            findViewById(R.id.switchWatched).setEnabled(false);
        }
        findViewById(R.id.buttonPrevious).setEnabled(enable);
        findViewById(R.id.buttonNext).setEnabled(enable);
        findViewById(R.id.textViewEpisodeId).setEnabled(enable);
        findViewById(R.id.buttonDetails).setEnabled(enable);
    }

    public void updateData(View view) {

        enableButtons(false);
        RequestQueue queue = Volley.newRequestQueue(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = preferences.getString("lang", "en");
        String root2 = preferences.getString("address", "");

        if (root2.equals("")) {
            Snackbar.make(view, R.string.no_api, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();
            return;
        }
        if (!root2.endsWith("/")) {
            root2 += "/";
        }
        final String root = root2;
        String episodeUrl = root + "episode/" + episodeId;

        JsonObjectRequest episodeRequest = new JsonObjectRequest(Request.Method.GET, episodeUrl, null, response -> {
            // On response:
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
                Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();
                e.printStackTrace();
                enableButtons(true);
                return;
            }
            String key = preferences.getString("key", "");
            if (key.equals("")) {
                Snackbar.make(view, R.string.no_key, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();
                enableButtons(true);
                return;
            }
            String watchedUrl = root + "watched/" + episodeId + "/?api_key=" + key;

            queue.add(getWatched(view, watchedUrl));
            enableButtons(true);
            TextView epId = findViewById(R.id.textViewEpisodeId);
            epId.setText(String.valueOf(episodeId));
        }, error -> {
            // On error:
            // If network response was obtained
            enableButtons(true);
            String errMessage;
            if (error.networkResponse != null) {
                switch (error.networkResponse.statusCode) {
                    case 429:
                        errMessage = getResources().getString(R.string.error_429);
                        break;
                    case 404:
                        // errMessage = getResources().getString(R.string.error_404);
                        episodeId--;
                        updateData(view);
                        return;
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
            Snackbar.make(view, errMessage, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).setAnchorView(R.id.buttonPrevious).show();
        });

        queue.add(episodeRequest);


    }

    public void markEpisode(View view) {
        enableButtons(false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RequestQueue queue = Volley.newRequestQueue(this);
        SwitchMaterial switchMaterial = findViewById(R.id.switchWatched);
        boolean action = switchMaterial.isChecked();
        JSONObject request = new JSONObject();
        try {
            request.put("watched", action);
            String root = preferences.getString("address", "");

            if (root.equals("")) {
                Snackbar.make(view, R.string.no_api, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();
                enableButtons(true);
                return;
            }
            if (!root.endsWith("/")) {
                root += "/";
            }
            String key = preferences.getString("key", "");
            if (key.equals("")) {
                Snackbar.make(view, R.string.no_key, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();
                enableButtons(true);
                return;
            }
            String watchedUrl = root + "watched/" + episodeId + "/?api_key=" + key;

            JsonObjectRequest postWatched = new JsonObjectRequest(Request.Method.POST, watchedUrl, request, response -> {
                // On response:
                try {
                    boolean watched = response.getBoolean("watched");
                    enableButtons(true);
                    findViewById(R.id.switchWatched).setEnabled(true);
                    switchMaterial.setChecked(watched);
                } catch (JSONException e) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();
                    e.printStackTrace();
                }
            }, error -> {
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
                        case 401:
                            errMessage = getResources().getString(R.string.error_401);
                            break;
                        case 400:
                            errMessage = getResources().getString(R.string.error_400);
                            break;
                        default:
                            errMessage = getResources().getString(R.string.error) + " (" + error.networkResponse.statusCode + ")";

                    }
                    queue.add(getWatched(view, watchedUrl));
                } else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    errMessage = getResources().getString(R.string.no_internet);
                } else {
                    errMessage = error.getMessage();
                    if (errMessage == null) {
                        errMessage = getResources().getString(R.string.error);
                    }
                }
                Snackbar.make(view, errMessage, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();

            });
            queue.add(postWatched);
        } catch (JSONException e) {
            Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();
            e.printStackTrace();
        }
    }

    private @NotNull JsonObjectRequest getWatched(View view, String watchedUrl) {
        return new JsonObjectRequest(Request.Method.GET, watchedUrl, null, response -> {
            // On response:
            try {
                boolean watched = response.getBoolean("watched");
                SwitchMaterial switchMaterial = findViewById(R.id.switchWatched);
                findViewById(R.id.switchWatched).setEnabled(true);
                enableButtons(true);
                switchMaterial.setChecked(watched);
            } catch (JSONException e) {
                Snackbar.make(findViewById(android.R.id.content), R.string.json_error, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();
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
                    case 401:
                        errMessage = getResources().getString(R.string.error_401);
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
            Snackbar.make(view, errMessage, Snackbar.LENGTH_SHORT).setAnchorView(R.id.buttonPrevious).show();


        });
    }

    public void openDetails(View view) {
        Intent intent = new Intent(this, EpisodeDetailsActivity.class);

        intent.putExtra(EPISODEID, episodeId);
        startActivity(intent);
    }

    public void openPrevious(View view) {
        if (episodeId > 1) {
            episodeId--;
            updateData(view);
        }

    }

    public void openNext(View view) {
        episodeId++;
        updateData(view);
    }

}
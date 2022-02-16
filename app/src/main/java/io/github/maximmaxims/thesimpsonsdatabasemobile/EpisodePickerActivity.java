package io.github.maximmaxims.thesimpsonsdatabasemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EpisodePickerActivity extends AppCompatActivity {
    public static final String EPISODE = "io.github.maximmaxims.thesimpsonsdatabasemobile.EPISODE";
    private JSONArray episodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_picker);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Spinner spinner = findViewById(R.id.spinnerEpisodes);
        Intent intent = getIntent();
        String response = intent.getStringExtra(EpisodeSearchActivity.NAMERESPONSE);

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
    }

    public void openEpisode(View view) {
        Spinner spinnerEpisodes = findViewById(R.id.spinnerEpisodes);
        int position = spinnerEpisodes.getSelectedItemPosition();
        Intent intent = new Intent(this, EpisodeActivity.class);
        try {
            JSONObject episode = episodes.getJSONObject(position);
            intent.putExtra(EPISODE, episode.getInt("overallId"));
            startActivity(intent);
        } catch (JSONException e) {
            Snackbar.make(view, R.string.json_error, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }


    }
}
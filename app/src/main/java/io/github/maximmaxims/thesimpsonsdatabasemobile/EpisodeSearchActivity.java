package io.github.maximmaxims.thesimpsonsdatabasemobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

public class EpisodeSearchActivity extends AppCompatActivity {

    public static final String NAMERESPONSE = "io.github.maximmaxims.thesimpsonsdatabasemobile.NAMERESPONSE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_search);
    }


    public void searchEpisode(View view) {
        EditText editTextEpisodeName = findViewById(R.id.editTextEpisodeName);

        RequestQueue queue = Volley.newRequestQueue(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String url = preferences.getString("address", "");
        if (url.equals("")) {
            Snackbar.make(view, R.string.no_api, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!url.endsWith("/")) {
            url += "/";
        }

        url += "search/" + preferences.getString("lang", "en") + "/" + editTextEpisodeName.getText();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Intent intent = new Intent(this, EpisodePickerActivity.class);
            intent.putExtra(NAMERESPONSE, response);
            startActivity(intent);

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

        queue.add(stringRequest);

    }
}
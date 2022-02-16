package io.github.maximmaxims.thesimpsonsdatabasemobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void openEpisodeSearch(View view) {
        Intent intent = new Intent(this, EpisodeSearchActivity.class);
        startActivity(intent);
    }

    public void openPreferences(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
package com.example.newplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST = 1;
    ArrayList<String> arrayList;
    ArrayList<Uri> contentList;
    ArrayList<Map> songInfoList;
    ListView listView;
    MediaPlayer mediaPlayer;
    ArrayAdapter<String> arrayAdapter;
    private int playedPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        }
        else {
            executeTask();
        }
    }

    public void executeTask() {
        listView = (ListView) findViewById(R.id.listView);
        arrayList = new ArrayList<>();
        contentList = new ArrayList<>();
        songInfoList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
        getMusic();
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mediaPlayer.isPlaying()) {
                    if (position == playedPosition)
                        mediaPlayer.pause();
                    else
                        mediaPlayer.release();
                } else {
                    if (position == playedPosition) {
                        mediaPlayer.start();
                    }
                }
                if (playedPosition != position) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(),contentList.get(position));
                    mediaPlayer.start();
                    playedPosition = position;
                    updateSongInfo();
                }
            }
        });
    }
    public void updateSongInfo() {
        TextView songTitle = (TextView) this.findViewById(R.id.titleText);
        TextView artistName = (TextView) this.findViewById(R.id.artistText);
        songTitle.setText(songInfoList.get(playedPosition).get("title").toString());
        artistName.setText(songInfoList.get(playedPosition).get("artist").toString());
        ImageButton playButton = (ImageButton) this.findViewById(R.id.playButton);
        if (mediaPlayer.isPlaying()) {
            playButton.setImageResource(R.drawable.pause);
        }
        else
            playButton.setImageResource(R.drawable.play);

    }
    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri,null,null,null,null);
        if( songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int itemID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);

            do {
                String currentID = songCursor.getString(itemID);
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                String currentLocation = songCursor.getString(songLocation);
                Map songInfo = new HashMap();
                songInfo.put("id",currentID);
                songInfo.put("title",currentTitle);
                songInfo.put("artist",currentArtist);
                songInfo.put("location",currentLocation);
                contentList.add(Uri.parse(MediaStore.Audio.Media.getContentUriForPath(currentLocation).toString() + "/" + currentID));
                songInfoList.add(songInfo);
                arrayList.add(currentTitle + "\n" + currentArtist);
            } while (songCursor.moveToNext());
        }
        songCursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this,"Permission granted", Toast.LENGTH_SHORT).show();
                        executeTask();
                    }
                } else {
                    Toast.makeText(this,"Permission not granted",Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    public void prevPressed(View view) {
        if (playedPosition >= 1) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), contentList.get(--playedPosition));
            mediaPlayer.start();
            updateSongInfo();
        }
    }

    public void playPressed(View view) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            else {
                mediaPlayer.start();
            }
            updateSongInfo();
        }

    public void nextPressed(View view) {
        if (playedPosition < contentList.size()) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), contentList.get(++playedPosition));
            mediaPlayer.start();
            updateSongInfo();
        }
    }

    class PlayerTask extends AsyncTask<String,Void,Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            return null;
        }
    }
}

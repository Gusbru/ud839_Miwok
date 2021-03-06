/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.miwok;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class NumbersActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private AudioManager am;
    private AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {

                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                            focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                        // Pause playback because your Audio Focus was
                        // temporarily stolen, but will be back soon.
                        // i.e. for a phone call
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(0);
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        // Stop playback, because you lost the Audio Focus.
                        // i.e. the user started some other playback app
                        // Remember to unregister your controls/buttons here.
                        // And release the kra — Audio Focus!
                        // You’re done.
                        releaseMediaPlayer();
                    }  else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        // Resume playback, because you hold the Audio Focus
                        // again!
                        // i.e. the phone call ended or the nav directions
                        // are finished
                        // If you implement ducking and lower the volume, be
                        // sure to return it to normal here, as well.
                        mediaPlayer.start();
                    }
                }
            };


    /**
     * This listener gets triggered when the {@link MediaPlayer} has completed
     * playing the audio file
     */
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numbers);

        // create and setup the {@link AudioManager} to request audio focus
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // create an ArrayList of the Word type
        final ArrayList<Word> words = new ArrayList<>();

        // populate the words ArrayList
        // TODO: this is going to be sync using Firebase
        words.add(new Word("one", "lutti", R.drawable.number_one, R.raw.number_one));
        words.add(new Word("two", "otiiko", R.drawable.number_two, R.raw.number_two));
        words.add(new Word("three", "tolookosu", R.drawable.number_three, R.raw.number_three));
        words.add(new Word("four", "oyyisa", R.drawable.number_four, R.raw.number_four));
        words.add(new Word("five", "massokka", R.drawable.number_five));
        words.add(new Word("six", "temmokka", R.drawable.number_six));
        words.add(new Word("seven", "kenekaku", R.drawable.number_seven));
        words.add(new Word("eight", "kawinta", R.drawable.number_eight));
        words.add(new Word("nine", "wo'e", R.drawable.number_nine));
        words.add(new Word("ten", "na'aacha", R.drawable.number_ten));

        // link to ListView on Numbers Activity
        ListView listView = (ListView) findViewById(R.id.listViewNumbers);

        // create a custom ArrayAdapter to treat the word objects
        WordAdapter textAdapter = new WordAdapter(this, words, R.color.category_numbers);

        // attach the adapter previous created to put the word objects into
        // the ListView
        listView.setAdapter(textAdapter);

        // action on click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (words.get(position).hasSound()) {

                    // release the media player if it currently exists
                    releaseMediaPlayer();

                    // request audio focus for playback
                    int result = am.requestAudioFocus(
                            afChangeListener,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        // create and setup the {@link MediaPlayer} for the audio resource
                        // associate with the current word
                        mediaPlayer = MediaPlayer.create(getApplicationContext(), words.get(position).getmSoundResourceId());

                        // start play the sound
                        mediaPlayer.start();

                        // Setup a listener on the media player, so that we can stop and
                        // release the media player once the sounds has finished.
                        mediaPlayer.setOnCompletionListener(mCompletionListener);
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "No sound available", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    /**
     * Clean up the media player by releasing its resource
     */
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;

            // After finish the sound, abandon the audio focus
            am.abandonAudioFocus(afChangeListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

}
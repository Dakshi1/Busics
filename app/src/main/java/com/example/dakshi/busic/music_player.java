package com.example.dakshi.busic;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.dakshi.busic.MainActivity.currentWindow;
import static com.example.dakshi.busic.MainActivity.playWhenReady;
import static com.example.dakshi.busic.MainActivity.playbackPosition;
import static com.example.dakshi.busic.MainActivity.player;
import static com.example.dakshi.busic.MainActivity.playerView;

/**
 * Created by dakshi on 3/3/18.
 */

public class music_player {

    static int CHECK_SONG=0;
    static int ADD_SONG=1;
    static Context mcontext;
    static ArrayList<String> m_audio_link;
    static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    static FirebaseUser currentUser = mAuth.getCurrentUser();
    static String arr[]=currentUser.toString().split("@");
    static boolean like=false;

    music_player(Context mcontext, ArrayList<String> m_audio_link)
    {
        this.mcontext=mcontext;
        this.m_audio_link=m_audio_link;
    }

    music_player(Context mcontext)
    {
        this.mcontext=mcontext;
    }

    public static void initializePlayer()
    {
        if(m_audio_link!=null) {

            player = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(mcontext),
                    new DefaultTrackSelector(), new DefaultLoadControl());


            player.addListener(new Player.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest) {

                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                    MainActivity.fab.setImageResource(R.drawable.like);
                    checkSongInDatabase(CHECK_SONG);
                }

                @Override
                public void onLoadingChanged(boolean isLoading) {

                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {

                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {

                }

                @Override
                public void onPositionDiscontinuity(int reason) {

                }

                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                }

                @Override
                public void onSeekProcessed() {

                }
            });
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
            MediaSource mediaSource = buildMediaSource(m_audio_link);
            player.prepare(mediaSource, true, false);
        }
    }

    private static MediaSource buildMediaSource(ArrayList<String> audio_link) {

        ExtractorMediaSource audioSource[]=new ExtractorMediaSource[audio_link.size()];
        Uri uri;
        for(int i=0;i<audio_link.size();i++)
        {
            uri=Uri.parse(audio_link.get(i));
            audioSource[i] =
                    new ExtractorMediaSource.Factory(
                            new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                            createMediaSource(uri);
        }

        return new ConcatenatingMediaSource(audioSource);
    }

    public static void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    public static boolean checkSongInDatabase(final int id)
    {
        String key=arr[1];
        final String song_id=m_audio_link.get(player.getCurrentWindowIndex());
        final SongDetails newSong=new SongDetails(song_id, "true");

        final DatabaseReference familyListReference = FirebaseDatabase.getInstance().getReference().child(key);
        familyListReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                boolean found=false;
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    //String key = (String) ds.getKey();
                    HashMap<String, String> songDetails=(HashMap<String, String>) ds.getValue();
                    if(songDetails.get("song_id").equals(song_id) && songDetails.get("like").equals("true") && id==CHECK_SONG)
                    {
                        MainActivity.fab.setImageResource(R.drawable.filled_like);
                        found=true;
                        break;
                    }
                    if(songDetails.get("song_id").equals(song_id) && songDetails.get("like").equals("true") && id==ADD_SONG)
                    {
                        MainActivity.fab.setImageResource(R.drawable.like);
                        familyListReference.child(ds.getKey()).setValue(new SongDetails(song_id, "false"));
                        found=true;
                        break;
                    }
                    if(songDetails.get("song_id").equals(song_id) && !songDetails.get("like").equals("true") && id==ADD_SONG)
                    {
                        MainActivity.fab.setImageResource(R.drawable.filled_like);
                        familyListReference.child(ds.getKey()).setValue(new SongDetails(song_id, "true"));
                        found=true;
                        break;
                    }

                }
                if(!found && id==ADD_SONG)
                {
                    familyListReference.push().setValue(newSong);
                    MainActivity.fab.setImageResource(R.drawable.filled_like);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TAG", "Read failed");
            }
        });

        return false;
    }

    static class SongDetails
    {
        String song_id;
        String like="false";

        public SongDetails() {
        }

        public SongDetails(String song_id, String like) {
            this.song_id = song_id;
            this.like = like;
        }

        public String getSong_id() {
            return song_id;
        }

        public void setSong_id(String song_id) {
            this.song_id = song_id;
        }

        public String isLike() {
            return like;
        }

        public void setLike(String like) {
            this.like = like;
        }
    }

}

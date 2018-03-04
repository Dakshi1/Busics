package com.example.dakshi.busic;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.util.ArrayList;

import static com.example.dakshi.busic.MainActivity.currentWindow;
import static com.example.dakshi.busic.MainActivity.playWhenReady;
import static com.example.dakshi.busic.MainActivity.playbackPosition;
import static com.example.dakshi.busic.MainActivity.player;
import static com.example.dakshi.busic.MainActivity.playerView;

/**
 * Created by dakshi on 3/3/18.
 */

public class music_player {

    static Context mcontext;
    static ArrayList<String> m_audio_link;

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

}

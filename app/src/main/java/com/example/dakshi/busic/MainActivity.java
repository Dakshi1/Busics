package com.example.dakshi.busic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;


import dk.nodes.filepicker.FilePickerActivity;
import dk.nodes.filepicker.FilePickerConstants;
import dk.nodes.filepicker.uriHelper.FilePickerUriHelper;


import static dk.nodes.filepicker.FilePickerConstants.RESULT_CODE_FAILURE;

@TargetApi(21)
public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 1317;
    PDFView pdfView;
    String selected_file;
    String path;
    SharedPreferences preferences;
    Gson gson;
    Type type;
    File file;
    int page_num;
    int page_to_play_music=-1;
    PdfReader reader;
    Toolbar tb;
    // exoplayer
    static Context mcontext;
    static ArrayList<String> m_audio_link;
    static SimpleExoPlayerView playerView;
    static SimpleExoPlayer player;
    private static boolean playWhenReady=true;
    static long playbackPosition=0;
    static int currentWindow=0;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing player
        playerView=findViewById(R.id.video_view);
        playerView.setControllerShowTimeoutMs(0);
        playerView.setUseArtwork(false);

        //setting customized actionbar
        tb=findViewById(R.id.toolbar);
        tb.setBackgroundColor(Color.BLUE);
        tb.setTitle("Welcome to Busics");
        setSupportActionBar(tb);
        // initializing main pdf view
        pdfView = findViewById(R.id.pdfView);
        preferences= PreferenceManager.getDefaultSharedPreferences(this);
        gson=new Gson();
        type=new TypeToken<File>(){}.getType();
        // retrieving saved file
        selected_file=preferences.getString("file",null);
        page_num=preferences.getInt("number",1);
        page_to_play_music=preferences.getInt("play_from",-1);
        path=preferences.getString("path",null);
        // retrieve file object from file path
        if(selected_file!=null)
        {
            file=gson.fromJson(selected_file,type);
            try {
                reader = new PdfReader(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            load_pdf();
        }
    }

    public void selectPdf()
    {
        Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
        intent.putExtra(FilePickerConstants.FILE, true);
        intent.putExtra(FilePickerConstants.TYPE, FilePickerConstants.MIME_PDF);
        page_num=0;
        startActivityForResult(intent, MY_REQUEST_CODE);
    }
    public static void initializePlayer(Context context, ArrayList<String> audio_link)
    {
        mcontext=context;
        m_audio_link=audio_link;
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(context),
                new DefaultTrackSelector(), new DefaultLoadControl());
        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        MediaSource mediaSource = buildMediaSource(audio_link);
        player.prepare(mediaSource, true, false);
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

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }
    public void load_pdf()
    {
        pdfView.fromFile(file)
                .defaultPage(page_num)
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .pageFitPolicy(FitPolicy.BOTH)
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount)
                    {
                        page_num=page;
                        if(page_to_play_music!=-1) {
                            pdfToString();
                            releasePlayer();
                        }
                        //Toast.makeText(MainActivity.this, ""+page+" "+pageCount, Toast.LENGTH_SHORT).show();
                    }
                })
                .load();
    }
    // result of selected pdf file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Log.d("path", FilePickerUriHelper.getUriString(data));
               // pdfToString(FilePickerUriHelper.getUriString(data));
                path=FilePickerUriHelper.getUriString(data);
                try {
                    reader = new PdfReader(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //selected_file=FilePickerUriHelper.getUriString(data);
                file = FilePickerUriHelper.getFile(MainActivity.this, data);
                selected_file=gson.toJson(file,type);
                load_pdf();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(MainActivity.this, "User Canceled", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == RESULT_CODE_FAILURE)
            {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //super.onConfigurationChanged(newConfig);

    }

    public void pdfToString()
    {

        final int page=page_num;
        String parsedText="";
        try {

            for (int i = page; i <page+1 ; i++) {
                parsedText   = parsedText+ PdfTextExtractor.getTextFromPage(reader, page_num+1).trim()+"\n"; //Extracting the content from the different pages
            }
            Log.d("Text of document",parsedText);
            //Toast.makeText(this, ""+parsedText, Toast.LENGTH_SHORT).show();
            //System.out.println(parsedText);
            //reader.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        sendText(parsedText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        saveDatatoSP();
        super.onSaveInstanceState(outState);
    }

    private void saveDatatoSP() {

        if(selected_file!=null)
        {
            preferences.edit().putString("path",path).apply();
            preferences.edit().putString("file",selected_file).apply();
            preferences.edit().putInt("number",page_num).apply();
            preferences.edit().putInt("play_from",page_to_play_music).apply();
            //Toast.makeText(this, ""+preferences.getString("file_path",null)+"  "+preferences.getInt("number",1), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        tb.inflateMenu(R.menu.app_menu);
        tb.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onOptionsItemSelected(item);
                    }
                });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_choose:
                selectPdf();
                break;
            // action with ID action_settings was selected
            case R.id.action_close:
                saveDatatoSP();
                finish();
                break;
            case R.id.action_refresh:
                if(page_to_play_music==-1)
                {
                    if (Util.SDK_INT > 23 ) {
                        //initializePlayer(mcontext,m_audio_link);
                        pdfToString();

                    }
                    page_to_play_music=page_num;
                }
                else
                    Toast.makeText(this, "Document already analysed", Toast.LENGTH_SHORT).show();
            default:
                break;
        }
        return true;
    }

    private void sendText(String text) {

        Toast.makeText(MainActivity.this, "sendText", Toast.LENGTH_SHORT).show();
        new ClassifyText(MainActivity.this).execute(text);
    }

    @Override
    protected void onDestroy() {
        if(selected_file!=null)
        {
            preferences.edit().putString("path",path).apply();
            preferences.edit().putString("file",selected_file).apply();
            preferences.edit().putInt("number",page_num).apply();
            preferences.edit().putInt("play_from",page_to_play_music).apply();
            //Toast.makeText(this, ""+preferences.getString("file_path",null)+"  "+preferences.getInt("number",1), Toast.LENGTH_SHORT).show();
        }
        reader.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null) && mcontext!=null) {
            initializePlayer(mcontext, m_audio_link);
        }
    }
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
}

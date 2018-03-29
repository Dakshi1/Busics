package com.example.dakshi.busic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;


import dk.nodes.filepicker.FilePickerActivity;
import dk.nodes.filepicker.FilePickerConstants;
import dk.nodes.filepicker.uriHelper.FilePickerUriHelper;


import static dk.nodes.filepicker.FilePickerConstants.RESULT_CODE_FAILURE;

@TargetApi(23)
public class MainActivity extends AppCompatActivity{

    private static final int MY_REQUEST_CODE = 1317;
    private static final int SPLASH_TIME_OUT = 4000;
    PDFView pdfView;
    String selected_file;
    String path;
    SharedPreferences preferences;
    Gson gson;
    Type type;
    File file;
    static int page_num;
    static MenuItem menuItem;
    static int page_to_play_music=-1;
    Toolbar tb;
    // exoplayer
    static SimpleExoPlayerView playerView;
    static SimpleExoPlayer player;
    static boolean playWhenReady=true;
    static long playbackPosition=0;
    static int currentWindow=0;
    static Handler handler;
    static Runnable runnable;
    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=MainActivity.this;
        handler=new Handler();

        music_player mplyr= new music_player(MainActivity.this, new ArrayList<String>());

        // initializing player
        playerView=findViewById(R.id.video_view);
        playerView.setControllerShowTimeoutMs(0);
        playerView.setUseArtwork(false);
        music_player.initializePlayer();

        //setting customized actionbar
        tb=findViewById(R.id.toolbar);
        tb.setBackgroundColor(Color.parseColor("#3b5998"));
        tb.setTitle("Enjoy Busics");
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
            new ExtractText(path,this).execute();
            file=gson.fromJson(selected_file,type);
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
    public void load_pdf()
    {
        Toast.makeText(this, "Document analysis started...", Toast.LENGTH_SHORT).show();

        pdfView.fromFile(file)
                .defaultPage(page_num)
                .enableDoubletap(true)
                .pageFitPolicy(FitPolicy.WIDTH)
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(final int page, int pageCount)
                    {
                        if(runnable!=null)
                            handler.removeCallbacks(runnable);
                        check_n_play();
                        page_num = page;
                    }
                })
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        pdfView.setVisibility(View.VISIBLE);
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
               // pdfToString(FilePickerUriHelper.getUriString(data));
                path=FilePickerUriHelper.getUriString(data);
                if(ExtractText.reader!=null)
                {
                    ExtractText.reader.close();
                }
                new ExtractText(path,this).execute();
                music_player.releasePlayer();
                page_to_play_music=-1;
                music_player.m_audio_link=null;
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
    public static void getResponse()
    {

    }

    public static void check_n_play()
    {
        if(page_to_play_music!=-1 && page_num>=page_to_play_music && check())
        {

            if (ExtractText.analysed)
            {
                runnable = new Runnable() {
                    @Override
                    public void run() {

                        //Toast.makeText(MainActivity.this, "called", Toast.LENGTH_SHORT).show();
                        menuItem.setActionView(R.layout.progress);
                        music_player.releasePlayer();
                        music_player.m_audio_link = null;
                        pdfToString();
                    }

                };
                handler.postDelayed(runnable, SPLASH_TIME_OUT);
            }
        }
    }
    public static void pdfToString()
    {
        String parsedText="";
        try {
            parsedText   = parsedText+ PdfTextExtractor.getTextFromPage(ExtractText.reader, page_num+1).trim()+"\n"; //Extracting the content from the different pages
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
        menuItem=menu.findItem(R.id.action_refresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_choose:
                music_player.releasePlayer();
                selectPdf();
                break;
            // action with ID action_settings was selected
            case R.id.action_close:
                saveDatatoSP();
                finish();
                break;
            case R.id.action_refresh:

                if(check())
                {
                    if(ExtractText.analysed)
                    {
                        if(file!=null) {
                            if(page_to_play_music==-1) {
                                item.setActionView(R.layout.progress);
                                music_player.releasePlayer();
                                music_player.m_audio_link = null;
                                pdfToString();
                                page_to_play_music = page_num;
                            }
                            Toast.makeText(this, "Page to play music is already set, to modify it again load the document ", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(this, "Load a pdf...", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this, "Document analysis in process...", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this, "Action requires Internet Connection", Toast.LENGTH_SHORT).show();
            default:
                break;
        }
        return true;
    }


    private static void sendText(String text) {

        if(check())
            new ClassifyText(context,menuItem).execute(text);
        else
            Toast.makeText(context, "Action requires Internet Connection", Toast.LENGTH_SHORT).show();
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
        if(ExtractText.reader!=null)
            ExtractText.reader.close();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            music_player.releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            music_player.releasePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null) && music_player.mcontext!=null) {
            music_player.initializePlayer();
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




    public static boolean check() {
        ConnectivityManager
                cm;
        Context xcontext=music_player.mcontext;
        cm = (ConnectivityManager)xcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }



}

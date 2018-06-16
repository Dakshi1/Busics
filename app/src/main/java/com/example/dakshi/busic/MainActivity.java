package com.example.dakshi.busic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;


import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;
import dk.nodes.filepicker.FilePickerActivity;
import dk.nodes.filepicker.FilePickerConstants;
import dk.nodes.filepicker.uriHelper.FilePickerUriHelper;


import static dk.nodes.filepicker.FilePickerConstants.RESULT_CODE_FAILURE;

@TargetApi(23)
public class MainActivity extends AppCompatActivity{

    private static int volume;
    int views_id=0;
    View[] views=new View[3];
    static ToneAnalyzer service;
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
    boolean like=false;
    static FloatingActionButton fab;
    TextView textView;
    static AudioManager audioManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        service = new ToneAnalyzer("2017-09-21");
        service.setUsernameAndPassword("12abdb43-b9be-4b1d-a94f-2a2ec19bf426", "2dR0qaH2B5bs");

        textView=findViewById(R.id.textview);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                music_player.checkSongInDatabase(music_player.ADD_SONG);
            }
        });

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
        views[0]=tb;
        views[1]=textView;
        views[2]=playerView;
        createOverlayOverView("select and load PDF buttons to sync the book");
        preferences= PreferenceManager.getDefaultSharedPreferences(this);
        gson=new Gson();
        type=new TypeToken<File>(){}.getType();
        // retrieving saved file
        selected_file=preferences.getString("file",null);

        // retrieve file object from file path
        if(selected_file!=null)
        {
            page_num=preferences.getInt("number",1);
            page_to_play_music=preferences.getInt("play_from",-1);
            path=preferences.getString("path",null);
            new ExtractText(path,this).execute();
            file=gson.fromJson(selected_file,type);
            load_pdf();
        }
        else
        {
            AssetManager assetManager = getAssets();
            try {
                InputStream is = assetManager.open("intro.pdf");
                pdfView.fromStream(is)
                        .load();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

    public static void check_n_play()
    {
        MainActivity.fab.setImageResource(R.drawable.like);
        if (player!=null)
            volume=audioManager.getStreamVolume(player.getAudioStreamType());
        if(page_to_play_music!=-1 && page_num>=page_to_play_music && check())
        {

            if (ExtractText.analysed)
            {
                /*Handler xhandler=new Handler();
                Runnable xrunnable=new Runnable() {
                    @Override
                    public void run() {

                        if(player!=null && audioManager.getStreamVolume(player.getAudioStreamType())>0)
                            audioManager.setStreamVolume(player.getAudioStreamType(), audioManager.getStreamVolume(player.getAudioStreamType())-1,0);
                    }
                };
                xhandler.postAtTime(xrunnable,System.currentTimeMillis()+1000);
                xhandler.postDelayed(xrunnable,1000);*/
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

    private void createOverlayOverView(String infotext) {


        new MaterialIntroView.Builder(this)
                .setMaskColor(R.color.app_light)
                .enableDotAnimation(true)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.MINIMUM)
                .setDelayMillis(200)
                .enableFadeAnimation(true)
                .performClick(true)
                .setInfoText(infotext)
                .setShape(ShapeType.RECTANGLE)
                .setTarget(views[views_id])
                .setUsageId(""+views_id)
                .setListener(new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String s) {

                        views_id++;
                        if(views_id==1){
                            createOverlayOverView("content of the PDF file");
                        }
                        else if (views_id==2)
                        {
                            createOverlayOverView("music player to play the instrumental songs");
                        }
                        else
                            textView.setVisibility(View.GONE);

                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_signout:
                FirebaseAuth.getInstance().signOut();
                music_player.m_audio_link = null;
                music_player.releasePlayer();
                finish();
                Intent i=new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                break;
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
                            else
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

        Log.d("category_text", text);
        if(check())
        {
            new ExtractEmotion(context,menuItem)
                    .execute(text);

            //new ClassifyText(context,menuItem).execute(text);
        }
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

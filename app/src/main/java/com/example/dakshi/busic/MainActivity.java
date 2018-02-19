package com.example.dakshi.busic;

import android.annotation.TargetApi;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import dk.nodes.filepicker.FilePickerActivity;
import dk.nodes.filepicker.FilePickerConstants;
import dk.nodes.filepicker.uriHelper.FilePickerUriHelper;
import nl.changer.audiowife.AudioWife;

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
    PdfReader reader;
    Toolbar tb;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayout=findViewById(R.id.child_rel_view);


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
           // Toast.makeText(this, "not null", Toast.LENGTH_SHORT).show();
            load_pdf();
        }
        // adding audio player
        new LongOperation().execute("");
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
                        pdfToString();
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

    public void pdfToString()
    {
        try {
            String parsedText="";
            for (int i = 0; i <1 ; i++) {
                parsedText   = parsedText+ PdfTextExtractor.getTextFromPage(reader, page_num+1).trim()+"\n"; //Extracting the content from the different pages
            }
            Log.d("Text of document",parsedText);
            //Toast.makeText(this, ""+parsedText, Toast.LENGTH_SHORT).show();
            //System.out.println(parsedText);
            //reader.close();
        } catch (Exception e) {
            System.out.println(e);
        }
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
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if(selected_file!=null)
        {
            preferences.edit().putString("path",path).apply();
            preferences.edit().putString("file",selected_file).apply();
            preferences.edit().putInt("number",page_num).apply();
            //Toast.makeText(this, ""+preferences.getString("file_path",null)+"  "+preferences.getInt("number",1), Toast.LENGTH_SHORT).show();
        }
        reader.close();
        super.onDestroy();
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        Uri uri;
        @Override
        protected String doInBackground(String... params) {

            /*AudioWife.getInstance().init(MainActivity.this, Uri.parse("https://dl.jatt.link/lq.jatt.link/cdn8/83cb553572dd8f1dfaf9f91b7dc2a0b9/bvlzv/Daru%20Badnaam-(Mr-Jatt.com).mp3"))
                    .useDefaultUi(relativeLayout, getLayoutInflater());*/
            //uri=Uri.parse("https://dl.jatt.link/lq.jatt.link/cdn8/83cb553572dd8f1dfaf9f91b7dc2a0b9/bvlzv/Daru%20Badnaam-(Mr-Jatt.com).mp3");
            uri=Uri.parse("http://cdn.za.vc/download/48/124057/Raghupati%20Raghav%20Raja%20Ram%20Instrumental%20Piano%20Sawan%20Dutta,Ajay%20Prasanna%20-%20(IndianWap.Net).mp3");

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            AudioWife.getInstance().init(MainActivity.this, uri)
                    .useDefaultUi(relativeLayout, getLayoutInflater());
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}

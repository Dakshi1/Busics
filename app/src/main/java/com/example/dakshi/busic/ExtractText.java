package com.example.dakshi.busic;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;

/**
 * Created by dakshi on 6/3/18.
 */

public class ExtractText extends AsyncTask<String, Void, String> {

    public static PdfReader reader;
    private String path;
    Context context;
    static boolean analysed=false;

    ExtractText(String path,Context context)
    {
        this.context=context;
        this.path=path;
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            reader = new PdfReader(path);
        }  catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        analysed=true;
        MainActivity.check_n_play();
        Toast.makeText(context, "Document completely analysed, Enjoy!!! ", Toast.LENGTH_SHORT).show();
    }
}

package com.example.dakshi.busic;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.util.ArrayList;

import dk.nodes.filepicker.FilePickerActivity;
import dk.nodes.filepicker.FilePickerConstants;
import dk.nodes.filepicker.uriHelper.FilePickerUriHelper;

import static dk.nodes.filepicker.FilePickerConstants.RESULT_CODE_FAILURE;


public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 1317;
    PDFView pdfView;
    private ArrayList<String> filePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();

        pdfView =(PDFView) findViewById(R.id.pdfView);

        Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
        //intent.putExtra(FilePickerConstants.FILE, true);
        intent.putExtra(FilePickerConstants.TYPE, FilePickerConstants.MIME_PDF);
        startActivityForResult(intent, MY_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, FilePickerUriHelper.getUriString(data), Toast.LENGTH_SHORT).show();
                //If its not an image we don't load any of the image views
                Log.d("path",FilePickerUriHelper.getUriString(data));
                pdfView.fromUri(FilePickerUriHelper.getUri(data));
                //fileIv.setImageURI(Uri.fromFile(FilePickerUriHelper.getFile(this, data)));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "User Canceled", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CODE_FAILURE) {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

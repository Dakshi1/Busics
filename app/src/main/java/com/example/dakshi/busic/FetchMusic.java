package com.example.dakshi.busic;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dakshi on 27/2/18.
 */

public class FetchMusic extends AsyncTask<String, Void, String> {

    MenuItem menuItem;
    Context context;
    String url="";
    private ArrayList<String> audio_link;
    Runnable runnable;

    FetchMusic(Context context, String query, MenuItem menuItem)
    {
        audio_link=new ArrayList<>();
        this.menuItem=menuItem;
        this.url="https://api.jamendo.com/v3.0/tracks/?client_id=d7a35ef5&format=json&fuzzytags="+query+"&vocalinstrumental=instrumental&order=relevance_desc";
        this.context=context;
    }

    @Override
    protected String doInBackground(String... strings) {

        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try
                {
                    JSONObject jsonObject=new JSONObject(response);
                    JSONArray results=jsonObject.getJSONArray("results");

                    for(int i=0;i<results.length();i++)
                    {
                        String text=results.getJSONObject(i).getString("audio");

                        audio_link.add(text);
                        if(audio_link.size()==results.length()-1)
                        {
                            music_url();
                            //counter=1;
                        }
                    }


                }
                catch (JSONException j)
                {
                    Log.d("Exception",j.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);

        return null;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        menuItem.setActionView(null);
    }

    private void music_url() {

        if(MainActivity.check()) {
            music_player mplayer = new music_player(context, audio_link);
            mplayer.initializePlayer();
        }
        else
            Toast.makeText(context, "Action requires Internet Connection", Toast.LENGTH_SHORT).show();
    }
}

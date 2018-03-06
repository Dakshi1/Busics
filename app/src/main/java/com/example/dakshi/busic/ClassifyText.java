package com.example.dakshi.busic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dakshi on 27/2/18.
 */

public class ClassifyText extends AsyncTask<String, Void, String> {

    MenuItem menuItem;
    Context context;
    String query="";
    ClassifyText(Context context, MenuItem menuItem)
    {
        this.context=context;
        this.menuItem=menuItem;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... strings) {

        String url = "http://api.meaningcloud.com/class-1.1?key=2943dd044c63d6125b8f02ed76803e43&txt="+strings[0]+"&model=IPTC_en";
        url = url.replaceAll(" ", "%20");
        url = url.replaceAll("\n", "%20");
        StringRequest stringRequest=new StringRequest(url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response volley",response.toString());
                try {

                    JSONObject baseObject=new JSONObject(response.toString());
                    JSONArray category=baseObject.getJSONArray("category_list");
                    for(int i=0;i<category.length();i++)
                    {
                        JSONObject object=category.getJSONObject(i);
                        query=query+object.getString("label");
                        if(i!=category.length()-1)
                            query=query+"+";

                    }
                    Log.d("q",query);
                    printmyquery(query);
                } catch (JSONException e) {
                    menuItem.setActionView(null);
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
        return null;
    }

    private void printmyquery(String c) {

        Log.d("qqqqqqqqqqqq",c);
        c =c.replaceAll("[ ](?=[ ])|[^_+,A-Za-z0-9 ]+", "");
        c =c.replaceAll("\\band\\b\\s*", "");
        //Toast.makeText(context, ""+c, Toast.LENGTH_SHORT).show();
        c=c.replaceAll(" ","%20");
        c=c.replaceAll("\n","%20");
        new FetchMusic(context, c, menuItem).execute();
    }
}

package com.example.dakshi.busic;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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

    private ProgressDialog progressDialog;
    Context context;
    String query="";
    ClassifyText(Context context)
    {
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog=new ProgressDialog(context);
        progressDialog.setTitle("Analyzing Text");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {

        Log.d("text to classify",strings[0]);
        String url = "http://api.meaningcloud.com/class-1.1?key=2943dd044c63d6125b8f02ed76803e43&txt="+strings[0]+"&model=IPTC_en";
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
        Log.d("qqqqqqqqqqqq",c);
        Toast.makeText(context, ""+c, Toast.LENGTH_SHORT).show();
        new FetchMusic(context, progressDialog, c).execute();
    }
}

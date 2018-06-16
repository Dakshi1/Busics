package com.example.dakshi.busic;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExtractEmotion extends AsyncTask<String, Void, String>
{
    ArrayList<String> keywords;
    String prev_tag="";
    MenuItem menuItem;
    Context context;
    ToneAnalysis tone;

    ExtractEmotion(Context context, MenuItem menuItem)
    {
        this.context=context;
        this.menuItem=menuItem;
    }
    @Override
    protected String doInBackground(String... strings) {

        Log.d("output_text", strings[0]);
        try {

            ToneOptions toneOptions = new ToneOptions.Builder().text(strings[0]).build();
            tone = MainActivity.service.tone(toneOptions).execute();
            return tone.toString();
        }
        catch (Exception e)
        {
            Log.d("Exception", e.toString());
            new FetchMusic(context, "piano+violin", menuItem).execute();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.d("output", s);
        if(!s.equals("")) {
            MyTones myTones = new MyTones();
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject doc = jsonObject.getJSONObject("document_tone");
                JSONArray tones = doc.getJSONArray("tones");

                for (int i = 0; i < tones.length(); i++) {
                    JSONObject par_obj = tones.getJSONObject(i);
                    String id = par_obj.getString("tone_id");
                    double value = par_obj.getDouble("score");
                    switch (id) {

                        case "analytical":
                            myTones.setScore_analtical(value);
                            break;
                        case "anger":
                            myTones.setScore_anger(value);
                            break;
                        case "fear":
                            myTones.setScore_fear(value);
                            break;
                        case "joy":
                            myTones.setScore_joy(value);
                            break;
                        case "sadness":
                            myTones.setScore_sad(value);
                            break;
                        case "confident":
                            myTones.setScore_confident(value);
                            break;
                        case "tentative":
                            myTones.setScore_tentative(value);
                            break;
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            do_analysis(myTones);
        }
    }

    private void do_analysis(MyTones myTones) {

        double happy_sad=myTones.getScore_joy()-myTones.getScore_sad();
        double anger_fear=myTones.getScore_anger()-myTones.getScore_fear();
        double conf_tent=myTones.getScore_confident()-myTones.getScore_tentative();
        double philosophical=myTones.getScore_analtical();

        HashMap<String, Double> hashMap=new HashMap<>();
        hashMap.put("happy_sad", Math.abs(happy_sad));
        hashMap.put("anger_fear", Math.abs(anger_fear));
        hashMap.put("conf_tent", Math.abs(conf_tent));
        hashMap.put("philosophical", Math.abs(philosophical));
        List<Map.Entry<String,Double>> sortedEntries = new ArrayList<>(hashMap.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<String,Double>>() {
                    @Override
                    public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );
        keywords=new ArrayList<>();
        for(int i = 0; i< Math.min(sortedEntries.size(),2); i++)
        {
            String arr[]=sortedEntries.get(i).toString().split("=");
            Toast.makeText(context, ""+arr[0], Toast.LENGTH_SHORT).show();
            if(Double.parseDouble(arr[1])<=0.1 && Double.parseDouble(arr[1])>=-0.1)
            {
                keywords.add("neutral");
            }
            else if(arr[0].equals("happy_sad") && happy_sad>0.1)
            {
                keywords.add("joy");
            }
            else if(arr[0].equals("happy_sad") && happy_sad<-0.1)
            {
                keywords.add("sad");
            }
            if(Double.parseDouble(arr[1])<=0.1 && Double.parseDouble(arr[1])>=-0.1)
            {
                keywords.add("neutral");
            }
            else if(arr[0].equals("anger_fear") && anger_fear>0.1)
            {
                keywords.add("anger");
            }
            else if(arr[0].equals("anger_fear") && anger_fear<-0.1)
            {
                keywords.add("fear");
            }
            if(Double.parseDouble(arr[1])<=0.1 && Double.parseDouble(arr[1])>=-0.1)
            {
                keywords.add("neutral");
            }
            else if(arr[0].equals("conf_tent") && conf_tent>0.1)
            {
                keywords.add("confident");
            }
            else if(arr[0].equals("conf_tent") && conf_tent<-0.1)
            {
                keywords.add("tentative");
            }

            if (arr[0].equals("philosophical"))
            {
                keywords.add("philosophical");
            }
            Log.d("output", arr[0]+"  "+hashMap.get(arr[0]));
        }
        for (int ix = 0; ix< Math.min(sortedEntries.size(),2); ix++)
            Log.d("output_most", keywords.get(ix));
        play_music_for();
    }

    private void play_music_for() {

        String output="";
        if(keywords.contains("philosophical")) {

            output="violin";

        }
        else if(!keywords.contains("fear") && !keywords.contains("anger") && !keywords.contains("joy") && !keywords.contains("sad"))
        {
            output="piano";
        }
        else if(keywords.contains("fear") && keywords.contains("sad"))
        {
            output="scary";
        }
        else if(keywords.contains("tentative") && keywords.contains("joy"))
        {
            //output="happy";
            output="happy&speed=high+veryhigh";
        }
        else if((keywords.contains("anger")||keywords.contains("fear")) && keywords.contains("joy"))
        {
            output="piano";
            //output[1]="high+veryhigh";
        }
        else if(keywords.contains("confident") && keywords.contains("joy"))
        {
            output="romance";
            //output[1]="high+veryhigh";
        }
        else if(keywords.contains("anger") && keywords.contains("confident"))
        {
            output="aggressive";
            //output[1]="high+veryhigh";
        }

        else if(keywords.contains("fear"))
        {
            output="scary";
            //output[1]="high+veryhigh";
        }
        else if(keywords.contains("anger") && keywords.contains("sad"))
        {
            //output="serious";
            output="serious&speed=medium";
        }
        else if(keywords.contains("anger"))
        {
            output="anger";
        }
        else if(keywords.contains("sad"))
        {
            output="sad";
            //output[1]="high+veryhigh";
        }
        else if(keywords.contains("joy"))
        {
            //output="happy";
            output="happy";
        }
        else
        {
            output="piano";
        }
        Log.d("output_music", output);
        Toast.makeText(context, ""+output, Toast.LENGTH_SHORT).show();
        if(!output.equals(prev_tag))
            new FetchMusic(context, output, menuItem).execute();
    }
}


/* Perceive emotion scores code:

Client client = ClientBuilder.newClient();
        Entity payload = Entity.json("{  'text': '"+strings[0]+"'}");
        Log.d("input", strings[0]);
        Response response = client.target("https://api.theysay.io/v1/emotion")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(payload);
        Log.d("status_output: " , String.valueOf(response.getStatus()));
        Log.d("headers_output: " , response.getHeaders().toString());
        Log.d("body_output:" , response.readEntity(String.class));

 */
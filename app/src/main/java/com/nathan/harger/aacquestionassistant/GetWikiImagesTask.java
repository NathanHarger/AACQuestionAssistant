package com.nathan.harger.aacquestionassistant;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GetWikiImagesTask extends AsyncTask<Object, Integer, List<String>> {

    private ImageSelectionRecyclerViewAdapter adapter;
    private String search;

    GetWikiImagesTask(ImageSelectionRecyclerViewAdapter adapter, String search) {
        this.adapter = adapter;
        this.search = search;
    }

    @Override
    protected List<String> doInBackground(Object... unused) {
        if (search.equals("")) {
            throw new IllegalArgumentException();
        }

        try {
            String endpoint = String.format("https://en.wikipedia.org/w/api.php?action=query&generator=images&titles=%s&prop=imageinfo&iiprop=url&format=json", search);
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "AACQuestionAssistantBot/1.0 (nathan.j.harger@gmail.com)");
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream responseBody = connection.getInputStream();
                StringBuffer buffer = new StringBuffer();


                BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                responseBody.close();

                String json = buffer.toString();
                JSONObject jsonObject = new JSONObject(json);
                return parseJSON(jsonObject);


            }
        } catch (Exception e) {
            Log.d("json: ", e.getMessage());
        }

        return null;
    }


    @Override
    protected void onPostExecute(List<String> images) {
        super.onPostExecute(images);
        List<Card> r = new LinkedList<>();
        for (String i : images) {
            r.add(new OnlineImageCard(i));
        }

        adapter.submitList(r);
    }

    private List<String> parseJSON(JSONObject root) {
        List<String> images = new LinkedList<>();
        if (root.length() <= 1) {
            return images;
        }
        try {

            JSONObject a = (JSONObject) ((JSONObject) root.get("query")).get("pages");
            Iterator iter = a.keys();
            while (iter.hasNext()) {
                String currKey = (String) iter.next();

                JSONObject curr = a.getJSONObject(currKey);
                JSONArray info = (JSONArray) curr.get("imageinfo");

                String url = (String) ((JSONObject) info.get(0)).get("url");

                if (url.endsWith(".jpg")) {
                    images.add(url);
                }

            }
            Log.d("parse", a.toString());
        } catch (Exception e) {
            Log.e("parsing json", e.getMessage());
        }

        return images;
    }


}

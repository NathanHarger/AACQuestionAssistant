package com.nathan.harger.aacquestionassistant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {


    private final ThreadLocal<ImageView> imageView = new ThreadLocal<ImageView>();


    DownloadImageTask(ImageView imageView) {
        this.imageView.set(imageView);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        if (params.length == 0) {
            throw new IllegalArgumentException("Need Url String");
        }
        String url = params[0];

        try {
            return BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());

        } catch (Exception e) {
            Log.e("url fetch: ", e.getLocalizedMessage());
        }

        return null;
    }


    @Override
    protected void onPostExecute(Bitmap image) {
        super.onPostExecute(image);
        imageView.get().setImageBitmap(image);

    }


}

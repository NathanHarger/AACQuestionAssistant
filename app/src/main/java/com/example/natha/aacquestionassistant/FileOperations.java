package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


public class FileOperations {

    private static String saveToInternalStorage(Bitmap bitmapImage, String filename, Context context){
        ContextWrapper cw = new ContextWrapper(context);
        File directory =  cw.getDir("imageDir", Context.MODE_PRIVATE);





        File mypath = new File(directory, filename +  ".png");

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(mypath);

            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100,fos);

        } catch (Exception e){
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    private static void loadImageFromStorage( Context context, String filename, ImageView img){
        try {
            ContextWrapper cw = new ContextWrapper(context);
            File directory =  cw.getDir("imageDir", Context.MODE_PRIVATE);

            File f = new File(directory, filename +".png");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            img.setImageBitmap(b);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void setImageSource(Context context, Card fileInfo, ImageView imageSource){
        int resourceLoc = fileInfo.resourceLocation;
        if(resourceLoc == 0) {
            Resources resources = context.getResources();


            final int resourceId = resources.getIdentifier(fileInfo.label, "drawable",
                                                            context.getPackageName());
            imageSource.setImageResource(resourceId);

        } else {
            loadImageFromStorage(context,(fileInfo.photoId) , imageSource);
        }

    }
    public static void writeNewVocabToSymbolInfo(Context context, Card fileInfo, Bitmap image){
        ContextWrapper cw = new ContextWrapper(context);
        String filename = fileInfo.label;
        String line = "";
        String split = ",";
        BufferedWriter b = null;
        File directory =  cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory,  "fringeVocab.csv");

        try {

            String[] directoryList =directory.list();
            Pattern p = Pattern.compile(filename+"(\\d+)\\.png|" + filename +".png");

            List<String> matches = new ArrayList<>();

            for (String s : directoryList){
                if(p.matcher(s).matches()){
                    matches.add(s);
                }
            }

            filename = filename + matches.size();
            fileInfo.label = filename;
            b = new BufferedWriter(new FileWriter(mypath,true));

            if(mypath.length() == 0){
                b.append((filename )+ "," +"1," + fileInfo.pronunciation );
            } else {
                b.append(("\n" + filename) + "," + "1," + fileInfo.pronunciation);
            }
            ImageDatabaseHelper.getInstance(context).addImage(fileInfo);
            saveToInternalStorage(image, filename, context);
        } catch (FileNotFoundException e) {
            Log.e("CSV parsing: ", String.valueOf(e.getStackTrace()));
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            if (b != null) {
                try {
                  b.close();
                } catch (IOException e) {
                    Log.e("CSV writing: ", String.valueOf(e.getStackTrace()));
                }
           }
        }
    }

    public static void readNewVocab(Context context, ImageDatabaseHelper idh){
        ContextWrapper cw = new ContextWrapper(context);
        String line = "";
        BufferedReader b = null;
        File directory =  cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory,  "fringeVocab.csv");

        try {
            b = new BufferedReader(new FileReader(mypath));

            while ((line = b.readLine()) != null ) {

                String[] tokens = line.split(",");
                idh.addImage(new Card(idh.getSize()+1, tokens));
            }

        } catch (FileNotFoundException e) {
            Log.e("CSV parsing: ", String.valueOf(e.getStackTrace()));
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            if (b != null) {
                try {
                    b.close();
                } catch (IOException e) {
                    Log.e("CSV writing: ", String.valueOf(e.getStackTrace()));
                }
            }
        }
    }
}

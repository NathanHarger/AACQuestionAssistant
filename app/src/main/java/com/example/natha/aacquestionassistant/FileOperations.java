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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static boolean loadImageFromStorage( Context context, String filename, ImageView img){
        try {
            ContextWrapper cw = new ContextWrapper(context);
            File directory =  cw.getDir("imageDir", Context.MODE_PRIVATE);


            File f = new File(directory, filename +".png");
            Bitmap b;
            if(f.exists()) {
                b = BitmapFactory.decodeStream(new FileInputStream(f));
                img.setImageBitmap(b);
                return true;
            } else{
                return false;
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean setImageSource(Context context, Card fileInfo, ImageView imageSource){
        int resourceLoc = fileInfo.resourceLocation;
        if(resourceLoc == 0) {
            Resources resources = context.getResources();



            final int resourceId = resources.getIdentifier(fileInfo.label, "drawable",
                                                            context.getPackageName());
            imageSource.setImageResource(resourceId);

        } else {


                return !loadImageFromStorage(context,(fileInfo.photoId) , imageSource);

        }
        return true;
    }
    public static String writeNewVocabToSymbolInfo(Context context, Card fileInfo, Bitmap image){
        ContextWrapper cw = new ContextWrapper(context);
        String filename = fileInfo.photoId;
        BufferedWriter b = null;
        File directory =  cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory,  "fringeVocab.csv");

        String photoId = "";

        try {

            String[] directoryList =directory.list();
            Pattern p = Pattern.compile(filename+"_(\\d+)\\.png|" + filename +".png");

            List<String> matches = new ArrayList<>();

            for (String s : directoryList){
                if(p.matcher(s).matches()){
                    matches.add(s);
                }
            }

             photoId = filename + "_"+matches.size();
            fileInfo.photoId = photoId;
            b = new BufferedWriter(new FileWriter(mypath,true));

            if(mypath.length() == 0){
                b.append(fileInfo.id + "," +(photoId )+ "," +"1," + fileInfo.pronunciation );
            } else {
                b.append(("\n"+ fileInfo.id+"," + photoId) + "," + "1," + fileInfo.pronunciation);
            }
            ImageDatabaseHelper.getInstance(context).addImage(fileInfo);
            saveToInternalStorage(image, photoId, context);
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
        return photoId;
    }

    public static void deleteCustomVocab(String filename, Context context){
        int targetNum = -1;
        String[] filenameTokens = filename.split("_");

        targetNum = Integer.parseInt(filenameTokens[1]);
        String targetFilename = filenameTokens[0];



        ContextWrapper cw = new ContextWrapper(context);
        File directory =  cw.getDir("imageDir", Context.MODE_PRIVATE);

        File f = new File(directory,  "fringeVocab.csv");
        File temp = new File(directory, "temp.csv");
        BufferedReader fr = null;
        BufferedWriter fw = null;
        try {
            fr = new BufferedReader(new FileReader(f));
             fw = new BufferedWriter(new FileWriter(temp));
            String line  = "";
             while((line =fr.readLine()) != null){
                 String[] tokens = line.split(",");
                 String currFilename = tokens[1];

                 String[] currFilnameTokens = currFilename.split("_");

                 int lastNum = Integer.parseInt(currFilnameTokens[1]);
                 currFilename = currFilnameTokens[0];


                 //if the filename don't match write  to file
                 if (!currFilename.equals(targetFilename)){

                         fw.append(line);
                        fw.append("\n");



                 } else{
                     // decrease all file counts after the deleted count

                      if(lastNum == targetNum){
                          File image = new File(directory, filename +".png" );
                          image.delete();

                          continue;
                     } else if(lastNum > targetNum) {
                         // write with num--
                          fw.append(currFilename+""+(lastNum-1) + "," +"1," + tokens[2]);
                          fw.append("\n");

                          File rename = new File(directory,currFilename+("" + lastNum)+".png");
                          rename.renameTo(new File(directory, currFilename+""+(lastNum-1)+".png"));


                      } else{
                          //write
                          fw.append(line);
                          fw.append("\n");
                          File rename = new File(directory,currFilename+("" + lastNum)+".png");
                          rename.renameTo(new File(directory, currFilename+""+(lastNum-1)+".png"));


                      }
                 }




             }

            boolean delete = f.delete();
            File newfile = new File(directory,"fringeVocab.csv");
            boolean renamed = temp.renameTo(newfile);
            Log.d("delete ", ""+renamed);

        } catch ( Exception e){

        } finally {
            try {
                fr.close();
                fw.close();
            } catch (Exception e){

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

                int id = Integer.parseInt(tokens[0]);
                idh.addImage(new Card(id, Arrays.copyOfRange(tokens,1, tokens.length)));
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

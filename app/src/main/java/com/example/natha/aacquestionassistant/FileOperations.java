package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class FileOperations {

    private static void saveToInternalStorage(Bitmap bitmapImage, String filename, Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, filename + ".png");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadImageFromStorage(Context context, String filename, ImageView img, TextView noImageText) {
        try {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File f = new File(directory, filename + ".png");
            Bitmap b;
            if (f.exists()) {
                b = BitmapFactory.decodeStream(new FileInputStream(f));
                img.setImageBitmap(b);
            } else {
                noImageText.setVisibility(View.VISIBLE);
                img.setImageBitmap(null);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    static void setImageSource(Context context, Card fileInfo, ImageView imageSource, TextView noImageText) {

        int resourceLoc = fileInfo.resourceLocation;
        if (resourceLoc == 0) {
            Resources resources = context.getResources();
            final int resourceId = resources.getIdentifier(fileInfo.photoId, "drawable",
                    context.getPackageName());
            imageSource.setImageResource(resourceId);
            noImageText.setVisibility(View.GONE);
        } else {

            loadImageFromStorage(context, (fileInfo.photoId), imageSource, noImageText);
        }
    }

    static String writeNewVocabToSymbolInfo(Context context, Card fileInfo, Bitmap image) {
        ContextWrapper cw = new ContextWrapper(context);
        String filename = fileInfo.photoId;
        BufferedWriter b = null;
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, "fringeVocab.csv");
        String photoId = "";
        try {

            if (image == null) {
                photoId = "";

            } else {
                photoId = filename + "-" + fileInfo.pronunciation;
            }
            fileInfo.photoId = photoId;
            b = new BufferedWriter(new FileWriter(mypath, true));
            if (mypath.length() == 0) {
                b.append(filename).append(",").append("1,").append(fileInfo.pronunciation);
            } else {
                b.append("\n").append(filename).append(",").append("1,").append(fileInfo.pronunciation);
            }
            ImageDatabaseHelper.getInstance(context).addImage(fileInfo);

            if (image != null)
                saveToInternalStorage(image, photoId, context);
        } catch (FileNotFoundException e) {
            Log.e("CSV parsing: ", e.getMessage());
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            if (b != null) {
                try {
                    b.close();
                } catch (IOException e) {
                    Log.e("CSV writing: ", e.getMessage());
                }
            }
        }
        return photoId;
    }

    static void deleteCustomVocab(String filename, Context context) {
        int targetId;
        String[] filenameTokens = filename.split("-");
        String filenameArray = filenameTokens[0];

        String pronunciationArray = filenameTokens.length == 2 ? filenameTokens[1] : "";

        String targetFilename = filenameArray;
        String targetPronunciation = pronunciationArray;

        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File f = new File(directory, "fringeVocab.csv");
        File temp = new File(directory, "temp.csv");
        try (BufferedReader fr = new BufferedReader(new FileReader(f)); BufferedWriter fw = new BufferedWriter(new FileWriter(temp))) {
            String line;
            while ((line = fr.readLine()) != null) {
                String[] tokens = line.split(",");
                String currFilename = tokens[0];
                String pronunciation = tokens.length == 3 ? tokens[2] : "";

                //if the filename don't match write  to file
                if (!currFilename.equals(targetFilename)) {
                    fw.append(line);
                    fw.append("\n");

                } else {
                    // if name and
                    if (targetPronunciation.equals(pronunciation)) {
                        File image = new File(directory, filename + ".png");
                        image.delete();
                    } else {

                        //write
                        fw.append(line);
                        fw.append("\n");
                    }
                }
            }

            f.delete();
            File newfile = new File(directory, "fringeVocab.csv");
            boolean renamed = temp.renameTo(newfile);
            Log.d("delete ", "" + renamed);
        } catch (Exception e) {
            Log.e("Delete New Vocab: ", e.getMessage());
        }
    }

    public static void readNewVocab(Context context, ImageDatabaseHelper idh) {
        ContextWrapper cw = new ContextWrapper(context);
        String line;
        BufferedReader b = null;
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, "fringeVocab.csv");
        try {
            b = new BufferedReader(new FileReader(mypath));
            while ((line = b.readLine()) != null) {
                String[] tokens = line.split(",");
                int id = -1;
                idh.addImage(new Card(id, tokens));
            }
        } catch (FileNotFoundException e) {
            Log.e("CSV parsing: ", e.getMessage());
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            if (b != null) {
                try {
                    b.close();
                } catch (IOException e) {
                    Log.e("CSV writing: ", (e.getMessage()));
                }
            }
        }
    }
}

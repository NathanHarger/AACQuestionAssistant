package com.nathan.harger.aacquestionassistant;

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
import java.util.List;

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
        } else if (resourceLoc == 1) {

            loadImageFromStorage(context, (fileInfo.photoId), imageSource, noImageText);
        } else {
            getPhotoFromWeb(((OnlineImageCard) fileInfo).url, imageSource);
        }
    }

    private static void getPhotoFromWeb(final String url, final ImageView image) {

        DownloadImageTask downloadImageTask = new DownloadImageTask(image);
        downloadImageTask.execute(url);


        //webImage = null;
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
            long id = ImageDatabaseHelper.getInstance(context).addImage(fileInfo);
            fileInfo.id = id;
            b = new BufferedWriter(new FileWriter(mypath, true));
            if (mypath.length() == 0) {
                b.append(filename).append(",").append("1,").append(fileInfo.pronunciation).append(",").append(String.valueOf(id));
            } else {
                b.append("\n").append(filename).append(",").append("1,").append(fileInfo.pronunciation).append(",").append(String.valueOf(id));
            }

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
        String[] filenameTokens = filename.split("-");
        String filenameArray = filenameTokens[0];

        String pronunciationArray = filenameTokens.length == 2 ? filenameTokens[1] : "";

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
                if (!currFilename.equals(filenameArray)) {
                    fw.append(line);
                    fw.append("\n");

                } else {
                    // if name and
                    if (pronunciationArray.equals(pronunciation)) {
                        File image = new File(directory, filename + ".png");

                        if (!image.delete()) {
                            throw new FileNotFoundException();
                        }
                    } else {

                        //write
                        fw.append(line);
                        fw.append("\n");
                    }
                }
            }

            if (!f.delete()) {
                throw new FileNotFoundException();
            }

            File newfile = new File(directory, "fringeVocab.csv");
            boolean renamed = temp.renameTo(newfile);
            Log.d("delete ", "" + renamed);
        } catch (Exception e) {
            Log.e("Delete New Vocab: ", e.getLocalizedMessage());
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
            while ((line = b.readLine()) != null && !line.equals("")) {
                String[] tokens = line.split(",");

                idh.addImage(new Card(-1, tokens));
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

    public static void readVocabGroup(Context context, ImageDatabaseHelper idh) {
        ContextWrapper cw = new ContextWrapper(context);
        String vocabKeyLine;
        BufferedReader b = null;
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(directory, "vocabGroup.csv");
        try {
            b = new BufferedReader(new FileReader(mypath));
            while ((vocabKeyLine = b.readLine()) != null && !vocabKeyLine.equals("")) {
                long vocabKey = Long.parseLong(vocabKeyLine);
                String line = b.readLine();
                String[] tokens = line.split(",");

                idh.addCardGroup(vocabKey, tokens);
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


    // deletedId is the id for the deleted card
    // deletedSets are the ids of sets with 0 card group members
    static void deleteVocabFromGroup(Long deletedId, List<Long> deletedSets, Context context) {


        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File f = new File(directory, "vocabGroup.csv");
        File temp = new File(directory, "tempVocabGroup.csv");

        try (BufferedReader fr = new BufferedReader(new FileReader(f)); BufferedWriter fw = new BufferedWriter(new FileWriter(temp))) {
            String vocabKeyLine;
            while ((vocabKeyLine = fr.readLine()) != null && !vocabKeyLine.equals("")) {

                long setKey = Long.parseLong(vocabKeyLine);

                String line = fr.readLine();
                String[] tokens = line.split(",");


                //if the set id is not deletedId
                if (deletedSets == null || !deletedSets.contains(setKey)) {
                    fw.append(String.valueOf(setKey)).append("\n");
                    if (deletedId == -1L) {
                        fw.append(line);
                    } else {
                        fw.append(tokens[0]);

                        for (int i = 1; i < tokens.length; i++) {
                            if (Long.parseLong(tokens[i]) != deletedId) {
                                fw.append(',').append(tokens[i]);
                            }
                        }
                    }

                    fw.append("\n");
                }
            }

            if (!f.delete()) {
                throw new FileNotFoundException();
            }

            File newfile = new File(directory, "vocabGroup.csv");
            boolean renamed = temp.renameTo(newfile);
            Log.d("delete ", "" + renamed);
        } catch (Exception e) {
            Log.e("Delete New Vocab: ", e.getMessage());
        }
    }

    public static void writeGroup(Context context, long groupkey, List<Card> cards) {
        {

            ContextWrapper cw = new ContextWrapper(context);

            BufferedWriter b = null;
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File mypath = new File(directory, "vocabGroup.csv");
            try {


                b = new BufferedWriter(new FileWriter(mypath, true));
                if (mypath.length() == 0) {
                    b.append(String.valueOf(groupkey)).append("\n");

                } else {
                    b.append("\n").append(String.valueOf(groupkey)).append("\n");

                }

                b.append(String.valueOf(cards.get(0).id));
                for (int i = 1; i < cards.size(); i++) {
                    b.append(",").append(String.valueOf(cards.get(i).id));
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
                        Log.e("CSV writing: ", e.getMessage());
                    }
                }
            }
        }
    }
}

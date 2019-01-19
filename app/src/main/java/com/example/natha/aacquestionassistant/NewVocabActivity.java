package com.example.natha.aacquestionassistant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickClick;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import static android.content.res.AssetManager.ACCESS_STREAMING;

public class NewVocabActivity extends AppCompatActivity implements IPickResult {
PickImageDialog pickImageDialog;
private ImageDatabaseHelper idh;
private Bitmap selectedImage;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_vocab_activity);
        idh = ImageDatabaseHelper.getInstance(NewVocabActivity.this);
    }
    private final int RESULT_LOAD_IMAGE = 1;
    public void imageSelect(View v){

        pickImageDialog = PickImageDialog.build(new PickSetup()).show(this);
    }

    public void submitVocab(View v){
        ImageView image = findViewById(R.id.imageView2);
        EditText word = findViewById(R.id.editText2);

        if(word.getText().toString().equals("")){
            Toast.makeText( this,"Vocab word cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(selectedImage == null){
            Toast.makeText(this, "Select an Image", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = word.getText().toString().replace(" ", "_");
        FileOperations.writeNewVocabToSymbolInfo(getApplicationContext(),
                new Card( idh.getSize() +1 , filename, 1,pronunciation), selectedImage);

        finish();
    }
    private PronunciationDialogFragment newDialog;
    public void editPronunciation(View v){
         newDialog = new PronunciationDialogFragment();
         EditText editText = findViewById(R.id.editText2);
         Bundle b = new Bundle();

         b.putString("vocab", pronunciation.equals("") ? editText.getText().toString(): pronunciation);
         newDialog.setArguments(b);
        newDialog.show(getSupportFragmentManager(),"test");
    }

    public void previewPronunciation(View v){
        EditText editText = ((ConstraintLayout)v.getParent()).findViewById(R.id.pronunciation);
        if(editText.getText().length() > 0) {
            TextToSpeechManager.speak(editText.getText());
        }
    }
    private String pronunciation ="";
    public void submitPronunciationDialog(View v){
        int id = v.getId();

        if(id == R.id.ok){
            pronunciation = ((EditText)
                    ((ConstraintLayout)v.getParent())
                            .findViewById(R.id.pronunciation)).getText().toString();
        }
        newDialog.dismiss();

    }

    @Override
    public void onPickResult(final PickResult r){
        if(r.getError() == null){
            selectedImage = r.getBitmap();
            ((ImageView)findViewById(R.id.imageView2)).setImageBitmap(selectedImage);


        } else{
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();

        }

        pickImageDialog.dismiss();

    }


}

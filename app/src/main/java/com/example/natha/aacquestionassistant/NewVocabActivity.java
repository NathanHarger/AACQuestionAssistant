package com.example.natha.aacquestionassistant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Toast;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;



import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


public class NewVocabActivity extends AppCompatActivity implements IPickResult {
PickImageDialog pickImageDialog;
private ImageDatabaseHelper idh;
private Bitmap selectedImage;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_vocab_activity);

        if(savedInstanceState != null){
            Bitmap bitmapid = savedInstanceState.getParcelable("bitmap");
            selectedImage = bitmapid;
            ImageView image = findViewById(R.id.imageView2);
            image.setImageBitmap(selectedImage);
        }

        Bundle b = getIntent().getExtras();
        if(b != null){
            String word = b.getString("word");
            ((EditText) findViewById(R.id.editText2)).setText(word);
        }
        idh = ImageDatabaseHelper.getInstance(NewVocabActivity.this);
    }
    private final int RESULT_LOAD_IMAGE = 1;
    public void imageSelect(View v){

        pickImageDialog = PickImageDialog.build(new PickSetup()).show(this);
    }




    public void submitVocab(View v){
        ImageView image = findViewById(R.id.imageView2);
        EditText word = findViewById(R.id.editText2);

        if(word.getText().toString().length() <3){
            Toast.makeText( this,"Vocab word must be longer than 3", Toast.LENGTH_SHORT).show();
            return;
        }

        if(selectedImage == null){
            Toast.makeText(this, "Select an Image", Toast.LENGTH_SHORT).show();
            return;
        }

        int id = idh.getSize() + 1;
        String label = word.getText().toString().replace(" ", "_");
        String fileid = FileOperations.writeNewVocabToSymbolInfo(getApplicationContext(),
                new Card( id, label,label, 1,pronunciation), selectedImage);


        Intent output = new Intent();
        output.putExtra("id", id);
        output.putExtra("name", label);
        output.putExtra("filename", fileid);
        output.putExtra("resourceLocation", 1);
        output.putExtra("pronunciation", pronunciation);
        setResult(Activity.RESULT_OK, output);
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


    @Override
    public void onSaveInstanceState(Bundle outstate) {
        if(selectedImage != null)
            outstate.putParcelable("bitmap", selectedImage);
        super.onSaveInstanceState(outstate);

    }

}

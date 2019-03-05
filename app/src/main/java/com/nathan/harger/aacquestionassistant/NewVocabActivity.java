package com.nathan.harger.aacquestionassistant;

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
    private final int ONLINE_IMAGE = 3;
    protected Bitmap selectedImage;
    private PickImageDialog pickImageDialog;
    private ImageDatabaseHelper idh;
    private String pronunciation = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_vocab_activity);

        if (savedInstanceState != null) {
            selectedImage = savedInstanceState.getParcelable("bitmap");
            ImageView image = findViewById(R.id.imageView2);
            image.setImageBitmap(selectedImage);
        }

        Bundle b = getIntent().getExtras();
        if (b != null) {
            String word = b.getString("word");
            ((EditText) findViewById(R.id.editText2)).setText(word);
        }
        idh = ImageDatabaseHelper.getInstance(NewVocabActivity.this);
    }

    public void imageSelect(View v) {

        pickImageDialog = PickImageDialog.build(new PickSetup()).show(this);
    }

    public void onlineImageSelect(View v) {

        Intent i = new Intent(this, OnlineImageSelectionActivity.class);
        EditText word = findViewById(R.id.editText2);
        if (word.getText().length() == 0) {
            Toast.makeText(this, "Enter vocab name", Toast.LENGTH_SHORT).show();
            return;
        }
        //extra.putString();
        i.putExtra("search", word.getText().toString());
        startActivityForResult(i, ONLINE_IMAGE);
    }

    public void submitVocab(View v) {
        ImageView image = findViewById(R.id.imageView2);
        EditText word = findViewById(R.id.editText2);
        EditText pronunciationEditText = findViewById(R.id.pronunciation);
        String pronunciation = pronunciationEditText.getText().toString();

        int id = idh.getSize() + 1;
        String label = word.getText().toString().replace(" ", "_");
        String fileid = FileOperations.writeNewVocabToSymbolInfo(getApplicationContext(),
                new Card(id, label, label, 1, pronunciation), selectedImage);
        Intent output = new Intent();
        output.putExtra("id", id);
        output.putExtra("name", label);
        output.putExtra("filename", fileid);
        output.putExtra("resourceLocation", 1);
        output.putExtra("pronunciation", pronunciation);
        setResult(Activity.RESULT_OK, output);
        finish();
    }


    @Override
    public void onBackPressed() {
        Intent output = new Intent();
        setResult(Activity.RESULT_OK, output);
        finish();
    }

    public void previewPronunciation(View v) {
        EditText editText = ((ConstraintLayout) v.getParent()).findViewById(R.id.pronunciation);
        if (editText.getText().length() > 0) {
            TextToSpeechManager.speak(editText.getText());
        }
    }


    @Override
    public void onPickResult(final PickResult r) {
        if (r.getError() == null) {
            selectedImage = r.getBitmap();
            ((ImageView) findViewById(R.id.imageView2)).setImageBitmap(selectedImage);

        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
        pickImageDialog.dismiss();
    }


    @Override
    public void onSaveInstanceState(Bundle outstate) {
        if (selectedImage != null)
            outstate.putParcelable("bitmap", selectedImage);
        super.onSaveInstanceState(outstate);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ONLINE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                DownloadImageTask downloadImageTask = new DownloadImageTask((ImageView) findViewById(R.id.imageView2));
                final String url = data.getStringExtra("url");
                downloadImageTask.execute(url);


            }

        }
    }


}

package com.nathan.harger.aacquestionassistant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


public class NewVocabActivity extends AppCompatActivity implements IPickResult {
    private final int ONLINE_IMAGE = 3;
    private Bitmap selectedImage;
    private PickImageDialog pickImageDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_vocab_activity);

        if (savedInstanceState != null) {
            selectedImage = BitmapFactory.decodeStream(new ByteArrayInputStream((byte[]) (savedInstanceState.getSerializable("bitmap"))));
            ImageView image = findViewById(R.id.imageView2);
            image.setImageBitmap(selectedImage);
        }

        Bundle b = getIntent().getExtras();
        if (b != null) {
            String word = b.getString("word");
            ((EditText) findViewById(R.id.editText2)).setText(word);
        }

    }


    public void imageSelect(View v) {

        // selectedImage = null;

        pickImageDialog = PickImageDialog.build(this).show(this);
    }

    public void onlineImageSelect(View v) {
        //selectedImage = null;
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

        String label = word.getText().toString().replace(" ", "_");
        Card c = new Card(-1, label, label, 1, pronunciation);
        String fileid = FileOperations.writeNewVocabToSymbolInfo(getApplicationContext(),
                c, selectedImage);
        Intent output = new Intent();
        output.putExtra("id", c.id);
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
    public void onPickResult(PickResult r) {

        if (r.getError() == null) {
            selectedImage = r.getBitmap();
            ((ImageView) findViewById(R.id.imageView2)).setImageBitmap(selectedImage);

        }
        pickImageDialog.dismiss();
    }


    @Override
    public void onSaveInstanceState(Bundle outstate) {
        if (selectedImage != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 25, out);
            outstate.putByteArray("bitmap", out.toByteArray());
        }
        super.onSaveInstanceState(outstate);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (pickImageDialog != null)
            pickImageDialog.onActivityResult(99, -1, data);
        if (requestCode == ONLINE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                DownloadImageTask downloadImageTask = new DownloadImageTask((ImageView) findViewById(R.id.imageView2));
                final String url = data.getStringExtra("url");
                downloadImageTask.execute(url);


            }

        }
    }


}

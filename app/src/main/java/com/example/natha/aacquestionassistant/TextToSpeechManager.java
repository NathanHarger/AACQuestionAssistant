package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TextToSpeechManager {
    static TextToSpeech tts;


    public static void initTextToSpeech(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        }
        );
    }

    public static boolean speak(CharSequence phrase) {
        if (tts == null) {
            return false;
        }
        if (!tts.isSpeaking()) {
            tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "");
        }
        return true;
    }


}

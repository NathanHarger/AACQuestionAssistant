package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

public class TextToSpeechManager  {
    static TextToSpeech tts;

    static UtteranceProgressListener f = new UtteranceProgressListener(){
        @Override
        public void onDone(String utteranceId) {
            if(!next.equals("")){
                String temp = next;
                next = "";
                tts.speak(temp, TextToSpeech.QUEUE_FLUSH,null,"");

            }
        }

        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onError(String utteranceId, int errorCode) {
            super.onError(utteranceId, errorCode);
        }

        @Override
        public void onError(String utteranceId) {

        }
    };

    public static void initTextToSpeech(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);

                }
            }
        }
        );

        tts.setOnUtteranceProgressListener(f);
    }
    static String next = "";
    public static boolean speak(CharSequence phrase) {
        if (tts == null) {
            return false;
        }
        if (!tts.isSpeaking()) {
            tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "");
        } else if(next.equals("")) {
            next = phrase.toString();

        }
        return true;
    }




}

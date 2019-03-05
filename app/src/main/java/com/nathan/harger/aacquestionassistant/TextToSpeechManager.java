package com.nathan.harger.aacquestionassistant;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

class TextToSpeechManager {
    static TextToSpeech tts;
    private static String next = "";
    private static final UtteranceProgressListener f = new UtteranceProgressListener() {
        @Override
        public void onDone(String utteranceId) {
            if (!next.equals("")) {
                String temp = next;
                next = "";
                tts.speak(temp, TextToSpeech.QUEUE_FLUSH, null, "");
            }
        }

        @Override
        public void onStart(String utteranceId) {
        }

        @Override
        public void onError(String utteranceId) {
        }
    };

    static void initTextToSpeech(Context context) {
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

    static void speak(CharSequence phrase) {
        if (tts == null) {
            return;
        }
        if (!tts.isSpeaking()) {
            tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "");
        } else if (next.equals("")) {
            next = phrase.toString();
        }
    }


}

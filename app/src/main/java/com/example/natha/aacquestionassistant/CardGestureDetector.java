package com.example.natha.aacquestionassistant;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;

public class CardGestureDetector extends GestureDetector {

    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        boolean handled = true;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:

                break;

            case MotionEvent.ACTION_POINTER_UP:

                break;

            case MotionEvent.ACTION_DOWN:



                onGestureListener.onDown(ev);


            case MotionEvent.ACTION_MOVE:


                break;

            case MotionEvent.ACTION_UP:
               break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }


        return handled;
    }





    GestureDetector.OnGestureListener onGestureListener;

    public CardGestureDetector(Context context, GestureDetector.OnGestureListener gestureListener){
        super(context,gestureListener);
        this.onGestureListener = gestureListener;

    }
}

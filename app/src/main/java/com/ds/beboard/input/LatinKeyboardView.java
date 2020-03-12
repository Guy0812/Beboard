package com.ds.beboard.input;

import android.inputmethodservice.KeyboardView;



import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.ds.beboard.R;

import java.util.List;

@TargetApi(Build.VERSION_CODES.CUPCAKE)

public class LatinKeyboardView extends KeyboardView {

    private PopupWindow popup ;
    static final int KEYCODE_OPTIONS = -100;
    // TODO: Move this into android.inputmethodservice.Keyboard
    static final int KEYCODE_LANGUAGE_SWITCH = -101;

    static final int KEYCODE_EMOJI_SWITCH  = -10;


    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {

            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else if (key.codes[0] == 113) {/*
            final View custom = LayoutInflater.from(context).inflate(R.layout.keyboard_popup_layout, new FrameLayout(context));
            popup = new PopupWindow(context);
            popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popup.showAtLocation(this, Gravity.NO_GRAVITY, key.x, key.y-50);*/
            return true;
        }  else {
            // Log.d("LatinKeyboardView", "KEY: " + key.codes[0]);
            return super.onLongPress(key);
        }
    }


    void setSubtypeOnSpaceKey(final InputMethodSubtype subtype) {
        final LatinKeyboard keyboard = (LatinKeyboard) getKeyboard();
        // keyboard.setSpaceIcon(getResources().getDrawable(subtype.getIconResId()));
        invalidateAllKeys();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(28);
        paint.setColor(getResources().getColor(R.color.white));

        List<Key> keys = getKeyboard().getKeys();
        for(Key key: keys) {
            if(key.label != null) {
                if (key.label.equals("◌̀")) {
                    canvas.drawText("1", key.x + (key.width - 10), key.y + 30, paint);
                } else if (key.label.equals("◌́")) {
                    canvas.drawText("2", key.x + (key.width - 10), key.y + 30, paint);
                } else if (key.label.equals("◌̂")) {
                    canvas.drawText("3", key.x + (key.width - 10), key.y + 30, paint);
                } else if (key.label.equals("◌̌")) {
                    canvas.drawText("4", key.x + (key.width - 10), key.y + 30, paint);
                } else if (key.label.equals("◌̄")) {
                    canvas.drawText("5", key.x + (key.width - 10), key.y + 30, paint);
                }else if (key.label.equals("◌̱")) {
                    canvas.drawText("6", key.x + (key.width - 10), key.y + 30, paint);
                }else if (key.label.equals("◌̃")) {
                    canvas.drawText("7", key.x + (key.width - 10), key.y + 30, paint);
                }else if (key.label.equals("◌̰")) {
                    canvas.drawText("8", key.x + (key.width - 10), key.y + 30, paint);
                }else if (key.label.equals("◌̇")) {
                    canvas.drawText("9", key.x + (key.width - 10), key.y + 30, paint);
                }else if (key.label.equals("◌̣")) {
                    canvas.drawText("0", key.x + (key.width - 10), key.y + 30, paint);
                }

            }


        }
    }
}

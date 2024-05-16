package com.spark.h9patches;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

public class NavigationButton extends ServiceFacility {

    String prefKey = getString(R.string.pref_accessibility_dock);

    public NavigationButton(Context context) {
        super(context);
    }

    @Override
    public void onServiceStart() {
        checkPrefNavigationButton();
    }

    @Override
    public void onSharedPreferenceChanged(String key) {
        if (key.equals(prefKey)) {
            checkPrefNavigationButton();
        }
    }

    private void checkPrefNavigationButton() {
        boolean prefValue = sharedPreferences.getBoolean(prefKey, getResourcesBoolean(R.bool.pref_accessibility_dock));
         int state = prefValue ? PatchesIntent.FLAG_STATE_ON : PatchesIntent.FLAG_STATE_OFF;
        toggleNavigationButton(state);
    }

    ImageButton floatingButton;
    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    private void toggleNavigationButton(int state) {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (floatingButton != null) {
            windowManager.removeView(floatingButton);
            floatingButton = null;
        }
        if (state != 1) {
            return;
        }
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        floatingButton = (ImageButton) layoutInflater.inflate(R.layout.button_floating, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(getContext())) {
                ImageButton button = floatingButton; //new ImageButton(getApplicationContext());
//                button.setAlpha(0.5f);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    ;
                } else {
                    params.type = WindowManager.LayoutParams.TYPE_PHONE;
                }
                params.format = PixelFormat.RGBA_8888;
                params.gravity = Gravity.TOP | Gravity.START;
//                params.horizontalMargin = 10f;
//                params.verticalMargin = 10f;
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                params.x = 10;
                params.y = displayMetrics.heightPixels + 24;
                params.width  = 80;
                params.height = 80;
                windowManager.addView(button, params);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            long now = SystemClock.uptimeMillis();
                            getApplication().getInputManager().injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK, 0), 0);
                            getApplication().getInputManager().injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK, 0), 0);
                        } catch (Exception ignored) {
                        }
                    }
                });
                button.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        try {
                            getApplication().getInputManager().injectInputEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME), 0);
                            getApplication().getInputManager().injectInputEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME), 0);
                        } catch (RemoteException ignored) {}
                        return true;
                    }
                });
                button.setOnTouchListener(new View.OnTouchListener() {
                    private int initialX;
                    private int initialY;
                    private float initialTouchX;
                    private float initialTouchY;
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = params.x;
                                initialY = params.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                                windowManager.updateViewLayout(button, params);
                                break;
                            case MotionEvent.ACTION_UP:
                                if (Math.abs(event.getRawX() - initialTouchX) < 30 && Math.abs(event.getRawY() - initialTouchY) < 30) {
                                    params.x = initialX;
                                    params.y = initialY;
                                    windowManager.updateViewLayout(button, params);
                                }
                                return params.x != initialX || params.y != initialY;
                        }
                        return false;
                    }
                });
            }
        }
    }
}

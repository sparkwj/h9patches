package com.spark.h9patches;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.DataOutputStream;

public class DPadSimulator extends ServiceFacility implements ActivityWatcher.OnTopActivityChangedListener {
    @SuppressLint("StaticFieldLeak")
    DataOutputStream runtime;
    ActivityWatcher watcher;

    public DPadSimulator(Context context, ActivityWatcher watcher) {
        super(context);
        try {
            Process process = Runtime.getRuntime().exec("sh");
            runtime = new DataOutputStream(process.getOutputStream());
        } catch (Exception ignored) {}
        this.watcher = watcher;
    }

    @Override
    public void onServiceStart() {
        this.watcher.registerOnTopActivityChanged(packageName -> {
            boolean needsDPad = ActivityWatcher.isTvApp(packageName);
            toggleDirectionPad(needsDPad ? 1 : 0);
        });
    }

    FrameLayout mDirectionPad;
    @SuppressLint("ClickableViewAccessibility")
    public void toggleDirectionPad(int state) {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (mDirectionPad != null) {
            windowManager.removeView(mDirectionPad);
            mDirectionPad = null;
        }
        if (state != 1) {
            return;
        }
        mDirectionPad = new FrameLayout(getContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(getContext())) {
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
                params.x = 0;
                params.y = 0;
                params.width  = displayMetrics.widthPixels;
                params.height = displayMetrics.heightPixels + 200;
                mDirectionPad.setBackgroundColor(Color.BLUE);
                mDirectionPad.setAlpha(0.F);
                mDirectionPad.setClickable(false);
                mDirectionPad.setFocusable(false);
                mDirectionPad.setFocusableInTouchMode(false);
                windowManager.addView(mDirectionPad, params);
                mDirectionPad.setOnClickListener(null);
                mDirectionPad.setOnLongClickListener(null);
                MotionEvent lastEvent;
                mDirectionPad.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        Log.d(TAG, "dpad simulator send keycode~~~");
                        sendKeyCode(KeyEvent.KEYCODE_VOLUME_UP);
                        return false;
                    }
                });
                mDirectionPad.setOnTouchListener(new View.OnTouchListener() {
                    float lastX;
                    float lastY;
                    boolean moved = false;
                    MotionEvent downEvent;
                    MotionEvent moveEvent;
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                downEvent = event;
                                moved = false;
                                break;
                            case MotionEvent.ACTION_MOVE:
                                float newX = event.getRawX();
                                float newY = event.getRawY();
                                float distanceX = newX - lastX;
                                float distanceY = newY - lastY;
                                float ratio = Math.abs(distanceX) / Math.abs(distanceY);
                                int keycode = 0;
                                float RATIO = 5;
                                float STEP = 40;

                                if (ratio > RATIO && Math.abs(distanceX) > STEP) {
                                    keycode = distanceX > 0 ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
                                } else if (ratio < 1 / RATIO && Math.abs(distanceY) > STEP) {
                                    keycode = distanceY > 0 ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN;
                                }
                                Log.d(TAG, "keycode: " + keycode);
                                if (keycode > 0) {
                                    sendKeyCode(keycode);
                                    lastX = newX;
                                    lastY = newY;
                                }
                                if (Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2)) > 5) {
                                    moved = true;
                                }
                                moveEvent = event;
                                break;
                            case MotionEvent.ACTION_UP:
                                if (!moved) {
                                    mDirectionPad.setVisibility(View.GONE);
                                    long now = SystemClock.uptimeMillis();
                                    int x = (int) event.getRawX();
                                    int y = (int) event.getRawY();
                                    try {
                                        runtime.writeBytes("input tap " + x + " " + y + "\n");
                                        runtime.flush();
//                                                inputManager.injectInputEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0, 0, 0, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0), 0);
//                                                inputManager.injectInputEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, x, y, 0, 0, 0, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0), 0);
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            try {
                                                mDirectionPad.setVisibility(View.VISIBLE);
                                            } catch (Exception ignored) {
                                            }
                                        }, 2000);

                                    }catch (Exception ignored) {}
                                }
                                return true;
                        }
                        return true;
                    }
                });
            }
        }
    }

    private void sendKeyCode(int keycode) {
        try {
            long now = System.currentTimeMillis();
            getApplication().getInputManager().injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keycode, 0), 0);
            getApplication().getInputManager().injectInputEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keycode, 0), 0);
        } catch (Exception ignored) {
        }
    }

    public boolean isEnabled() {
        return mDirectionPad != null && mDirectionPad.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onTopActivityChanged(String packageName) {
        boolean needsDPad = ActivityWatcher.isTvApp(packageName);
        if (needsDPad && !isEnabled()) {
            toggleDirectionPad(PatchesIntent.FLAG_STATE_ON);
        } else if (!needsDPad && isEnabled()) {
            toggleDirectionPad(PatchesIntent.FLAG_STATE_OFF);
        }
    }
}

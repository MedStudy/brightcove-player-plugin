//package com.medstudy.cordova.bcplayerplugin;
package net.nopattern.cordova.brightcoveplayer;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.view.WindowManager;
import android.view.Gravity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.graphics.Color;

import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.media.Catalog;
import com.brightcove.player.media.VideoListener;
import com.brightcove.player.media.VideoFields;
import com.brightcove.player.model.CuePoint;
import com.brightcove.player.model.Video;
import com.brightcove.player.model.Source;
import com.brightcove.player.util.StringUtil;
import com.brightcove.player.view.BrightcovePlayer;
import com.brightcove.player.view.BrightcoveVideoView;

import android.content.pm.ActivityInfo;
import android.view.Display;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BCPlayerActivity extends BrightcovePlayer {
  private static final String ID = "id";
  private static final String TAG = "BCPluginActivity";
  private static final String LAYOUT = "layout";
  private static final String BRIGHTCOVE_VIEW = "brightcove_video_view";
  private static final String BRIGHTCOVE_ACTIVITY = "bundled_video_activity_brightcove";

  static final String ACTIVITY_EVENT = "ACTIVITY_EVENT";

  private EventEmitter eventEmitter;
  private ActivityReceiver activityReceiver;
  private int layoutX, layoutY, layoutWidth, layoutHeight;
  private String token = null;
  private String restart = null;

  private class ActivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent arg1) {
      Video video = null;
      Cmd cmd = Cmd.valueOf(arg1.getStringExtra("EXTRA_CMD"));
      switch (cmd){
        case LOAD:
          String rid = arg1.getStringExtra("EXTRA_DATA1");
          video = BCVideoRetriever.retrieveVideo(token, rid);
          if(video != null){
            addVideoToViewer(video);
          }
          break;
        case LOADED:
          video = BCVideoRetriever.getVideo();
          if(video != null){
            Intent intent = new Intent();
            intent.setAction(ACTIVITY_EVENT);
            String duration = Integer.toString(video.getDuration() / 1000);
            intent.putExtra("DATA_BACK", "brightcovePlayer.loaded");
            intent.putExtra("DURATION", duration);
            sendBroadcast(intent);
            addVideoToViewer(video);
          }
          break;
        case DISABLE:
          unregisterReceiver(activityReceiver);
          brightcoveVideoView.getLayoutParams();
          finish();
          break;
        case ENABLE:
          break;
        case PAUSE:
          if(brightcoveVideoView != null){
            brightcoveVideoView.pause();
          }
          break;
        case PLAY:
          if(brightcoveVideoView != null){
            brightcoveVideoView.start();
          }
          break;
        case SEEK:
          if(brightcoveVideoView != null){
            int location = Integer.parseInt(arg1.getStringExtra("EXTRA_DATA1"));
            brightcoveVideoView.seekTo(location*1000);
          }
          break;
        case HIDE:
          DisplayMetrics metrics = new DisplayMetrics();
          getWindowManager().getDefaultDisplay().getMetrics(metrics);
          reposition(0, metrics.heightPixels, 1, 1);
          break;
        case SHOW:
          reposition(layoutX, layoutY, layoutWidth, layoutHeight);
          break;
        case REPOSITION:
          int x = Integer.parseInt(arg1.getStringExtra("EXTRA_DATA1"));
          int y = Integer.parseInt(arg1.getStringExtra("EXTRA_DATA2"));
          int width = Integer.parseInt(arg1.getStringExtra("EXTRA_DATA3"));
          int height = Integer.parseInt(arg1.getStringExtra("EXTRA_DATA4"));
          layoutX = x;
          layoutY = y;
          layoutWidth = width;
          layoutHeight = height;
          reposition(x, layoutY, width, height);
          BCVideoRetriever.setRect(y, x, height, width);
          break;
      }
    }
  }

  private void reposition(int x, int y, int width, int height){

    Log.d(TAG, "Height:" + height);
    if(brightcoveVideoView != null){
      final View view = getWindow().getDecorView();
      final Context context = view.getContext();
      //WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

      WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();

      Rect rect;
      lp.gravity = Gravity.TOP;
      lp.x = toPixels(context, x);
      lp.y = toPixels(context, y);
      lp.width = toPixels(context, width);
      lp.height = toPixels(context, height);
      getWindowManager().updateViewLayout(view, lp);
      Log.d(TAG, "Height: -->" + height);
      Log.d(TAG, "Width: -->" + width);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Window window = getWindow();
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    setContentView(this.getIdFromResources(BRIGHTCOVE_ACTIVITY, LAYOUT));
    window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

    super.onCreate(savedInstanceState);
    final View view = getWindow().getDecorView();
    view.setBackgroundColor(Color.BLACK);

    brightcoveVideoView = (BrightcoveVideoView) findViewById(this.getIdFromResources(BRIGHTCOVE_VIEW, ID));
    //brightcoveVideoView.setBackgroundColor(Color.TRANSPARENT);
    brightcoveVideoView.setBackgroundColor(Color.BLACK);

    activityReceiver = new ActivityReceiver();
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BCPlayerPlugin.PLUGIN_CMD);
    registerReceiver(activityReceiver, intentFilter);

    Intent intent = getIntent();
    token = intent.getStringExtra("brightcove-token");
    restart = intent.getStringExtra("brightcove-restart");

    eventEmitter = brightcoveVideoView.getEventEmitter();
    eventEmitter.on(EventType.DID_ENTER_FULL_SCREEN, new EventListener() {
      @Override
      public void processEvent(Event event) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final View view = getWindow().getDecorView();
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();

        lp.gravity = Gravity.TOP;
        lp.x = 0;
        lp.y = 0;
        lp.width = metrics.widthPixels;
        lp.height = metrics.heightPixels;
        getWindowManager().updateViewLayout(view, lp);
      }
    });

    eventEmitter.on(EventType.DID_EXIT_FULL_SCREEN, new EventListener() {
      @Override
      public void processEvent(Event event) {
        reposition(layoutX, layoutY, layoutWidth, layoutHeight);
      }
    });

    eventEmitter.on(EventType.DID_PLAY, new EventListener(){
      @Override
      public void processEvent(Event event) {
        Intent intent = new Intent();
        intent.setAction(ACTIVITY_EVENT);
        String duration = Float.toString((float) brightcoveVideoView.getDuration() / 1000);
        String position = Float.toString((float) brightcoveVideoView.getCurrentPosition() / 1000);
        intent.putExtra("DATA_BACK", "brightcovePlayer.play");
        intent.putExtra("DURATION", duration);
        intent.putExtra("POSITION", position);
        sendBroadcast(intent);
      }
    });

    eventEmitter.on(EventType.DID_PAUSE, new EventListener(){
      @Override
      public void processEvent(Event event) {
        Intent intent = new Intent();
        intent.setAction(ACTIVITY_EVENT);
        String position = Float.toString((float)brightcoveVideoView.getCurrentPosition() / 1000);
        intent.putExtra("DATA_BACK", "brightcovePlayer.pause");
        intent.putExtra("POSITION", position);
        sendBroadcast(intent);
      }
    });

    eventEmitter.on(EventType.PROGRESS, new EventListener(){
      @Override
      public void processEvent(Event event) {
        Intent intent = new Intent();
        intent.setAction(ACTIVITY_EVENT);
        int pos = brightcoveVideoView.getCurrentPosition() / 1000;
        BCVideoRetriever.setLastLocation(pos);
        String position = Integer.toString(pos);
        intent.putExtra("DATA_BACK", "brightcovePlayer.progress");
        intent.putExtra("POSITION", position);
        sendBroadcast(intent);

        Log.d("Time Pos: ", position);
      }
    });

    eventEmitter.on(EventType.COMPLETED, new EventListener(){
      @Override
      public void processEvent(Event event) {
        sendEvent("brightcovePlayer.end");
      }
    });

    eventEmitter.on(EventType.ERROR, new EventListener(){
      @Override
      public void processEvent(Event event) {
        sendEvent("brightcovePlayer.error");
      }
    });

    if(restart.compareToIgnoreCase("yes") == 0){
      String rid = intent.getStringExtra("brightcove-rid");
      Video video = BCVideoRetriever.retrieveVideo(token, rid);
      if(video != null){
        addVideoToViewer(video);
        Integer location = BCVideoRetriever.getLastLocation();
        brightcoveVideoView.seekTo(location * 1000);
        /*
        final Rect rect = BCVideoRetriever.getRect();
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            //reposition(rect.left, rect.top, rect.right, rect.bottom);
            this.cancel();
          }
        }, 0, 1000);
        */
      }
    } else {
      sendEvent("brightcovePlayer.inited");
    }

    Log.d(TAG, "Inited");

    return;
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    final View view = getWindow().getDecorView();
    final Context context = view.getContext();
    final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();

    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);

    final Rect rect = BCVideoRetriever.getRect();

    lp.gravity = Gravity.TOP;
    if(restart.compareToIgnoreCase("yes") == 0) {
      lp.x = toPixels(context, 0);
      lp.y = toPixels(context, rect.top);
      lp.width = toPixels(context, rect.right);
      lp.height = toPixels(context, rect.bottom);
    }
    else{
      lp.x = toPixels(context, 0);
      lp.y = toPixels(context, rect.top);
      lp.width = toPixels(context, 1);
      lp.height = toPixels(context, 1);
    }
    getWindowManager().updateViewLayout(view, lp);

    view.setBackgroundColor(Color.BLACK);
    brightcoveVideoView.setBackgroundColor(Color.BLACK);
    //brightcoveVideoView.setBackgroundColor(Color.TRANSPARENT);
    //view.setVisibility(View.INVISIBLE);
  }

  private int toPixels(Context context, int dp) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
  }

  @Override
  protected void onDestroy() {
    Intent intent = new Intent();
    intent.setAction(ACTIVITY_EVENT);

    String position = Float.toString((float)brightcoveVideoView.getCurrentPosition() / 1000);

    intent.putExtra("DATA_BACK", "brightcovePlayer.hide");
    intent.putExtra("POSITION", position);

    sendBroadcast(intent);

    Log.d(TAG, "Activity destroyed");

    super.onDestroy();
  }

  /*
  public void finishActivity(){
    finish();
  }
  */

  private int getIdFromResources(String what, String where){
    String package_name = getApplication().getPackageName();
    Resources resources = getApplication().getResources();
    return resources.getIdentifier(what, where, package_name);
  }

  private void addVideoToViewer(Video video) {
    brightcoveVideoView.clear();
    brightcoveVideoView.add(video);
    brightcoveVideoView.start();
    return;
  }

  /*
  private void load(String token, String rid) {
    Log.d(TAG, "Playing video from brightcove ID: " + rid);

    Catalog catalog = new Catalog(token);
    catalog.findVideoByReferenceID(rid, new VideoListener() {
      public void onVideo(Video video) {
        brightcoveVideoView.clear();
        brightcoveVideoView.add(video);
        brightcoveVideoView.start();

        Intent intent = new Intent();
        intent.setAction(ACTIVITY_EVENT);
        String duration = Integer.toString(video.getDuration() / 1000);
        intent.putExtra("DATA_BACK", "brightcovePlayer.loaded");
        intent.putExtra("DURATION", duration);
        sendBroadcast(intent);
      }

      public void onError(String error) {
        Log.e(TAG, error);
        sendEvent("brightcovePlayer.error");
      }
    });

    return;
  }
  */

  private void sendEvent(String event) {
    Intent intent = new Intent();
    intent.setAction(ACTIVITY_EVENT);
    intent.putExtra("DATA_BACK", event);
    sendBroadcast(intent);
  }
}

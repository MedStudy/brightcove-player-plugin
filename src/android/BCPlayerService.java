package net.nopattern.cordova.brightcoveplayer;

/**
 * Created by peterchin on 6/24/16.
 */

import android.app.Service;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;
import android.os.IBinder;
import android.view.WindowManager;
import android.view.Gravity;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.net.Uri;

import com.brightcove.player.event.Default;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventLogger;
import com.brightcove.player.model.Source;
import com.brightcove.player.model.SourceCollection;
import com.brightcove.player.util.LifecycleUtil;
import com.brightcove.player.view.BaseVideoView;
import com.brightcove.player.view.BrightcoveVideoView;
import com.brightcove.player.media.DeliveryType;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BaseVideoView;
import com.brightcove.player.view.BrightcoveVideoView;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

public class BCPlayerService extends Service implements MediaPlayer.OnPreparedListener{
    public static final String TAG = BCPlayerService.class.getSimpleName();
    @Deprecated
    public static final String POSITION = "position";
    @Deprecated
    public static final String WAS_PLAYING = "wasPlaying";
    protected BaseVideoView brightcoveVideoView;
    private LifecycleUtil lifecycleUtil;
    private EventLogger eventLogger;
    private Bundle savedInstanceState;

    private static MediaPlayer mediaPlayer;
    private static ServiceReceiver serviceReceiver;
    private static String ridCurrent = null;
    private static Timer posTimer;

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
        player.setVolume(1, 1);
        int pos = BCVideoRetriever.getLastLocation();
        player.seekTo(pos * 1000);
    }

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    @Override public void onCreate() {
        super.onCreate();

        // Create a new media player and set the listeners
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);

        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BCPlayerActivity.SERVICE_CMD);
        registerReceiver(serviceReceiver, intentFilter);
    }

    protected void startUpdater() {
        Log.v(TAG, "startUpdater");

        posTimer = new Timer();
        posTimer.schedule(new TimerTask() {
          @Override
          public void run() {
            Intent intent = new Intent();
            intent.setAction("ACTIVITY_EVENT");
            int pos = mediaPlayer.getCurrentPosition() / 1000;
            String position = Integer.toString(pos);
            intent.putExtra("DATA_BACK", "brightcovePlayer.progress");
            intent.putExtra("POSITION", position);
            sendBroadcast(intent);
          }
        }, 0, 1000);

        /*
        this.updater = EXECUTOR.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    if(VideoDisplayComponent.this.mediaPlayer != null && VideoDisplayComponent.this.hasPrepared && VideoDisplayComponent.this.hasSurface && VideoDisplayComponent.this.mediaPlayer.isPlaying() && VideoDisplayComponent.this.mediaPlayer.getCurrentPosition() >= 0) {
                        HashMap e = new HashMap(4);
                        e.put("video", VideoDisplayComponent.this.currentVideo);
                        e.put("source", VideoDisplayComponent.this.currentSource);
                        VideoDisplayComponent.this.playheadPosition = VideoDisplayComponent.this.mediaPlayer.getCurrentPosition();
                        e.put("playheadPosition", Integer.valueOf(VideoDisplayComponent.this.playheadPosition));
                        e.put("duration", Integer.valueOf(VideoDisplayComponent.this.mediaPlayer.getDuration()));
                        if(VideoDisplayComponent.this.playheadPosition > 0 && !VideoDisplayComponent.this.hasPlaybackStarted) {
                            VideoDisplayComponent.this.eventEmitter.emit("didPlay", e);
                            VideoDisplayComponent.this.hasPlaybackStarted = true;
                        }

                        VideoDisplayComponent.this.eventEmitter.emit("progress", e);
                    }
                } catch (IllegalStateException var2) {
                    VideoDisplayComponent.this.destroyPlayer();
                    Log.e(VideoDisplayComponent.TAG, "Media player position sampled when it was in an invalid state: " + var2.getMessage(), var2);
                    VideoDisplayComponent.this.eventEmitter.emit("error", Collections.singletonMap("error", var2));
                } catch (Exception var3) {
                    VideoDisplayComponent.this.destroyPlayer();
                    Log.e(VideoDisplayComponent.TAG, "Error monitoring playback progress" + var3.getMessage(), var3);
                    VideoDisplayComponent.this.eventEmitter.emit("error", Collections.singletonMap("error", var3));
                }

            }
        }, 0L, 500L, TimeUnit.MILLISECONDS);
        */
    }

    protected void stopUpdater() {
        if(posTimer != null) {
            posTimer.cancel();
            posTimer = null;
        }
    }

    private class ServiceReceiver extends BroadcastReceiver {
        private void loadMedia(){
            try {
                String url = BCVideoRetriever.getVideoSource();
                if (url != null) {
                    mediaPlayer.reset();
                    Uri uri = Uri.parse(url);
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                    mediaPlayer.prepareAsync(); // prepare async to not block main thread
                    //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.start();
                }
            } catch (Exception e) {

            }
        }

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String cmd = arg1.getStringExtra("SERVICE_CMD");
            if(cmd.compareToIgnoreCase("SVC_PLAY") == 0){
                String rid = BCVideoRetriever.getRid();
                if(rid != null ){
                    if(ridCurrent != null ){
                        if(ridCurrent.compareToIgnoreCase(rid) != 0) {
                            loadMedia();
                        }
                        else {
                            int pos = BCVideoRetriever.getLastLocation();
                            mediaPlayer.start();
                            mediaPlayer.seekTo(pos*1000);
                        }
                    }
                    else{
                        loadMedia();
                    }
                    ridCurrent = rid;
                    startUpdater();
                }
            }
            else if(cmd.compareToIgnoreCase("SVC_PAUSE") == 0){
                mediaPlayer.pause();
                stopUpdater();

                int pos = mediaPlayer.getCurrentPosition() / 1000;
                BCVideoRetriever.setServiceLastLocation(pos);
                Intent intent = new Intent();
                intent.setAction(BCPlayerPlugin.PLUGIN_CMD);
                intent.putExtra("EXTRA_CMD", Cmd.RESUME.name());
                sendBroadcast(intent);
            }
        }
    }
}

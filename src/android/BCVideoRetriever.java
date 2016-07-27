package net.nopattern.cordova.brightcoveplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;

import com.brightcove.player.media.Catalog;
import com.brightcove.player.media.VideoListener;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcovePlayer;

/**
 * Created by peterchin on 7/18/16.
 */
public class BCVideoRetriever {
    private static String ridCurrent = "";
    private static Video videoCurrent = null;
    private static Context context = null;
    private static int lastLocation = 0;
    private static Rect rect = new Rect();

    public static void setContext(Context c) {
        context = c;
    }

    public static Rect getRect(){
        return rect;
    }

    public static void setRect(int top, int left, int bottom, int right){
        rect.top = top;
        rect.left = left;
        rect.bottom = bottom;
        rect.right = right;
    }

    public static int getLastLocation(){
        return lastLocation;
    }

    public static void setLastLocation(int location){
        lastLocation = location;
    }

    public static Video getVideo(){
        return videoCurrent;
    }

    public static Video retrieveVideo(final String token, final String rid){
        Video video = null;
        if(ridCurrent.compareToIgnoreCase(rid) != 0) {
            Catalog catalog = new Catalog(token);
            catalog.findVideoByReferenceID(rid, new VideoListener() {
                public void onVideo(Video v) {
                    videoCurrent = v;
                    ridCurrent = rid;
                    if(context != null) {
                        Intent intent = new Intent();
                        intent.setAction(BCPlayerPlugin.PLUGIN_CMD);
                        intent.putExtra("EXTRA_CMD", Cmd.LOADED.name());
                        context.sendBroadcast(intent);
                    }
                }

                public void onError(String error) {
                    if (context != null) {
                        Intent intent = new Intent();
                        intent.setAction(BCPlayerActivity.ACTIVITY_EVENT);
                        intent.putExtra("DATA_BACK", "brightcovePlayer.error");
                        context.sendBroadcast(intent);
                    }
                }
            });
        }
        else {
            video = videoCurrent;
        }

        return  video;
    }

    private static void sendMessage(String action, String type, Cmd msg){
        if(context != null) {
            Intent intent = new Intent();
            intent.setAction(action);
            intent.putExtra(type, msg);
            context.sendBroadcast(intent);
        }
    }
}

package net.nopattern.cordova.brightcoveplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;

import com.brightcove.player.controller.NoSourceFoundException;
import com.brightcove.player.media.Catalog;
import com.brightcove.player.media.DeliveryType;
import com.brightcove.player.media.VideoListener;
import com.brightcove.player.model.Source;
import com.brightcove.player.model.SourceCollection;
import com.brightcove.player.model.Video;
import com.brightcove.player.util.ErrorUtil;
import com.brightcove.player.view.BrightcovePlayer;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by peterchin on 7/18/16.
 */
public class BCVideoRetriever {
    private static String ridCurrent = "";
    private static Video videoCurrent = null;
    private static Context context = null;
    private static int lastLocation = 0;
    private static int lastServiceLocation = 0;
    private static Rect rect = new Rect();
    private static final Integer DEFAULT_BIT_RATE = Integer.valueOf(262144);

    public static void setContext(Context c) {
        context = c;
    }

    public static Rect getRect(){
        return rect;
    }

    public static String getRid(){
        return ridCurrent;
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

    public static int getServiceLastLocation(){
        return lastServiceLocation;
    }

    public static void setServiceLastLocation(int location){
        lastServiceLocation = location;
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

    // DefaultSourceSelectionController
    public static String getVideoSource() throws NoSourceFoundException{
        Source source = null;
        if(videoCurrent == null) {
            throw new IllegalArgumentException(ErrorUtil.getMessage("videoRequired"));
        } else {
            Map collections = videoCurrent.getSourceCollections();
            if(collections != null && collections.size() != 0) {
                Set sources;
                if(source == null && collections.containsKey(DeliveryType.HLS)) {
                    sources = ((SourceCollection)collections.get(DeliveryType.HLS)).getSources();
                    if(sources != null && sources.size() > 0) {
                        source = (Source)sources.iterator().next();
                    }
                }

                if(source == null && collections.containsKey(DeliveryType.MP4)) {
                    source = findBestSourceByBitRate((SourceCollection) collections.get(DeliveryType.MP4), DEFAULT_BIT_RATE);
                }

                if(source == null && collections.containsKey(DeliveryType.DASH)) {
                    sources = ((SourceCollection)collections.get(DeliveryType.DASH)).getSources();
                    if(sources != null && sources.size() > 0) {
                        source = (Source)sources.iterator().next();
                    }
                }

                if(source == null && collections.containsKey(DeliveryType.UNKNOWN)) {
                    sources = ((SourceCollection)collections.get(DeliveryType.UNKNOWN)).getSources();
                    if(sources != null && sources.size() > 0) {
                        source = (Source)sources.iterator().next();
                    }
                }

                if(source != null && source.getUrl() != null) {
                    return source.getUrl();
                } else {
                    throw new NoSourceFoundException();
                }
            } else {
                throw new NoSourceFoundException();
            }
        }
    }

    public static Source findBestSourceByBitRate(SourceCollection sourceCollection, Integer bitRate) throws NoSourceFoundException {
        if(sourceCollection.getSources() != null && sourceCollection.getSources().size() != 0) {
            Source bestMatch = (Source)sourceCollection.getSources().iterator().next();
            int bestDelta = 2147483647;
            Iterator i$ = sourceCollection.getSources().iterator();

            while(i$.hasNext()) {
                Source source = (Source)i$.next();
                if(source.getBitRate() != null && source.getBitRate().intValue() > 0) {
                    int thisDelta = Math.abs(source.getBitRate().intValue() - bitRate.intValue());
                    if(thisDelta <= bestDelta) {
                        bestMatch = source;
                        bestDelta = thisDelta;
                    }
                }
            }

            return bestMatch;
        } else {
            return null;
        }
    }
}

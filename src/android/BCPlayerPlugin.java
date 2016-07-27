//package com.medstudy.cordova.bcplayerplugin;
package net.nopattern.cordova.brightcoveplayer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.content.ComponentName;
import android.content.pm.PackageManager;

public class BCPlayerPlugin extends CordovaPlugin {
  private CordovaWebView appView;
  private PluginReceiver receiver;
  private String token = "";
  private String rid = null;

  static final String PLUGIN_CMD = "PLUGIN_CMD";

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    appView = webView;
    receiver = new PluginReceiver();
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BCPlayerActivity.ACTIVITY_EVENT);
    cordova.getActivity().getApplicationContext().registerReceiver(receiver, intentFilter);
    BCVideoRetriever.setContext(appView.getView().getContext());
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if(action.equals("init")) {
      Context context = this.cordova.getActivity().getApplicationContext();
      //enableComponent(context, BCPlayerActivity.class, true);
      token = args.getString(0);
      this.init(callbackContext, "no");
      return true;
    } else if (action.equals("enable")) {
      this.init(callbackContext, "yes");
      //sendCommand(Cmd.ENABLE, "", "", "", "");
      //Context context = this.cordova.getActivity().getApplicationContext();
      //enableComponent(context, BCPlayerActivity.class, true);
      return true;
    } else if (action.equals("disable")) {
      sendCommand(Cmd.DISABLE, "", "", "", "");
      //Context context = this.cordova.getActivity().getApplicationContext();
      //enableComponent(context, BCPlayerActivity.class, false);
      return true;
    } else if (action.equals("load")) {
      rid = args.getString(0);
      sendCommand(Cmd.LOAD, rid, "", "", "");
      return true;
    } else if (action.equals("hide")) {
      sendCommand(Cmd.HIDE, "", "", "", "");
      return true;
    } else if (action.equals("show")) {
      sendCommand(Cmd.SHOW, "", "", "", "");
      return true;
    } else if (action.equals("pause")) {
      sendCommand(Cmd.PAUSE, "", "", "", "");
      return true;
    } else if (action.equals("play")) {
      sendCommand(Cmd.PLAY, "", "", "", "");
      return true;
    } else if (action.equals("seek")) {
      String location = args.getString(0);
      sendCommand(Cmd.SEEK, location, "", "", "");
      return true;
    } else if(action.equals("reposition")) {
      String y = args.getString(1);
      String width = args.getString(2);
      String height = args.getString(3);
      sendCommand(Cmd.REPOSITION, "0", y, width, height);
      return true;
    }

    return false;
  }

  private void sendCommand(Cmd cmd, String data1, String data2, String data3, String data4){
    Log.d("BCPluginActivity", cmd.name());

    Intent intent = new Intent();
    intent.setAction(PLUGIN_CMD);
    intent.putExtra("EXTRA_CMD", cmd.name());
    intent.putExtra("EXTRA_DATA1", data1);
    intent.putExtra("EXTRA_DATA2", data2);
    intent.putExtra("EXTRA_DATA3", data3);
    intent.putExtra("EXTRA_DATA4", data4);
    appView.getView().getContext().sendBroadcast(intent);
  }

  private void enableComponent(Context context, Class<?> componentClass,
                               boolean isEnable) {
    int enableFlag = isEnable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    context.getPackageManager().setComponentEnabledSetting(
            new ComponentName(context, componentClass),
            enableFlag, PackageManager.DONT_KILL_APP);
  }

  private void init(CallbackContext callbackContext, String restart) {
    if (token != null && token.length() > 0){
      Context context = this.cordova.getActivity().getApplicationContext();
      Intent intent = new Intent(context, BCPlayerActivity.class);
      intent.putExtra("brightcove-token", token);
      intent.putExtra("brightcove-restart", restart);
      intent.putExtra("brightcove-rid", rid);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);

      callbackContext.success("Brightcove inited");
    } else{
      callbackContext.error("Empty Brightcove token!");
    }
  }

  private class PluginReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context arg0, Intent arg1) {
      String orgData = arg1.getStringExtra("DATA_BACK");
      String duration = arg1.getStringExtra("DURATION");
      String position = arg1.getStringExtra("POSITION");
      appView.sendJavascript("cordova.fireWindowEvent('" + orgData + "', { 'duration':" + duration + ", 'currentTime': " + position + "})");
    }
  }
}
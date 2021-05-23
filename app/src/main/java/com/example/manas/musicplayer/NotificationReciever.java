package com.example.manas.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.example.manas.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.example.manas.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.manas.musicplayer.ApplicationClass.ACTION_PREVIOUS;

public class NotificationReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context,MusicService.class);
        if(actionName != null){

            switch (actionName){
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName","play");
                    context.startService(serviceIntent);
                    break;

                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName","next");
                    context.startService(serviceIntent);
                    break;

                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("ActionName","previous");
                    context.startService(serviceIntent);
                    break;

            }
        }
    }
}

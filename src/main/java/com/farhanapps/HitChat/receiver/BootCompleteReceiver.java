package com.farhanapps.HitChat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.farhanapps.HitChat.services.MessageGateway;

/**
 * Created by farhan on 16-04-2016.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MessageGateway.class));
    }
}

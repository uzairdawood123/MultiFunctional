package com.xsm.exa.multifunctionmonitoring.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.xsm.exa.multifunctionmonitoring.MainActivity;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//            Intent it = new Intent(context, MainActivity.class);
//            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(it);
            Intent it = new Intent(context, DataProcessService.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(it);
        }
    }

}

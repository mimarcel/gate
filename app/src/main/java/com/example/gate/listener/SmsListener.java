package com.example.gate.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.gate.Gate;
import com.example.gate.Individual;
import com.example.gate.activity.MainActivity;
import com.example.gate.R;

public class SmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        if (MainActivity.Gate.SETUP_ERROR != null) {
            Log.d("SmsListener", "Skipping checking sms: Gate setup failed.");
            return;
        }

        if (!MainActivity.Gate.isAutoCallEnabled()) {
            Log.d("SmsListener", "Skipping checking sms: Auto Call is not enabled.");
            return;
        }

        String openTextLowerCase = context.getResources().getString(R.string.open).toLowerCase();

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            try {
                SmsMessage[] msgs;
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String msgPhone = msgs[i].getOriginatingAddress();
                    Individual msgIndividual = Gate.FindGuyByPhone(MainActivity.Gate.GATE_ALLOWED_INDIVIDUALS, msgPhone);
                    if (msgIndividual == null) {
                        Log.i("SmsListener", "Ignoring message: Sender is not allowed to request to open the gate.");
                        return;
                    }
                    String msgBody = msgs[i].getMessageBody();
                    if (msgBody.toLowerCase().trim().equals(openTextLowerCase)) {
                        Log.i("SmsListener", "Received OPEN message from " + msgIndividual.Name + ".");
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        callIntent.setData(Uri.parse("tel:" + MainActivity.Gate.GATE_INDIVIDUAL.Phone));
                        context.startActivity(callIntent);
                        Log.i("SmsListener", String.format("Calling %s...", MainActivity.Gate.GATE_INDIVIDUAL.Name));
                    } else {
                        Log.i("SmsListener", "Ignoring message: Received non-OPEN message from " + msgIndividual.Name + ".");
                    }
                }
            } catch (Exception ex) {
                Log.e("Failure", "Failure to handle received SMS", ex);
                Toast.makeText(context, "Failure to handle received SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


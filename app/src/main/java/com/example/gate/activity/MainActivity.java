package com.example.gate.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.gate.Gate;
import com.example.gate.R;

public class MainActivity extends AppCompatActivity {
    SwitchCompat enableAutoCall;
    public com.example.gate.Gate Gate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Gate = new Gate(this);

        enableAutoCall = findViewById(R.id.enableAutoCallSwitch);
        enableAutoCall.setChecked(Gate.isAutoCallEnabled());
        enableAutoCall.setOnCheckedChangeListener(this::onEnableAutoCallChecked);
    }

    @SuppressLint("IntentReset")
    public void onOpenClick(View v) {
        if (Gate.SETUP_ERROR != null ) {
            Toast.makeText(MainActivity.this, Gate.SETUP_ERROR, Toast.LENGTH_LONG).show();
            return;
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                "android.permission.SEND_SMS",
        }, 200);

        String openText = getResources().getString(R.string.open);

        try {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.setType("vnd.android-dir/mms-sms");
            smsIntent.putExtra("address", Gate.GATE_OWNER_INDIVIDUAL.Phone);
            smsIntent.putExtra("sms_body", openText);
            startActivity(smsIntent);
            Log.i("onOpenClick", String.format("Sent message to %s.", Gate.GATE_OWNER_INDIVIDUAL.Name));
        } catch (Exception ex) {
            Log.e("onOpenClick", "Failure to send SMS", ex);
            String message = String.format(
                    "Sending SMS failed, please try again later or manually send SMS to %s with message '%s'.",
                    Gate.GATE_OWNER_INDIVIDUAL.Name, openText);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    public void onInfoClick(View v) {
        String appName = v.getContext().getResources().getString(R.string.app_name);
        int gateAllowedIndividualsCount = Gate.GATE_ALLOWED_INDIVIDUALS == null ? 0 : Gate.GATE_ALLOWED_INDIVIDUALS.size();
        String[] gateAllowedIndividualNames = new String[gateAllowedIndividualsCount];
        for (int i = 0; i < gateAllowedIndividualsCount; i++) {
            gateAllowedIndividualNames[i] = Gate.GATE_ALLOWED_INDIVIDUALS.get(i).Name;
        }
        new AlertDialog.Builder(v.getContext())
                .setTitle(appName + " Info")
                .setMessage(Html.fromHtml(String.format(
                        "<p><b>Gate Owner</b>: %s</p><p><b>Allowed to open Gate</b>:<ol><li>%s</li></ol></p>",
                        Gate.GATE_OWNER_INDIVIDUAL == null ? "NOT SET" : Gate.GATE_OWNER_INDIVIDUAL.Name,
                        gateAllowedIndividualsCount == 0 ? "NOT SET" : String.join("</li><li> ", gateAllowedIndividualNames))))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public void onEnableAutoCallChecked(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    "android.permission.RECEIVE_SMS",
                    "android.permission.READ_SMS",
                    "android.permission.CALL_PHONE",
            }, 200);

            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }

            Gate.enableAutoCall(true);
            Toast.makeText(MainActivity.this,
                    "Auto Call enabled", Toast.LENGTH_SHORT).show();
        } else {
            Gate.enableAutoCall(false);
            Toast.makeText(MainActivity.this,
                    "Auto Call disabled", Toast.LENGTH_SHORT).show();
        }
    }
}

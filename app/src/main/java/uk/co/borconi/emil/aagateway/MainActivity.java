package uk.co.borconi.emil.aagateway;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AAGateWay";
    private SharedPreferences preferences;
    private IntentFilter filter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Log.w(TAG, "ON_RECEIVE");

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.w(TAG, "FOUND");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.w(TAG, "CONNECTED");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.w(TAG, "FINISHED");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.w(TAG, "DISCONNECT_REQ");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.w(TAG, "DISCONNECTED");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.aawifi)
                .setContentTitle("Title")
                .setContentText("Content");
        Notification n = builder.build();
        NotificationManager nm = (NotificationManager)this.getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(45468, n);

        Log.w(TAG, "CREATING");

        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);

        if (getIntent().getAction()!=null && getIntent().getAction().equalsIgnoreCase("android.intent.action.MAIN")) {

            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            ToggleButton listenswitch = findViewById(R.id.swListening);
            listenswitch.setChecked(preferences.getBoolean(Preferences.LISTENING_MODE, true));
            listenswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = preferences.edit()
                            .putBoolean(Preferences.LISTENING_MODE, isChecked);
                    editor.commit();
                }
            });

            ToggleButton ignoreipv6switch = findViewById(R.id.swIpMode);
            ignoreipv6switch.setChecked(preferences.getBoolean(Preferences.IGNORE_IPV6, true));
            ignoreipv6switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = preferences.edit()
                            .putBoolean(Preferences.IGNORE_IPV6, isChecked);
                    editor.commit();
                }
            });

            Button button = findViewById(R.id.exitButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    @Override
    protected  void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, filter);

        Intent paramIntent = getIntent();
        Intent i = new Intent(this, HackerService.class);

        if (paramIntent.getAction() != null && paramIntent.getAction().equalsIgnoreCase("android.hardware.usb.action.USB_ACCESSORY_DETACHED")) {
            Log.d(TAG, "USB DISCONNECTED");
            stopService(i);
            finish();
        } else if (paramIntent.getAction() != null && paramIntent.getAction().equalsIgnoreCase("android.hardware.usb.action.USB_ACCESSORY_ATTACHED")) {
            Log.d(TAG, "USB CONNECTED");

            // findViewById(R.id.textView).setVisibility(View.VISIBLE);
            //((TextView)findViewById(R.id.textView)).setText(paramIntent.getParcelableExtra("accessory").toString());


            if (paramIntent.getParcelableExtra("accessory") != null) {
                i.putExtra("accessory", paramIntent.getParcelableExtra("accessory"));
                ContextCompat.startForegroundService(this,i);
            }
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent paramIntent)
    {
        Log.i(TAG, "Got new intent: " + paramIntent);
        super.onNewIntent(paramIntent);
        setIntent(paramIntent);
    }

}

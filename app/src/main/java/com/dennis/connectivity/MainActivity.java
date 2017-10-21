package com.dennis.connectivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    BluetoothAdapter bluetoothAdapter;
    private static final int CAMERA_REQUEST_CODE=1;
    private static final int CAMERA_PRMISSION_REQUEST_CODE=2;
    private ImageView pic;
    Button onoff;


//    mONITORS THE BLUTOOTH STATESA NS UBLISHES THEM TO THE APPLICATION
//

    private final BroadcastReceiver myreciever=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Recieveing.......................");
            String action =intent.getAction();
//-------BLUTOOTH ADAPTER IS THE REAL BLUTOOTH OBJECT-------------------//
            if(action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)){

                int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: STATE ON");
                        Toast.makeText(MainActivity.this, "ON", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: TURNING ON");
                        Toast.makeText(MainActivity.this, "TURNING ON", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: TURNING OFF");
                        Toast.makeText(MainActivity.this, "TURNING OFF", Toast.LENGTH_SHORT).show();
                        break;

                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(MainActivity.this, " OFF", Toast.LENGTH_SHORT).show();
                        break;

                }

            }


        }
    };
//get called when an image is taken,resultcode okay mans we got n image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult: Getting results from the camera");
            
            if (resultCode==RESULT_OK){
                Log.d(TAG, "onActivityResult: Picture taken");

                Uri imageUri=data.getData();
                Log.d(TAG, "onActivityResult: The image Uri is"+imageUri);
                
                Bundle bundle=data.getExtras();
                Bitmap bitmap=(Bitmap) bundle.get("data");
                
                if (bitmap!=null){
                    Log.d(TAG, "onActivityResult: We have a pictre");

                    pic.setImageBitmap(bitmap);

                    showShareOptions();
                }
                
                
            }
        }
    }

    private boolean showShareOptions() {
        Log.d(TAG, "showShareOptions: Tryimg to share the taken image");
        return true;
    }
//FOR SHOWING THE MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        if (showShareOptions()){
            return true;
        }
      return super.onCreateOptionsMenu(menu);
    }
//CALLED FIRST WHEN THE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: CREATED");
pic=findViewById(R.id.picholder);
            ImageButton imageButton=findViewById(R.id.takepic);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkUserPermissionsForCamera();
                }
            });
        onoff=findViewById(R.id.onoff);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        onoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableDisableBluetooth();}
        });

    }
//hecking for people with android 6 and above
    private void checkUserPermissionsForCamera() {
        String [] permissions={Manifest.permission.CAMERA,Manifest.permission.BLUETOOTH};
        ActivityCompat.requestPermissions(this,permissions,CAMERA_PRMISSION_REQUEST_CODE);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode==CAMERA_PRMISSION_REQUEST_CODE){
            if (grantResults.length>0){
                openCamera();
            }else {
                ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
            }
        }
        
        
        
    }
//------------CALLED ONLY WHEN PERMISSIONS ARE GRANTED
    private void openCamera() {
        Log.d(TAG, "openCamera: Oppenning the camera");
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i,CAMERA_REQUEST_CODE);
    }

    private void enableDisableBluetooth() {
        Log.d(TAG, "enableDisableBluetooth: Enabling....................");
        if (bluetoothAdapter==null){
            Log.d(TAG, "enableDisableBluetooth: Cant find bluetooth");
            Toast.makeText(this, "No btn on device", Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled()){
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
            IntentFilter intentFilter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

            registerReceiver(myreciever,intentFilter);
        }
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
            IntentFilter intentFilter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

            registerReceiver(myreciever,intentFilter);
        }
    }
//WHEN THE ACTIVITY GETS CDESTROY THE RECIEVER HAS TO BE TURNED OFF
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        unregisterReceiver(myreciever);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (!bluetoothAdapter.isEnabled()){
            enableDisableBluetooth();
            Toast.makeText(this, "Bluetooth is not enabled\n Enable it first", Toast.LENGTH_SHORT).show();
        }else if (bluetoothAdapter.isEnabled()){

            //------------------GET INTENT TO SHARE IMAGE CONTENT
            Intent i=new Intent(Intent.ACTION_SEND);
//            i.putExtra(Intent.ACTION_ATTACH_DATA,);
            //--ADD THE IMAGE WE CAPTURED
            i.setType("text/plain");
            startActivity(i);

            startActivity(i);

        }
        return super.onOptionsItemSelected(item);
    }
}

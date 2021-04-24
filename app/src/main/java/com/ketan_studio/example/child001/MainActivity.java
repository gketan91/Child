package com.ketan_studio.example.child001;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    static MainActivity instance;
    FusedLocationProviderClient fusedLocationProviderClient;
    SharedPreferences LOGINpreferences;
    public static final String TAG_SEND_DATA = "Sending data to server";
    public String str_number, str_contact_name, str_call_type, str_call_full_date,
            str_call_date, str_call_time, str_call_duration;

    Cursor cursor ;
    String name, phonenumber ;
    String number,body,date;
    String stringBuffer;
    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        CheckLoginOrNot();



    }//onCreate

    private void SettingUpPeriodicWork() {
        // Create Network constraint
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build();


        PeriodicWorkRequest periodicSendDataWork =
                new PeriodicWorkRequest.Builder(SendDataWorker.class, 15, TimeUnit.MINUTES)
                        .addTag(TAG_SEND_DATA)
                        .setConstraints(constraints)
                        // setting a backoff on case the work needs to retry
                        //.setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(periodicSendDataWork);
    }

    private void CheckLoginOrNot() {
        LOGINpreferences = getSharedPreferences("LOGIN_PREFERENCE",MODE_PRIVATE);
        String LOGEDIN = LOGINpreferences.getString("ISLOGIN","NO");
        if (LOGEDIN.equals("YES")){
//            RequestPermissionMain();
            SettingUpPeriodicWork();

//            Read_SMS();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //For Contact First Time Load
                            //For Contact First Time Load
                            SharedPreferences preferences = getSharedPreferences("ContactPREFERENCE",MODE_PRIVATE);
                            String FirstTime = preferences.getString("FIRSTTIMELogin","NO");
                            if (FirstTime.equals("YES")){

                            }else {

                                EnableRuntimePermissionForContact();//FUCN
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("FIRSTTIMELogin","YES");
                                editor.apply();

                            }//For Contact First Time Load END

                        }
                    });
                }
            }, 4000);


            Handler handlerforcallLog = new Handler();
            handlerforcallLog.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //For Contact First Time Load
                            SharedPreferences preferences = getSharedPreferences("CallLogPREFERENCE",MODE_PRIVATE);
                            String FirstTime = preferences.getString("FIRSTTIMELogin","NO");
                            if (FirstTime.equals("YES")){

                            }else {
                                GetAllCallLogs();//FUCN
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("FIRSTTIMELogin","YES");
                                editor.apply();
                            }//For Contact First Time Load END

                        }
                    });
                }
            }, 9000);


            Toast.makeText(instance, "LOGIN", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(instance, "NOT LOGIN", Toast.LENGTH_SHORT).show();
        }
    }


    public void GetAllCallLogs() {
        String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            Toast.makeText(MainActivity.this, "Permission Missing", Toast.LENGTH_SHORT).show();
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cursor = MainActivity.this.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);

        //looping through the cursor to add data into arraylist
        while (cursor.moveToNext()){
            str_number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            str_contact_name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            str_contact_name = str_contact_name==null || str_contact_name.equals("") ? "Unknown" : str_contact_name;
            str_call_type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            str_call_full_date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
            str_call_duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                    "dd MMM yyyy");
            str_call_date = dateFormatter.format(new Date(Long.parseLong(str_call_full_date)));

            SimpleDateFormat timeFormatter = new SimpleDateFormat(
                    "HH:mm:ss");
            str_call_time = timeFormatter.format(new Date(Long.parseLong(str_call_full_date)));



            switch(Integer.parseInt(str_call_type)){
                case CallLog.Calls.INCOMING_TYPE:
                    str_call_type = "Incoming";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    str_call_type = "Outgoing";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    str_call_type = "Missed";
                    break;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    str_call_type = "Voicemail";
                    break;
                case CallLog.Calls.REJECTED_TYPE:
                    str_call_type = "Rejected";
                    break;
                case CallLog.Calls.BLOCKED_TYPE:
                    str_call_type = "Blocked";
                    break;
                case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                    str_call_type = "Externally Answered";
                    break;
                default:
                    str_call_type = "NA";
            }




            SharedPreferences UsernamePreference = getSharedPreferences("UsernameCHILDPREFERENCE",MODE_PRIVATE);
            String UserChildPREFERENCE = UsernamePreference.getString("UsernameCHILD","");
            if (!UserChildPREFERENCE.equals("")){
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Child");
                HashMap<String,Object> mapping = new HashMap<>();
                mapping.put("CallDate",str_call_date);
                mapping.put("Call_Duration",str_call_duration);
                mapping.put("Call_Time",str_call_time);
                mapping.put("Call_Type",str_call_type);
                mapping.put("Contact_Name",str_contact_name);
                mapping.put("Call_Number",str_number);
                String result = str_call_date+str_call_time;
                myRef.child(UserChildPREFERENCE).child("CallLog").child(str_call_full_date).setValue(mapping);
                Toast.makeText(getApplicationContext(), "Activated", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(instance, "Empty", Toast.LENGTH_SHORT).show();

            }

        }
    }



    public void RequestPermissionMain() {
        ActivityCompat.requestPermissions(MainActivity.this
                ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                ,100);
        ActivityCompat.requestPermissions(MainActivity.this
                ,new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.PROCESS_OUTGOING_CALLS,
                        Manifest.permission.READ_PHONE_STATE}
                ,100);
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        updateLocation();
                        Toast.makeText(instance, "PermissionGrantedResponse", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(MainActivity.this
                                , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                                , 100);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    public  void updateLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {

                        String longitude = (String.valueOf(location.getLongitude()));
                        String latatitude = (String.valueOf(location.getLatitude()));

                        Toast.makeText(MainActivity.this, "Location 1:" + longitude +"" + latatitude, Toast.LENGTH_SHORT).show();
                        SaveLongitudetoDataBase(longitude,latatitude);
                    } else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);
                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();

                                String lo = (String.valueOf(location1.getLongitude()));
                                String lat = (String.valueOf(location1.getLatitude()));
                                Toast.makeText(MainActivity.this, "Location 2:" + lo+"" + lat, Toast.LENGTH_SHORT).show();
                            }
                        };
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                locationCallback, Looper.myLooper());
                    }
                }
            });
        }
        else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            ));
        }
    }

    public void SaveLongitudetoDataBase(String longitude, String latatitude) {

        SharedPreferences UsernamePreference = getSharedPreferences("UsernameCHILDPREFERENCE",MODE_PRIVATE);
        String UserChildPREFERENCE = UsernamePreference.getString("UsernameCHILD","");
        Toast.makeText(instance, "PRE" + UserChildPREFERENCE, Toast.LENGTH_SHORT).show();
        if(!UserChildPREFERENCE.equals(null)){
            Toast.makeText(MainActivity.this, UserChildPREFERENCE+"", Toast.LENGTH_SHORT).show();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Child");//Adding to child database
            HashMap<String,Object> map = new HashMap<>();
            map.put("LONGITUDE",longitude);
            map.put("LATITUDE",latatitude);
            myRef.child(UserChildPREFERENCE).child("Location").updateChildren(map)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("JOB","Added to DAta base");
                            Toast.makeText(MainActivity.this, "Added to Child DAta base", Toast.LENGTH_SHORT).show();
                            SharedPreferences ParentUID = getSharedPreferences("ParentUIDPREFERENCE",MODE_PRIVATE);
                            String ParentUIDPREFERENCE = ParentUID.getString("ParentUID","");
                            if(!ParentUIDPREFERENCE.equals("")){
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference ParentRef = database.getReference("Parent");
                                HashMap<String,Object> Pmap = new HashMap<>();
                                Pmap.put("LONGITUDE",longitude);
                                Pmap.put("LATITUDE",latatitude);
                                ParentRef.child(ParentUIDPREFERENCE).child("CHILD").child(UserChildPREFERENCE).child("Location").updateChildren(Pmap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(MainActivity.this, "Added To Parent Database", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }else {
                                Toast.makeText(MainActivity.this, "Parent Failed Data Adding", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed Data Adding", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(instance, "Error Shared Preferencr User", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences preferences = getSharedPreferences("PREFERENCE",MODE_PRIVATE);
        String FirstTime = preferences.getString("FIRSTTIME","NO");
        if (FirstTime.equals("YES")){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.darkmode_menu_item,menu);
        }else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.login_menu_item,menu);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("FIRSTTIME","YES");
            editor.apply();
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.login_menu_display:
                Intent i = new Intent(this,LoginActivity.class);
                overridePendingTransition(0,0);
                startActivity(i);
                finish();
                break;
            case R.id.darkmode_btn:
                int ss = AppCompatDelegate.getDefaultNightMode();
                if (ss == 2){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    break;
                }else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    overridePendingTransition(0,0);
                    finish();
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }




    //Contact permission
    public void EnableRuntimePermissionForContact(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.READ_CONTACTS))
        {


//            Toast.makeText(MainActivity.this,"CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.READ_CONTACTS}, 1);

        }

    }


    public void GetContactDetails(){

        SharedPreferences UsernamePreference = getSharedPreferences("UsernameCHILDPREFERENCE",MODE_PRIVATE);
        String UserChildName = UsernamePreference.getString("UsernameCHILD","");
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Child").child(UserChildName).child("Contact");
            HashMap<String,Object> mapp = new HashMap<>();
            mapp.put("Name",name);
            mapp.put("PhoneNumber",phonenumber);
            myRef.push().setValue(mapp);
        }

        cursor.close();

    }
    public void Read_SMS(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);


        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                    date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    SimpleDateFormat dateFormatter = new SimpleDateFormat(
                            "dd MMM yyyy");
                    String messdate = dateFormatter.format(new Date(Long.parseLong(date)));

                    SimpleDateFormat timeFormatter = new SimpleDateFormat(
                            "HH:mm:ss");
                    String mess_time = timeFormatter.format(new Date(Long.parseLong(date)));

                    number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    Date dateFormat = new Date(Long.valueOf(date));
                    String type;
                    switch (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                            type = "inbox";
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_SENT:
                            type = "sent";
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                            type = "outbox";
                            break;
                        default:
                            break;


                    }
                    stringBuffer = stringBuffer + msgData + "                  NExt" +
                            "";
                    SharedPreferences UsernamePreference = getSharedPreferences("UsernameCHILDPREFERENCE",MODE_PRIVATE);
                    String UserChildName = UsernamePreference.getString("UsernameCHILD","");
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("Child").child(UserChildName).child("MESSAGE");
                    HashMap<String,Object> mapp = new HashMap<>();
                    mapp.put("Date",messdate+" "+mess_time);
                    mapp.put("Number",number);
                    mapp.put("Body",body);
                    myRef.child(date).updateChildren(mapp);

                    // use msgData

                }


        }while (cursor.moveToNext()) ;
            Toast.makeText(this, "LLLL :" + stringBuffer, Toast.LENGTH_SHORT).show();
        }
        else {
            // empty box, no SMS
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case 1:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    GetContactDetails();
//                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }
}
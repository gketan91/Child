package com.ketan_studio.example.child001;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    Button submit;
    EditText email,pass;
    TextView forgetPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText)findViewById(R.id.email_et_login);
        pass = (EditText)findViewById(R.id.pass_et_login);
        forgetPass = (TextView)findViewById(R.id.forgetPass_tv_login);
        submit = (Button)findViewById(R.id.submit_button_login);

    }

    public void Login(View view) {
        String username = email.getText().toString();
        String keyuser = pass.getText().toString();
        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Username is Empty", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(keyuser)){
            Toast.makeText(this, "Key is Empty", Toast.LENGTH_SHORT).show();
        }else {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Child");
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(username).exists()){
                        Toast.makeText(LoginActivity.this, "Child Exist", Toast.LENGTH_SHORT).show();
                        String name =snapshot.child(username).child("Name").getValue().toString();
                        String KEY =snapshot.child(username).child("Key").getValue().toString();
                        String ParentUID =snapshot.child(username).child("Parent UID").getValue().toString();
                        Toast.makeText(LoginActivity.this, ""+name+KEY, Toast.LENGTH_SHORT).show();
                        if (KEY.equals(keyuser)){
                            SharedPreferences preferences = getSharedPreferences("UsernameCHILDPREFERENCE",MODE_PRIVATE);
                            SharedPreferences.Editor preferenceseditor = preferences.edit();
                            preferenceseditor.putString("UsernameCHILD",username);
                            preferenceseditor.commit();

                            SharedPreferences ParentUIDPreferences = getSharedPreferences("ParentUIDPREFERENCE",MODE_PRIVATE);
                            SharedPreferences.Editor ParentUIDPreferencesEditor = ParentUIDPreferences.edit();
                            ParentUIDPreferencesEditor.putString("ParentUID",ParentUID);
                            ParentUIDPreferencesEditor.commit();



                            Toast.makeText(LoginActivity.this, "Key Match", Toast.LENGTH_SHORT).show();
                            SharedPreferences LOGINpreferences = getSharedPreferences("LOGIN_PREFERENCE",MODE_PRIVATE);
                            SharedPreferences.Editor editor = LOGINpreferences.edit();
                            editor.putString("ISLOGIN","YES");
                            editor.apply();
                            Intent i = new Intent(LoginActivity.this,MainActivity.class);
                            i.putExtra("User",username);

                            SharedPreferences ChildName = getSharedPreferences("UsernameCHILDPREFERENCE",MODE_PRIVATE);
                            String ParentUIDPREFERENCE = ChildName.getString("UsernameCHILD","");

                            FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                            DatabaseReference myReff = database1.getReference("Child").child(ParentUIDPREFERENCE);
                            HashMap<String,Object> map = new HashMap<>();
                            map.put("IsLogin","true");
                            myReff.updateChildren(map);

                            overridePendingTransition(0,0);
                            finish();
//                            Toast.makeText(LoginActivity.this, "Please Restart The Application", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "Wrong KEY", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(LoginActivity.this, "Invalid Child", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(LoginActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
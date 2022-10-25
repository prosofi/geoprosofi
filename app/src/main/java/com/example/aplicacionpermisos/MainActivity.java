package com.example.aplicacionpermisos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private int LOCATION_PERMISSION_CODE = 1;
    private TextView textDatos;
    private CheckBox checkBox;
    private Button buttonRequest;
    private ScrollView sv;

    //Tests
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    public static final String PATH_ENTIDADES="entidades/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textDatos = findViewById(R.id.text_data_policy);
        checkBox = findViewById(R.id.checkBox);
        buttonRequest = findViewById(R.id.button);
        sv =  findViewById(R.id.scrollviewmain);

        buttonRequest.setVisibility(View.GONE);

        //Tests
        database = FirebaseDatabase.getInstance();
        //loadEntidades();





        textDatos.setMovementMethod(LinkMovementMethod.getInstance());


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked){
                    buttonRequest.setVisibility(View.VISIBLE);
                    sv.post(new Runnable() {
                        public void run() {
                            sv.scrollTo(0, sv.getBottom());
                        }
                    });                }
                else{
                    buttonRequest.setVisibility(View.GONE);
                }
            }
        });


        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(getApplicationContext(), MapDisplayActivity.class);
                    loadEntidades();
                    //startActivity(i);
                } else {
                    requestStoragePermission();
                }
            }
        });
    }
    //Tests
    private void loadEntidades() {
        myRef = database.getReference(PATH_ENTIDADES);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Entidad entidad = singleSnapshot.getValue(Entidad.class);
                    Log.i("TEST", "Encontró entidad: " + entidad.getNombre());
                    Toast.makeText(MainActivity.this, "Se encontraron los datos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TEST", "error en la consulta", databaseError.toException());
            }
        });
    }



    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
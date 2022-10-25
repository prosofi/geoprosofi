package com.example.aplicacionpermisos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

public class GraciasActivity extends AppCompatActivity {

    private Button backbutton;
    private Button buttonCerrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gracias);

        backbutton = findViewById(R.id.button_back);
        buttonCerrar = findViewById(R.id.button_cerrar);

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapDisplayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        buttonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
                System.exit(0);            }
        });


        String nombre = getIntent().getExtras().getString("nombre");
        String nombreRegistrador = getIntent().getExtras().getString("nombreRegistrador");
        String barrio = getIntent().getExtras().getString("barrio");
        String direccion = getIntent().getExtras().getString("direccion");
        String latitud = getIntent().getExtras().getString("latitud");
        String longitud = getIntent().getExtras().getString("longitud");

        MultiAutoCompleteTextView tv = findViewById(R.id.textGracias);

        String text = "Ha sido registrado exitosamente!\nNombre de la entidad: " + nombre +"\nNombre del registrador: "+nombreRegistrador+
                "\nBarrio: " + barrio +
                "\nDirección: "+ direccion + "\nLatitud: " + latitud +"\nLongitud: " + longitud  +
                "\nPuede agregar otro punto seleccionando la opción de volver."
                +"\n\nSi no va a agregar más puntos, ya puede desinstalar la aplicación";
        tv.setText(text);
    }
}
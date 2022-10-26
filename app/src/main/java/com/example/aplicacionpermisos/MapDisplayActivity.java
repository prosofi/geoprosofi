package com.example.aplicacionpermisos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MapDisplayActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CHECK_SETTINGS = 101;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private int LOCATION_PERMISSION_CODE = 1;
    private TextView textViewLatitud;
    private TextView textViewLongitud;
    private Button buttonSend;
    private double latitud, longitud;
    private EditText editDireccion, editNombre, editBarrio, editNombreRegistrador;
    //private MapsFragment mapsFragment;
    private MapView mapView;
    private LatLng lastLocation ;
    private GoogleMap map;

    //Firebase conexion
    public static final String PATH_ENTIDADES="entidades/";
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);

        textViewLatitud = findViewById(R.id.textViewLatitud);
        textViewLongitud = findViewById(R.id.textViewLongitud);
        editBarrio = findViewById(R.id.textoBarrio);
        editDireccion = findViewById(R.id.textoDireccion);
        editNombre = findViewById(R.id.textoNombre);
        editNombreRegistrador = findViewById(R.id.textoNombreRegistrador);

        buttonSend = findViewById(R.id.buttonRefresh);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();

        lastLocation = new LatLng(0,0);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) findViewById(R.id.mapView);


        // Gets to GoogleMap from the MapView and does initialization stuff

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        ///////

        //FIREBASE CONEXION
        database = FirebaseDatabase.getInstance();


        if (ContextCompat.checkSelfPermission(MapDisplayActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
            displayLocation();
        } else {
            requestLocationPermission();
        }

        // listener que recibe actualizaciones de la posicion y llama a displayLocation con
        // cada actualizacion
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                Log.i("LOCATION", "Location update in the callback: " + location);
                if (location != null) {
                    latitud = location.getLatitude();
                    longitud = location.getLongitude();
                    displayLocation();
                    updateMap();
                }
            }
        };

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates(); //Todas las condiciones para recibir localizaciones
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {// Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MapDisplayActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

        startLocationUpdates();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapDisplayActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setMessage("Está a punto de enviar esta información ¿Está seguro de que" +
                        " los datos son correctos y de que en este momento se " +
                        "encuentra en la dirección que acaba de indicar?");
                builder.setIcon(R.drawable.ic_launcher_background);
                builder.setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        //sendRequest();
                        if(enviarDatos())
                        {
                            Intent i = new Intent(MapDisplayActivity.this, GraciasActivity.class);
                            i.putExtra("direccion", editDireccion.getText().toString());
                            i.putExtra("nombre", editNombre.getText().toString());
                            i.putExtra("nombreRegistrador", editNombreRegistrador.getText().toString());
                            i.putExtra("barrio", editBarrio.getText().toString());
                            i.putExtra("latitud", String.valueOf(latitud));
                            i.putExtra("longitud", String.valueOf(longitud));
                            startActivity(i);
                        }
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void updateMap() {

        LatLng current = new LatLng(latitud, longitud);

        if(!parecido(current , lastLocation)){
            lastLocation = current;
            map.clear();
            map.addMarker(new MarkerOptions().position(current).title(editNombre.getText().toString()));

            map.moveCamera(CameraUpdateFactory.newLatLng(current));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 15);
            map.animateCamera(cameraUpdate);
        }


    }

    private boolean parecido(LatLng l1 , LatLng l2){
        if(Math.abs(l1.latitude - l2.latitude) < 0.001 &&
        Math.abs(l1.longitude - l2.longitude ) < 0.001){
            return true;
        }
        return false;
    }

    private void displayLocation() {
        textViewLatitud.setText(String.valueOf(latitud));
        textViewLongitud.setText(String.valueOf(longitud));
    }

    private void getLocation() {
        Log.i("getLocation->latitud", String.valueOf(latitud));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "no sirve", Toast.LENGTH_SHORT).show();
        } else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitud = location.getLatitude();
                                longitud = location.getLongitude();
                                displayLocation();
                            }
                        }
                    });

        }

    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Se necesita localización")
                    .setMessage("Esta aplicación necesita el permiso de GPS y localización para funcionar")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapDisplayActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
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
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void startLocationUpdates() { //Verificación de permiso!!
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
        Log.i("...", "startLocationUpdates: entre starlocationUpdates");
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); //tasa de refresco en milisegundos
        locationRequest.setFastestInterval(5000); //máxima tasa de refresco
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    // Si no se tiene permisos...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this,
                            "Sin acceso a localización, hardware deshabilitado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }



    //Método para enviar datos ACTUALIZADO 25-10-2022
    private boolean enviarDatos()
    {
        //Obtenemos los datos de los EditText
        String nombre = editNombre.getText().toString();
        String nombreRegistrador = editNombreRegistrador.getText().toString();
        String barrio = editBarrio.getText().toString();
        String direccion = editDireccion.getText().toString();
        String latitud = textViewLatitud.getText().toString();
        String longitud = textViewLongitud.getText().toString();

        //Verificamos que los campos no estén vacíos
       if(nombre.isEmpty() || barrio.isEmpty() || direccion.isEmpty() || latitud.isEmpty() || longitud.isEmpty())
        {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

       //Crear objeto de la clase Entidad y subirlo a la BD
        myRef = database.getReference(PATH_ENTIDADES);
        String key = myRef.push().getKey();
        Entidad nuevaEntidad = new Entidad();
        nuevaEntidad.setNombre(nombre);
        nuevaEntidad.setNombreRegistrador(nombreRegistrador);
        nuevaEntidad.setBarrio(barrio);
        nuevaEntidad.setDireccion(direccion);
        nuevaEntidad.setLatitud(latitud);
        nuevaEntidad.setLongitud(longitud);
        myRef=database.getReference(PATH_ENTIDADES+key);
        myRef.setValue(nuevaEntidad);
        //Toast.makeText(this, "Nueva entidad creada correctamente con el código "+key, Toast.LENGTH_SHORT).show();
        Log.i("TEST", "enviarDatos: "+key);
        return true;

    }

    /*
    // envia la peticion al web service
    private boolean sendRequest() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url;
        try {
            url = makeURLString();
        } catch (Exception e) {
            return false;
        }

        final boolean[] result = {false};

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent i = new Intent(getApplicationContext(), GraciasActivity.class);
                        i.putExtra("direccion", editDireccion.getText().toString());
                        i.putExtra("nombre", editNombre.getText().toString());
                        i.putExtra("barrio", editBarrio.getText().toString());
                        i.putExtra("latitud", String.valueOf(latitud));
                        i.putExtra("longitud", String.valueOf(longitud));
                        startActivity(i);

                        result[0] = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Hubo un error, intentalo de nuevo mas tarde", Toast.LENGTH_LONG).show();
                result[0] = false;
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        Log.i("debug", "onResponse: " + String.valueOf(result[0]));
        return result[0];

    }

    // agrega la informacion que metio el usuario a la url
    private String makeURLString() {
        // esta url deberia cambiar
        String service = "https://us-central1-prosofi-ea89e.cloudfunctions.net/saveLocation?";
        String url = "";

        try {
            url += "latitud=";
            url += URLEncoder.encode(String.valueOf(latitud), StandardCharsets.UTF_8.toString());
            url += "&";
            url += "longitud=";
            url += URLEncoder.encode(String.valueOf(longitud), StandardCharsets.UTF_8.toString());
            url += "&";
            url += "nombre=";
            url += URLEncoder.encode(editNombre.getText().toString(), StandardCharsets.UTF_8.toString());
            url += "&";
            url += "direccion=";
            url += URLEncoder.encode(editDireccion.getText().toString(), StandardCharsets.UTF_8.toString());
            url += "&";
            url += "barrio=";
            url += URLEncoder.encode(editBarrio.getText().toString(), StandardCharsets.UTF_8.toString());

        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getCause());
        }


        String res = service + url;
        Log.i("string", "makeURLString: " + res);
        return res;


    }
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        map.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            map.setMyLocationEnabled(true);
        }


        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(4.518640, -74.092700), 15);
        map.animateCamera(cameraUpdate);
        map.getUiSettings().setMapToolbarEnabled(false);

    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
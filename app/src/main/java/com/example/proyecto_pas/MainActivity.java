package com.example.proyecto_pas;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_pas.Interfaz.JsonPlaceholderApi;
import com.example.proyecto_pas.Model.Entrada;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    public List<Entrada> entradaList; //Aqui se guardan todas las estaciones de carga
    EditText txtRango;
    GoogleMap mMap;
    Button btn_exit;
    Button btn_apply;
    FirebaseAuth mAuth;
    LatLng myPosicion;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtRango = findViewById(R.id.editTextNumberDecimal);
        btn_exit = findViewById(R.id.exitButton);
        btn_apply = findViewById(R.id.applyButton);
        
        mAuth = FirebaseAuth.getInstance();        

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient((this));

        //Cargar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Hacer llamada de la API
        getPosts();
        
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtRango.getText().toString() != null && !txtRango.getText().toString().isEmpty()){
                    double rango = Double.parseDouble(txtRango.getText().toString());

                    if (rango > 0){
                        mostrarEstacionesEnRango(rango);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Introduzca un rango válido", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void getPosts() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://datosabiertos.jcyl.es/web/jcyl/risp/es/energia/vehiculo_electrico/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceholderApi jsonPlaceholderApi = retrofit.create(JsonPlaceholderApi.class);

        Call<List<Entrada>> call = jsonPlaceholderApi.getEntradas();

        call.enqueue(new Callback<List<Entrada>>() {
            @Override
            public void onResponse(Call<List<Entrada>> call, Response<List<Entrada>> response) {
                //Aqui se maneja la respuesta cuando ha ido bien
                if (!response.isSuccessful()) {
                    //Cuando haya problema con la conexion a la API
                    Toast.makeText(MainActivity.this, "Codigo Error: "+ response.code(), Toast.LENGTH_SHORT).show();//Con el codigo de respuesta se puede saber que ha ocurrido
                    return;
                }

                entradaList = response.body();
            }

            @Override
            public void onFailure(Call<List<Entrada>> call, Throwable throwable) {
                //Aqui se manejan los errores fatales que provocan un throwable
                Toast.makeText(MainActivity.this,throwable.getMessage(), Toast.LENGTH_LONG).show();//Mostramos el mensaje del throwable

            }
        });
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null){
                            //Sacamos latitud y longitud
                            Log.e("Location","Latitude: "+location.getLatitude()+"\n Longitud: "+location.getLongitude());
                            myPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosicion));
                        }
                    }
                });

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        //Se genera un evento al mantener pulsado el mapa

    }

    public double gradosARadianes(double grados){
        return (grados*(Math.PI/180));
    }
    public double distanciaKmEntreCoordenadas(double latA,double lonA,double latB,double lonB){
        //calcular la distancia en Km entre dos coordenadas

        float radioTierra = 6371;

        double dLat= gradosARadianes(latB - latA);
        double dLon= gradosARadianes(lonB - lonA);

        latA = gradosARadianes(latA);
        latB = gradosARadianes(latB);

        double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.sin(dLon/2)*Math.sin(dLon/2)*Math.cos(latA)*Math.cos(latB);

        double c = 2* Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return radioTierra*c;
    }
    public List<Entrada> calcularEstacionesCercanas(double rango){

        List<Entrada> entradasRango = new ArrayList<Entrada>();

        for(Entrada entrada: entradaList){
            double lat2 = entrada.getFields().getDd().get(0);
            double lon2 = entrada.getFields().getDd().get(1);

            double dist = distanciaKmEntreCoordenadas(myPosicion.latitude, myPosicion.longitude, lat2, lon2);

            if(Math.abs(dist) < rango){
                entradasRango.add(entrada);
            }
        }
        return entradasRango;
    }

    public void mostrarEstacionesEnRango(double rango){
        List<Entrada> entradasMostrar = new ArrayList<Entrada>();

        entradasMostrar = calcularEstacionesCercanas(rango);

        if(entradasMostrar != null){
            mMap.clear();

            for (Entrada entrada : entradasMostrar) {
                LatLng posicionNew = new LatLng(entrada.getFields().getDd().get(0), entrada.getFields().getDd().get(1));
                mMap.addMarker(new MarkerOptions().position(posicionNew).title(entrada.getFields().getNombre()).snippet(entrada.getFields().getOperador()));
            }
        }
    }

}
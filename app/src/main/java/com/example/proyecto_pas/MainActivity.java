package com.example.proyecto_pas;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    public List<Entrada> entradaList; //Aqui se guardan todas las estaciones de carga
    List<Entrada> entradaList_diez;
    EditText txtLatitud, txtLongitud;
    GoogleMap mMap;
    private TextView mJsonTextView;

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

        mJsonTextView = findViewById(R.id.jsonText);
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient((this));

        //Cargar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Hacer llamada de la API
        getPosts();



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
                    mJsonTextView.setText("Codigo: " + response.code());//Con el codigo de respuesta se puede saber que ha ocurrido
                    return;
                }

                entradaList = response.body();

                /*String content = "";
                content += "datasetid:" + entrada.getDatasetid() + "\n";
                content += "recordid:" + entrada.getRecordid() + "\n";
                content += "dms:" + entrada.getFields().getDms() + "\n";
                content += "tipo:" + entrada.getFields().getDms() + "\n";
                content += "dd1:" + entrada.getFields().getDd().get(0) + "\n";
                content += "dd2:" + entrada.getFields().getDd().get(1) + "\n\n";
                mJsonTextView.append(content);*/

            }

            @Override
            public void onFailure(Call<List<Entrada>> call, Throwable throwable) {
                //Aqui se manejan los errores fatales que provocan un throwable
                mJsonTextView.setText(throwable.getMessage()); //Mostramos el mensaje del throwable

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
        txtLatitud.setText(""+latLng.latitude);
        txtLongitud.setText(""+latLng.longitude);
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        //Se genera un evento al mantener pulsado el mapa
        txtLatitud.setText(""+latLng.latitude);
        txtLongitud.setText(""+latLng.longitude);

        List<Entrada> entradasMostrar = new ArrayList<Entrada>();

        entradasMostrar = calcularEstacionesCercanas(100.0);

        if(entradasMostrar != null){
            mMap.clear();

            for (Entrada entrada : entradasMostrar) {
                LatLng posicionNew = new LatLng(entrada.getFields().getDd().get(0), entrada.getFields().getDd().get(1));
                mMap.addMarker(new MarkerOptions().position(posicionNew).title(entrada.getFields().getNombre()).snippet(entrada.getFields().getOperador()));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(posicionNew));
            }
        }

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
}
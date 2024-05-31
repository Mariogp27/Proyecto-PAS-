package com.example.proyecto_pas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_pas.Interfaz.JsonPlaceholderApi;
import com.example.proyecto_pas.Model.Entrada;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    List<Entrada> entradaList; //Aqui se guardan todas las estaciones de carga

    Button btn_exit;

    FirebaseAuth mAuth;
    private TextView mJsonTextView;
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
        getPosts();

        mAuth = FirebaseAuth.getInstance();

        btn_exit = findViewById(R.id.exitButton);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

    }

    private void getPosts(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://datosabiertos.jcyl.es/web/jcyl/risp/es/energia/vehiculo_electrico/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceholderApi jsonPlaceholderApi = retrofit.create(JsonPlaceholderApi.class);

        Call<List<Entrada>> call = jsonPlaceholderApi.getEntradas();

        call.enqueue(new Callback<List<Entrada>>() {
            @Override
            public void onResponse(Call<List<Entrada>> call, Response<List<Entrada>> response) {
                //Aqui se maneja la respuesta cuando ha ido todo guay
                if(!response.isSuccessful()){
                    //Cuando haya problema con la conexion a la API
                    mJsonTextView.setText("Codigo: " + response.code());//Con el codigo de respuesta se puede saber que ha ocurrido
                    return;
                }

                entradaList = response.body();

                for(Entrada entrada: entradaList){
                    String content = "";
                    content += "datasetid:" + entrada.getDatasetid() + "\n";
                    content += "recordid:" + entrada.getRecordid() + "\n";
                    content += "dms:" + entrada.getFields().getDms() + "\n";
                    content += "tipo:" + entrada.getFields().getDms() + "\n";
                    content += "dd1:" + entrada.getFields().getDd().get(0) + "\n";
                    content += "dd2:" + entrada.getFields().getDd().get(1) + "\n\n";
                    mJsonTextView.append(content);
                }
            }

            @Override
            public void onFailure(Call<List<Entrada>> call, Throwable throwable) {
                //Aqui se manejan los errores fatales que provocan un throwable
                mJsonTextView.setText(throwable.getMessage()); //Mostramos el mensaje del throwable

            }
        });
    }




}
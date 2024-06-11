package com.example.proyecto_pas.Interfaz;

import com.example.proyecto_pas.Model.Entrada;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JsonPlaceholderApi {
    //Es la interfaz
    //Contiene el metodo que hace un get a toda la informacion
    //El Call nos devolvera un response del tipo Entrada (nuestro modelo)
    @GET("1284273412751.json") //Esta es la parte variable variable de la url
    Call<List<Entrada>> getEntradas();
}

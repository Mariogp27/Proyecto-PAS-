package com.example.proyecto_pas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    Button saveButton, readButton, updateButton, removeButton;
    EditText uploadData, uploadData2;
    DatabaseReference rootDatabaseref;
    TextView textView,textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadData = findViewById(R.id.uploadData);
        uploadData2 = findViewById(R.id.uploadData2);
        saveButton = findViewById(R.id.saveButton);
        readButton = findViewById(R.id.readButton);
        updateButton = findViewById(R.id.updateButton);
        removeButton = findViewById(R.id.removeButton);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        rootDatabaseref = FirebaseDatabase.getInstance("https://proyectopas-3b39d-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference().child("Location");

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootDatabaseref.child("User1").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            Map<String,Object> map = (Map<String, Object>) snapshot.getValue();

                            Object id = map.get("ID");
                            String name = (String) map.get("Name");

                            textView.setText(""+id);
                            textView2.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = Integer.parseInt(uploadData.getText().toString());
                String name = uploadData2.getText().toString();

                HashMap hashMap = new HashMap();
                hashMap.put("ID", id);
                hashMap.put("Name", name);
                rootDatabaseref.child("User1").setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(UploadActivity.this, "Dato correctamente añadido", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this, "Error", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int id = Integer.parseInt(uploadData.getText().toString());
                String name = uploadData2.getText().toString();
                HashMap hashMap = new HashMap();
                hashMap.put("Name", name);

                rootDatabaseref.child("User1").updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(UploadActivity.this, "Tus datos estan correctamente actualizados", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootDatabaseref.child("User1").child("ID").removeValue();
            }
        });
    }
}

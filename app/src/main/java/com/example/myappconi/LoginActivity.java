package com.example.myappconi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText edit_text_username, edit_text_password;
    private Button button_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edit_text_username = findViewById(R.id.edit_text_username);
        edit_text_password = findViewById(R.id.edit_text_password);
        button_login = findViewById(R.id.button_login);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // URL para el emulador, usando el puerto 80
                String url = "http://10.0.2.2/login.php";

                String username = edit_text_username.getText().toString().trim();
                String password = edit_text_password.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject postData = new JSONObject();
                try {
                    postData.put("username", username);
                    postData.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status = response.getString("status");
                                    if (status.equals("success")) {
                                        String rol = response.getString("rol");

                                        if (rol.equals("admin")) {
                                            Intent intent = new Intent(LoginActivity.this, PerfilAdminActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (rol.equals("usuario")) {
                                            Intent intent = new Intent(LoginActivity.this, PerfilUsuarioActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Rol desconocido", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        String errorMessage = response.getString("message");
                                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(LoginActivity.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(LoginActivity.this, "Error en la conexión: " + error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                );

                RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
                requestQueue.add(jsonObjectRequest);
            }
        });
    }
}
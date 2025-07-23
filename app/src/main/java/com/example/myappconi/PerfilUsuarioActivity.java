package com.example.myappconi;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class PerfilUsuarioActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        View headerInclude = findViewById(R.id.header_layout_include);
        if (headerInclude != null) {
            menuIcon = headerInclude.findViewById(R.id.ImageViewLateral);
        }

        if (menuIcon != null) {
            menuIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId(); // Obtener el ID del item seleccionado

                if (id == R.id.nav_logout) {
                    // Cierra la sesión
                    Intent intent = new Intent(PerfilUsuarioActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_equipos) { // <-- NUEVO CÓDIGO AQUÍ
                    Intent intent = new Intent(PerfilUsuarioActivity.this, EquiposActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                // Agrega más else if para otros ítems del menú si los tienes

                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
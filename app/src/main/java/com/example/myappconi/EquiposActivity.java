package com.example.myappconi;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Bloque de importaciones de Volley consolidado y limpio
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquiposActivity extends AppCompatActivity {

    private static final String TAG = "EquiposActivity";
    // URL base de tu servlet (ajusta si es necesario para tu dispositivo físico)
    private static final String BASE_URL = "http://10.0.2.2:8080/CONI1.0/EquipoServlet";
    // Si se usa un dispositivo físico en la misma red, se reemplaza 10.0.2.2 con la IP local del computador.


    private EditText etNInventario, etNSerie, etMarca, etRam, etDisco, etProcesador;
    private Spinner spinnerClase, spinnerTipo, spinnerEstado, spinnerFiltroEstado;
    private Button btnSubmit, btnCancelEdit;
    private TextView tvFormTitle;
    private ListView lvEquipos;

    private RequestQueue requestQueue;
    private boolean isEditing = false;
    private String currentEditNInventario = null; // Para guardar el n_inventario cuando se edita

    private ArrayAdapter<String> claseAdapter;
    private ArrayAdapter<String> tipoAdapter;
    private ArrayAdapter<String> estadoAdapter;
    private ArrayAdapter<String> filtroEstadoAdapter;
    private EquiposAdapter equiposAdapter;
    private List<Equipo> equiposList; // Lista de objetos Equipo

    // Mapeo de clases a tipos, similar a `tipoPorClases` en React
    private Map<String, String[]> tipoPorClases = new HashMap<String, String[]>() {{
        put("periferico", new String[]{"MOUSE", "TECLADO", "MONITOR", "IMPRESORA", "PROYECTOR", "PARLANTE"});
        put("equipo", new String[]{"LAPTOP", "ESCRITORIO", "TABLET"});
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipos);

        // Inicializar vistas
        etNInventario = findViewById(R.id.et_n_inventario);
        etNSerie = findViewById(R.id.et_n_serie);
        etMarca = findViewById(R.id.et_marca);
        etRam = findViewById(R.id.et_ram);
        etDisco = findViewById(R.id.et_disco);
        etProcesador = findViewById(R.id.et_procesador);
        spinnerClase = findViewById(R.id.spinner_clase);
        spinnerTipo = findViewById(R.id.spinner_tipo);
        spinnerEstado = findViewById(R.id.spinner_estado);
        spinnerFiltroEstado = findViewById(R.id.spinner_filtro_estado);
        btnSubmit = findViewById(R.id.btn_submit_equipo);
        btnCancelEdit = findViewById(R.id.btn_cancel_edit);
        tvFormTitle = findViewById(R.id.tv_form_title);
        lvEquipos = findViewById(R.id.lv_equipos);

        requestQueue = Volley.newRequestQueue(this);
        equiposList = new ArrayList<>();
        equiposAdapter = new EquiposAdapter(this, equiposList); // Necesitarás crear este adaptador
        lvEquipos.setAdapter(equiposAdapter);

        setupSpinners();
        setupListeners();
        fetchEquipos(""); // Cargar todos los equipos al iniciar
    }

    private void setupSpinners() {
        // Spinner Clase
        claseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"", "periferico", "equipo"});
        claseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClase.setAdapter(claseAdapter);

        // Spinner Tipo (se actualiza en base a la clase)
        tipoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(tipoAdapter);

        // Spinner Estado
        estadoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"", "DISPONIBLE", "ASIGNADO", "PENDIENTE"});
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(estadoAdapter);

        // Spinner Filtro Estado
        filtroEstadoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"TODOS", "DISPONIBLE", "ASIGNADO", "PENDIENTE"});
        filtroEstadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroEstado.setAdapter(filtroEstadoAdapter);

        // Listener para el spinner de clase para actualizar el spinner de tipo
        spinnerClase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedClase = (String) parent.getItemAtPosition(position);
                updateTipoSpinner(selectedClase);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private void updateTipoSpinner(String selectedClase) {
        tipoAdapter.clear();
        if (tipoPorClases.containsKey(selectedClase)) {
            tipoAdapter.add(""); // Opción predeterminada
            tipoAdapter.addAll(tipoPorClases.get(selectedClase));
        }
        tipoAdapter.notifyDataSetChanged();
        spinnerTipo.setSelection(0); // Restablecer selección
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> {
            if (isEditing) {
                handleActualizarEquipo();
            } else {
                handleAgregarEquipo();
            }
        });

        btnCancelEdit.setOnClickListener(v -> cancelarEdicion());

        spinnerFiltroEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFiltro = (String) parent.getItemAtPosition(position);
                if ("TODOS".equals(selectedFiltro)) {
                    fetchEquipos("");
                } else {
                    fetchEquipos(selectedFiltro);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        // Click largo en un elemento de la lista para editar/eliminar (simulando acciones)
        lvEquipos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Equipo equipo = equiposList.get(position);
                // Aquí podrías mostrar un AlertDialog con opciones de Editar y Eliminar
                // Por ahora, lo haremos directo con un click para iniciar edición.
                // En una app real, usarías un LongClickListener o un botón dentro de cada item.
                iniciarEdicion(equipo);
            }
        });

        // Para simular el "eliminar", podríamos añadir un LongClickListener
        lvEquipos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Equipo equipoToDelete = equiposList.get(position);
                handleEliminarEquipo(equipoToDelete.getN_inventario());
                return true; // Consume el evento
            }
        });
    }

    // --- Métodos de API (similares a los de equipo.js) ---

    private void fetchEquipos(String estadoFiltro) {
        String url = BASE_URL + "?accion=listar";
        if (!estadoFiltro.isEmpty()) {
            url += "&estado=" + estadoFiltro.toUpperCase();
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        equiposList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                Equipo equipo = new Equipo(
                                        obj.optString("n_inventario", ""),
                                        obj.optString("n_serie", ""),
                                        obj.optString("tipo", ""),
                                        obj.optString("clase", ""),
                                        obj.optString("marca", ""),
                                        obj.optString("ram", ""),
                                        obj.optString("disco", ""),
                                        obj.optString("procesador", ""),
                                        obj.optString("estado", "")
                                );
                                equiposList.add(equipo);
                            }
                            equiposAdapter.notifyDataSetChanged(); // Actualizar la ListView
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON array: " + e.getMessage());
                            Toast.makeText(EquiposActivity.this, "Error al procesar los datos de equipos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching equipos: " + error.toString());
                        Toast.makeText(EquiposActivity.this, "Error al cargar equipos: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

    private void verificarSerieUnica(String serie, final OnSerieCheckListener listener) {
        String url = BASE_URL + "?verificarSerie=" + serie;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean existe = response.getBoolean("existe");
                            listener.onResult(!existe); // Devuelve true si NO existe (es única)
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing serie check response: " + e.getMessage());
                            listener.onResult(false); // Asume que no es única si hay error
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error checking serie: " + error.toString());
                        Toast.makeText(EquiposActivity.this, "Error al verificar número de serie.", Toast.LENGTH_SHORT).show();
                        listener.onResult(false); // Asume que no es única si hay error de red
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    interface OnSerieCheckListener {
        void onResult(boolean isUnique);
    }

    private void handleAgregarEquipo() {
        final String nSerie = etNSerie.getText().toString().trim();
        final String clase = spinnerClase.getSelectedItem().toString();
        final String tipo = spinnerTipo.getSelectedItem().toString();
        final String marca = etMarca.getText().toString().trim();
        final String ram = etRam.getText().toString().trim();
        final String disco = etDisco.getText().toString().trim();
        final String procesador = etProcesador.getText().toString().trim();
        final String estado = spinnerEstado.getSelectedItem().toString();

        if (nSerie.isEmpty() || clase.isEmpty() || tipo.isEmpty() || marca.isEmpty() || estado.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos requeridos (Serie, Clase, Tipo, Marca, Estado).", Toast.LENGTH_LONG).show();
            return;
        }

        verificarSerieUnica(nSerie, new OnSerieCheckListener() {
            @Override
            public void onResult(boolean isUnique) {
                if (!isUnique) {
                    Toast.makeText(EquiposActivity.this, "El número de serie ya existe.", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject postData = new JSONObject();
                try {
                    postData.put("n_serie", nSerie);
                    postData.put("tipo", tipo);
                    postData.put("clase", clase);
                    postData.put("marca", marca);
                    postData.put("ram", ram);
                    postData.put("disco", disco);
                    postData.put("procesador", procesador);
                    postData.put("estado", estado);
                } catch (JSONException e) {
                    Log.e(TAG, "Error creating JSON for add: " + e.getMessage());
                    return;
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, postData,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.getBoolean("success")) { // Asumiendo que el Servlet devuelve un campo 'success'
                                        Toast.makeText(EquiposActivity.this, "Equipo registrado correctamente", Toast.LENGTH_SHORT).show();
                                        limpiarCampos();
                                        fetchEquipos(""); // Recargar lista
                                    } else {
                                        Toast.makeText(EquiposActivity.this, "Error al registrar: " + response.optString("message", "Error desconocido"), Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error parsing add response: " + e.getMessage());
                                    Toast.makeText(EquiposActivity.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Error adding equipo: " + error.toString());
                                Toast.makeText(EquiposActivity.this, "Error al conectar con el servidor para agregar.", Toast.LENGTH_LONG).show();
                            }
                        });
                requestQueue.add(jsonObjectRequest);
            }
        });
    }


    private void handleActualizarEquipo() {
        final String nInventario = etNInventario.getText().toString().trim();
        final String nSerie = etNSerie.getText().toString().trim();
        final String clase = spinnerClase.getSelectedItem().toString();
        final String tipo = spinnerTipo.getSelectedItem().toString();
        final String marca = etMarca.getText().toString().trim();
        final String ram = etRam.getText().toString().trim();
        final String disco = etDisco.getText().toString().trim();
        final String procesador = etProcesador.getText().toString().trim();
        final String estado = spinnerEstado.getSelectedItem().toString();

        if (nInventario.isEmpty() || nSerie.isEmpty() || clase.isEmpty() || tipo.isEmpty() || marca.isEmpty() || estado.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos para actualizar.", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject putData = new JSONObject();
        try {
            putData.put("n_inventario", nInventario);
            putData.put("n_serie", nSerie);
            putData.put("tipo", tipo);
            putData.put("clase", clase);
            putData.put("marca", marca);
            putData.put("ram", ram);
            putData.put("disco", disco);
            putData.put("procesador", procesador);
            putData.put("estado", estado);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON for update: " + e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, BASE_URL, putData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getBoolean("success")) {
                                Toast.makeText(EquiposActivity.this, "Equipo actualizado exitosamente.", Toast.LENGTH_SHORT).show();
                                cancelarEdicion(); // Restablece el formulario y el estado
                                fetchEquipos(""); // Recargar lista
                            } else {
                                Toast.makeText(EquiposActivity.this, "Error al actualizar: " + response.optString("message", "Error desconocido"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing update response: " + e.getMessage());
                            Toast.makeText(EquiposActivity.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error updating equipo: " + error.toString());
                        Toast.makeText(EquiposActivity.this, "Error al conectar con el servidor para actualizar.", Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void handleEliminarEquipo(String nInventario) {
        // Confirmación de eliminación
        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Equipo")
                .setMessage("¿Seguro que deseas eliminar el equipo con N° Inventario: " + nInventario + "?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    String url = BASE_URL + "?n_inventario=" + nInventario;
                    StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        if (jsonResponse.getBoolean("success")) {
                                            Toast.makeText(EquiposActivity.this, "Equipo eliminado.", Toast.LENGTH_SHORT).show();
                                            fetchEquipos(""); // Recargar lista
                                            cancelarEdicion(); // Por si se eliminó el que se estaba editando
                                        } else {
                                            Toast.makeText(EquiposActivity.this, "Error al eliminar: " + jsonResponse.optString("message", "Error desconocido"), Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Error parsing delete response: " + e.getMessage());
                                        Toast.makeText(EquiposActivity.this, "Error al procesar la respuesta del servidor.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "Error deleting equipo: " + error.toString());
                                    Toast.makeText(EquiposActivity.this, "Error al conectar con el servidor para eliminar.", Toast.LENGTH_LONG).show();
                                }
                            });
                    requestQueue.add(stringRequest);
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // --- Funciones de UI ---

    private void iniciarEdicion(Equipo equipo) {
        isEditing = true;
        currentEditNInventario = equipo.getN_inventario();
        tvFormTitle.setText("Editar Equipo");
        btnSubmit.setText("Actualizar");
        btnCancelEdit.setVisibility(View.VISIBLE);

        etNInventario.setText(equipo.getN_inventario());
        etNSerie.setText(equipo.getN_serie());

        // Seleccionar clase y tipo
        int clasePosition = claseAdapter.getPosition(equipo.getClase().toLowerCase());
        spinnerClase.setSelection(clasePosition);

        // Retrasar la selección del tipo para asegurar que el spinner de tipo se haya actualizado
        spinnerClase.post(() -> {
            updateTipoSpinner(equipo.getClase().toLowerCase()); // Asegurarse de que los tipos se carguen para la clase correcta
            int tipoPosition = tipoAdapter.getPosition(equipo.getTipo().toUpperCase());
            if (tipoPosition != -1) {
                spinnerTipo.setSelection(tipoPosition);
            }
        });


        etMarca.setText(equipo.getMarca());
        etRam.setText(equipo.getRam());
        etDisco.setText(equipo.getDisco());
        etProcesador.setText(equipo.getProcesador());

        int estadoPosition = estadoAdapter.getPosition(equipo.getEstado().toUpperCase());
        spinnerEstado.setSelection(estadoPosition != -1 ? estadoPosition : 0);
    }

    private void cancelarEdicion() {
        isEditing = false;
        currentEditNInventario = null;
        tvFormTitle.setText("Agregar Nuevo Equipo");
        btnSubmit.setText("Agregar");
        btnCancelEdit.setVisibility(View.GONE);
        limpiarCampos();
    }

    private void limpiarCampos() {
        etNInventario.setText("");
        etNSerie.setText("");
        spinnerClase.setSelection(0);
        spinnerTipo.setSelection(0);
        etMarca.setText("");
        etRam.setText("");
        etDisco.setText("");
        etProcesador.setText("");
        spinnerEstado.setSelection(0);
    }
}

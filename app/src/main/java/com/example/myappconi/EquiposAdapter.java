package com.example.myappconi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EquiposAdapter extends ArrayAdapter<Equipo> {

    public EquiposAdapter(Context context, List<Equipo> equipos) {
        super(context, 0, equipos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtener el objeto Equipo para esta posición
        Equipo equipo = getItem(position);

        // Si no se está reciclando una vista, inflar una nueva
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            // Podrías crear un layout personalizado si quieres mostrar más campos directamente en el ListView item
            // Por ejemplo: R.layout.item_equipo con varios TextViews
        }

        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        if (equipo != null) {
            text1.setText("Inv: " + equipo.getN_inventario() + " | Serie: " + equipo.getN_serie());
            text2.setText("Tipo: " + equipo.getTipo() + " | Clase: " + equipo.getClase() + " | Marca: " + equipo.getMarca() + " | Estado: " + equipo.getEstado());
        }

        return convertView;
    }
}

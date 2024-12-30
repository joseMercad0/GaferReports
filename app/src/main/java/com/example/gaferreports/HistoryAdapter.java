package com.example.gaferreports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryEntry> historyEntries;

    public HistoryAdapter(List<HistoryEntry> historyEntries) {
        this.historyEntries = historyEntries;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntry entry = historyEntries.get(position);

        // Verificar si la entrada es válida (no tiene valores nulos o vacíos importantes)
        if (entry != null && !isEntryEmpty(entry)) {
            holder.bind(entry); // Solo enlazar si la entrada no está vacía
        } else {
            // Si la entrada está vacía, puedes omitirla o mostrar un mensaje vacío
            // Aquí se podría ocultar la vista o mostrar algo diferente
            holder.itemView.setVisibility(View.INVISIBLE); // Se usa INVISIBLE para evitar problemas de indexación
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = 0; // Altura 0 para "ocultar" el elemento
            holder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return historyEntries.size();
    }

    // Método para verificar si una entrada tiene valores importantes vacíos o nulos
    private boolean isEntryEmpty(HistoryEntry entry) {
        // Si alguno de los valores principales es nulo o vacío, consideramos que la entrada está vacía
        return (entry.getTrapType() == null || entry.getTrapType().isEmpty()) &&
                (entry.getPoisonType() == null || entry.getPoisonType().isEmpty()) &&
                entry.getPoisonAmount() == 0 &&
                !entry.isConsumption() &&  // Si no hubo consumo
                !entry.isReplace();         // Si no hubo reemplazo
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTimestamp, textViewTrapType, textViewPoisonType, textViewPoisonAmount, textViewConsumption, textViewConsumptionPercentage, textViewReplace, textViewReplaceAmount, textViewReplacePoisonType;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            textViewTrapType = itemView.findViewById(R.id.textViewTrapType);
            textViewPoisonType = itemView.findViewById(R.id.textViewPoisonType);
            textViewPoisonAmount = itemView.findViewById(R.id.textViewPoisonAmount);
            textViewConsumption = itemView.findViewById(R.id.textViewConsumption);
            textViewConsumptionPercentage = itemView.findViewById(R.id.textViewConsumptionPercentage);
            textViewReplace = itemView.findViewById(R.id.textViewReplace);
            textViewReplaceAmount = itemView.findViewById(R.id.textViewReplaceAmount);
            textViewReplacePoisonType = itemView.findViewById(R.id.textViewReplacePoisonType);
        }

        public void bind(HistoryEntry entry) {
            textViewTimestamp.setText(entry.getDate());
            textViewTrapType.setText("Tipo de Trampa: " + entry.getTrapType());
            textViewPoisonType.setText("Tipo de Veneno: " + entry.getPoisonType());
            textViewPoisonAmount.setText("Cantidad de Veneno: " + entry.getPoisonAmount());

            textViewConsumption.setText("Hubo Consumo: " + (entry.isConsumption() ? "Sí" : "No"));
            if (entry.isConsumption()) {
                textViewConsumptionPercentage.setVisibility(View.VISIBLE);
                textViewConsumptionPercentage.setText("Consumo: " + entry.getConsumptionPercentage() + "%");
            } else {
                textViewConsumptionPercentage.setVisibility(View.GONE);
            }

            textViewReplace.setText("Habrá Reemplazo: " + (entry.isReplace() ? "Sí" : "No"));
            if (entry.isReplace()) {
                textViewReplaceAmount.setVisibility(View.VISIBLE);
                textViewReplacePoisonType.setVisibility(View.VISIBLE);
                textViewReplaceAmount.setText("Cantidad de Reemplazo: " + entry.getReplaceAmount());
                textViewReplacePoisonType.setText("Tipo de Veneno Reemplazado: " + entry.getReplacePoisonType());
            } else {
                textViewReplaceAmount.setVisibility(View.GONE);
                textViewReplacePoisonType.setVisibility(View.GONE);
            }
        }
    }
}

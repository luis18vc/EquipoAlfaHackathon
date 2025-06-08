package com.example.vistanotas.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.vistanotas.R;
import com.example.vistanotas.models.notas.Nota;
import java.util.List;

public class NotasAdapter extends RecyclerView.Adapter<NotasAdapter.NotaViewHolder> {

    private List<Nota> listaNotas;

    public NotasAdapter(List<Nota> listaNotas) {
        this.listaNotas = listaNotas;
    }

    @NonNull
    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nota_item, parent, false);
        return new NotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotaViewHolder holder, int position) {
        Nota nota = listaNotas.get(position);
        holder.tvNombreCurso.setText(nota.getNombre());
        holder.tvAbreviatura.setText(nota.getAbreviatura());
        holder.tvValor.setText("Nota: " + nota.getValor());
    }

    @Override
    public int getItemCount() {
        return listaNotas.size();
    }

    static class NotaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreCurso, tvAbreviatura, tvValor;

        NotaViewHolder(View itemView) {
            super(itemView);
            tvNombreCurso = itemView.findViewById(R.id.tvNombreCurso);
            tvAbreviatura = itemView.findViewById(R.id.tvAbreviatura);
            tvValor = itemView.findViewById(R.id.tvValor);
        }
    }
}

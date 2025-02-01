package baigorriap.auditoriabpm.ui.reporte;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import baigorriap.auditoriabpm.model.ItemNoOk;
import baigorriap.auditoriabpm.databinding.ItemNoOkListItemBinding;

public class ItemsNoOkAdapter extends ListAdapter<ItemNoOk, ItemsNoOkAdapter.ViewHolder> {

    protected ItemsNoOkAdapter() {
        super(new DiffUtil.ItemCallback<ItemNoOk>() {
            @Override
            public boolean areItemsTheSame(@NonNull ItemNoOk oldItem, @NonNull ItemNoOk newItem) {
                return oldItem.getDescripcion().equals(newItem.getDescripcion()) &&
                       oldItem.getOperario().equals(newItem.getOperario());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ItemNoOk oldItem, @NonNull ItemNoOk newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoOkListItemBinding binding = ItemNoOkListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemNoOk item = getItem(position);
        holder.bind(item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNoOkListItemBinding binding;

        ViewHolder(ItemNoOkListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ItemNoOk item) {
            binding.textOperario.setText(item.getOperario());
            binding.textDescripcion.setText(item.getDescripcion());
            binding.textCount.setText(String.format("Cantidad: %d", item.getCount()));
            
            // Mostrar comentarios si hay
            if (item.getComentariosAuditoria() != null && !item.getComentariosAuditoria().isEmpty()) {
                StringBuilder comentarios = new StringBuilder();
                for (String comentario : item.getComentariosAuditoria()) {
                    if (!comentario.isEmpty()) {
                        if (comentarios.length() > 0) comentarios.append("\n");
                        comentarios.append("• ").append(comentario);
                    }
                }
                if (comentarios.length() > 0) {
                    binding.textComentarios.setText(comentarios.toString());
                    binding.textComentarios.setVisibility(android.view.View.VISIBLE);
                } else {
                    binding.textComentarios.setVisibility(android.view.View.GONE);
                }
            } else {
                binding.textComentarios.setVisibility(android.view.View.GONE);
            }
        }
    }
}

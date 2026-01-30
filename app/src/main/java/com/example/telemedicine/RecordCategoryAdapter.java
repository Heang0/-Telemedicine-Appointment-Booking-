package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordCategoryAdapter extends RecyclerView.Adapter<RecordCategoryAdapter.RecordCategoryViewHolder> {
    private List<RecordCategory> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(RecordCategory category);
    }

    public RecordCategoryAdapter(List<RecordCategory> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecordCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record_category, parent, false);
        return new RecordCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordCategoryViewHolder holder, int position) {
        RecordCategory category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<RecordCategory> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    class RecordCategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconImage;
        private TextView titleText;
        private TextView descriptionText;
        private TextView countText;

        public RecordCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            
            iconImage = itemView.findViewById(R.id.category_icon);
            titleText = itemView.findViewById(R.id.category_title);
            descriptionText = itemView.findViewById(R.id.category_description);
            countText = itemView.findViewById(R.id.record_count);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCategoryClick(categories.get(position));
                }
            });
        }

        public void bind(RecordCategory category) {
            titleText.setText(category.getTitle());
            descriptionText.setText(category.getDescription());
            countText.setText(String.valueOf(category.getRecordCount()));
            iconImage.setImageResource(category.getIconResId());
        }
    }
}
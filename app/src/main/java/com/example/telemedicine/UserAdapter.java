package com.example.telemedicine;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private OnUserActionListener onUserActionListener;
    private OnUserClickListener onUserClickListener;

    public interface OnUserActionListener {
        void onEditUser(User user);
        void onDeleteUser(User user);
    }

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public UserAdapter(List<User> users, OnUserClickListener onUserClickListener) {
        this.users = users;
        this.onUserClickListener = onUserClickListener;
    }

    public void setOnUserActionListener(OnUserActionListener listener) {
        this.onUserActionListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, onUserActionListener, onUserClickListener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView textUserName, textUserEmail, textUserRole, textSpecialization;
        private Button btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.text_user_name);
            textUserEmail = itemView.findViewById(R.id.text_user_email);
            textUserRole = itemView.findViewById(R.id.text_user_role);
            textSpecialization = itemView.findViewById(R.id.text_specialization);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(User user, OnUserActionListener listener, OnUserClickListener clickListener) {
            textUserName.setText(user.getFullName());
            textUserEmail.setText(user.getEmail());
            textUserRole.setText(capitalizeFirstLetter(user.getRole()));

            // Show specialization for doctors
            if (UserRole.DOCTOR.getRoleName().equals(user.getRole()) && user.getSpecialization() != null) {
                textSpecialization.setText("Specialization: " + user.getSpecialization());
                textSpecialization.setVisibility(View.VISIBLE);
            } else {
                textSpecialization.setVisibility(View.GONE);
            }

            // Set role color - using main color for all user types
            textUserRole.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
            if (UserRole.ADMIN.getRoleName().equals(user.getRole())) {
                textUserRole.setBackgroundColor(Color.parseColor("#2196F3")); // Main blue
            } else if (UserRole.DOCTOR.getRoleName().equals(user.getRole())) {
                textUserRole.setBackgroundColor(Color.parseColor("#FF9800")); // Orange for doctors
            } else {
                textUserRole.setBackgroundColor(Color.GRAY); // Gray for patients
            }

            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onUserClick(user);
                }
            });

            // Set click listeners for edit and delete buttons
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditUser(user);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(user);
                }
            });
        }

        private String capitalizeFirstLetter(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}
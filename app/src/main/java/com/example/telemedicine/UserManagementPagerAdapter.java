package com.example.telemedicine;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class UserManagementPagerAdapter extends FragmentStateAdapter {

    public UserManagementPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: // All Users
                return new AllUsersFragment();
            case 1: // Patients
                return new PatientsFragment();
            case 2: // Doctors
                return new DoctorsFragment();
            default:
                return new AllUsersFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // All Users, Patients, Doctors
    }
}
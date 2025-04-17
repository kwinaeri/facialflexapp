package com.example.facialflex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {
    private List<Doctor> doctors;
    private Context context;

    public DoctorAdapter(List<Doctor> doctors, Context context) {
        this.doctors = doctors;
        this.context = context;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.ptName.setText(doctor.getName());
        holder.ptPos.setText(doctor.getCertificate());
        holder.ptEmail.setText(doctor.getEmail());
        holder.ptNum.setText(doctor.getNumber());
        holder.ptSchedule.setText(doctor.getSchedule());

        // Load the profile image using Glide
        String ptProfileURL = doctor.getProfileImage(); // Fetches the profile image URL
        if (ptProfileURL != null && !ptProfileURL.isEmpty()) {
            Glide.with(context)
                    .load(ptProfileURL)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ptProfile);
        } else {
            holder.ptProfile.setImageResource(R.drawable.baseline_person_24); // Placeholder
        }
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView ptName, ptPos, ptEmail, ptNum,  ptSchedule;
        ImageView ptProfile;

        DoctorViewHolder(View itemView) {
            super(itemView);
            ptName = itemView.findViewById(R.id.pt_name);
            ptPos = itemView.findViewById(R.id.pt_pos);
            ptEmail = itemView.findViewById(R.id.pt_email);
            ptNum = itemView.findViewById(R.id.pt_num);
            ptProfile = itemView.findViewById(R.id.pt_profile);
            ptSchedule = itemView.findViewById(R.id.pt_schedule);
        }
    }
}

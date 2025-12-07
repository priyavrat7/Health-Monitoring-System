package com.example.childhealthcareproject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class BabyFileAdapter extends FirestoreRecyclerAdapter<BabyFileClass, BabyFileAdapter.BabyFileViewHolder> {

    Context context;


    public BabyFileAdapter(@NonNull FirestoreRecyclerOptions<BabyFileClass> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull BabyFileViewHolder holder, int position, @NonNull BabyFileClass babyFile) {
        holder.babyNameTextView.setText(babyFile.babyFirstName + " " + babyFile.babyLastName);
        holder.babyBirthDayTextView.setText(babyFile.dateOfBirth);
        holder.babyPhoneNumTextView.setText(babyFile.phoneNo);
        holder.babyEmailTextView.setText(babyFile.email);

        holder.itemView.setOnClickListener((v) -> {
            Intent intent = new Intent(context, BabyDoctorActivity.class);
            String babyDocId = this.getSnapshots().getSnapshot(position).getId();
            Log.d("ChildID from DoctorActivity to BabyDoctorActivity", String.valueOf(babyDocId));
            intent.putExtra("childDataID", babyDocId);
            context.startActivity(intent);
        });

    }

    @NonNull
    @Override
    public BabyFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_baby_item, parent, false);
        return new BabyFileViewHolder(view);
    }

    class BabyFileViewHolder extends RecyclerView.ViewHolder {

        TextView babyNameTextView, babyBirthDayTextView, babyPhoneNumTextView, babyEmailTextView;

        public BabyFileViewHolder(@NonNull View itemView) {
            super(itemView);
            babyNameTextView = itemView.findViewById(R.id.baby_name_text_view);
            babyBirthDayTextView = itemView.findViewById(R.id.birthday_text_view);
            babyPhoneNumTextView = itemView.findViewById(R.id.phone_num_text_view);
            babyEmailTextView = itemView.findViewById(R.id.email_address_text_view);
        }
    }
}

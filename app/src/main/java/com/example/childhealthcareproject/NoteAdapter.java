package com.example.childhealthcareproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder> {

    Context context;

    String babyDocId, userType;
    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context, String babyDocId, String userType) {
        super(options);
        this.context = context;
        this.babyDocId = babyDocId;
        this.userType = userType;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note note) {
        holder.noteTitleTextView.setText(note.title);
        holder.noteDoctorNameTextView.setText(note.doctorName);
        holder.noteContentTextView.setText(note.content);
        holder.notePrescriptionTextView.setText(note.prescription);
        holder.timestampTextView.setText(new SimpleDateFormat("MM/dd/yyyy").format(note.timestamp.toDate()));

        holder.itemView.setOnClickListener((v)-> {
            if (userType.equals("Doctor")) {
                Intent intent = new Intent(context, EditNoteActivity.class);
                intent.putExtra("Note Title", note.title);
                intent.putExtra("Note Doctor Name", note.doctorName);
                intent.putExtra("Note Content", note.content);
                intent.putExtra("Note Prescription", note.prescription);
                intent.putExtra("BabyDocId", this.babyDocId);
                String noteDocId = this.getSnapshots().getSnapshot(position).getId();
                intent.putExtra("NoteDocId", noteDocId);
                context.startActivity(intent);
            }
            if (userType.equals("Parent")) {
                Intent intent = new Intent(context, ViewNoteActivity.class);
                intent.putExtra("Note Title", note.title);
                intent.putExtra("Note Doctor Name", note.doctorName);
                intent.putExtra("Note Content", note.content);
                intent.putExtra("Note Prescription", note.prescription);
                intent.putExtra("BabyDocId", this.babyDocId);
                String noteDocId = this.getSnapshots().getSnapshot(position).getId();
                intent.putExtra("NoteDocId", noteDocId);
                context.startActivity(intent);
            }

        });

    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_note_item, parent, false);
        return new NoteViewHolder(view);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView noteTitleTextView, noteDoctorNameTextView, noteContentTextView, notePrescriptionTextView, timestampTextView;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitleTextView           = itemView.findViewById(R.id.note_title_text_view);
            noteDoctorNameTextView      = itemView.findViewById(R.id.note_doctor_name_view);
            noteContentTextView         = itemView.findViewById(R.id.note_content_text_view);
            notePrescriptionTextView    = itemView.findViewById(R.id.note_prescription_text_view);
            timestampTextView           = itemView.findViewById(R.id.note_tstmp_text_view);
        }
    }
}


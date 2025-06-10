package com.example.mad_project.ui.pages.profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.EmergencyContactEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder> {
    private final List<EmergencyContactEntity> contacts = new ArrayList<>();
    private final Context context;
    private final ContactActionListener listener;

    public interface ContactActionListener {
        void onEditContact(EmergencyContactEntity contact);
        void onDeleteContact(EmergencyContactEntity contact);
        void onSetPrimaryContact(EmergencyContactEntity contact);
    }

    public EmergencyContactAdapter(Context context, ContactActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContactEntity contact = contacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateContacts(List<EmergencyContactEntity> newContacts) {
        contacts.clear();
        contacts.addAll(newContacts);
        notifyDataSetChanged();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView relationshipText;
        private final Chip primaryChip;
        private final MaterialButton callButton;
        private final MaterialButton messageButton;
        private final MaterialButton editButton;

        ContactViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_contact_name);
            relationshipText = itemView.findViewById(R.id.text_relationship);
            primaryChip = itemView.findViewById(R.id.chip_primary);
            callButton = itemView.findViewById(R.id.btn_call);
            messageButton = itemView.findViewById(R.id.btn_message);
            editButton = itemView.findViewById(R.id.btn_edit);
        }

        void bind(EmergencyContactEntity contact) {
            nameText.setText(contact.getName());
            relationshipText.setText(contact.getRelationship());
            primaryChip.setVisibility(contact.isPrimaryContact() ? View.VISIBLE : View.GONE);

            callButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
                context.startActivity(intent);
            });

            messageButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + contact.getPhoneNumber()));
                context.startActivity(intent);
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditContact(contact);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteContact(contact);
                }
                return true;
            });

            primaryChip.setOnClickListener(v -> {
                if (listener != null && !contact.isPrimaryContact()) {
                    listener.onSetPrimaryContact(contact);
                }
            });
        }
    }
}
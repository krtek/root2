package cz.krtinec.root2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class TestActivity2 extends Activity {
    private static final int ACTIVITY_PICK_CONTACT = 42;
    private static final int DIALOG_SHOW_CONTACT = 10;
    private static final int MENU_FIND_CONTACT = 1;

    private static Uri pickedContact;
    private static ContactDTO contact;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (contact == null) {
            setContentView(R.layout.empty);
        } else {
            displayContact(contact);
        }
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, ACTIVITY_PICK_CONTACT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_FIND_CONTACT, 0, "Vyber kontakt").setIcon(android.R.drawable.ic_menu_add);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_FIND_CONTACT: {
                pickContact();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (ACTIVITY_PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    //hotovo, máme kontakt
                    pickedContact = data.getData();
                    showDialog(DIALOG_SHOW_CONTACT);
                    ContactDTO detail = DataProvider.lookupContact(this, pickedContact);
                    displayContact(detail);
                    return;
                }
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_SHOW_CONTACT: {
                return new AlertDialog.Builder(this).
                        setTitle("URI kontaktu").
                        setMessage("Message").
                        setCancelable(true).
                        setPositiveButton("OK", null).
                        create();
            }
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_SHOW_CONTACT: {
                if (pickedContact != null) {
                    ((AlertDialog) dialog).setMessage(pickedContact.toString());
                }
            }
        }
    }

    private void displayContact(final ContactDTO dto) {
        setContentView(R.layout.view_contact);
        this.contact = dto;
        ((TextView) findViewById(R.id.name)).setText(dto.name);
        ((TextView) findViewById(R.id.phone)).setText(dto.phone);
        ((TextView) findViewById(R.id.email)).setText(dto.email);
        ((EditText) findViewById(R.id.note)).setText(contact.note == null ? "" : contact.note.note);

/*
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((EditText) findViewById(R.id.note)).setText(contact.note == null ? "" : contact.note.note);
            }
        });
*/

        final Context ctx = this;
        findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String newNote = ((EditText) findViewById(R.id.note)).getText().toString();
                DataProvider.saveNote(ctx, contact.id, newNote, contact.note);
                Toast.makeText(ctx, "Uloženo...", 500).show();
            }
        });

        final ImageView photo = (ImageView) findViewById(R.id.photo);


        Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                InputStream photoStream;
                Drawable drawable;
                if ((photoStream = DataProvider.openPhoto(ctx, dto.id)) != null) {
                    drawable = Drawable.createFromStream(photoStream, "src");
                } else {
                    drawable = null;
                }

                if (drawable != null) {
                    photo.setImageDrawable(drawable);
                } else {
                    photo.setImageResource(R.drawable.icon);
                }
            }
        });
    }
}
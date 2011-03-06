package cz.krtinec.root2;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: krtek
 * Date: 6.3.11
 * Time: 14:53
 * To change this template use File | Settings | File Templates.
 */
public class DataProvider {
    public static ContactDTO lookupContact(Context ctx, Uri contactUri) {
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
        };
        ContactDTO dto = null;
        Cursor c = ctx.getContentResolver().query(contactUri, projection, null, null, null);
        if (c != null && c.moveToFirst()) {
            dto = new ContactDTO(c.getLong(0), c.getString(1));
            dto.phone = getPhoneNumber(ctx, dto.id);
            dto.email = getEmail(ctx, dto.id);
            dto.note = getNote(ctx, dto.id);
        }
        if (c != null) {
            c.close();
        }
        return dto;
    }


    private static String getPhoneNumber(Context ctx, long contactId) {
        Cursor cursor = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "= ?" +
                        " AND " + ContactsContract.CommonDataKinds.Phone.TYPE +
                        "=" + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                new String[]{String.valueOf(contactId)},
                null);


        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    private static String getEmail(Context ctx, long contactId) {
        Cursor cursor = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Email.DATA},
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "= ?",
                new String[]{String.valueOf(contactId)}, null);


        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    private static NoteDTO getNote(Context ctx, long contactId) {
        Cursor cursor = ctx.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Note._ID,
                        ContactsContract.CommonDataKinds.Note.NOTE},
                ContactsContract.CommonDataKinds.Note.CONTACT_ID + "= ? AND " +
                ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE + "'",
                new String[]{String.valueOf(contactId)}, null);


        try {
            if (cursor.moveToFirst()) {
                return new NoteDTO(cursor.getLong(0), cursor.getString(1));
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }

    }

    public static void saveNote(Context ctx, long contactId, String note, NoteDTO prevNote) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>(1);
        if (prevNote == null) {
            //insert
            Long rawContactId = getRawContactIds(ctx, contactId)[0]; //tady by si mel uzivatel spravne vybrat
            batch.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).
                withValue(ContactsContract.CommonDataKinds.Note.RAW_CONTACT_ID, String.valueOf(rawContactId)).
                withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE).
                withValue(ContactsContract.CommonDataKinds.Note.NOTE, note).
                build()
            );
        } else {
            //update
            batch.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI).
                withSelection(ContactsContract.CommonDataKinds.Note._ID + "= ?",
                        new String[]{String.valueOf(prevNote.id)}).
                withValue(ContactsContract.CommonDataKinds.Note.NOTE, note).
                build()
            );
        }

        try {
        ContentProviderResult[] result = ctx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, batch);
            Log.i("DataProvider", "Update result: " + result);
        } catch (RemoteException e) {
            Log.i("DataProvider", "Exception: ", e);
        } catch (OperationApplicationException e) {
            Log.i("DataProvider", "Exception: ", e);
        }

    }

    private static Long[] getRawContactIds(Context ctx, Long contactId) {
        Cursor cursor =  ctx.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]
                        {ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.ACCOUNT_TYPE,
                        ContactsContract.RawContacts.ACCOUNT_NAME},
                ContactsContract.RawContacts.CONTACT_ID + " = ?",
                new String[]{String.valueOf(contactId)}, null);

        List<Long> result = new ArrayList<Long>();
        while (cursor.moveToNext()) {
            Log.d("DataProvider",
                    "RawId: " + cursor.getLong(0) + ", type: " + cursor.getString(1) + ", name: " + cursor.getString(2));
            result.add(cursor.getLong(0));
        }
        return result.toArray(new Long[result.size()]);
    }

    public static InputStream openPhoto(Context ctx, long contactId) {
    	Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId));
		return ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(), contactUri);
    }

}

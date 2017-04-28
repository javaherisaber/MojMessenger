package ir.logicbase.mojmessenger.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;

public class DatabaseProvider extends ContentProvider {

    private static final Object lock = new Object();
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize.
         */

        /*
         * Sets the integer value for multiple rows in table 3 to 1. Notice that no wildcard is used
         * in the path
         */
        uriMatcher.addURI("com.example.app.provider", "table3", 1);

        /*
         * Sets the code for a single row to 2. In this case, the "#" wildcard is
         * used. "content://com.example.app.provider/table3/3" matches, but
         * "content://com.example.app.provider/table3 doesn't.
         */
        uriMatcher.addURI("com.example.app.provider", "table3/#", 2);
    }

    public DatabaseProvider() {
    }

    /**
     * Implement this to initialize your content provider on startup.
     *
     * @return True if provider enabled or False if you don't enabled it
     */
    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) throws IllegalArgumentException {
        synchronized (lock) {
            try {
                /*
                * Choose the table to query and a sort order based on the code returned for the incoming
                * URI. Here, too, only the statements for table 3 are shown.
                */
                switch (uriMatcher.match(uri)) {

                    // If the incoming URI was for all of table3
                    case 1:

                        if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
                        break;

                    // If the incoming URI was for a single row
                    case 2:
                         /*
                         * Because this URI was for a single row, the _ID value part is
                         * present. Get the last path segment from the URI; this is the _ID value.
                         * Then, append the value to the WHERE clause for the query
                         */
                        selection = selection + "_ID = " + uri.getLastPathSegment();
                        break;
                    default:
                        // If the URI is not recognized, you should do some error handling here.
                        throw new IllegalArgumentException();
                }
                db = dbHelper.getWritableDatabase();
                return db.query("tableName", projection, selection, selectionArgs, null, null, "Some column ASC");
                // TODO: 8/8/2017 implement this method
            } catch (SQLiteException e) {
                return null;
            } finally {
                db.close();
            }
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO: Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @return a string of MIME type with android's vendor-specific format
     */
    @Override
    public String getType(Uri uri) {
        String mimeForSingleRow = "vnd.android.cursor.item/vnd.ir.logicbase.provider.Chat";
        String mimeForMultipleRows = "vnd.android.cursor.dir/vnd.ir.logicbase.provider.Chat";
        // TODO: Implement this to handle requests for the MIME type of the data at the given URI.
        return "vnd.android.cursor.item/vnd.ir.logicbase.provider.Chat";
    }

}

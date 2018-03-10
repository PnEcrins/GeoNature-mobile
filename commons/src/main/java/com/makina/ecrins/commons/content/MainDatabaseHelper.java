package com.makina.ecrins.commons.content;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.provider.BaseColumns;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Simple {@code SQLiteOpenHelper} implementation.
 * <p>
 * Try to open database from a given path.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainDatabaseHelper
        extends SQLiteOpenHelper {

    private static final String TAG = MainDatabaseHelper.class.getName();

    private File mDatabaseFile;

    public MainDatabaseHelper(Context context,
                              String databaseName,
                              int databaseVersion) throws
                                                   IOException {

        super(context,
              databaseName,
              null,
              databaseVersion);

        mDatabaseFile = FileUtils.getFile(FileUtils.getDatabaseFolder(context,
                                                                      MountPoint.StorageType.INTERNAL),
                                          databaseName);

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "MainDatabaseHelper using database '" + mDatabaseFile.getAbsolutePath() + "'");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // do nothing ...
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {
        // do nothing ...
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {

        return SQLiteDatabase.openDatabase(mDatabaseFile.getPath(),
                                           new LeaklessCursorFactory(),
                                           SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * Custom implementation of {@code CursorFactory} that use
     * {@link com.makina.ecrins.commons.content.MainDatabaseHelper.LeaklessCursor} to close
     * automatically database instance.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private static class LeaklessCursorFactory
            implements CursorFactory {

        @Override
        public Cursor newCursor(SQLiteDatabase db,
                                SQLiteCursorDriver masterQuery,
                                String editTable,
                                SQLiteQuery query) {

            return new LeaklessCursor(db,
                                      masterQuery,
                                      editTable,
                                      query);
        }
    }

    /**
     * Custom implementation of {@code SQLiteCursor} to close automatically database instance.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    private static class LeaklessCursor
            extends SQLiteCursor {

        final SQLiteDatabase mDatabase;

        LeaklessCursor(SQLiteDatabase db,
                       SQLiteCursorDriver driver,
                       String editTable,
                       SQLiteQuery query) {

            super(driver,
                  editTable,
                  query);

            this.mDatabase = db;
        }

        @Override
        public void close() {

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "Closing LeaklessCursor: '" + mDatabase.getPath() + "'");
            }

            mDatabase.close();

            super.close();
        }
    }

    /**
     * {@code observers} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class ObserversColumns
            implements BaseColumns {

        private ObserversColumns() {

        }

        static final String TABLENAME = "observers";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String IDENT = "ident";
        public static final String LASTNAME = "lastname";
        public static final String FIRSTNAME = "firstname";
        public static final String FILTER = "filter";
    }

    /**
     * {@code taxa} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class TaxaColumns
            implements BaseColumns {

        private TaxaColumns() {

        }

        static final String TABLENAME = "taxa";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String NAME = "name";
        public static final String NAME_FR = "name_fr";
        public static final String CLASS_ID = "class_id";
        public static final String FULL_CLASS_ID = TABLENAME + "." + CLASS_ID;
        public static final String NUMBER = "number";
        public static final String PATRIMONIAL = "patrimonial";
        public static final String MESSAGE = "message";
        public static final String FILTER = "filter";
    }

    /**
     * {@code taxa_unities} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class TaxaUnitiesColumns
            implements BaseColumns {

        private TaxaUnitiesColumns() {

        }

        static final String TABLENAME = "taxa_unities";

        public static final String UNITY_ID = "unity_id";
        public static final String FULL_UNITY_ID = TABLENAME + "." + UNITY_ID;
        public static final String TAXON_ID = "taxon_id";
        public static final String FULL_TAXON_ID = TABLENAME + "." + TAXON_ID;
        public static final String DATE = "date";
        public static final String COLOR = "color";
        public static final String NB_OBS = "nb_obs";
    }

    /**
     * {@code criterion} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class CriteriaColumns
            implements BaseColumns {

        private CriteriaColumns() {

        }

        static final String TABLENAME = "criterion";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String NAME = "name";
        public static final String SORT = "sort";
        public static final String CLASS_ID = "class_id";
    }

    /**
     * {@code environments} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class EnvironmentsColumns
            implements BaseColumns {

        private EnvironmentsColumns() {

        }

        static final String TABLENAME = "environments";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String NAME = "name";
    }

    /**
     * {@code inclines} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class InclinesColumns
            implements BaseColumns {

        private InclinesColumns() {

        }

        static final String TABLENAME = "inclines";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String VALUE = "value";
        public static final String NAME = "name";
    }

    /**
     * {@code phenology} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class PhenologyColumns
            implements BaseColumns {

        private PhenologyColumns() {

        }

        static final String TABLENAME = "phenology";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String CODE = "code";
        public static final String NAME = "name";
    }

    /**
     * {@code physiognomy} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class PhysiognomyColumns
            implements BaseColumns {

        private PhysiognomyColumns() {

        }

        static final String TABLENAME = "physiognomy";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String GROUP_NAME = "group_name";
        public static final String NAME = "name";
    }

    /**
     * {@code disturbances} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class DisturbancesColumns
            implements BaseColumns {

        private DisturbancesColumns() {

        }

        static final String TABLENAME = "disturbances";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String CODE = "code";
        public static final String CLASSIFICATION = "classification";
        public static final String DESCRIPTION = "description";
    }

    /**
     * {@code prospecting_areas} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class ProspectingAreasColumns
            implements BaseColumns {

        private ProspectingAreasColumns() {

        }

        static final String TABLENAME = "prospecting_areas";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String TAXON_ID = "taxon_id";
        public static final String GEOMETRY = "geometry";
    }

    /**
     * {@code search} SQLite table.
     *
     * @author @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public static final class SearchColumns
            implements BaseColumns {

        private SearchColumns() {

        }

        static final String TABLENAME = "search";

        public static final String FULL_ID = TABLENAME + "." + _ID;
        public static final String TAXON = "taxon";
        public static final String DATE_OBS = "dateobs";
        public static final String OBSERVER = "observer";
        public static final String GEOMETRY = "geometry";
    }
}

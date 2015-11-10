package com.makina.ecrins.commons.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.settings.AbstractAppSettings;

import java.io.IOException;

/**
 * Simple {@code ContentProvider} implementation.
 * <p/>
 * Uses {@link MainDatabaseHelper}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractMainContentProvider
        extends ContentProvider {

    private static final String TAG = AbstractMainContentProvider.class.getName();

    // used for the UriMacher
    public static final int OBSERVERS = 10;
    public static final int OBSERVER_ID = 11;
    public static final int TAXA = 20;
    public static final int TAXA_BY_UNITY = 21;
    public static final int CRITERIA = 30;
    public static final int CRITERIA_BY_CLASS = 31;
    public static final int ENVIRONMENTS = 40;
    public static final int INCLINES = 50;
    public static final int INCLINE_ID = 51;
    public static final int PHENOLOGY = 60;
    public static final int PHYSIOGNOMY_GROUPS = 71;
    public static final int PHYSIOGNOMY_BY_GROUP = 72;
    public static final int DISTURBANCES_CLASSIFICATIONS = 81;
    public static final int DISTURBANCES_BY_CLASSIFICATION = 82;
    public static final int PROSPECTING_AREAS_BY_TAXON = 91;
    public static final int SEARCH = 100;
    public static final int SEARCH_TAXON = 101;

    protected static final String PATH_OBSERVERS = "observers";
    protected static final String PATH_TAXA = "taxas";
    protected static final String PATH_TAXA_UNITY = "taxas_unity";
    protected static final String PATH_CRITERIA = "criteria";
    protected static final String PATH_CRITERIA_CLASS = "criteria_class";
    protected static final String PATH_ENVIRONMENTS = "environments";
    protected static final String PATH_INCLINES = "inclines";
    protected static final String PATH_PHENOLOGY = "phenology";
    protected static final String PATH_PHYSIOGNOMY_GROUPS = "physiognomy_groups";
    protected static final String PATH_DISTURBANCES_CLASSIFICATIONS = "disturbances_classifications";
    protected static final String PATH_PROSPECTING_AREAS_TAXON = "prospecting_areas_taxon";
    protected static final String PATH_SEARCH = "search";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private MainDatabaseHelper mDatabase = null;

    @Override
    public boolean onCreate() {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onCreate");
        }

        initializeUriMatcher();

        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "query : " + uri);
        }

        final StringBuilder groupBy = new StringBuilder();
        String defaultSortOrder = null;

        // using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (sURIMatcher.match(uri)) {
            case OBSERVERS:
                queryBuilder.setTables(MainDatabaseHelper.ObserversColumns.TABLENAME);
                defaultSortOrder = MainDatabaseHelper.ObserversColumns.LASTNAME + " COLLATE NOCASE ASC";
                break;
            case OBSERVER_ID:
                queryBuilder.setTables(MainDatabaseHelper.ObserversColumns.TABLENAME);
                queryBuilder.appendWhere(MainDatabaseHelper.ObserversColumns._ID + " = " + uri.getLastPathSegment());
                defaultSortOrder = MainDatabaseHelper.ObserversColumns.LASTNAME + " COLLATE NOCASE ASC";
                break;
            case TAXA:
                queryBuilder.setTables(MainDatabaseHelper.TaxaColumns.TABLENAME);
                defaultSortOrder = MainDatabaseHelper.TaxaColumns.NAME + " COLLATE NOCASE ASC";
                break;
            case TAXA_BY_UNITY:
                queryBuilder.setTables(MainDatabaseHelper.TaxaColumns.TABLENAME + " LEFT OUTER JOIN " + MainDatabaseHelper.TaxaUnitiesColumns.TABLENAME + " ON " + MainDatabaseHelper.TaxaColumns.FULL_ID + " = " + MainDatabaseHelper.TaxaUnitiesColumns.FULL_TAXON_ID + " AND " + MainDatabaseHelper.TaxaUnitiesColumns.FULL_UNITY_ID + " = " + uri.getLastPathSegment());
                defaultSortOrder = MainDatabaseHelper.TaxaColumns.NAME + " COLLATE NOCASE ASC";
                break;
            case CRITERIA:
                queryBuilder.setTables(MainDatabaseHelper.CriteriaColumns.TABLENAME);
                defaultSortOrder = MainDatabaseHelper.CriteriaColumns.SORT + " COLLATE NOCASE ASC";
                break;
            case CRITERIA_BY_CLASS:
                queryBuilder.setTables(MainDatabaseHelper.CriteriaColumns.TABLENAME);
                queryBuilder.appendWhere(MainDatabaseHelper.CriteriaColumns.CLASS_ID + " = " + uri.getLastPathSegment());
                defaultSortOrder = MainDatabaseHelper.CriteriaColumns.SORT + " COLLATE NOCASE ASC";
                break;
            case ENVIRONMENTS:
                queryBuilder.setTables(MainDatabaseHelper.EnvironmentsColumns.TABLENAME);
                defaultSortOrder = MainDatabaseHelper.EnvironmentsColumns._ID;
                break;
            case INCLINES:
                queryBuilder.setTables(MainDatabaseHelper.InclinesColumns.TABLENAME);
                defaultSortOrder = MainDatabaseHelper.InclinesColumns.VALUE;
                break;
            case INCLINE_ID:
                queryBuilder.setTables(MainDatabaseHelper.InclinesColumns.TABLENAME);
                queryBuilder.appendWhere(MainDatabaseHelper.InclinesColumns._ID + " = " + uri.getLastPathSegment());
                defaultSortOrder = MainDatabaseHelper.InclinesColumns.VALUE;
                break;
            case PHENOLOGY:
                queryBuilder.setTables(MainDatabaseHelper.PhenologyColumns.TABLENAME);
                defaultSortOrder = MainDatabaseHelper.PhenologyColumns.CODE;
                break;
            case PHYSIOGNOMY_GROUPS:
                queryBuilder.setTables(MainDatabaseHelper.PhysiognomyColumns.TABLENAME);
                groupBy.append(MainDatabaseHelper.PhysiognomyColumns.GROUP_NAME);
                defaultSortOrder = MainDatabaseHelper.PhysiognomyColumns._ID;
                break;
            case PHYSIOGNOMY_BY_GROUP:
                queryBuilder.setTables(MainDatabaseHelper.PhysiognomyColumns.TABLENAME);
                queryBuilder.appendWhere(MainDatabaseHelper.PhysiognomyColumns.GROUP_NAME + " = " + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment()));
                defaultSortOrder = MainDatabaseHelper.PhysiognomyColumns.NAME;
                break;
            case DISTURBANCES_CLASSIFICATIONS:
                queryBuilder.setTables(MainDatabaseHelper.DisturbancesColumns.TABLENAME);
                groupBy.append(MainDatabaseHelper.DisturbancesColumns.CLASSIFICATION);
                defaultSortOrder = MainDatabaseHelper.DisturbancesColumns._ID;
                break;
            case DISTURBANCES_BY_CLASSIFICATION:
                queryBuilder.setTables(MainDatabaseHelper.DisturbancesColumns.TABLENAME);
                queryBuilder.appendWhere(MainDatabaseHelper.DisturbancesColumns.CLASSIFICATION + " = " + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment()));
                defaultSortOrder = MainDatabaseHelper.DisturbancesColumns.DESCRIPTION;
                break;
            case PROSPECTING_AREAS_BY_TAXON:
                queryBuilder.setTables(MainDatabaseHelper.ProspectingAreasColumns.TABLENAME);
                queryBuilder.appendWhere(MainDatabaseHelper.ProspectingAreasColumns.TAXON_ID + " = " + uri.getLastPathSegment());
                break;
            case SEARCH:
                queryBuilder.setTables(MainDatabaseHelper.SearchColumns.TABLENAME);
                break;
            case SEARCH_TAXON:
                queryBuilder.setTables(MainDatabaseHelper.SearchColumns.TABLENAME);
                queryBuilder.appendWhere(MainDatabaseHelper.SearchColumns.TAXON + " = " + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment()));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : " + uri);
        }

        // use the default sort order if needed
        if (!TextUtils.isEmpty(sortOrder)) {
            defaultSortOrder = sortOrder;
        }

        try {
            SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
            Cursor cursor = queryBuilder.query(db,
                                               projection,
                                               selection,
                                               selectionArgs,
                                               (groupBy.length() == 0) ? null : groupBy.toString(),
                                               null,
                                               defaultSortOrder);

            // make sure that potential listeners are getting notified
            cursor.setNotificationUri(getContext().getContentResolver(),
                                      uri);

            return cursor;
        }
        catch (IOException ioe) {
            Log.e(TAG,
                  ioe.getMessage(),
                  ioe);

            return null;
        }
        catch (SQLiteException se) {
            Log.e(TAG,
                  se.getMessage(),
                  se);

            return null;
        }
    }

    @Override
    public Uri insert(
            Uri uri,
            ContentValues values) {
        // nothing to do ...
        return null;
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {
        // nothing to do ...
        return 0;
    }

    @Override
    public int delete(
            Uri uri,
            String selection,
            String[] selectionArgs) {
        // nothing to do ...
        return 0;
    }

    @Override
    public String getType(Uri uri) {

        return null;
    }

    public abstract String getAuthority();

    public abstract AbstractAppSettings getAppSettings();

    private MainDatabaseHelper getDatabaseHelper() throws IOException {

        if (mDatabase == null) {
            mDatabase = new MainDatabaseHelper(getContext(),
                                               getAppSettings().getDbSettings()
                                                               .getDbName(),
                                               getAppSettings().getDbSettings()
                                                               .getDbVersion());
        }

        return mDatabase;
    }

    private void initializeUriMatcher() {

        sURIMatcher.addURI(getAuthority(),
                           PATH_OBSERVERS,
                           OBSERVERS);
        sURIMatcher.addURI(getAuthority(),
                           PATH_OBSERVERS + "/#",
                           OBSERVER_ID);
        sURIMatcher.addURI(getAuthority(),
                           PATH_TAXA,
                           TAXA);
        sURIMatcher.addURI(getAuthority(),
                           PATH_TAXA_UNITY + "/#",
                           TAXA_BY_UNITY);
        sURIMatcher.addURI(getAuthority(),
                           PATH_CRITERIA,
                           CRITERIA);
        sURIMatcher.addURI(getAuthority(),
                           PATH_CRITERIA_CLASS + "/#",
                           CRITERIA_BY_CLASS);
        sURIMatcher.addURI(getAuthority(),
                           PATH_ENVIRONMENTS,
                           ENVIRONMENTS);
        sURIMatcher.addURI(getAuthority(),
                           PATH_INCLINES,
                           INCLINES);
        sURIMatcher.addURI(getAuthority(),
                           PATH_INCLINES + "/#",
                           INCLINE_ID);
        sURIMatcher.addURI(getAuthority(),
                           PATH_PHENOLOGY,
                           PHENOLOGY);
        sURIMatcher.addURI(getAuthority(),
                           PATH_PHYSIOGNOMY_GROUPS,
                           PHYSIOGNOMY_GROUPS);
        sURIMatcher.addURI(getAuthority(),
                           PATH_PHYSIOGNOMY_GROUPS + "/*",
                           PHYSIOGNOMY_BY_GROUP);
        sURIMatcher.addURI(getAuthority(),
                           PATH_DISTURBANCES_CLASSIFICATIONS,
                           DISTURBANCES_CLASSIFICATIONS);
        sURIMatcher.addURI(getAuthority(),
                           PATH_DISTURBANCES_CLASSIFICATIONS + "/*",
                           DISTURBANCES_BY_CLASSIFICATION);
        sURIMatcher.addURI(getAuthority(),
                           PATH_PROSPECTING_AREAS_TAXON + "/#",
                           PROSPECTING_AREAS_BY_TAXON);
        sURIMatcher.addURI(getAuthority(),
                           PATH_SEARCH,
                           SEARCH);
        sURIMatcher.addURI(getAuthority(),
                           PATH_SEARCH + "/*",
                           SEARCH_TAXON);
    }
}

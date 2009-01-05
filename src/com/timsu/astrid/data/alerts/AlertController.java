/*
 * ASTRID: Android's Simple Task Recording Dashboard
 *
 * Copyright (c) 2009 Tim Su
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.timsu.astrid.data.alerts;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.timsu.astrid.data.AbstractController;
import com.timsu.astrid.data.alerts.Alert.AlertDatabaseHelper;
import com.timsu.astrid.data.task.TaskIdentifier;

/** Controller for Tag-related operations */
public class AlertController extends AbstractController {

    private SQLiteDatabase alertDatabase;

    /** Get a cursor to tag identifiers */
    public Cursor getTaskAlertsCursor(TaskIdentifier taskId) throws SQLException {
        Cursor cursor = alertDatabase.query(ALERT_TABLE_NAME,
                Alert.FIELD_LIST, Alert.TASK + " = ?",
                new String[] { taskId.idAsString() }, null, null, null);
        return cursor;
    }
    /** Get a list of tag identifiers for the given task */
    public List<Date> getTaskAlerts(TaskIdentifier
            taskId) throws SQLException {
        List<Date> list = new LinkedList<Date>();
        Cursor cursor = alertDatabase.query(ALERT_TABLE_NAME,
                Alert.FIELD_LIST, Alert.TASK + " = ?",
                new String[] { taskId.idAsString() }, null, null, null);

        try {
            if(cursor.getCount() == 0)
                return list;
            do {
                cursor.moveToNext();
                list.add(new Alert(cursor).getDate());
            } while(!cursor.isLast());

            return list;
        } finally {
            cursor.close();
        }
    }

    /** Remove all alerts from the task */
    public boolean removeAlerts(TaskIdentifier taskId)
            throws SQLException{
        return alertDatabase.delete(ALERT_TABLE_NAME,
                String.format("%s = ?",
                        Alert.TASK),
                new String[] { taskId.idAsString() }) > 0;
    }

    /** Add the given tag to the task */
    public boolean addAlert(TaskIdentifier taskId, Date date)
            throws SQLException {
        ContentValues values = new ContentValues();
        values.put(Alert.DATE, date.getTime());
        values.put(Alert.TASK, taskId.getId());
        return alertDatabase.insert(ALERT_TABLE_NAME, Alert.TASK,
                values) >= 0;
    }

    // --- boilerplate

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     */
    public AlertController(Context context) {
        this.context = context;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public AlertController open() throws SQLException {
        alertDatabase = new AlertDatabaseHelper(context,
                ALERT_TABLE_NAME, ALERT_TABLE_NAME).getWritableDatabase();

        return this;
    }

    /** Closes database resource */
    public void close() {
        alertDatabase.close();
    }
}

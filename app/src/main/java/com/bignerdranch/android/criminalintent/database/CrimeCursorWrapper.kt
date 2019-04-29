package com.bignerdranch.android.criminalintent.database

import android.database.Cursor
import android.database.CursorWrapper
import com.bignerdranch.android.criminalintent.Crime
import java.util.Date
import java.util.UUID

class CrimeCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {

    fun getCrime(): Crime {
        val uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID))

        return Crime(UUID.fromString(uuidString)).apply {
            title = getString(getColumnIndex(CrimeTable.Cols.TITLE))
            date = Date(getLong(getColumnIndex(CrimeTable.Cols.DATE)))
            isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED)) != 0
            suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT))
        }
    }
}
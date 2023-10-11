package vn.trunglt.demoloadmoretwoway.core.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import androidx.core.database.getStringOrNull

abstract class SqlDbHelper(
    context: Context,
    databaseName: String,
    databaseVersion: Int
) : SQLiteOpenHelper(context, databaseName, null, databaseVersion) {

    abstract fun createTable(): String
    abstract fun deleteTable(): String


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTable())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(deleteTable())
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}

class StudentModel(
    val name: String?,
    val age: String?,
    val sex: String?
)

class StudentDbHelper(context: Context) : SqlDbHelper(
    context,
    DATABASE_NAME,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FeedReader.db"
    }

    object STUDENT_TABLE : BaseColumns {
        const val TABLE_NAME = "entry"
        const val COLUMN_FULL_NAME = "name"
        const val COLUMN_AGE = "age"
        const val COLUMN_SEX = "sex"
    }

    override fun createTable() = "CREATE TABLE ${STUDENT_TABLE.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
            "${STUDENT_TABLE.COLUMN_FULL_NAME} TEXT," +
            "${STUDENT_TABLE.COLUMN_AGE} TEXT," +
            "${STUDENT_TABLE.COLUMN_SEX} TEXT)"

    override fun deleteTable() = "DROP TABLE IF EXISTS ${STUDENT_TABLE.TABLE_NAME}"

    fun insertStudent(data: StudentModel) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(STUDENT_TABLE.COLUMN_FULL_NAME, data.name)
            put(STUDENT_TABLE.COLUMN_AGE, data.age)
            put(STUDENT_TABLE.COLUMN_SEX, data.sex)
        }
        // Insert the new row, returning the primary key value of the new row
        db?.insert(STUDENT_TABLE.TABLE_NAME, null, values)
    }

    fun getStudent(id: Int): StudentModel? {
        val db = this.readableDatabase
        val cursor = db.query(
            STUDENT_TABLE.TABLE_NAME,
            null,
            BaseColumns._ID + " = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        return cursor?.let {
            it.moveToFirst()
            val data = StudentModel(
                it.getStringOrNull(0),
                it.getStringOrNull(1),
                it.getStringOrNull(2),
            )
            it.close()
            data
        } ?: run {
            null
        }
    }

    fun getAllStudents(): List<StudentModel> {
        val studentList = mutableListOf<StudentModel>()
        val query = "SELECT * FROM" + STUDENT_TABLE.TABLE_NAME
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        cursor?.let {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val student = StudentModel(
                    cursor.getStringOrNull(0),
                    cursor.getStringOrNull(1),
                    cursor.getStringOrNull(2),
                )
                studentList.add(student)
                cursor.moveToNext()
            }
        }
        cursor.close()
        return studentList
    }
}
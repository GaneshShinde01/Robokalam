package com.project.robokalam.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.project.robokalam.Model.PortfolioModel;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "robokalam.db";
    public static final int DB_VERSION = 1;


    public SQLiteHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    String createUser = "CREATE TABLE users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "email TEXT UNIQUE," +
            "is_logged_in INTEGER DEFAULT 0);";

    String createPortfolio = "CREATE TABLE portfolio (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "email TEXT," +
            "name TEXT," +
            "college TEXT," +
            "skills TEXT," +
            "project_title TEXT," +
            "project_description TEXT);";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createUser);
        db.execSQL(createPortfolio);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS portfolio");
        onCreate(db);
    }



    public boolean insertPortfolio(PortfolioModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", model.getEmail());
        cv.put("name", model.getName());
        cv.put("college", model.getCollege());
        cv.put("skills", model.getSkills());
        cv.put("project_title", model.getProjectTitle());
        cv.put("project_description", model.getProjectDescription());

        long result = db.insert("portfolio", null, cv);
        Log.d("DB_RESULT", "Insert result: " + result);
        return result != -1;
    }


    public PortfolioModel getPortfolioByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM portfolio WHERE email = ?", new String[]{email});
        PortfolioModel portfolio = null;

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndexOrThrow("name");
            int collegeIndex = cursor.getColumnIndexOrThrow("college");
            int skillsIndex = cursor.getColumnIndexOrThrow("skills");
            int projectTitleIndex = cursor.getColumnIndexOrThrow("project_title");
            int projectDescIndex = cursor.getColumnIndexOrThrow("project_description");

            String name = cursor.getString(nameIndex);
            String college = cursor.getString(collegeIndex);
            String skills = cursor.getString(skillsIndex);
            String projectTitle = cursor.getString(projectTitleIndex);
            String projectDesc = cursor.getString(projectDescIndex);

            portfolio = new PortfolioModel(email, name, college, skills, projectTitle, projectDesc);
        }
        cursor.close();
        return portfolio;
    }



}

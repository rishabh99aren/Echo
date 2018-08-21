package com.rishabh.example.echo.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.rishabh.example.echo.Songs
import com.rishabh.example.echo.adapters.FavouriteAdapter
import com.rishabh.example.echo.fragments.FavouriteFragment

class EchoDatabase: SQLiteOpenHelper{

    object Staticated{
        val DB_NAME = "FavouriteDatabase"
        var _songList = ArrayList<Songs>()
        val TABLE_NAME = "FavouriteTable"
        val COLUMN_ID = "SongID"
        val COLUMN_SONG_TITLE = "SongTitle"
        val COLUMN_SONG_ARTIST = "SongArtist"
        val COLUMN_SONG_PATH = "SongPath"
        var DB_VERSION = 1
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase?) {
        sqLiteDatabase?.execSQL("CREATE TABLE "+ Staticated.TABLE_NAME + "(" + Staticated.COLUMN_ID + " INTEGER," + Staticated.COLUMN_SONG_ARTIST +
                " STRING," + Staticated.COLUMN_SONG_TITLE + " STRING," + Staticated.COLUMN_SONG_PATH + " STRING);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)
    constructor(context: Context?) : super(context, Staticated.DB_NAME, null, Staticated.DB_VERSION)

    fun storeAsFavourite(id: Int?, artist: String?, title: String?, path: String?){
        val db = this.writableDatabase
        var contentValues = ContentValues()
        contentValues.put(Staticated.COLUMN_ID, id)
        contentValues.put(Staticated.COLUMN_SONG_ARTIST, artist)
        contentValues.put(Staticated.COLUMN_SONG_TITLE, title)
        contentValues.put(Staticated.COLUMN_SONG_PATH, path)
        db.insert(Staticated.TABLE_NAME, null, contentValues)
        db.close()
        FavouriteFragment.Statified.favouriteAdapter?.notifyDataSetChanged()
    }

    fun queryDBList(): ArrayList<Songs>?{
        try{
            val db = this.readableDatabase
            val query_params = "SELECT * FROM " + Staticated.TABLE_NAME
            var cSon = db.rawQuery(query_params, null)
            if(cSon.moveToFirst()){
                do{
                    var id = cSon.getInt(cSon.getColumnIndexOrThrow("SongID"))
                    var artist = cSon.getString(cSon.getColumnIndexOrThrow("SongArtist"))
                    var title = cSon.getString(cSon.getColumnIndexOrThrow("SongTitle"))
                    var path = cSon.getString(cSon.getColumnIndexOrThrow("SongPath"))
                    Staticated._songList.add(Songs(id.toLong(), title, artist, path, 0))
                }while(cSon.moveToNext())
            }else{
                return null
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return Staticated._songList
    }

    fun checkifidexists(_id: Int): Boolean{
        var storeId = -1090
        val db = this.readableDatabase
        val query_params = "SELECT * FROM " + Staticated.TABLE_NAME + " WHERE SongID = " + _id
        val cSon = db.rawQuery(query_params, null)
        if(cSon.moveToFirst()){
            do{
                storeId = cSon.getInt(cSon.getColumnIndexOrThrow(Staticated.COLUMN_ID))
            }while(cSon.moveToNext())
        }else{
            return false
        }
        return storeId != -1090
    }

    fun deletefavourite(_id: Int){
        val db = this.writableDatabase
        db.delete(Staticated.TABLE_NAME, "SongID = " + _id, null)
        db.close()
        FavouriteFragment.Statified.favouriteAdapter?.notifyDataSetChanged()
    }

    fun checkSize(): Int{
        var counter = 0
        val db = this.readableDatabase
        val query_params = "SELECT * FROM " + Staticated.TABLE_NAME
        val cSon = db.rawQuery(query_params, null)
        if(cSon.moveToFirst()){
            do{
                counter = counter + 1
            }while(cSon.moveToNext())
        }
        cSon.close()
        return counter
    }
}

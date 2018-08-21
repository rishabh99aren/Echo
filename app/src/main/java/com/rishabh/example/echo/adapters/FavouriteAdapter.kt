package com.rishabh.example.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.rishabh.example.echo.R
import com.rishabh.example.echo.Songs
import com.rishabh.example.echo.fragments.SongPlayingFragment

class FavouriteAdapter(_songDetails: ArrayList<Songs>, _context: Context): RecyclerView.Adapter<FavouriteAdapter.MyViewHolder>(){
    var songsDetails: ArrayList<Songs>? = null
    var mContext: Context? = null
    init{
        this.songsDetails=_songDetails
        this.mContext=_context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_mainscreen_adapter, parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if(songsDetails == null){
            return 0
        }else{
            return (songsDetails as ArrayList<Songs>).size
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObject = songsDetails?.get(position)
        holder.trackTitle?.text=songObject?.songTitle
        holder.trackArtist?.text=songObject?.artist
        holder.contentHolder?.setOnClickListener ({
            SongPlayingFragment.Statified.mediaPlayer?.stop()
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songObject?.artist)
            args.putString("songPath", songObject?.songData)
            args.putString("songTitle", songObject?.songTitle)
            args.putInt("songId", songObject?.songID?.toInt() as Int)
            args.putInt("songPosition", position)
            args.putParcelableArrayList("songData", songsDetails)
            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragmentFavourite")
                    .commit()
        })
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init{
            trackTitle = view.findViewById(R.id.trackTitle) as TextView
            trackArtist = view.findViewById(R.id.trackArtist) as TextView
            contentHolder = view.findViewById(R.id.contentRow) as RelativeLayout
        }
    }

}
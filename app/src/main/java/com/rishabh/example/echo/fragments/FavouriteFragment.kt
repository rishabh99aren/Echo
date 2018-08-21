package com.rishabh.example.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.rishabh.example.echo.R
import com.rishabh.example.echo.R.id.favouriteRecycler
import com.rishabh.example.echo.Songs
import com.rishabh.example.echo.adapters.FavouriteAdapter
import com.rishabh.example.echo.databases.EchoDatabase
import com.rishabh.example.echo.fragments.FavouriteFragment.Statified.favouriteAdapter
import java.util.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FavouriteFragment : Fragment() {

    var myActivity: Activity? = null
    var noFavourites: TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    var favouriteContent: EchoDatabase? = null


    var refreshList: ArrayList<Songs>? = null
    var getListfromDatabase: ArrayList<Songs>? = null

    object Statified{
        var mediaPlayer: MediaPlayer? = null
        var favouriteAdapter: FavouriteAdapter? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.title = "Favourites"
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        favouriteContent = EchoDatabase(myActivity)
        noFavourites = view?.findViewById(R.id.noFavourites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        songTitle = view?.findViewById<TextView>(R.id.songTitle)
        recyclerView = view?.findViewById(R.id.favouriteRecycler)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        display_favourites_by_searching()
        bottomBarSetup()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item?.itemId
        if(switcher == R.id.action_sort_ascending){
            val editor = myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("action_sort_ascending", true)
            editor?.putBoolean("action_sort_recent", false)
            editor?.apply()
            if(refreshList != null){
                Collections.sort(refreshList, Songs.Statified.nameComparater)
            }
            favouriteAdapter?.notifyDataSetChanged()
            return false
        }else if(switcher == R.id.action_sort_recent) {
            val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean("action_sort_ascending", false)
            editor?.putBoolean("action_sort_recent", true)
            editor?.apply()
            if (refreshList != null) {
                Collections.sort(refreshList, Songs.Statified.dateComparator)
            }
            favouriteAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = false
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = true
    }

    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            var songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            var songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            var songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            var dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }
        }
        return arrayList
    }

    fun bottomBarSetup(){
        try{
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener ({
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComplete()
            })
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }else{
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("songPath", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar","Success")
            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()
                    ?.replace(R.id.details_fragment, songPlayingFragment)
                    ?.addToBackStack("SongPlayingFragment")
                    ?.commit()
        })

        playPauseButton?.setOnClickListener({
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                SongPlayingFragment.Statified.currentSongHelper?.isPlaying = false
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    fun display_favourites_by_searching(){
        if(favouriteContent?.checkSize() as Int > 0){
            refreshList = ArrayList<Songs>()
            refreshList?.clear()
            getListfromDatabase = favouriteContent?.queryDBList()
            var fetchListfromDevice = getSongsFromPhone()
            if(fetchListfromDevice.isNotEmpty()){
                for(i in 0..fetchListfromDevice?.size - 1){
                    for(j in 0..getListfromDatabase?.size as Int - 1){
                        if((getListfromDatabase?.get(j)?.songID) == (fetchListfromDevice?.get(i)?.songID)){
                            refreshList?.add((getListfromDatabase as ArrayList<Songs>)[j])
                            break
                        }
                    }
                }
            }
            if(refreshList?.isEmpty() as Boolean){
                recyclerView?.visibility = View.INVISIBLE
                noFavourites?.visibility = View.VISIBLE
            }else{
                favouriteAdapter = FavouriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                var mLayoutManager = LinearLayoutManager(myActivity)

                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favouriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }else{
            recyclerView?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }
    }
}

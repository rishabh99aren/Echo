package com.rishabh.example.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.rishabh.example.echo.CurrentSongHelper
import com.rishabh.example.echo.R
import com.rishabh.example.echo.Songs
import com.rishabh.example.echo.databases.EchoDatabase
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {

    object Statified{
        var myActivity: Activity?=null

        var mediaPlayer: MediaPlayer?=null

        var songTitleView: TextView?=null
        var songArtistView: TextView?=null
        var seekBar: SeekBar?=null
        var startTimeText: TextView?= null
        var endTimeText: TextView?=null
        var shuffleImageButton: ImageButton?= null
        var previousImageButton: ImageButton?=null
        var playPauseImageButton: ImageButton?=null
        var nextImageButton: ImageButton?=null
        var loopImageButton: ImageButton?=null

        var currentPosition: Int?=0
        var fetchSongs: ArrayList<Songs>?=null

        var currentSongHelper: CurrentSongHelper?=null

        var audioVisualization: AudioVisualization?=null
        var glView: GLAudioVisualizationView?=null

        var fab: ImageButton? = null
        var favouriteContent: EchoDatabase? = null

        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null

        var MY_PREFS_NAME = "ShakeFeature"
        var updateSongTime = object: Runnable{
            override fun run() {
                val getCurrent = mediaPlayer?.currentPosition

                startTimeText?.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                        (TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long)))))
                seekBar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this, 1000)
            }
        }
    }



    object Staticated{
        var My_Prefs_Shuffle = "Shuffle Feature"
        var My_Prefs_Loop = "Loop Feature"

        fun onSongComplete(){
            if(Statified.currentSongHelper?.isShuffle as Boolean){
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying=true
            }else{
                if(Statified.currentSongHelper?.isloop as Boolean){
                    Statified.currentSongHelper?.isPlaying=true
                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition as Int)
                    Statified.currentSongHelper?.songPath=nextSong?.songData
                    Statified.currentSongHelper?.songTitle=nextSong?.songTitle
                    Statified.currentSongHelper?.songArtist=nextSong?.artist
                    Statified.currentSongHelper?.songId=nextSong?.songID
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition as Int

                    updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

                    Statified.mediaPlayer?.reset()
                    try{
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }else{
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying=true
                }
            }
            if (Statified.favouriteContent?.checkifidexists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songTitle: String, songArtist: String){
            var songTitleUpdate = songTitle
            var songArtistUpdate = songArtist
            if(songTitle.equals("<unknown>",true)){
                songTitleUpdate = "unknown"
            }
            if(songArtist.equals("<unknown>",true)){
                songArtistUpdate = "unknown"
            }
            Statified.songTitleView?.setText(songTitleUpdate)
            Statified.songArtistView?.setText(songArtistUpdate)
        }

        fun processInformation(mediaPlayer: MediaPlayer){
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekBar?.max = finalTime

            Statified.startTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
                    (TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long)))))

            Statified.endTimeText?.setText(String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long),
                    (TimeUnit.MILLISECONDS.toSeconds(finalTime?.toLong() as Long) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long)))))

            Statified.seekBar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }

        fun playNext(check: String){
            if(check.equals("PlayNextNormal", true)){
                Statified.currentPosition = Statified.currentPosition?.plus(1) as Int
            }else if(check.equals("PlayNextLikeNormalShuffle", true)){
                var randomObject = Random()
                var randomPosition = randomObject?.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition
            }
            if(Statified.currentPosition == Statified.fetchSongs?.size){
                Statified.currentPosition = 0
            }
            if(Statified.currentSongHelper?.isPlaying as Boolean){
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }else {
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }
            Statified.currentSongHelper?.isloop=false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition as Int)
            Statified.currentSongHelper?.songPath=nextSong?.songData
            Statified.currentSongHelper?.songTitle=nextSong?.songTitle
            Statified.currentSongHelper?.songArtist=nextSong?.artist
            Statified.currentSongHelper?.songId=nextSong?.songID as Long
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition as Int

            updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()
            try{
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            }catch (e: Exception){
                e.printStackTrace()
            }
            if (Statified.favouriteContent?.checkifidexists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))
            }
        }
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        activity?.title = "Now Playing"
        val view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)

        setHasOptionsMenu(true)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.seekBar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favouriteIcon)
        Statified.fab?.alpha = 0.8f

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity=context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity=activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener, Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        Statified.audioVisualization?.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Statified.audioVisualization?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favouriteContent = EchoDatabase(Statified.myActivity)

        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isloop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long? = 0

        try {
            path = arguments?.getString("songPath")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")?.toLong()

            Statified.currentPosition = arguments?.getInt("songPosition")
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")


            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition as Int

            Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        var fromMainBottomBar = arguments?.get("MainBottomBar") as? String
        if(fromFavBottomBar != null){
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer
            if (Statified.currentSongHelper?.isPlaying as Boolean) {
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            } else {
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }
        }else if(fromMainBottomBar != null){
            Statified.mediaPlayer = MainScreenFragment.Statified.mediaPlayer
            if (Statified.currentSongHelper?.isPlaying as Boolean) {
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            } else {
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }
        }else{
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)

        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }

        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        clickHandler()

        var visualizorHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisualization?.linkTo(visualizorHandler)

        if (Statified.favouriteContent?.checkifidexists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))
        }

        var prefsForShuffle = Statified.myActivity?.getSharedPreferences(Staticated.My_Prefs_Shuffle, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isloop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.My_Prefs_Loop, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isloop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            Statified.currentSongHelper?.isloop = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
    }

    fun clickHandler(){

        Statified.fab?.setOnClickListener({
            if (Statified.favouriteContent?.checkifidexists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))
                Statified.favouriteContent?.deletefavourite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity, "Removed from favourites", Toast.LENGTH_SHORT).show()
            } else {
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
                Statified.favouriteContent?.storeAsFavourite(Statified.currentSongHelper?.songId?.toInt(), Statified.currentSongHelper?.songArtist,
                        Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity, "Added to favourite", Toast.LENGTH_SHORT).show()
            }
        })

        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.My_Prefs_Shuffle, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.My_Prefs_Loop, Context.MODE_PRIVATE)?.edit()
            if(Statified.currentSongHelper?.isShuffle as Boolean){
                Statified.currentSongHelper?.isShuffle=false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                Toast.makeText(Statified.myActivity, "Shuffle off", Toast.LENGTH_SHORT).show()
            }else{
                Statified.currentSongHelper?.isShuffle=true
                Statified.currentSongHelper?.isloop=false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
                Toast.makeText(Statified.myActivity, "Shuffle on", Toast.LENGTH_SHORT).show()
            }
        })

        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying=true
            if(Statified.currentSongHelper?.isloop as Boolean){
                Statified.currentSongHelper?.isloop=false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })

        Statified.playPauseImageButton?.setOnClickListener({
            if(Statified.mediaPlayer?.isPlaying as Boolean){
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying=false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying=true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })

        Statified.nextImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying=true
            if(Statified.currentSongHelper?.isShuffle as Boolean){
                Staticated.playNext("PlayNextLikeNormalShuffle")
            }else{
                Staticated.playNext("PlayNextNormal")
            }
        })

        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle = Statified.myActivity?.getSharedPreferences(Staticated.My_Prefs_Shuffle, Context.MODE_PRIVATE)?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(Staticated.My_Prefs_Loop, Context.MODE_PRIVATE)?.edit()
            if(Statified.currentSongHelper?.isloop as Boolean){
                Statified.currentSongHelper?.isloop=false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
                Toast.makeText(Statified.myActivity, "Loop off", Toast.LENGTH_SHORT).show()
            }else{
                Statified.currentSongHelper?.isloop=true
                Statified.currentSongHelper?.isShuffle=false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                Toast.makeText(Statified.myActivity, "Loop on", Toast.LENGTH_SHORT).show()
            }
        })
    }



    fun playPrevious(){
        Statified.currentPosition = Statified.currentPosition?.minus(1) as Int
        if(Statified.currentPosition == -1){
            Statified.currentPosition = 0
        }
        if(Statified.currentSongHelper?.isPlaying as Boolean){
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }else{
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }
        Statified.currentSongHelper?.isloop=false
        var nextSong = Statified.fetchSongs?.get(Statified.currentPosition as Int)
        Statified.currentSongHelper?.songPath=nextSong?.songData
        Statified.currentSongHelper?.songTitle=nextSong?.songTitle
        Statified.currentSongHelper?.songArtist=nextSong?.artist
        Statified.currentSongHelper?.songId=nextSong?.songID

        Staticated.updateTextViews(Statified.currentSongHelper?.songTitle as String, Statified.currentSongHelper?.songArtist as String)

        Statified.mediaPlayer?.reset()
        try{
            Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        }catch (e: Exception){
            e.printStackTrace()
        }
        if (Statified.favouriteContent?.checkifidexists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity as Context, R.drawable.favorite_off))
        }
    }

    fun bindShakeListener(){
        Statified.mSensorListener = object: SensorEventListener{
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt((x*x + y*y + z*z).toDouble()).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration * 0.9f + delta
                if(mAcceleration > 12) {
                    val prefs = Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")
                    }
                }
            }

        }
    }
}

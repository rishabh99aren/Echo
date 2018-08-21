package com.rishabh.example.echo.utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.rishabh.example.echo.R
import com.rishabh.example.echo.activities.MainActivity
import com.rishabh.example.echo.fragments.SongPlayingFragment

class CaptureBroadcast: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_NEW_OUTGOING_CALL){
            try{
                MainActivity.Statified.notificationManager?.cancel(1978)
            }catch (e: Exception){
                e.printStackTrace()
            }
            try{
                if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }else{
            val tm:TelephonyManager = context?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            when(tm?.callState){
                TelephonyManager.CALL_STATE_RINGING ->{
                    try{
                        MainActivity.Statified.notificationManager?.cancel(1978)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                    try{
                        if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                            SongPlayingFragment.Statified.mediaPlayer?.pause()
                            SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
                else ->{

                }
            }
        }
    }

}
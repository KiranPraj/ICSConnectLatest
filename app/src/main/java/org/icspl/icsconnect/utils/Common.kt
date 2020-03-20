package org.icspl.icsconnect.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.icspl.icsconnect.R
import org.icspl.icsconnect.callbacks.ApiService
import org.icspl.icsconnect.networking.RetrofitClient
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class Common {
     fun getTimesheetAPI1():ApiService
    {
       // return RetrofitClient.getClient("http://192.168.0.243:99/Home/").create(ApiService::class.java)  // local
        return RetrofitClient.getClient("http://icspl.org:5005/Home/").create(ApiService::class.java)  // live


    }

    companion object {
      // private val BASE_URL = "http://192.168.0.243:99/Home/"
       private val BASE_URL = "http://192.168.0.243:99/"  // local 11 nov19

    //   public val BASE_URL = "http://icspl.org:5005/"


        public fun getAPI():ApiService
        {
            return RetrofitClient.getClient(BASE_URL).create(ApiService::class.java)
        }
       /* public fun getAPI(value:String ): ApiService {
            var k: ApiService? =null
        if(value.equals("common"))
            {
                k= RetrofitClient.getClient("http://192.168.0.243:99/Home/").create(ApiService::class.java)
            }
            else if(value.equals("timesheet"))
            {
                k= RetrofitClient.getClient("http://192.168.0.243:99/Timesheet/").create(ApiService::class.java)
            }
        return k!!
        }*/

        public fun isConnectedMobile(context: Context): Boolean {

            val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE)
                    as ConnectivityManager?

            val networkInfo = connectivityManager!!.activeNetworkInfo

            return networkInfo != null && networkInfo.isConnected
        }
        public fun getFormatedDate(date: String): Int {
            val dateFormat = SimpleDateFormat("MM/d/yyyy", Locale.ENGLISH)

            val sdate = dateFormat.parse(date)

            val formatter = SimpleDateFormat("yyyy", Locale.ENGLISH)
            val newDate = formatter.format(sdate).toInt()

            return newDate
        }

    }

    fun convertMultipart(imagePath: String?, paramName: String): MultipartBody.Part {
        val mRequestBody: RequestBody
        val body5: MultipartBody.Part
        if (imagePath != null)
            if (!imagePath.equals("NA", ignoreCase = true)) {
                val mFile = File(imagePath)
                mRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), mFile)
                body5 = MultipartBody.Part.createFormData(paramName, mFile.name, mRequestBody)
                Log.i("Multipart body", "convertMultipart: " + mFile.name)
            } else {
                mRequestBody = RequestBody.create(MediaType.parse("text/plain"), "")
                body5 = MultipartBody.Part.createFormData(paramName, "NA", mRequestBody)
            }
        else {
            mRequestBody = RequestBody.create(MediaType.parse("text/plain"), "")
            body5 = MultipartBody.Part.createFormData(paramName, "NA", mRequestBody)
        }
        return body5
    }

    public fun getFormatedDate(date: String): String {
        val dateFormat = SimpleDateFormat("MM/d/yyyy", Locale.ENGLISH)

        val sdate = dateFormat.parse(date)

        val formatter = SimpleDateFormat("yyyy", Locale.ENGLISH)
        val newDate = formatter.format(sdate)

        return newDate
    }

    public fun getDateFormat(): String {
        val mSDF = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return mSDF.format(Date())
    }

    public fun getTime(): String {
        val mSDF = SimpleDateFormat("HH:mm", Locale.getDefault())
        return mSDF.format(Date())
    }

    fun getDownloadSpeedString(context: Context, downloadedBytesPerSecond: Long): String {
        if (downloadedBytesPerSecond < 0) {
            return ""
        }
        val kb = downloadedBytesPerSecond.toDouble() / 1000.toDouble()
        val mb = kb / 1000.toDouble()
        val decimalFormat = DecimalFormat(".##")
        return when {
            mb >= 1 -> context.getString(R.string.downloadSpeedMb, decimalFormat.format(mb))
            kb >= 1 -> context.getString(R.string.downloadSpeedKb, decimalFormat.format(kb))
            else -> context.getString(R.string.downloadSpeedBytes, downloadedBytesPerSecond)
        }
    }

}

package de.siebes.fabian.virtucard

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class Utils {

    companion object {
        const val BASE_URL = "https://virtucard.fabsie.tk/"
        private const val BASE_PROFILE_URL = BASE_URL + "p/"

        fun getProfileUrl(id: String): String? {
            if (id.isNotEmpty()) {
                return BASE_PROFILE_URL + id
            }
            return null
        }

        fun getProfileUrl(id: String, pw: String): String? {
            if (pw.isEmpty()) {
                return getProfileUrl(id)
            }
            if (id.isNotEmpty()) {
                return "$BASE_PROFILE_URL$id/$pw"
            }
            return null
        }

        fun isNetworkAvailable(context: Context?): Boolean {
            val ctx = context ?: return false
            val connectivityManager =
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    }
}
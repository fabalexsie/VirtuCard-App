package de.siebes.fabian.virtucard

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
    }
}
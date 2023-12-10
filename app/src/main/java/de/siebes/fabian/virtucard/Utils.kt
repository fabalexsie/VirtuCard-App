package de.siebes.fabian.virtucard

class Utils {
    companion object {
        fun getProfileUrl(id: String): String {
            return Consts.BASE_PROFILE_URL + id
        }
    }
}
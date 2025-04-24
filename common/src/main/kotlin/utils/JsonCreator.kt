package utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class JsonCreator {
    inline fun <reified T> objectToString (obj: T): String{
        return Json.encodeToString(serializer(), obj)
    }

    inline fun <reified T> stringToObject(string: String): T{
        return Json.decodeFromString(serializer(), string)
    }


}
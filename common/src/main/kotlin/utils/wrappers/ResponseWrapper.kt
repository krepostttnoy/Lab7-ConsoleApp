package utils.wrappers

import kotlinx.serialization.Serializable

@Serializable
class ResponseWrapper(
    val responseType: ResponseType,
    val message: String
) {
}

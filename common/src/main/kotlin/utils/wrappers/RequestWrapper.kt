package utils.wrappers

import kotlinx.serialization.Serializable

@Serializable
class RequestWrapper(
    val requestType: RequestType,
    val message: String,
    val args: Map<String, String>) {
}
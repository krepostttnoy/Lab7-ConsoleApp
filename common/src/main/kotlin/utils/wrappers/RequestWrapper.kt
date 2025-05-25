package utils.wrappers

import kotlinx.serialization.Serializable

@Serializable
class RequestWrapper(
    val requestType: RequestType,
    override var message: String,
    val args: MutableMap<String, String>,
    override var token: String = ""
) : Sending{
}
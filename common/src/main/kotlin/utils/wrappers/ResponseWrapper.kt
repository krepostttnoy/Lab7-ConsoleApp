package utils.wrappers

import kotlinx.serialization.Serializable

@Serializable
class ResponseWrapper(
    val responseType: ResponseType,
    override var message: String,
    override var token: String = "",
    var receiver: String
) : Sending{
}

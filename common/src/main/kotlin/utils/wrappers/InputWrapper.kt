package utils.wrappers

import kotlinx.serialization.Serializable

@Serializable
class InputWrapper(
    val requestType: String,
    val message: String,
    val args: Map<String, String>
    ) {

}
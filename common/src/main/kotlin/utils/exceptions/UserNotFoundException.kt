package utils.exceptions

class UserNotFoundException: Exception() {
    override val message: String = "User with this login didn't find"
}
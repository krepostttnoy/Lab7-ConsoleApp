package utils.exceptions

class UserIsNotAuthorizedException: Exception() {
    override val message = "User is not authorized"
}
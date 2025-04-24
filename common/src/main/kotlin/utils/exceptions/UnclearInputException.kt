package utils.exceptions

class UnclearInputException: Exception() {
    override val message: String = "Unclear input from client."
}
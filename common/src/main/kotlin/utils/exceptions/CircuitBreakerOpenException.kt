package utils.exceptions

class CircuitBreakerOpenException(text: String): Exception() {
    override val message = text
}
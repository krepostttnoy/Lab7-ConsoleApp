package org.example.clientUtils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference


class CircuitBreaker(
    private val failuresHold: Int = 3,
    private val timeout: Long = 20000
) {
    private var failureCount = AtomicInteger(0)
    private var lastFailureTime = AtomicLong(0)
    private var state = AtomicReference(State.CLOSED)
    private val logger: Logger = LogManager.getLogger(CircuitBreaker::class.java)

    init {
        logger.info("CircuitBreaker initialized")
    }

    fun allowRequest(): Boolean{
        return when(state.get()){
            State.CLOSED -> true
            State.OPEN -> {
                val currentTime = System.currentTimeMillis()
                if(currentTime - lastFailureTime.get() > timeout){
                    logger.warn("Client is half-open.")
                    state.set(State.HALF_OPEN)
                    true
                }else{
                    logger.error("Failures occurred. Please wait ${timeout.toInt() / 1000} sec.")
                    false
                }
            }
            State.HALF_OPEN -> true
        }
    }

    fun recordSuccess(){
        failureCount.set(0)
        state.set(State.CLOSED)
    }

    fun recordFailure(){
        val newCount = failureCount.incrementAndGet()
        lastFailureTime.set(System.currentTimeMillis())

        logger.warn("Operation failed. Failure count: $newCount/$failuresHold")

        println("$failureCount : $lastFailureTime")

        if (newCount >= failuresHold) {
            state.set(State.OPEN)
            logger.error("Circuit breaker tripped to OPEN state")
        }
    }
}
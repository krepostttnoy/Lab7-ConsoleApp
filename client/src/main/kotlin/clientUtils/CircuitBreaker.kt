package org.example.clientUtils

class CircuitBreaker(
    private val failuresHold: Int = 3,
    private val timeout: Long = 20000
) {
    private var failureCount = 0
    private var lastFailureTime = 0L
    private var state: State = State.CLOSED

    fun allowRequest(): Boolean{
        return when(state){
            State.CLOSED -> true
            State.OPEN -> {
                if(System.currentTimeMillis() - lastFailureTime > timeout){
                    state = State.HALF_OPEN
                    true
                }else{false}
            }
            State.HALF_OPEN -> true
        }
    }

    fun getState(): State{
        return state
    }

    fun recordSuccess(){
        failureCount = 0
        state = State.CLOSED
    }

    fun recordFailure(){
        failureCount++
        lastFailureTime = System.currentTimeMillis()
        if(failureCount >= failuresHold){
            state = State.OPEN
        }
    }
}
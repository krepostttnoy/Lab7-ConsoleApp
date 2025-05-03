package baseClasses

object ExitFlag {
    @Volatile
    private var workStatus: Boolean = false

    var exitFlag: Boolean
        get() {
            return workStatus
        }
        set(value) {
            workStatus = value
        }
}
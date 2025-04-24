package baseClasses

object ExitFlag {
    private var workStatus: Boolean = false

    var exitFlag: Boolean
        get() {
            return workStatus
        }
        set(value) {
            workStatus = value
        }
}
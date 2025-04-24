package utils

import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager

class IOThread {
    companion object{
        val outputManager = OutputManager()
        val inputManager = InputManager(outputManager)
    }
}
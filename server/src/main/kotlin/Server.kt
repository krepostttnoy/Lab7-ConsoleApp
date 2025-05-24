package org.example

import org.example.serverUtils.Console
import kotlin.concurrent.thread


fun main() {
    val console = Console()
    val port = 6789
    val host = "localhost"

    console.initialize()

    val consoleThread = thread{
        while(true){
            when(readlnOrNull()){
                "exit" -> {
                    console.save()
                    console.stop()
                    break
                }
                "save" -> {
                    console.save()
                }
            }
        }
    }

    console.startServer(host, port)
    console.startInteractiveMode()
    consoleThread.join()
}
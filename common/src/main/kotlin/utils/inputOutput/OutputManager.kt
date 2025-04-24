package utils.inputOutput

import java.io.IOException
import java.io.OutputStream

class OutputManager() {
    private var outputStream: OutputStream = System.out
    private var outputMode = OutputMode.ACTIVE

    private enum class OutputMode(){
        SILENT, ACTIVE
    }

    constructor(outputStream: OutputStream): this(){
        this.outputStream = outputStream
    }

    fun print(string: String){
        try{
            if (outputMode == OutputMode.ACTIVE){
                outputStream.write(string.toByteArray())
            }
        }catch(e: IOException){
            println(e.message)
        }
    }

    fun println(string: String){
        try{
            if(outputMode == OutputMode.ACTIVE){
                outputStream.write(string.toByteArray())
                outputStream.write("\n".toByteArray())
            }
        } catch (e: IOException){
            println(e.message)
        }
    }

    fun surePrint(string: String){
        try{
            outputStream.write(string.toByteArray())
            outputStream.write("\n".toByteArray())
        }catch (e: IOException){
            println(e.message)
        }
    }

    fun disableOutput(){
        outputMode = OutputMode.SILENT
    }

    fun enableOutput(){
        outputMode = OutputMode.ACTIVE
    }
}
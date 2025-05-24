package org.example.commands

import baseClasses.Coordinates
import baseClasses.FuelType
import baseClasses.Vehicle
import clientUtils.ConsoleVehicleManager
import org.example.clientUtils.ConnectionManager
import org.example.clientUtils.readers.Reader
import org.example.clientUtils.readers.Validator
import utils.JsonCreator
import utils.inputOutput.InputManager
import utils.inputOutput.OutputManager
import utils.wrappers.RequestType
import utils.wrappers.RequestWrapper

class CommandReceiver(
    private val outputManager: OutputManager,
    private val inputManager: InputManager,
    private val commandInvoker: CommandInvoker,
    private val connectionManager: ConnectionManager
) {
    private val jsonCreator = JsonCreator()
    private val reader = Reader(outputManager, inputManager)
    private val validator = Validator(outputManager, inputManager)
    private val vm = ConsoleVehicleManager(reader)
    var buffer = mutableSetOf<RequestWrapper>()

    fun getHelp() {
        val commands = commandInvoker.getCommand()
        outputManager.surePrint("Доступные команды: ${commands.keys.joinToString(", ").lowercase()}")
    }

    fun unknownCommand(name: String, args: Map<String, String>, inputArgs: String?){
        val sending = mutableMapOf<String, String>()

        val argList = inputArgs?.trim()?.split("\\s+".toRegex()) ?: emptyList()

        if (name == "update_id") {
            val listRequest = RequestWrapper(RequestType.COMMAND_EXEC, "show", emptyMap(), sender = "")
            val listResponse = connectionManager.checkSendReceive(listRequest)
            outputManager.println(listResponse.message)

            outputManager.println("Введите ID элемента для обновления: ")
            val id = if (inputManager.isScriptMode()) {
                val idStr = inputManager.read().trim()
                idStr.toIntOrNull() ?: run {
                    outputManager.println("Неверный ID в скрипте. Операция отменена.")
                    return
                }
            } else {
                validator.readInt() ?: run {
                    outputManager.println("Неверный ID. Операция отменена.")
                    return
                }
            }
            sending["id"] = jsonCreator.objectToString(id)

            outputManager.print("Какое поле обновить? (name, coordinates, enginePower, capacity, distanceTravelled, fuelType): ")
            val field = if (inputManager.isScriptMode()) {
                inputManager.read().trim()
            } else {
                inputManager.read().trim()
            }
            if (field !in listOf("name", "coordinates", "coords", "enginePower", "ep", "capacity", "cap", "distanceTravelled", "dt", "fuelType", "ft")) {
                outputManager.println("Неверное поле. Операция отменена.")
                return
            }
            sending["field"] = field

            val value = when (field.lowercase()) {
                "name" -> {
                    if (inputManager.isScriptMode()) inputManager.read().trim() else reader.readName()
                }
                "coordinates", "coords" -> {
                    val x = reader.readCoordinateX()
                    val y = reader.readCoordinateY()
                    jsonCreator.objectToString(Coordinates(x, y))
                }
                "enginepower", "ep" -> {
                    val engPw = if (inputManager.isScriptMode()) inputManager.read().trim().toFloatOrNull() ?: 0f else reader.readEnginePower()
                    jsonCreator.objectToString(engPw)
                }
                "capacity", "cap" -> {
                    val cap = if (inputManager.isScriptMode()) inputManager.read().trim().toFloatOrNull() ?: 0f else reader.readCapacity()
                    jsonCreator.objectToString(cap)
                }
                "distancetravelled", "dt" -> {
                    val dt = if (inputManager.isScriptMode()) inputManager.read().trim().toIntOrNull() ?: 0 else reader.readDistanceTravelled()
                    jsonCreator.objectToString(dt)
                }
                "fueltype", "ft" -> {
                    val ft = if (inputManager.isScriptMode()) FuelType.valueOf(inputManager.read().trim().uppercase()) else reader.readFuelType()
                    jsonCreator.objectToString(ft)
                }
                else -> ""
            }
            sending["value"] = value
        }else{
            for (arg in args.keys){
                sending[arg] = when (args[arg]) {
                    "EnginePower" -> {
                        val engPw = reader.readEnginePower()
                        jsonCreator.objectToString(engPw)
                    }
                    "Coordinates" -> {
                        val x = reader.readCoordinateX()
                        val y = reader.readCoordinateY()
                        jsonCreator.objectToString(Coordinates(x, y))
                    }
                    "FuelType" -> {
                        val fuelType = reader.readFuelType()
                        jsonCreator.objectToString(fuelType)
                    }
                    "Vehicle" -> {
                        val vehicle = vm.setVehicle()
                        jsonCreator.objectToString(vehicle)
                    }
                    "Float" -> {
                        argList.getOrNull(args.keys.indexOf(arg)) ?: run {
                            outputManager.print("Enter a float number: ")
                            val result = validator.readFloat()
                            jsonCreator.objectToString(result)
                        }
                    }
                    "Int" -> {
                        argList.getOrNull(args.keys.indexOf(arg)) ?: run {
                            val result = validator.readInt()
                            jsonCreator.objectToString(result)
                        }
                    }
                    else -> ""
                }
            }
        }

        val request = RequestWrapper(RequestType.COMMAND_EXEC, name, sending, sender = "")
        buffer.add(request)
        val response = connectionManager.checkSendReceive(request)
        outputManager.println(response.message)
    }

}
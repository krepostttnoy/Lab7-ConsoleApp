package org.example.serverUtils
/**
 * Интерфейс для управления чтением и записью данных в файл.
 * Определяет контракт для классов, которые должны предоставлять методы для загрузки, сохранения
 * и получения пути к файлу.
 */
interface IFileManager {
    fun loadFromFile(filePath: String)
    fun saveToFile()
    fun getFilePath(): String
}
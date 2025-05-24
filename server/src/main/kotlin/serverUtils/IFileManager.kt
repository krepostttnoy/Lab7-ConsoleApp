package org.example.serverUtils

import org.example.users.UserManager

/**
 * Интерфейс для управления чтением и записью данных в файл.
 * Определяет контракт для классов, которые должны предоставлять методы для загрузки, сохранения
 * и получения пути к файлу.
 */
interface IFileManager {
    fun loadCollection()
    fun saveCollection(userManager: UserManager)
}
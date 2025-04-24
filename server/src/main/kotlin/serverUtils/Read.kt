package org.example.serverUtils

import kotlinx.serialization.Serializable

/**
 * Интерфейс для чтения данных из консоли или другого источника.
 * Определяет контракт для классов, которые должны предоставлять методы для чтения различных типов данных.
 */
@Serializable
 sealed interface Read {
     fun readInt(): Int?
     fun readLong(): Long?
     fun readFloat(): Float?
     fun readLineTrimmed(): String?
}
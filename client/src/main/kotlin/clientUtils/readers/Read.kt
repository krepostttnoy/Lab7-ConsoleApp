package org.example.clientUtils.readers

/**
 * Интерфейс для чтения данных из консоли или другого источника.
 * Определяет контракт для классов, которые должны предоставлять методы для чтения различных типов данных.
 */
 interface Read {
     fun readInt(): Int?
     fun readLong(): Long?
     fun readFloat(): Float?
     fun readLineTrimmed(): String?
}
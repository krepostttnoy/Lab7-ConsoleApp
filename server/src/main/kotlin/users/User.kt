package org.example.users

import java.sql.Timestamp

data class User(private var name: String, private var password: String) {
    private var token = ""
    private var accessTime = Timestamp(System.currentTimeMillis())

    fun getName(): String{
        return this.name
    }

    fun setName(name: String){
        this.name = name
    }

    fun setPassword(password: String){
        this.password = password
    }

    fun getPassword(): String{
        return this.password
    }

    fun getToken() : String {
        return token
    }

    fun setToken(token: String) {
        this.token = token
    }

    fun getAccessTime() : Timestamp {
        return accessTime
    }

    fun setAccessTime(accessTime: Timestamp) {
        this.accessTime = accessTime
    }
}
package com.example.projekt_aplikacje.database


data class User(val name : String, val favourites : List<String>) {
    companion object {
        var name = "TestUser"
    }
}
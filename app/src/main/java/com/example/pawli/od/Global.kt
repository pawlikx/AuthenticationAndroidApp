package com.example.pawli.od

import com.example.pawli.od.MongoDB.Classes.User

class Global {
    companion object {

        var usersList: ArrayList<User> = ArrayList()

        var adding: Boolean = false
        //LOGOWANIE
        var isLogged: Boolean = false
        var userName: String = ""
        var user: User = User()
        var firstLogin: Boolean = true
        var userPIN: String = "0000"
    }

}
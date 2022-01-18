package com.example.bookapplicationv1.classes

class User {
    var fullName: String? = null
    var userEmail: String? = null
    var userType: String? = null
    var userTimestamp: Long? = null
    var userVerified: Boolean? = null
    var userUID : String?= null
    var userProfilePic: String?= null

    constructor(){}

    constructor(fullName: String?, userEmail: String?, userType: String?, userTimestamp: Long?, userVerified: Boolean?, userUID: String?, userProfilePic: String?){
        this.fullName = fullName
        this.userEmail = userEmail
        this.userType = userType
        this.userTimestamp = userTimestamp
        this.userVerified = userVerified
        this.userUID = userUID
        this.userProfilePic = userProfilePic
    }
}

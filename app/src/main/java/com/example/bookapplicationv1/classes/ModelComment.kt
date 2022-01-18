package com.example.bookapplicationv1.classes

class ModelComment {
    var id = ""
    var bookId = ""
    var timestamp = ""
    var comment = ""
    var uid = ""
    var rating = ""

    constructor()

    constructor(id: String, bookId: String, timestamp: String, comment: String, uid: String, rating: String) {
        this.id = id
        this.bookId = bookId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
        this.rating = rating
    }


}
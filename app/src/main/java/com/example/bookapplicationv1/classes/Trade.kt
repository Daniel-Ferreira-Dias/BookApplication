package com.example.bookapplicationv1.classes

class Trade {

    var id = ""
    var currentUserselectedBookId = ""
    var otherUserselectedBookId = ""
    var status = 0
    var tradeOwner = ""
    var tradeReciever = ""

    constructor()

    constructor(id: String, currentUserselectedBookId: String, otherUserselectedBookId: String, status: Int, tradeOwner: String, tradeReciever: String) {
        this.id = id
        this.currentUserselectedBookId = currentUserselectedBookId
        this.otherUserselectedBookId = otherUserselectedBookId
        this.status = status
        this.tradeOwner = tradeOwner
        this.tradeReciever = tradeReciever
    }
}
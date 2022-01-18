package com.example.bookapplicationv1.classes

class ModelPDF {
    var uid:String = ""
    var titulo:String = ""
    var descricao:String = ""
    var categoria:String = ""
    var livroEncriptacao:String = ""
    var url:String = ""
    var timestamp:Long = 0
    var id:String = ""
    var totalreviews: Int = 0
    var atualreviews: Int = 0
    var autor: String = ""
    var isFavorite: Boolean = false


    constructor()

    constructor(
        uid: String,
        titulo: String,
        descricao: String,
        categoria: String,
        livroEncriptacao: String,
        url: String,
        timestamp: Long,
        id: String,
        totalreviews: Int,
        atualreviews: Int,
        autor: String,
        isFavorite: Boolean,
    ) {
        this.uid = uid
        this.titulo = titulo
        this.descricao = descricao
        this.categoria = categoria
        this.livroEncriptacao = livroEncriptacao
        this.url = url
        this.timestamp = timestamp
        this.id = id
        this.totalreviews = totalreviews
        this.atualreviews = atualreviews
        this.autor = autor
        this.isFavorite = isFavorite
    }


}
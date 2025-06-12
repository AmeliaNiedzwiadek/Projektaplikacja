 package com.example.projekt.ui.model

data class Pomiar(
    var id: String? = null,
    var userId: String? = null,
    var data: String? = null,
    var cisnienieSkurczowe: Int = 0,
    var cisnienieRozkurczowe: Int = 0,
    var puls: Int = 0
)

package com.example.projekt.ui.model

data class Pomiar(
    val id: String = "",
    val data: String = "",
    val godzina: String = "",
    val cisnienieSkurczowe: Int = 0,
    val cisnienieRozkurczowe: Int = 0,
    val puls: Int = 0,
    val userId: String = ""  // <- do filtrowania pomiarów po użytkowniku
)


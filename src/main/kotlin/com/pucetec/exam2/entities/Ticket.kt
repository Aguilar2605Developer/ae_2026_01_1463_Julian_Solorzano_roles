package com.pucetec.exam2.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tickets")
class Ticket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val placa: String,

    @Column(nullable = false)
    val fechaEntrada: LocalDateTime = LocalDateTime.now(),

    var fechaSalida: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_id", nullable = false)
    val espacio: Espacio
)
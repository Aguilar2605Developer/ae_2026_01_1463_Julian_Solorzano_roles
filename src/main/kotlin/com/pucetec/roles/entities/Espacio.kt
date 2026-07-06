package com.pucetec.roles.entities

import jakarta.persistence.*

@Entity
@Table(name = "espacios")
class Espacio(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val codigo: String,

    @Column(nullable = false)
    var disponible: Boolean = true
)
package br.pucpr.cinelog.directors

import jakarta.persistence.*

@Entity
@Table(name = "DirectorTable")
class Director(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(nullable = false)
    var nationality: String,

    @Column(nullable = false)
    var birthYear: Int,

    @Column(nullable = false, length = 1000)
    var biography: String = ""
)

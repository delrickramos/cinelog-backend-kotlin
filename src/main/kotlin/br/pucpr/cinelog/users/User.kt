package br.pucpr.cinelog.users

import br.pucpr.cinelog.roles.Role
import jakarta.persistence.*
import jakarta.validation.constraints.Email

@Entity
@Table(name = "UserTable")
class User(
    @Id @GeneratedValue
    var id: Long? = null,

    @Email
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false, length = 72)
    var password: String,

    @Column(nullable = false)
    var name: String,

    @ManyToMany
    @JoinTable(
        name = "UserRole",
        joinColumns = [JoinColumn(name = "idUser")],
        inverseJoinColumns = [JoinColumn(name = "idRole")],
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "idProfile", referencedColumnName = "id")
    var profile: UserProfile? = null
)

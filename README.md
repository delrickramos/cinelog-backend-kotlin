# CineLog API

API RESTful desenvolvida em **Kotlin** com **Spring Boot** para gerenciamento de um catálogo de filmes, diretores, gêneros e avaliações de usuários. Projeto desenvolvido para a disciplina de **Backend: Web Services** (Sistemas de Informação - PUCPR).

## Equipe

- **Delrick Ramos**
- **Clara Amazonas**
- **Geovanna Soto**

---

## Tecnologias Utilizadas

- **Kotlin 2.2** & **Java 21**
- **Spring Boot 4** (Spring Web MVC, Spring Data JPA, Jakarta Validation)
- **H2 Database** (banco em memória)
- **SpringDoc OpenAPI (Swagger UI)** para documentação interativa
- **JUnit 5**, **Kotest** e **MockK** para testes unitários

---

## Funcionalidades Implementadas

### Modelagem de Dados e Relacionamentos
- **`User` e `Role`**: Usuários do sistema e perfis de acesso (`@ManyToMany`).
- **`Movie`**: Catálogo de filmes com título, sinopse, ano de lançamento, duração e pôster.
- **`Director`**: Diretores associados aos filmes (`@ManyToOne` / `@OneToMany`).
- **`Genre`**: Gêneros cinematográficos associados aos filmes (`@ManyToMany`).
- **`Review`**: Entidade associativa (`@ManyToOne` para `User` e `Movie`) contendo nota (`rating`), comentário e data de criação.

### Endpoints Disponíveis

#### Usuários e Roles
- `GET /api/users?role={role}&sortDir={ASC|DESC}` — Listagem de usuários com ordenação ou filtro por role
- `GET /api/users/{id}` — Busca de usuário por ID
- `POST /api/users` — Cadastro de usuário
- `PATCH /api/users/{id}` — Atualização de nome do usuário
- `DELETE /api/users/{id}` — Exclusão de usuário
- `PUT /api/users/{id}/roles/{name}` — Atribuição de role ao usuário
- `GET /api/roles` e `POST /api/roles` — Listagem e criação de roles

#### Filmes (`Movie`)
- `GET /api/movies?genre={genre}&sortDir={ASC|DESC}` — Listagem de filmes com ordenação ou filtro por gênero
- `GET /api/movies/{id}` — Busca de filme por ID
- `POST /api/movies` — Cadastro de novo filme
- `PATCH /api/movies/{id}` — Atualização de título do filme
- `DELETE /api/movies/{id}` — Exclusão de filme
- `PUT /api/movies/{id}/genres/{genreName}` — Associação de gênero a um filme

#### Diretores (`Director`) e Gêneros (`Genre`)
- `GET /api/directors`, `GET /api/directors/{id}` e `POST /api/directors`
- `GET /api/genres` e `POST /api/genres`

---

## O que ainda falta implementar

- [ ] **Operações restantes de `Director` e `Genre`:** Endpoints de atualização e remoção (`PUT`/`PATCH` e `DELETE`) para diretores e gêneros, além de desassociação de gênero nos filmes.
- [ ] **Camada de serviço e rotas de `Review`:** A entidade e o repositório já estão modelados, faltando expor os endpoints no controller e regras de negócio no service para criação, listagem e exclusão de avaliações de filmes.
- [ ] **Autenticação e Autorização (Spring Security + JWT):** Implementar login de usuário, geração/validação de token JWT e proteção das rotas que exigem permissão de administrador.
- [ ] **Testes Unitários:** Expandir os testes unitários para cobrir os demais services da aplicação (`DirectorService`, `GenreService`, `ReviewService`).

---

## Como Executar

```bash
./gradlew bootRun
```

- **Swagger UI:** `http://localhost:8080/api/swagger-ui/index.html` (ou `http://localhost:8080/swagger-ui/index.html`)
- **Health Check:** `http://localhost:8080/api/health`
- **H2 Console:** `http://localhost:8080/api/h2-console` (`jdbc:h2:mem:cinelogdb`, usuário `sa`, senha `sa`)

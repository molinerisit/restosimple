package ar.ticketsimple.pos.infrastructure.db

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object DataSeeder {

    fun seedIfEmpty() {
        transaction {
            if (UserTable.selectAll().count() > 0L) return@transaction

            val ownerId = UUID.randomUUID().toString()
            UserTable.insert {
                it[id]   = ownerId
                it[name] = "Admin"
                it[pin]  = "1234"
                it[role] = "OWNER"
            }
            UserTable.insert {
                it[id]   = UUID.randomUUID().toString()
                it[name] = "Cajero"
                it[pin]  = "0000"
                it[role] = "CASHIER"
            }

            val cats = listOf(
                Triple(UUID.randomUUID().toString(), "Cafés",     "#5B3A2E"),
                Triple(UUID.randomUUID().toString(), "Panadería", "#C46A3A"),
                Triple(UUID.randomUUID().toString(), "Pasteles",  "#8A3D57"),
                Triple(UUID.randomUUID().toString(), "Bebidas",   "#2563EB"),
                Triple(UUID.randomUUID().toString(), "Snacks",    "#7F9B7A")
            )
            cats.forEachIndexed { i, (catId, catName, catColor) ->
                CategoryTable.insert {
                    it[id]        = catId
                    it[name]      = catName
                    it[color]     = catColor
                    it[sortOrder] = i
                }
            }

            val (cafesId, panaId, pastId, bebId, snackId) = cats.map { it.first }

            val products = listOf(
                listOf(cafesId,  "Café Espresso",   800.0,  false),
                listOf(cafesId,  "Café Cortado",    900.0,  false),
                listOf(cafesId,  "Café con Leche",  1000.0, false),
                listOf(cafesId,  "Cappuccino",      1200.0, false),
                listOf(cafesId,  "Latte",           1300.0, false),
                listOf(panaId,   "Medialuna",       500.0,  false),
                listOf(panaId,   "Croissant",       600.0,  false),
                listOf(panaId,   "Tostado Simple",  1200.0, false),
                listOf(panaId,   "Facturas x4",     1800.0, false),
                listOf(panaId,   "Pan Artesanal",   0.0,    true),
                listOf(pastId,   "Alfajor",         800.0,  false),
                listOf(pastId,   "Torta de Chocolate (porción)", 1500.0, false),
                listOf(pastId,   "Cheesecake",      1600.0, false),
                listOf(pastId,   "Brownie",         900.0,  false),
                listOf(bebId,    "Agua Mineral",    600.0,  false),
                listOf(bebId,    "Jugo de Naranja", 1100.0, false),
                listOf(bebId,    "Gaseosa",         800.0,  false),
                listOf(bebId,    "Jugo Natural",    0.0,    true),
                listOf(snackId,  "Chips",           700.0,  false),
                listOf(snackId,  "Mix de Frutas Secas", 0.0, true)
            )

            products.forEachIndexed { i, (catId, prodName, price, byW) ->
                ProductTable.insert {
                    it[id]         = UUID.randomUUID().toString()
                    it[name]       = prodName as String
                    it[categoryId] = catId as String
                    it[ProductTable.price]    = price as Double
                    it[byWeight]   = byW as Boolean
                    it[sortOrder]  = i
                }
            }
        }
    }
}

private operator fun <E> List<E>.component6(): E = this[5]

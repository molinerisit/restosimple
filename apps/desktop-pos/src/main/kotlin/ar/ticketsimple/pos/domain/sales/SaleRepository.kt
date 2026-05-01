package ar.ticketsimple.pos.domain.sales

import java.time.LocalDate

interface SaleRepository {
    fun save(sale: Sale): Sale
    fun findById(id: String): Sale?
    fun salesByShift(shiftId: String): List<Sale>
    fun salesByDate(date: LocalDate): List<Sale>
    fun todaySales(): List<Sale>
}

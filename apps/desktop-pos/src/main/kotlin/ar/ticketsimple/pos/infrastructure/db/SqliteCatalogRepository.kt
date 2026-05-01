package ar.ticketsimple.pos.infrastructure.db

import ar.ticketsimple.pos.domain.catalog.Category
import ar.ticketsimple.pos.domain.catalog.CatalogRepository
import ar.ticketsimple.pos.domain.catalog.Product
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class SqliteCatalogRepository : CatalogRepository {

    override fun allCategories(): List<Category> = transaction {
        CategoryTable
            .selectAll()
            .where { CategoryTable.active eq true }
            .orderBy(CategoryTable.sortOrder)
            .map { it.toCategory() }
    }

    override fun allProducts(): List<Product> = transaction {
        ProductTable
            .selectAll()
            .orderBy(ProductTable.sortOrder)
            .map { it.toProduct() }
    }

    override fun productsByCategory(categoryId: String): List<Product> = transaction {
        ProductTable
            .selectAll()
            .where { ProductTable.categoryId eq categoryId }
            .orderBy(ProductTable.sortOrder)
            .map { it.toProduct() }
    }

    override fun searchProducts(query: String): List<Product> = transaction {
        val q = "%${query.lowercase()}%"
        ProductTable
            .selectAll()
            .where { ProductTable.name.lowerCase() like q }
            .orderBy(ProductTable.sortOrder)
            .map { it.toProduct() }
    }

    override fun findProduct(id: String): Product? = transaction {
        ProductTable
            .selectAll()
            .where { ProductTable.id eq id }
            .singleOrNull()?.toProduct()
    }

    override fun saveCategory(category: Category) {
        transaction {
            val exists = CategoryTable.selectAll().where { CategoryTable.id eq category.id }.count() > 0
            if (exists) {
                CategoryTable.update({ CategoryTable.id eq category.id }) {
                    it[name]      = category.name
                    it[color]     = category.color
                    it[sortOrder] = category.sortOrder
                    it[active]    = category.active
                }
            } else {
                CategoryTable.insert {
                    it[id]        = category.id
                    it[name]      = category.name
                    it[color]     = category.color
                    it[sortOrder] = category.sortOrder
                    it[active]    = category.active
                }
            }
        }
    }

    override fun saveProduct(product: Product) {
        transaction {
            val exists = ProductTable.selectAll().where { ProductTable.id eq product.id }.count() > 0
            if (exists) {
                ProductTable.update({ ProductTable.id eq product.id }) {
                    it[name]       = product.name
                    it[sku]        = product.sku
                    it[categoryId] = product.categoryId
                    it[price]      = product.price
                    it[available]  = product.available
                    it[byWeight]   = product.byWeight
                    it[imageUrl]   = product.imageUrl
                    it[sortOrder]  = product.sortOrder
                }
            } else {
                ProductTable.insert {
                    it[id]         = product.id
                    it[name]       = product.name
                    it[sku]        = product.sku
                    it[categoryId] = product.categoryId
                    it[price]      = product.price
                    it[available]  = product.available
                    it[byWeight]   = product.byWeight
                    it[imageUrl]   = product.imageUrl
                    it[sortOrder]  = product.sortOrder
                }
            }
        }
    }

    override fun setProductAvailability(productId: String, available: Boolean) {
        transaction {
            ProductTable.update({ ProductTable.id eq productId }) {
                it[ProductTable.available] = available
            }
        }
    }

    private fun ResultRow.toCategory() = Category(
        id        = this[CategoryTable.id],
        name      = this[CategoryTable.name],
        color     = this[CategoryTable.color],
        sortOrder = this[CategoryTable.sortOrder],
        active    = this[CategoryTable.active]
    )

    private fun ResultRow.toProduct() = Product(
        id         = this[ProductTable.id],
        sku        = this[ProductTable.sku],
        name       = this[ProductTable.name],
        categoryId = this[ProductTable.categoryId],
        price      = this[ProductTable.price],
        available  = this[ProductTable.available],
        byWeight   = this[ProductTable.byWeight],
        imageUrl   = this[ProductTable.imageUrl],
        sortOrder  = this[ProductTable.sortOrder]
    )
}

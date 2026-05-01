package ar.ticketsimple.pos.application.catalog

import ar.ticketsimple.pos.domain.catalog.Category
import ar.ticketsimple.pos.domain.catalog.CatalogRepository
import ar.ticketsimple.pos.domain.catalog.Product

class CatalogService(private val repo: CatalogRepository) {

    fun categories(): List<Category> = repo.allCategories()

    fun products(): List<Product> = repo.allProducts()

    fun productsByCategory(categoryId: String): List<Product> =
        repo.productsByCategory(categoryId)

    fun search(query: String): List<Product> =
        if (query.isBlank()) repo.allProducts() else repo.searchProducts(query)

    fun setAvailability(productId: String, available: Boolean) =
        repo.setProductAvailability(productId, available)

    fun saveProduct(product: Product) = repo.saveProduct(product)

    fun saveCategory(category: Category) = repo.saveCategory(category)
}

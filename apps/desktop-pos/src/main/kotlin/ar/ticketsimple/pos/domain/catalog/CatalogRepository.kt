package ar.ticketsimple.pos.domain.catalog

interface CatalogRepository {
    fun allCategories(): List<Category>
    fun allProducts(): List<Product>
    fun productsByCategory(categoryId: String): List<Product>
    fun searchProducts(query: String): List<Product>
    fun findProduct(id: String): Product?
    fun saveCategory(category: Category)
    fun saveProduct(product: Product)
    fun setProductAvailability(productId: String, available: Boolean)
}

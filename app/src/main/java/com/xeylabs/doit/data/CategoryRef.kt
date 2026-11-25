package com.xeylabs.doit.data

data class CategoryRef(
    val key: String,
    val label: String,
    val type: TxnType,
    val iconKey: String,
    val colorHex: Long
)

fun customCategoryKey(id: Long): String = "CUSTOM_$id"

fun customCategoryId(key: String): Long? =
    if (key.startsWith("CUSTOM_")) key.removePrefix("CUSTOM_").toLongOrNull() else null

fun builtinColorHex(category: Category): Long = when (category) {
    Category.BELANJA -> 0xFF10B981L
    Category.MAKAN -> 0xFF34D399L
    Category.TRANSPORT -> 0xFF6EE7B7L
    Category.DARURAT -> 0xFFFBBF24L
    Category.LAIN_KELUAR -> 0xFF86948AL
    Category.TRANSFER -> 0xFF86948AL
    else -> 0xFF4EDEA3L
}

fun builtinCategoryRef(category: Category): CategoryRef =
    CategoryRef(category.name, category.label, category.type, category.name, builtinColorHex(category))

fun resolveCategoryRef(key: String, customs: List<CustomCategory>): CategoryRef {
    val customId = customCategoryId(key)
    if (customId != null) {
        val custom = customs.firstOrNull { it.id == customId }
        if (custom != null) {
            return CategoryRef(
                key = key,
                label = custom.name,
                type = runCatching { TxnType.valueOf(custom.type) }.getOrDefault(TxnType.EXPENSE),
                iconKey = custom.iconKey,
                colorHex = custom.colorHex
            )
        }
    }
    val builtin = Category.entries.firstOrNull { it.name == key }
    if (builtin != null) return builtinCategoryRef(builtin)
    return CategoryRef(key, "Lain-lain", TxnType.EXPENSE, Category.LAIN_KELUAR.name, builtinColorHex(Category.LAIN_KELUAR))
}

fun categoryRefsForType(type: TxnType, customs: List<CustomCategory>): List<CategoryRef> {
    val builtins = Category.forType(type).map { builtinCategoryRef(it) }
    val customRefs = customs
        .filter { it.type == type.name }
        .map { resolveCategoryRef(customCategoryKey(it.id), customs) }
    return builtins + customRefs
}

fun fallbackCategoryKey(type: TxnType): String =
    if (type == TxnType.INCOME) Category.LAIN_MASUK.name else Category.LAIN_KELUAR.name

val CUSTOM_CATEGORY_ICON_KEYS = listOf(
    "shopping", "restaurant", "car", "health", "work",
    "schedule", "payments", "bank", "phone", "category"
)

val CUSTOM_CATEGORY_COLOR_HEXES = listOf(
    0xFF4EDEA3L, 0xFF38BDF8L, 0xFFA78BFAL, 0xFFF472B6L,
    0xFFFB923CL, 0xFFFBBF24L, 0xFF34D399L, 0xFFF43F5EL
)

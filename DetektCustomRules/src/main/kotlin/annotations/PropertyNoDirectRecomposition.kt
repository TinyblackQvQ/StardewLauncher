package org.miluko.annotations

/**
 * Indicates that directly modifying this property will NOT trigger a Compose recomposition.
 * To trigger recomposition, you must update the parent State holding this object,
 * or ensure the property itself is wrapped in a Compose observable state like `MutableState`.
 *
 * 指定此属性的修改不会触发Compose重新组合。要触发重新组合，您必须更新包含此对象的父状态，
 */
@Target(AnnotationTarget.PROPERTY) // 仅能用于属性
annotation class PropertyNoDirectRecomposition
package org.miluko.rules

import io.gitlab.arturbosch.detekt.api.*
import io.gitlab.arturbosch.detekt.rules.hasAnnotation
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPsiUtil
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import org.miluko.annotations.PropertyNoDirectRecomposition

/**
 * Detekt rule to warn when a property annotated with @NoDirectRecomposition is directly modified.
 */
class NoDirectRecompositionRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "NoDirectRecompositionUsage", // Unique ID for your issue
        severity = Severity.Warning,      // You can change this to Severity.Error
        description = "Modifying a @NoDirectRecomposition property will not trigger a Compose recomposition. " +
                "Consider wrapping it in `MutableState<T>` or updating the parent object.",
        debt = Debt.FIVE_MINS
    )

    // This is the core logic. We need to find properties with the annotation,
    // and then check for direct assignments or modifications to them.
    // This requires traversing the AST.

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        // Check if the property declaration itself has the annotation
        if (property.hasAnnotation(PropertyNoDirectRecomposition::class.java.canonicalName)) {
            // This rule is about *usage* of the property, not its declaration.
            // We'll report if we find a direct modification of it later.
            // For now, just remember we've seen this annotated property.
            // A more complex rule would store a reference and then check all usages.
            // For simplicity, let's assume we want to warn directly on its declaration if it's mutable.
            // However, the intent is to warn on *modification sites*.

            // Let's refine: We want to find *assignments* to a property that is annotated.
            // This is complex as it requires global analysis or a different visitor.
            // The most practical approach for a beginner custom rule is to check for
            // common mutation patterns for properties.

            // A more direct approach: check if it's a 'var' and then try to find usage.
            // For this specific rule, checking assignments (KtBinaryExpression for assignment ops)
            // where the left-hand side is a reference to our annotated property is key.
            // However, Detekt rules are visit-based; they visit nodes.
            // Finding *all* assignments to a property from its declaration context is hard.
            // A common pattern is to visit *binary expressions* (like assignments)
            // and then check if the assigned variable has the annotation.
        }
    }

    // This method visits binary expressions (like `a = b`, `a += b`, etc.)
    override fun visitBinaryExpression(expression: KtBinaryExpression) {
        super.visitBinaryExpression(expression)

        // Check if it's an assignment operation
        if (KtPsiUtil.isAssignment(expression)) {
            val leftHandSide = expression.left ?: return // The property being assigned to

            // Try to resolve the property being assigned to.
            // This often requires Detekt's TypeResolution facility,
            // which needs a BindingContext. For simpler cases,
            // we can check if it's a simple property reference.

            // Let's assume it's a simple property reference for now
            // If it's a complex qualified expression (e.g., `obj.prop.nestedProp = ...`)
            // you'll need to dig deeper.

            // Check if the property being assigned has our annotation
            // This requires access to the PSI element of the property declaration.
            // Detekt rules are based on traversing the PSI tree.

            // A simplified check: if the LHS is a qualified expression like `obj.myProp`
            // and `myProp` (if it's the target) has the annotation.
            val resolvedProperty = when (leftHandSide) {
                is KtDotQualifiedExpression -> leftHandSide.selectorExpression
                else -> leftHandSide
            }?.let {
                // Find the declaration of the property being assigned to
                // This is a simplified lookup, real-world might need symbol resolution
                it.findDescendantOfType<KtProperty> { prop -> prop.name == it.text }
            }

            if (resolvedProperty?.hasAnnotation(PropertyNoDirectRecomposition::class.java.canonicalName) == true) {
                // Report the issue at the location of the assignment
                report(CodeSmell(
                    issue,
                    Entity.from(expression), // Report on the assignment expression itself
                    "Direct modification of property '${resolvedProperty.name}' annotated with @NoDirectRecomposition will not trigger recomposition. " +
                            "Consider alternatives like `MutableState<T>` or parent object replacement."
                ))
            }
        }
    }

    // You might also want to check for call expressions that modify internal state
    // like `myList.add()` if `myList` is annotated with @NoDirectRecomposition
    // This gets more complex as it requires data flow analysis or stricter rules.
    // For now, let's focus on direct assignments.
}
package org.miluko.providers

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider
import org.miluko.rules.NoDirectRecompositionRule

class NoDirectRecompositionProvider : RuleSetProvider {
    override val ruleSetId: String = "NoRecompositionRules" // A unique ID for your rule set

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                NoDirectRecompositionRule(config) // Add your rule here
            )
        )
    }
}
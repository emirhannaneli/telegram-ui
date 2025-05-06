package dev.emirman.lib.telegram.model

import java.util.*

class StepByStep {
    companion object {
        fun builder(): StepByStepBuilder {
            return StepByStepBuilder()
        }
    }

    class StepByStepBuilder {
        private val steps: MutableList<Step> = mutableListOf()

        fun addStep(step: Step): StepByStepBuilder {
            steps.add(step)
            return this
        }

        fun addStep(
            name: String,
            action: (Long) -> Unit?,
            data: String? = null,
            nextStep: Step? = null,
            prevStep: Step? = null,
        ): StepByStepBuilder {
            val step = Step(name, action, data, nextStep, prevStep, steps.size + 1)
            steps.add(step)
            return this
        }

        fun build(): TreeSet<Step> {
            val sortedSteps = TreeSet<Step>(compareBy { it.index })
            steps.forEach { step ->
                step.next?.let { nextStep ->
                    step.next = sortedSteps.find { it.name == nextStep.name }
                }
                sortedSteps.add(step)
            }
            return sortedSteps
        }
    }
}
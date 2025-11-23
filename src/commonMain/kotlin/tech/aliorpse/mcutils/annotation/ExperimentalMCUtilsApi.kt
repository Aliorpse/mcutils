package tech.aliorpse.mcutils.annotation

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
)
public annotation class ExperimentalMCUtilsApi

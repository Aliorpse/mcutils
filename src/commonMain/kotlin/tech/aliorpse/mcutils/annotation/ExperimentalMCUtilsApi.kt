package tech.aliorpse.mcutils.annotation

@RequiresOptIn(
    "This API is unstable and may change at any time, use it with caution in production.",
    RequiresOptIn.Level.WARNING
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
@MustBeDocumented
public annotation class ExperimentalMCUtilsApi

package tech.aliorpse.mcutils.annotation

@RequiresOptIn(
    "This API is for internal use and may change at any time, without a major version bump.",
    RequiresOptIn.Level.WARNING
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
public annotation class InternalMCUtilsApi

package tech.aliorpse.mcutils.annotations

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "Internal API, might be change, or delete in the future."
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
public annotation class InternalMcUtilsApi

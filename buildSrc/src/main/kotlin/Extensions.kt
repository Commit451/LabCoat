import org.gradle.api.Project

fun Project.propertyOrEmpty(propertyName: String): String {
    return if (this.hasProperty(propertyName)) this.property(propertyName) as String else ""
}

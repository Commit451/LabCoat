import org.gradle.api.Project

object ProjectStuff {
    fun fabricKey(project: Project): String {
        return project.propertyOrEmpty("LABCOAT_FABRIC_KEY")
    }
}

import org.gradle.api.Project

object BuildHelper {

    private var locatedFile: Boolean? = null

    fun firebaseEnabled(project: Project): Boolean {
        return fileExists(project)
    }

    private fun fileExists(project: Project): Boolean {
        val located = locatedFile
        return if (located != null) {
            located
        } else {
            val fileExists = project.file("${project.rootDir}/app/google-services.json").exists()
            locatedFile = fileExists
            printFirebase()
            fileExists
        }
    }

    private fun printFirebase() {
        println(
            """

 / _(_)         | |
 | |_ _ _ __ ___| |__   __ _ ___  ___
 |  _| | '__/ _ \ '_ \ / _` / __|/ _ \
 | | | | | |  __/ |_) | (_| \__ \  __/
 |_| |_|_|  \___|_.__/ \__,_|___/\___|
 enabled

            """.trimIndent()
        )
    }
}

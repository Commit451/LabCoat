import org.gradle.api.Project
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
object BuildHelper {

    private var locatedFile: Boolean? = null

    fun appVersionName(): String {
        return "2.7.2"
    }

    fun appVersionCode(): Int {
        val parts = appVersionName().split(".")
        val versionMajor = parts[0].toInt()
        val versionMinor = parts[1].toInt()
        val versionPatch = parts[2].toInt()
        // this is something I got from u2020 a while back... meh
        return versionMajor * 1000000 + versionMinor * 10000 + versionPatch * 100
    }

    fun firebaseEnabled(project: Project): Boolean {
        return fileExists(project)
    }

    fun keystoreFile(project: Project): File {
        return project.file("${project.rootDir}/app/${project.propertyOrEmpty("KEYSTORE_NAME")}")
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

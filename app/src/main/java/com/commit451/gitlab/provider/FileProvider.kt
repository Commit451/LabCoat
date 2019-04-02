package com.commit451.gitlab.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.Build
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import com.commit451.gitlab.R
import com.commit451.gitlab.activity.FileActivity
import com.commit451.gitlab.data.Prefs
import com.commit451.gitlab.extension.base64Decode
import com.commit451.gitlab.util.FileUtil
import java.io.File
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets
import android.os.AsyncTask
import com.commit451.gitlab.App
import com.commit451.gitlab.BuildConfig
import com.commit451.gitlab.api.GitLab
import com.commit451.gitlab.api.GitLabFactory
import com.commit451.gitlab.api.OkHttpClientFactory
import com.commit451.gitlab.model.Account
import com.commit451.gitlab.model.api.*
import okhttp3.Headers
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.net.URLEncoder

private const val ROOT = "root"

private val gitlabCache = mutableMapOf<String, GitLab>()
private val commitCache = mutableMapOf<FileProvider.CommitCacheKey, RepositoryCommit>()
private val fileMetaCache = mutableMapOf<FileProvider.FileCacheKey, RepositoryFile>()
private val projectsCache = mutableMapOf<FileProvider.ProjectsCacheKey, List<Project>>()
private val branchCache = mutableMapOf<FileProvider.RevisionCacheKey, List<Branch>>()
private val tagCache = mutableMapOf<FileProvider.RevisionCacheKey, List<Tag>>()
private val fileChildrenCache = mutableMapOf<FileProvider.FileCacheKey, List<RepositoryTreeObject>>()

@RequiresApi(Build.VERSION_CODES.KITKAT)
class FileProvider : DocumentsProvider() {

    private val DEFAULT_ROOT_PROJECTION = arrayOf(DocumentsContract.Root.COLUMN_ROOT_ID, DocumentsContract.Root.COLUMN_MIME_TYPES, DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.COLUMN_ICON, DocumentsContract.Root.COLUMN_TITLE, DocumentsContract.Root.COLUMN_SUMMARY, DocumentsContract.Root.COLUMN_DOCUMENT_ID, DocumentsContract.Root.COLUMN_AVAILABLE_BYTES)
    private val DEFAULT_DOCUMENT_PROJECTION = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_FLAGS, DocumentsContract.Document.COLUMN_SIZE)

    override fun onCreate(): Boolean {
        Prefs.init(context!!)
        return true
    }

    override fun queryRoots(projection: Array<String>?): Cursor {

        val result = MatrixCursor(resolveRootProjection(projection))
        val accounts = Prefs.getAccounts()

        if(accounts.isEmpty()){
            return result
        }

        accounts.forEach {

            val path = GitLabPath(getAccountId(it))
            result.newRow().apply {

                add(DocumentsContract.Root.COLUMN_ROOT_ID, getRootId(path))
                add(DocumentsContract.Root.COLUMN_SUMMARY, context!!.getString(R.string.fileprovider_account_summary, it.username, it.serverUrl))
                add(DocumentsContract.Root.COLUMN_TITLE, context!!.getString(R.string.app_name))
                add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, toDocumentId(path))
                add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher)

            }

        }

        return result

    }

    override fun openDocument(documentId: String?, mode: String?, signal: CancellationSignal?): ParcelFileDescriptor? {
        Timber.d( "openDocument: %s", documentId)

        documentId?.let { d ->

            val path = resolvePath(d)
            val service = getGitLab(path.account!!)

            var done = false
            var outFile : File? = null

            DownloadFileTask(service!!, FileUtil.getProviderDirectory(context!!), object : DownloadFileTask.Callback {

                override fun onFinished(file: File?) {
                    outFile = file
                    done = true
                }

            }).execute(path)

           while(!done){

               Thread.sleep(100)
               if(signal?.isCanceled == true){
                   return null
               }

           }

            return ParcelFileDescriptor.open(outFile, ParcelFileDescriptor.MODE_READ_ONLY)

        }

        return null

    }

    override fun queryChildDocuments(parentDocumentId: String?, projection: Array<String>?, sortOrder: String?): Cursor {
        Timber.d("queryChildDocuments: %s", parentDocumentId)

        val result = MatrixCursor(resolveDocumentProjection(projection))

        parentDocumentId?.let { parent ->

            val path = resolvePath(parent)
            when(path.level){

                GitlabPathLevel.PATH_LEVEL_ACCOUNT -> loadProjects(path, result)
                GitlabPathLevel.PATH_LEVEL_PROJECT -> loadRevisions(path, result)
                GitlabPathLevel.PATH_LEVEL_REVISION, GitlabPathLevel.PATH_LEVEL_PATH -> loadFiles(path, result)
                GitlabPathLevel.PATH_LEVEL_UNSPECIFIED -> {}

            }

        }

        return result

    }

    override fun queryDocument(documentId: String?, projection: Array<String>?): Cursor {
        Timber.d("queryDocument: %s", documentId)
        val result = MatrixCursor(resolveDocumentProjection(projection))

        documentId?.let {

            val path = resolvePath(documentId)
            when(path.level){

                GitlabPathLevel.PATH_LEVEL_ACCOUNT -> { addAccountToResult(result, path) }
                GitlabPathLevel.PATH_LEVEL_PROJECT -> { loadProjects(path, result, path.project)}
                GitlabPathLevel.PATH_LEVEL_REVISION -> { loadRevisions(path, result, path.revision) }
                GitlabPathLevel.PATH_LEVEL_PATH -> {

                    path.getParent()?.let { p ->
                        loadFiles(p, result, path.getName())
                    }


                }
                else -> {}

            }

        }


        return result

    }

    private fun loadFiles(parentPath: GitLabPath, result: MatrixCursor, filter: String? = null){
        Timber.d("loadFiles: %s, %s", parentPath, filter)

        if(parentPath.account != null && parentPath.project != null && parentPath.revision != null) {

            loadChildren(parentPath.account, parentPath.project, parentPath.revision, parentPath.strPath())
                    ?.filter { filter == null || filter == it.name }
                    ?.forEach { f ->

                val subPath = if (parentPath.path == null) mutableListOf(f.name ?: "") else mutableListOf<String>().apply { addAll(parentPath.path); add(f.name ?: "") }
                val path = GitLabPath(parentPath.account, parentPath.project, parentPath.revision, subPath)

                if (f.type == RepositoryTreeObject.TYPE_FILE) {

                    val file = loadMetaFile(parentPath.account, parentPath.project, parentPath.revision, path.strPath()!!)
                    val commit = loadCommit(parentPath.account, parentPath.project, file?.lastCommitId ?: "")

                    file?.let { r -> addFileToResult(result, path, r.fileName ?: "", r.size, commit?.createdAt?.time) }

                } else if (f.type == RepositoryTreeObject.TYPE_FOLDER) {
                    addFolderToResult(result, path, f.name ?: "")
                }

            }

        }

    }

    private fun loadRevisions(parentPath: GitLabPath, result: MatrixCursor, filter: String? = null){

        if(parentPath.account != null && parentPath.project != null) {

            loadAllBranches(parentPath.account, parentPath.project)
                    ?.filter { filter == null || filter == it.name }
                    ?.forEach { b ->

                val path = GitLabPath(parentPath.account, parentPath.project, b.name)
                addFolderToResult(result, path, b.name ?: "", prefix = "${context!!.getString(R.string.fileprovider_branch_prefix)} ")

            }

            loadAllTags(parentPath.account, parentPath.project)
                    ?.filter { filter == null || filter == it.name }
                    ?.forEach { t ->

                val path = GitLabPath(parentPath.account, parentPath.project, t.name)
                addFolderToResult(result, path, t.name ?: "", prefix = "${context!!.getString(R.string.fileprovider_tag_prefix)} ")

            }

        }

    }

    data class ProjectsCacheKey(val account: String)
    data class RevisionCacheKey(val account: String, val project: Long)
    data class CommitCacheKey(val account: String, val project: Long, val commitId: String?)
    data class FileCacheKey(val account: String, val project: Long, val revision: String, val path: String?)

    private fun loadChildren(account: String, project: Long, revision: String, path: String?) : List<RepositoryTreeObject>? {
        return cacheOrLoad(FileCacheKey(account, project, revision, path), fileChildrenCache) { getGitLab(account)?.getTree(project, revision, path)?.blockingGet() }
    }

    private fun loadAllProjects(account: String) : List<Project>?{
        return cacheOrLoad(ProjectsCacheKey(account), projectsCache) { getGitLab(account)?.getAllProjects()?.blockingGet()?.body() }
    }

    private fun loadAllBranches(account: String, project: Long) : List<Branch>? {
        return cacheOrLoad(RevisionCacheKey(account, project), branchCache) { getGitLab(account)?.getBranches(project)?.blockingGet()?.body() }
    }

    private fun loadAllTags(account: String, project: Long) : List<Tag>? {
        return cacheOrLoad(RevisionCacheKey(account, project), tagCache) { getGitLab(account)?.getTags(project)?.blockingGet() }
    }

    private fun loadCommit(account: String, project: Long, commitId: String) : RepositoryCommit? {
        return cacheOrLoad(CommitCacheKey(account, project, commitId), commitCache) { getGitLab(account)?.getCommit(project, commitId)?.blockingGet() }
    }

    private fun loadMetaFile(account: String, project: Long, revision: String, path: String) : RepositoryFile? {
        return cacheOrLoad(FileCacheKey(account, project, revision, path), fileMetaCache) { repositoryFileFromHeader(getGitLab(account)?.getFileHead(project, path, revision)?.blockingGet()?.headers()) }
    }

    private fun loadProjects(parentPath: GitLabPath, result: MatrixCursor, filter: Long? = null){

        if(parentPath.account != null) {

            loadAllProjects(parentPath.account)
                    ?.filter { filter == null || it.id == filter }
                    ?.forEach { p ->

                val path = GitLabPath(parentPath.account, p.id)
                addFolderToResult(result, path, p.name ?: "", prefix = "${context!!.getString(R.string.fileprovider_project_prefix)} ")

            }

        }

    }

    private fun addAccountToResult(result: MatrixCursor, path: GitLabPath){

        path.account?.let { pa ->

            findAccount(pa)?.let {

                result.newRow().apply {

                    add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, toDocumentId(path))
                    add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, it.username +"@"+it.serverUrl)
                    add(DocumentsContract.Document.COLUMN_SIZE, 0)
                    add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR)
                    add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, 0)
                    add(DocumentsContract.Document.COLUMN_FLAGS, 0)
                    add(DocumentsContract.Document.COLUMN_ICON, R.mipmap.ic_launcher)

                }

            }

        }

    }

    private fun addFileToResult(result: MatrixCursor, path: GitLabPath, name: String, size: Long? = 0, lastModified: Long? = 0){

        result.newRow().apply {

            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, toDocumentId(path))
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, name)
            add(DocumentsContract.Document.COLUMN_SIZE, size)
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, lastModified)
            add(DocumentsContract.Document.COLUMN_MIME_TYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileActivity.fileExtension(name)) ?: "application/octet-stream")

        }

    }

    private fun addFolderToResult(result: MatrixCursor, path: GitLabPath, name: String, size: Long? = 0, lastModified: Long? = 0, prefix: String? = null){

        result.newRow().apply {

            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, toDocumentId(path))
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, if(prefix == null) name else prefix + name)
            add(DocumentsContract.Document.COLUMN_SIZE, size ?: 0)
            add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, lastModified ?: 0)
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)

        }

    }

    private fun findAccount(input: String) : Account? {
        return Prefs.getAccounts().firstOrNull { input == getAccountId(it) }
    }

    private fun resolveRootProjection(projection: Array<String>?): Array<String> {
        return projection ?: DEFAULT_ROOT_PROJECTION
    }

    private fun resolveDocumentProjection(projection: Array<String>?): Array<String> {
        return projection ?: DEFAULT_DOCUMENT_PROJECTION
    }

    private fun resolvePath(path: String) : GitLabPath {

        var input = path

        while(input.indexOf("//") > -1) {
            path.replace("//", "/")
        }

        if(input.endsWith("/")) {
            input = input.substring(0, input.length-1)
        }

        val parts =  input.split("/")
        return GitLabPath(
                if (parts.size > 0) java.net.URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name()); else null,
                if (parts.size > 1) parts[1].toLong() else null,
                if (parts.size > 2) java.net.URLDecoder.decode(parts[2], StandardCharsets.UTF_8.name()) else null,
                if (parts.size > 3) decodeArray(parts.slice(3 until parts.size)) else null)

    }

    private fun decodeArray(input: List<String>) : List<String> {
        return input.map { java.net.URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
    }

    private fun toDocumentId(path: GitLabPath) : String {

        val builder = StringBuilder()

        if(path.account != null){
            builder.append(java.net.URLEncoder.encode(path.account, StandardCharsets.UTF_8.name())).append("/")
        }

        if(path.project != null){
            builder.append(path.project.toString()).append("/")
        }

        if(path.revision != null){
            builder.append(java.net.URLEncoder.encode(path.revision, StandardCharsets.UTF_8.name())).append("/")
        }

        if(path.path != null && !path.path.isEmpty()){
            builder.append(path.path.joinToString("/") { URLEncoder.encode(it, StandardCharsets.UTF_8.name()) }).append("/")
        }

        if(builder.endsWith("/")) {
            builder.removeRange(builder.length-2, builder.length-1)
        }

        return builder.toString()

    }

    private fun repositoryFileFromHeader(headers : Headers?) : RepositoryFile? {

        if(headers == null) return null

        val file = RepositoryFile()
        file.fileName = headers.get("X-Gitlab-File-Name")
        file.filePath = headers.get("X-Gitlab-File-Path")
        file.blobId = headers.get("X-Gitlab-Blob-Id")
        file.encoding = headers.get("X-Gitlab-Encoding")
        file.commitId = headers.get("X-Gitlab-File-Name")
        file.lastCommitId = headers.get("X-Gitlab-Last-Commit-Id")
        file.ref = headers.get("X-Gitlab-Ref")
        file.size = headers.get("X-Gitlab-Size")?.toLong() ?: 0
        return file

    }

    private fun getRootId(path : GitLabPath) : String {
        return "$ROOT:${toDocumentId(path)}"
    }

    private fun getAccountId(account : Account) : String {
        return "${account.username}@${account.serverUrl}"
    }

    private fun getGitLab(accountId : String) : GitLab? {

        if(gitlabCache[accountId] != null){
            return gitlabCache[accountId]
        }

        findAccount(accountId)?.let {  account ->

            val clientBuilder = OkHttpClientFactory.create(account)
            if (BuildConfig.DEBUG) {
                clientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
            val gitlab = GitLabFactory.createGitLab(account, clientBuilder)
            gitlabCache[accountId] = gitlab
            return gitlab

        }

        return null

    }

    private fun <T, U> cacheOrLoad(key : T, cache : MutableMap<T, U>, load : (() -> U?)) : U? {

        var value = cache[key]
        if(value != null){
            return value
        }

        value = load()

        if(value != null) {
            cache[key] = value
        }

        return value

    }

    companion object {

        fun getAuthority() : String {
            return App.get().getString(R.string.file_provider_authority)
        }

    }

}

enum class GitlabPathLevel { PATH_LEVEL_UNSPECIFIED, PATH_LEVEL_ACCOUNT, PATH_LEVEL_PROJECT, PATH_LEVEL_REVISION, PATH_LEVEL_PATH }

data class GitLabPath(val account: String? = null, val project: Long? = null, val revision: String? = null, val path : List<String>? = null) {

    val level = calcLevel()

    private fun calcLevel() : GitlabPathLevel {

        if(path != null) return GitlabPathLevel.PATH_LEVEL_PATH
        if(revision != null) return GitlabPathLevel.PATH_LEVEL_REVISION
        if(project != null) return GitlabPathLevel.PATH_LEVEL_PROJECT
        if(account != null) return GitlabPathLevel.PATH_LEVEL_ACCOUNT
        return GitlabPathLevel.PATH_LEVEL_UNSPECIFIED

    }

    fun strPath() : String? {
        return path?.joinToString("/")
    }

    fun getName() : String? {

        return when(level){

            GitlabPathLevel.PATH_LEVEL_ACCOUNT -> account
            GitlabPathLevel.PATH_LEVEL_PROJECT -> project?.toString()
            GitlabPathLevel.PATH_LEVEL_REVISION -> revision
            GitlabPathLevel.PATH_LEVEL_PATH -> path?.get(path.size-1)
            else -> null

        }

    }

    fun getParent() : GitLabPath? {

        return when(level){

            GitlabPathLevel.PATH_LEVEL_PATH -> if(path == null || path.size <= 1) GitLabPath(account, project, revision) else GitLabPath(account, project, revision, path.slice(0 until path.size-1))
            GitlabPathLevel.PATH_LEVEL_REVISION -> GitLabPath(account, project)
            GitlabPathLevel.PATH_LEVEL_PROJECT -> GitLabPath(account)
            else -> null

        }


    }

}

class DownloadFileTask(private val service: GitLab, private val outDir: File, private val callback: Callback) : AsyncTask<GitLabPath, Any, File?>() {

    private var outFile : File? = null

    override fun doInBackground(vararg params: GitLabPath?): File? {

        try {

            params[0]?.let { path ->

                val file = service.getFile(path.project!!, path.strPath()!!, path.revision!!).blockingGet()
                val blob = file?.content?.base64Decode()?.blockingGet()

                if(file != null && blob != null) {
                    outFile = FileUtil.saveBlobToProviderDirectory(outDir, blob, path.strPath() + "/" + file.fileName)
                    return outFile
                }

            }

            return null

        } catch (e: Exception){
            return null
        }

    }

    override fun onPostExecute(result: File?) {
        super.onPostExecute(result)
        callback.onFinished(result)
    }

    override fun onCancelled(result: File?) {
        super.onCancelled(result)
        callback.onFinished(null)
    }

    override fun onCancelled() {
        super.onCancelled()
        callback.onFinished(null)

    }

    interface Callback {
        fun onFinished(file : File?)
    }

}
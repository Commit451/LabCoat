package com.commit451.gitlab.providers

import android.annotation.TargetApi
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Build
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import com.commit451.gitlab.R
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
import com.commit451.gitlab.providers.cursors.FilesCursor
import com.commit451.gitlab.providers.cursors.RootsCursor
import okhttp3.Headers
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.net.URLEncoder

private const val ROOT = "root"

private val gitlabCache = mutableMapOf<String, GitLab>()

private val commitCache = mutableMapOf<FileProvider.CommitCacheKey, CacheEntry<RepositoryCommit>>()
private val fileMetaCache = mutableMapOf<FileProvider.FileCacheKey, CacheEntry<RepositoryFile>>()
private val projectsCache = mutableMapOf<FileProvider.ProjectsCacheKey, CacheEntry<List<Project>>>()
private val revisionCache = mutableMapOf<FileProvider.RevisionCacheKey, CacheEntry<List<Revision>>>()
private val fileChildrenCache = mutableMapOf<FileProvider.FileCacheKey, CacheEntry<List<FileEntry>>>()

@TargetApi(Build.VERSION_CODES.KITKAT)
class FileProvider : DocumentsProvider() {

    override fun onCreate(): Boolean {
        Prefs.init(context!!)
        return true
    }

    override fun queryRoots(projection: Array<String>?): Cursor {

        val result = RootsCursor()
        val accounts = Prefs.getAccounts()

        if(accounts.isEmpty()){
            return result
        }

        accounts.forEach {

            val path = GitLabPath(getAccountId(it))
            result.addRoot(context!!, getRootId(path), toDocumentId(path), it)

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

        val result = FilesCursor()

        parentDocumentId?.let { parent ->

            val path = resolvePath(parent)
            when(path.level){

                GitlabPathLevel.PATH_LEVEL_ACCOUNT -> loadProjects(path, result, async = true)
                GitlabPathLevel.PATH_LEVEL_PROJECT -> loadRevisions(path, result)
                GitlabPathLevel.PATH_LEVEL_REVISION, GitlabPathLevel.PATH_LEVEL_PATH -> loadFiles(path, result)
                GitlabPathLevel.PATH_LEVEL_UNSPECIFIED -> {}

            }

        }

        return result

    }

    override fun queryDocument(documentId: String?, projection: Array<String>?): Cursor {
        Timber.d("queryDocument: %s", documentId)
        val result = FilesCursor()

        documentId?.let {

            val path = resolvePath(documentId)
            when(path.level){

                GitlabPathLevel.PATH_LEVEL_ACCOUNT -> { addAccountToResult(result, path) }
                GitlabPathLevel.PATH_LEVEL_PROJECT -> { loadProjects(path, result, path.project, false)}
                GitlabPathLevel.PATH_LEVEL_REVISION -> { loadRevisions(path, result, path.revision, false) }
                GitlabPathLevel.PATH_LEVEL_PATH -> {

                    path.getParent()?.let { p ->
                        loadFiles(p, result, path.getName(), false)
                    }


                }
                else -> {}

            }

        }


        return result

    }

    private fun loadFiles(parentPath: GitLabPath, result: FilesCursor, filter: String? = null, async: Boolean = true){
        Timber.d("loadFiles: %s, %s", parentPath, filter)

        if(parentPath.account != null && parentPath.project != null && parentPath.revision != null) {

            loadChildren(result, parentPath.account, toDocumentId(parentPath), parentPath.project, parentPath.revision, parentPath.strPath(), async)
                    ?.filter { filter == null || filter == it.name }
                    ?.forEach { f ->

                val subPath = if (parentPath.path == null) mutableListOf(f.name) else mutableListOf<String>().apply { addAll(parentPath.path); add(f.name) }
                val path = GitLabPath(parentPath.account, parentPath.project, parentPath.revision, subPath)

                if (!f.isFolder) {
                    result.addFile(toDocumentId(path), f.name, f.size, f.lastModified)
                } else {
                    result.addFolder(toDocumentId(path), f.name)
                }

            }

        }

    }

    private fun loadRevisions(parentPath: GitLabPath, result: FilesCursor, filter: String? = null, async: Boolean = true){

        if(parentPath.account != null && parentPath.project != null) {

            loadAllBranchesAndTags(result, parentPath.account, toDocumentId(parentPath), parentPath.project, async)
                    ?.filter { filter == null || filter == it.name }
                    ?.forEach { r ->

                val prefix = if(r.type == REVISION_TYPE_BRANCH) "${context!!.getString(R.string.fileprovider_branch_prefix)} " else "${context!!.getString(R.string.fileprovider_tag_prefix)} "
                val path = GitLabPath(parentPath.account, parentPath.project, r.name)
                result.addFolder(toDocumentId(path), r.name, prefix = prefix)

            }


        }

    }

    data class ProjectsCacheKey(val account: String)
    data class RevisionCacheKey(val account: String, val project: Long)
    data class CommitCacheKey(val account: String, val project: Long, val commitId: String?)
    data class FileCacheKey(val account: String, val project: Long, val revision: String, val path: String?)

    private fun loadChildren(cursor: FilesCursor, account: String, requestDocumentId: String, project: Long, revision: String, path: String?, async: Boolean) : List<FileEntry>? {
        return fetch(cursor, account, requestDocumentId, FileCacheKey(account, project, revision, path), fileChildrenCache, async, load = { fetchAllChildren(account, it, resolvePath(requestDocumentId), project, revision, path) }, map = { it })
    }

    private fun loadAllProjects(cursor: FilesCursor, account: String, requestDocumentId: String,  async : Boolean): List<Project>? {
        return fetch(cursor, account, requestDocumentId, ProjectsCacheKey(account), projectsCache, async, load = { it.getAllProjects().blockingGet() }, map = { it?.body() } )
    }

    private fun loadAllBranchesAndTags(cursor: FilesCursor, account: String, requestDocumentId: String, project: Long, async: Boolean) : List<Revision>? {
        return fetch(cursor, account, requestDocumentId, RevisionCacheKey(account, project), revisionCache, async, load = { fetchAllBranchesAndTags(it, project) }, map = { it })
    }

    private fun fetchAllChildren(account: String, gitlab: GitLab, parentPath: GitLabPath, project: Long, revision: String, path: String?) : List<FileEntry> {

        val output = mutableListOf<FileEntry>()
        gitlab.getTree(project, revision, path).blockingGet()?.forEach { e ->

            try {

                if (e.type == RepositoryTreeObject.TYPE_FILE || e.type == RepositoryTreeObject.TYPE_FOLDER) {

                    var size = 0L
                    var lastModified = 0L

                    if (e.type == RepositoryTreeObject.TYPE_FILE) {

                        val subPath = if (parentPath.path == null) mutableListOf(e.name ?: "") else mutableListOf<String>().apply {
                            addAll(parentPath.path)
                            add(e.name ?: "")
                        }

                        val filePath = GitLabPath(parentPath.account, parentPath.project, parentPath.revision, subPath)

                        val file = cacheOrLoad(FileCacheKey(account, project, revision, path), fileMetaCache, load = { gitlab.getFileHead(project, filePath.strPath()!!, revision).blockingGet() }, map = { repositoryFileFromHeader(it?.headers()) })
                        val commit = if (file?.lastCommitId != null) cacheOrLoad(CommitCacheKey(account, project, file.commitId), commitCache, load = { gitlab.getCommit(project, file.lastCommitId!!).blockingGet() }, map = { it }) else null

                        size = file?.size ?: 0L
                        lastModified = commit?.createdAt?.time ?: 0L

                    }

                    output.add(FileEntry(e.type == RepositoryTreeObject.TYPE_FOLDER, e.name ?: "", size, lastModified))

                }

            } catch (e: java.lang.Exception){
                Timber.e(e)
            }

        }

        return output

    }

    private fun fetchAllBranchesAndTags(gitlab : GitLab, project : Long) : List<Revision> {

        return mutableListOf<Revision>().apply {

            try {

                gitlab.getBranches(project).blockingGet().body()?.forEach { b ->
                    this.add(Revision(REVISION_TYPE_BRANCH, b.name ?: ""))
                }

                gitlab.getTags(project).blockingGet()?.forEach { t ->
                    this.add(Revision(REVISION_TYPE_TAG, t.name ?: ""))
                }

            } catch (e: java.lang.Exception){
                Timber.e(e)
            }

        }

    }

    private fun loadProjects(parentPath: GitLabPath, result: FilesCursor, filter: Long? = null, async : Boolean = true){

        if(parentPath.account != null) {
            loadAllProjects(result, parentPath.account, toDocumentId(parentPath), async)?.filter { filter == null || it.id == filter }?.forEach { p ->
                val path = GitLabPath(parentPath.account, p.id)
                result.addFolder(toDocumentId(path), p.name ?: "", prefix = "${context!!.getString(R.string.fileprovider_project_prefix)} ")
            }
        }

    }

    private fun addAccountToResult(result: FilesCursor, path: GitLabPath){

        path.account?.let { pa ->
            findAccount(pa)?.let {
                result.addFolder(toDocumentId(path), it.username +"@"+it.serverUrl)
            }
        }

    }

    private fun findAccount(input: String) : Account? {
        return Prefs.getAccounts().firstOrNull { input == getAccountId(it) }
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

    private var currentSyncActivity : Any? = null

    private fun <T, U, V> fetch(cursor: FilesCursor, account: String, requestDocumentId: String, key : U, cache : MutableMap<U, CacheEntry<T>>, async : Boolean, load: (GitLab) -> V?, map: (V?) -> T?): T? {

        val service = getGitLab(account)
        if(!async){

            var value = cache[key]?.entry
            if(value != null && cache[key]?.isValid() == true){
                return value
            }

            service?.let { s ->

                value = map(load(s))
                value?.let { cache[key] = CacheEntry(System.currentTimeMillis(), it) }

            }

            return value

        }

        val browsedDirIdUri = DocumentsContract.buildChildDocumentsUri(getAuthority(), requestDocumentId)
        cursor.setNotificationUri(context!!.contentResolver, browsedDirIdUri)

        var hasMore = false

        if(currentSyncActivity != key) {

            service?.let { s ->

                currentSyncActivity = key
                hasMore = true

                Thread {

                    try {

                        Thread.sleep(250)
                        val values = map(load(s))

                        if (values != null) {
                            cache[key] = CacheEntry(System.currentTimeMillis(), values)
                        }

                        if (currentSyncActivity == key) {
                            context?.contentResolver?.notifyChange(browsedDirIdUri, null)
                        }

                    } catch (e: Exception){
                        Timber.e(e)
                    }

                }.start()

            }


        } else {
            currentSyncActivity = null
        }

        cursor.setHasMore(hasMore)
        return cache[key]?.entry

    }

    private fun <T, U, V> cacheOrLoad(key : V, cache : MutableMap<V, CacheEntry<T>>, load : () -> U?, map : (U?) -> T?) : T? {

        var value = cache[key]?.entry
        if(value != null && cache[key]?.isValid() == true){
            return value
        }

        value = map(load())
        value?.let { cache[key] = CacheEntry(System.currentTimeMillis(), it) }

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

const val REVISION_TYPE_BRANCH = "branch"
const val REVISION_TYPE_TAG = "tag"
data class Revision(val type : String, val name : String)

data class FileEntry(val isFolder : Boolean, val name : String, val size : Long, val lastModified : Long)

const val MAX_CACHE_TIME = 1000 * 60 * 60 * 24
data class CacheEntry<T>(val time : Long, val entry : T) {

    fun isValid() : Boolean {
        return System.currentTimeMillis() < (time + MAX_CACHE_TIME)
    }

}
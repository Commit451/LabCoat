package com.commit451.gitlab.util

import java.util.regex.Pattern

/**
 * Parses special GitLab markdown and transforms it into normal markdown links within the app
 * [https://gitlab.com/help/user/markdown#special-gitlab-references]
 */
object GitLabMarkdownParser {

    private val patternIssue by lazy {
        //starts with #, followed by any number of digits
        Pattern.compile("#\\d+")
    }

    private val patternMergeRequest by lazy {
        //starts with !, followed by any number of digits
        Pattern.compile("!\\d+")
    }

    private val patternLabel by lazy {
        //starts with ~, followed by any number of digits
        Pattern.compile("~\\d+")
    }

    private val patternMilestone by lazy {
        //starts with %, followed by any number of digits
        Pattern.compile("%\\d+")
    }

    fun parse(text: String): String {
        return parseMilestones(parseLabels(parseMergeRequests(parseIssues(text))))
    }

    fun parseIssues(text: String): String {
        val matcher = patternIssue.matcher(text)
        return matcher.replaceAll("hi")
    }

    fun parseMergeRequests(text: String): String {
        val matcher = patternMergeRequest.matcher(text)
        return matcher.replaceAll("hi")
    }

    fun parseLabels(text: String): String {
        val matcher = patternLabel.matcher(text)
        return matcher.replaceAll("hi")
    }

    fun parseMilestones(text: String): String {
        val matcher = patternMilestone.matcher(text)
        return matcher.replaceAll("hi")
    }
}
package com.commit451.gitlab

import com.commit451.gitlab.util.GitLabMarkdownParser
import org.junit.Assert
import org.junit.Test

/**
 * Tests custom GitLab markdown parsing
 */
class MarkdownParserTests {

    @Test
    fun issuesTest() {
        val text = "#1 #2 blah blah #3"
        val expected = "hi hi blah blah hi"
        val parsed = GitLabMarkdownParser.parse(text)
        Assert.assertEquals(expected, parsed)
    }

    @Test
    fun mergeRequestTest() {
        val text = "!1 !2 blah blah !3"
        val expected = "hi hi blah blah hi"
        val parsed = GitLabMarkdownParser.parse(text)
        Assert.assertEquals(expected, parsed)
    }

    @Test
    fun labelsTest() {
        val text = "~1 ~2 blah blah ~3"
        val expected = "hi hi blah blah hi"
        val parsed = GitLabMarkdownParser.parse(text)
        Assert.assertEquals(expected, parsed)
    }

    @Test
    fun milestonesTest() {
        val text = "%1 %2 blah blah %3"
        val expected = "hi hi blah blah hi"
        val parsed = GitLabMarkdownParser.parse(text)
        Assert.assertEquals(expected, parsed)
    }
}
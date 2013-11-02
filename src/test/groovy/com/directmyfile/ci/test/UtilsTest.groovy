package com.directmyfile.ci.test

import com.directmyfile.ci.Utils
import org.junit.Test

class UtilsTest extends GroovyTestCase {
    @Test
    void testFindCommand() {
        def expected = "/bin/sh" // Might Fail if sh is not in /bin/
        def actual = Utils.findCommandOnPath("sh")
        assertFalse "Command was not found!", actual==null
        assertEquals("Command found in wrong spot!", expected, actual.absolutePath)
    }
}

package com.directmyfile.ci.test

import com.directmyfile.ci.utils.Utils
import org.junit.Test

class UtilsTest extends GroovyTestCase {
    @Test
    void testFindCommand() {
        def actual = Utils.findCommandOnPath("sh")
        assertFalse "Command was not found!", actual == null
    }
}

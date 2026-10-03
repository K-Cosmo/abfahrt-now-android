package now.abfahrt.transit.quality

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LocaleParityTest {

    @Test
    fun allDeclaredLocaleFilesHaveTheSameStringKeysAsDefaultValues() {
        val resDir = listOf(File("src/main/res"), File("app/src/main/res")).first { it.exists() }
        val defaultKeys = stringKeys(File(resDir, "values/strings.xml"))
        val localeFiles = resDir.listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") }
            .map { File(it, "strings.xml") }
            .filter { it.exists() }

        assertTrue("Expected at least one translated locale file", localeFiles.isNotEmpty())

        val errors = localeFiles.mapNotNull { file ->
            val keys = stringKeys(file)
            val missing = defaultKeys - keys
            val extra = keys - defaultKeys
            if (missing.isEmpty() && extra.isEmpty()) null else {
                "${file.parentFile?.name ?: file.path}: missing=${missing.sorted()} extra=${extra.sorted()}"
            }
        }

        assertEquals(emptyList<String>(), errors)
    }

    private fun stringKeys(file: File): Set<String> {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isIgnoringComments = true
            isCoalescing = true
        }
        val document = factory.newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("string")
        return buildSet {
            for (i in 0 until nodes.length) {
                val element = nodes.item(i) as Element
                add(element.getAttribute("name"))
            }
        }
    }
}

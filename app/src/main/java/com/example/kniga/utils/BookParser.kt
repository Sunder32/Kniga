package com.example.kniga.utils

import android.content.Context
import com.example.kniga.data.local.entity.BookFormat
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

object BookParser {
    
    fun parseBook(context: Context, filePath: String, format: String): BookContent {
        val cacheKey = BookCache.getCacheKey(filePath, format)
        
        BookCache.get(cacheKey)?.let {
            return it
        }
        
        val content = when (format) {
            BookFormat.EPUB -> parseEpub(filePath)
            BookFormat.PDF -> parsePdf(filePath)
            BookFormat.FB2 -> parseFb2(filePath)
            BookFormat.MOBI -> parseEpub(filePath)
            BookFormat.TXT -> parseTxt(filePath)
            else -> parseEpub(filePath)
        }
        
        BookCache.put(cacheKey, content)
        
        return content
    }
    
    private fun parseEpub(filePath: String): BookContent {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return BookContent(
                    chapters = listOf(
                        Chapter(
                            title = "Файл не найден",
                            content = "Файл $filePath не найден на устройстве.\n\nПопробуйте импортировать книгу заново."
                        )
                    )
                )
            }
            
            val chapters = mutableListOf<Chapter>()
            
            ZipInputStream(FileInputStream(file)).use { zip ->
                var entry = zip.nextEntry
                var chapterIndex = 0
                
                while (entry != null) {
                    if (entry.name.endsWith(".html") || entry.name.endsWith(".xhtml") || entry.name.endsWith(".htm")) {
                        val content = zip.bufferedReader().use { it.readText() }
                        val cleanContent = cleanHtmlContent(content)
                        
                        if (cleanContent.isNotBlank() && cleanContent.length > 50) {
                            chapters.add(
                                Chapter(
                                    title = "Глава ${++chapterIndex}",
                                    content = cleanContent
                                )
                            )
                        }
                    }
                    entry = zip.nextEntry
                }
            }
            
            if (chapters.isEmpty()) {
                chapters.add(
                    Chapter(
                        title = "Пустая книга",
                        content = "Не удалось извлечь текст из этого EPUB файла."
                    )
                )
            }
            
            BookContent(chapters = chapters)
        } catch (e: Exception) {
            BookContent(
                chapters = listOf(
                    Chapter(
                        title = "Ошибка чтения",
                        content = "Не удалось прочитать EPUB файл:\n\n${e.message}\n\nПроверьте, что файл не поврежден."
                    )
                )
            )
        }
    }
    
    private fun parsePdf(filePath: String): BookContent {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return BookContent(
                    chapters = listOf(
                        Chapter(
                            title = "Файл не найден",
                            content = "PDF файл не найден."
                        )
                    )
                )
            }
            
            val document = PDDocument.load(file)
            val stripper = PDFTextStripper()
            val chapters = mutableListOf<Chapter>()
            
            val totalPages = document.numberOfPages
            
            for (pageNum in 1..totalPages) {
                stripper.startPage = pageNum
                stripper.endPage = pageNum
                
                val pageText = stripper.getText(document)
                
                if (pageText.isNotBlank()) {
                    chapters.add(
                        Chapter(
                            title = "",
                            content = pageText.trim()
                        )
                    )
                }
            }
            
            document.close()
            
            if (chapters.isEmpty()) {
                chapters.add(
                    Chapter(
                        title = "Пустой PDF",
                        content = "Не удалось извлечь текст из PDF файла."
                    )
                )
            }
            
            BookContent(chapters = chapters)
        } catch (e: Exception) {
            BookContent(
                chapters = listOf(
                    Chapter(
                        title = "Ошибка",
                        content = "Ошибка при чтении PDF: ${e.message}"
                    )
                )
            )
        }
    }
    
    private fun parseFb2(filePath: String): BookContent {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return BookContent(
                    chapters = listOf(
                        Chapter(
                            title = "Файл не найден",
                            content = "FB2 файл не найден."
                        )
                    )
                )
            }
            
            val xmlContent = file.readText()
            val chapters = mutableListOf<Chapter>()
            
            val sectionRegex = Regex("<section[^>]*>(.*?)</section>", RegexOption.DOT_MATCHES_ALL)
            val titleRegex = Regex("<title>(.*?)</title>", RegexOption.DOT_MATCHES_ALL)
            val pRegex = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
            
            val sections = sectionRegex.findAll(xmlContent)
            
            sections.forEachIndexed { index, match ->
                val sectionContent = match.groupValues[1]
                
                val titleMatch = titleRegex.find(sectionContent)
                val title = titleMatch?.groupValues?.get(1)?.let { cleanXmlTags(it) } ?: "Глава ${index + 1}"
                
                val paragraphs = pRegex.findAll(sectionContent)
                    .map { cleanXmlTags(it.groupValues[1]) }
                    .filter { it.isNotBlank() }
                    .joinToString("\n\n")
                
                if (paragraphs.isNotBlank()) {
                    chapters.add(
                        Chapter(
                            title = title,
                            content = paragraphs
                        )
                    )
                }
            }
            
            if (chapters.isEmpty()) {
                val allText = pRegex.findAll(xmlContent)
                    .map { cleanXmlTags(it.groupValues[1]) }
                    .filter { it.isNotBlank() }
                    .joinToString("\n\n")
                
                if (allText.isNotBlank()) {
                    chapters.add(
                        Chapter(
                            title = file.nameWithoutExtension,
                            content = allText
                        )
                    )
                } else {
                    chapters.add(
                        Chapter(
                            title = "Пустой файл",
                            content = "Не удалось извлечь текст из FB2 файла."
                        )
                    )
                }
            }
            
            BookContent(chapters = chapters)
        } catch (e: Exception) {
            BookContent(
                chapters = listOf(
                    Chapter(
                        title = "Ошибка",
                        content = "Ошибка при чтении FB2: ${e.message}"
                    )
                )
            )
        }
    }
    
    private fun parseTxt(filePath: String): BookContent {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return BookContent(
                    chapters = listOf(
                        Chapter(
                            title = "Файл не найден",
                            content = "TXT файл не найден."
                        )
                    )
                )
            }
            
            val content = file.readText()
            BookContent(
                chapters = listOf(
                    Chapter(
                        title = file.nameWithoutExtension,
                        content = content
                    )
                )
            )
        } catch (e: Exception) {
            BookContent(
                chapters = listOf(
                    Chapter(
                        title = "Ошибка",
                        content = "Ошибка при чтении TXT: ${e.message}"
                    )
                )
            )
        }
    }
    
    private fun cleanHtmlContent(html: String): String {
        var text = html
        
        text = text.replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
            .replace("</p>", "\n\n")
            .replace("</div>", "\n\n")
            .replace("</h1>", "\n\n")
            .replace("</h2>", "\n\n")
            .replace("</h3>", "\n\n")
        
        text = text.replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
        text = text.replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
        text = text.replace(Regex("<[^>]+>"), "")
        
        text = text.replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
        
        text = text.replace(Regex("\n{3,}"), "\n\n")
        
        return text.trim()
    }
    
    private fun cleanXmlTags(xml: String): String {
        var text = xml
        
        text = text.replace(Regex("<[^>]+>"), "")
        
        text = text.replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
        
        return text.trim()
    }
}

data class BookContent(
    val chapters: List<Chapter>
)

data class Chapter(
    val title: String,
    val content: String
)

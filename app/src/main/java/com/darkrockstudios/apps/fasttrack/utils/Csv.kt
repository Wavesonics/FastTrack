package com.darkrockstudios.apps.fasttrack.utils

/**
 * Minimal RFC-4180 CSV helpers, used by logbook import/export.
 *
 * Notes fields may contain commas, quotes, and newlines, so naive
 * `split(",")` / `split("\n")` cannot be used — these handle quoting properly.
 */

/** Escape a single field for CSV output, quoting only when necessary. */
fun csvEscape(field: String): String {
	val needsQuoting = field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
	if (!needsQuoting) return field
	val escaped = field.replace("\"", "\"\"")
	return "\"$escaped\""
}

/**
 * Parse CSV text into rows of fields. Handles quoted fields containing
 * commas, doubled-quote escapes (`""`), and embedded CR/LF. Accepts both
 * LF and CRLF row terminators.
 */
fun parseCsv(text: String): List<List<String>> {
	val rows = mutableListOf<List<String>>()
	var row = mutableListOf<String>()
	val field = StringBuilder()
	var inQuotes = false
	var i = 0

	fun endField() {
		row.add(field.toString())
		field.setLength(0)
	}

	fun endRow() {
		endField()
		rows.add(row)
		row = mutableListOf()
	}

	while (i < text.length) {
		val c = text[i]
		if (inQuotes) {
			when {
				c == '"' && i + 1 < text.length && text[i + 1] == '"' -> {
					field.append('"'); i++
				}
				c == '"' -> inQuotes = false
				else -> field.append(c)
			}
		} else {
			when (c) {
				'"' -> inQuotes = true
				',' -> endField()
				'\r' -> { /* ignore; the following \n (if any) ends the row */ }
				'\n' -> endRow()
				else -> field.append(c)
			}
		}
		i++
	}
	// Flush trailing field/row (unless the text ended exactly on a newline)
	if (field.isNotEmpty() || row.isNotEmpty()) endRow()

	return rows
}

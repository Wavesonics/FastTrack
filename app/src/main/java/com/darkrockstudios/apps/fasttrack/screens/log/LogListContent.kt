package com.darkrockstudios.apps.fasttrack.screens.log

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.darkrockstudios.apps.fasttrack.R
import com.darkrockstudios.apps.fasttrack.data.log.FastingLogEntry
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Composable
fun LogListContent(
	entries: List<FastingLogEntry>,
	onEdit: (FastingLogEntry) -> Unit,
	onDelete: (FastingLogEntry) -> Unit,
	contentPadding: PaddingValues,
	modifier: Modifier = Modifier,
) {
	LazyColumn(
		modifier = modifier.fillMaxSize(),
		contentPadding = contentPadding,
	) {
		if (entries.isNotEmpty()) {
			items(entries, key = { it.id }) { entry ->
				FastEntryItem(
					entry = entry,
					onEdit = { onEdit(entry) },
					onDelete = { onDelete(entry) }
				)
			}
		} else {
			item {
				Text(
					stringResource(R.string.log_no_entries),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 8.dp),
					textAlign = TextAlign.Center
				)
			}
		}
	}
}

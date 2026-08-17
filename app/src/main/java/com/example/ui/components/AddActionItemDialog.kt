package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun AddActionItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, assignee: String, dueDate: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Action Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description *") },
                    placeholder = { Text("e.g. Circulate revised pro-forma budget") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_title_input")
                )

                OutlinedTextField(
                    value = assignee,
                    onValueChange = { assignee = it },
                    label = { Text("Assignee") },
                    placeholder = { Text("e.g. Sarah Jenkins") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_assignee_input")
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date / Timeframe") },
                    placeholder = { Text("e.g. Next Friday, End of Sprint") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_duedate_input")
                )

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) },
                            modifier = Modifier.testTag("priority_chip_$p")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), assignee.trim(), dueDate.trim(), priority)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_action_item_button")
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_action_item_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

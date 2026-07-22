package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExportDialog(
    csvText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var exportFormat by remember { mutableStateOf("CSV") }

    val contentToCopy = when (exportFormat) {
        "CSV" -> csvText
        "JSON" -> csvToJson(csvText)
        else -> csvToPdfReportText(csvText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row {
                Icon(imageVector = Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Analysis Results", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { exportFormat = "CSV" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CSV", fontWeight = if (exportFormat == "CSV") FontWeight.Bold else FontWeight.Normal)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedButton(
                        onClick = { exportFormat = "JSON" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("JSON", fontWeight = if (exportFormat == "JSON") FontWeight.Bold else FontWeight.Normal)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedButton(
                        onClick = { exportFormat = "PDF Summary" },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("PDF Text", fontWeight = if (exportFormat == "PDF Summary") FontWeight.Bold else FontWeight.Normal)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = contentToCopy,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("FootyAnalytics Export", contentToCopy)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied $exportFormat to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.testTag("copy_export_to_clipboard_button")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy to Clipboard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun csvToJson(csv: String): String {
    val lines = csv.lineSequence().filter { it.isNotBlank() }.toList()
    if (lines.isEmpty()) return "[]"
    val headers = lines[0].split(",")
    val sb = StringBuilder("[\n")
    for (i in 1 until lines.size) {
        sb.append("  {\n")
        val values = lines[i].split("\",\"").map { it.replace("\"", "") }
        for (j in values.indices) {
            val key = headers.getOrNull(j)?.replace("\"", "") ?: "col$j"
            sb.append("    \"$key\": \"${values[j]}\"")
            if (j < values.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("  }")
        if (i < lines.size - 1) sb.append(",")
        sb.append("\n")
    }
    sb.append("]")
    return sb.toString()
}

private fun csvToPdfReportText(csv: String): String {
    return "=== FOOTYANALYTICS GLOBAL MULTI-MARKET REPORT ===\nGenerated at: ${java.util.Date()}\n50,000 Monte Carlo Simulations per Match\n\n" + csv
}

package com.jb.voyageur.core.ui.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.jb.voyageur.R

@Composable
fun SaisieNumerique(
    titre: String,
    valeurInitiale: Int,
    min: Int,
    max: Int,
    onValider: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textFieldValue by remember {
        val text = valeurInitiale.toString()
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(0, text.length)))
    }
    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titre) },
        text = {
            TextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val valeurSaisie = textFieldValue.text.toIntOrNull()
                if (valeurSaisie != null) {
                    onValider(valeurSaisie.coerceIn(min, max))
                } else {
                    onValider(valeurInitiale)
                }
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.annuler))
            }
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

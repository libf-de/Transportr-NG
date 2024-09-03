/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2024 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.transportr.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.grobox.transportr.R
import de.grobox.transportr.locations.WrapLocation
import java.util.regex.Pattern

private fun getHighlightedText(l: WrapLocation, search: String?): AnnotatedString {
    return if (search != null && search.length >= 3) {
        val regex = "(?i)(" + Pattern.quote(search.toString()) + ")"
        val str = l.fullName.replace(regex.toRegex(), "<b>$1</b>")
        AnnotatedString.fromHtml(str)
    } else {
        AnnotatedString.fromHtml(l.fullName)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LocationGpsInput(
    modifier: Modifier = Modifier,
    location: WrapLocation?,
    suggestions: Set<WrapLocation>?,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onAcceptSuggestion: (WrapLocation) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    label: String = "",
    placeholder: String = label,
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = modifier.onFocusChanged {
                isFocused = it.isFocused
                onFocusChange(it.isFocused)
            },
            value = text,
            onValueChange = {
                onValueChange(it)
                text = it
                showSuggestions = true
            },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.ic_gps),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (isLoading && isFocused)
                    CircularProgressIndicator()
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            maxLines = 1
        )

        if(isFocused && showSuggestions && suggestions != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(suggestions.toList()) { sug ->
                    DropdownMenuItem(
                        onClick = {
                            onAcceptSuggestion(sug)
                            text = sug.fullName
                        },
                        text = {
                            if (sug.wrapType == WrapLocation.WrapType.GPS)
                                Text(
                                    text = stringResource(R.string.location_gps),
                                    fontStyle = FontStyle.Italic
                                )
                            else
                                Text(text = getHighlightedText(sug, text))
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(sug.drawable),
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun CompactLocationGpsInput(
    modifier: Modifier = Modifier,
    location: WrapLocation?,
    suggestions: Set<WrapLocation>?,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onAcceptSuggestion: (WrapLocation) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    label: String = "",
    placeholder: String = label,
    padding: PaddingValues = PaddingValues(8.dp, 8.dp, 8.dp, 8.dp)
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }

    CompactTextField(
        modifier = modifier.onFocusChanged {
            isFocused = it.isFocused
            onFocusChange(it.isFocused)
        }.height(48.dp),
        value = text,
        onValueChange = {
            onValueChange(it)
            text = it
            showSuggestions = true
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_gps),
                contentDescription = null
            )
        },
        trailingIcon = {
            if (isLoading && isFocused)
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp)
                )
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        padding = padding,
        textStyle = MaterialTheme.typography.bodyLarge
    )

    if(isFocused && showSuggestions) {
        DropdownMenu(
            expanded = !suggestions.isNullOrEmpty(),
            onDismissRequest = { showSuggestions = false },
            modifier = Modifier.fillMaxWidth(),
            // This line here will accomplish what you want
            properties = PopupProperties(focusable = false)
        ) {
            suggestions?.forEach { sug ->
                DropdownMenuItem(
                    onClick = {
                        onAcceptSuggestion(sug)
                        text = sug.fullName
                    },
                    text = {
                        if (sug.wrapType == WrapLocation.WrapType.GPS)
                            Text(
                                text = stringResource(R.string.location_gps),
                                fontStyle = FontStyle.Italic
                            )
                        else
                            Text(text = getHighlightedText(sug, text))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(sug.drawable),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrokenLocationGpsInput(
    modifier: Modifier = Modifier,
    location: WrapLocation?,
    suggestions: Set<WrapLocation>?,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onAcceptSuggestion: (WrapLocation) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    label: String = "",
    placeholder: String = label,
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = isFocused && showSuggestions && !suggestions.isNullOrEmpty(),
        onExpandedChange = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = modifier.onFocusChanged {
                isFocused = it.isFocused
                onFocusChange(it.isFocused)
                if(!it.isFocused) showSuggestions = true
            }.menuAnchor(),
            value = text,
            onValueChange = {
                onValueChange(it)
                text = it
                showSuggestions = true
            },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.ic_gps),
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (isLoading && isFocused)
                    CircularProgressIndicator()
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            maxLines = 1
        )

        ExposedDropdownMenu(
            expanded = isFocused && showSuggestions && !suggestions.isNullOrEmpty(),
            onDismissRequest = { showSuggestions = false },
            modifier = Modifier
        ) {
            suggestions?.forEach { sug ->
                DropdownMenuItem(
                    onClick = {
                        onAcceptSuggestion(sug)
                        text = sug.fullName
                    },
                    text = {
                        if (sug.wrapType == WrapLocation.WrapType.GPS)
                            Text(
                                text = stringResource(R.string.location_gps),
                                fontStyle = FontStyle.Italic
                            )
                        else
                            Text(text = getHighlightedText(sug, text))
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(sug.drawable),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
//
//    if(isFocused && showSuggestions) {
//        DropdownMenu(
//            expanded = !suggestions.isNullOrEmpty(),
//            onDismissRequest = { showSuggestions = false },
//            modifier = Modifier.fillMaxWidth(),
//            // This line here will accomplish what you want
//            properties = PopupProperties(focusable = false)
//        ) {
//
//        }
//    }

}

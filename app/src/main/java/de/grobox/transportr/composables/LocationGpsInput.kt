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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import de.grobox.transportr.R
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.directions.composables.CompactTextField
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
fun BaseLocationGpsInput(
    modifier: Modifier = Modifier,
    location: WrapLocation?,
    suggestions: Set<WrapLocation>?,
    onValueChange: (String) -> Unit,
    onAcceptSuggestion: (WrapLocation) -> Unit,
    onFocusChange: (Boolean) -> Unit = {},
    isLoading: Boolean = false,
    placeholder: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf(location?.fullName ?: "") }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row() {
                BasicTextField(
                    modifier = modifier.onFocusChanged {
                        isFocused = it.isFocused
                        onFocusChange(it.isFocused)
                    }
                        .weight(1f)
                        .then(Modifier.wrapContentHeight(Alignment.CenterVertically)),
                    value = text,
                    onValueChange = {
                        onValueChange(it)
                        text = it
                        showSuggestions = true
                    },
                    maxLines = 1,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                )

                if(isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if(text.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onValueChange("")
                            text = ""
                            showSuggestions = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = stringResource(R.string.clear_location),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


            if (text.isBlank()) {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }


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
                                painter = painterResource(sug.drawableInt),
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
    gpsLoading: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf(location?.getName() ?: "") }

    LaunchedEffect(location) {
        location?.getName()?.takeIf { it.isNotBlank() }?.let { text = it }
    }

    var infiniteTransition = rememberInfiniteTransition();
    val gpsIconAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

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
                if(location != null) {
                    Icon(
                        painterResource(location.drawableInt),
                        contentDescription = null
                    )
                } else if(gpsLoading) {
                    Icon(
                        painterResource(R.drawable.ic_gps),
                        contentDescription = null,
                        modifier = Modifier.alpha(gpsIconAlpha)
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.ic_location),
                        contentDescription = null
                    )
                }

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
                                painter = painterResource(sug.drawableInt),
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
fun LocationInputField(
    modifier: Modifier = Modifier,
    location: WrapLocation?,
    suggestions: Set<WrapLocation>?,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onAcceptSuggestion: (WrapLocation) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    isGpsLoading: Boolean = false,
    placeholder: String = "",
    textField: @Composable (
        modifier: Modifier,
        value: String,
        onValueChange: (String) -> Unit,
        leadingIcon: @Composable () -> Unit,
        trailingIcon: @Composable () -> Unit,
        placeholder: @Composable () -> Unit,
    ) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf(location?.fullName ?: "") }

    val infiniteTransition = rememberInfiniteTransition(label = "gpsInfiniteTransition")
    val gpsIconAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gpsIconAlpha"
    )

    LaunchedEffect(location) {
        location?.getName()?.takeIf { it.isNotBlank() }?.let { text = it }
    }

    textField(
        modifier.onFocusChanged { isFocused = it.isFocused; onFocusChange(it.isFocused) }, // Modifier
        text, // value
        { onValueChange(it); text = it; showSuggestions = true }, // onValueChange
        { // LeadingIcon
            if(isGpsLoading) {
                Icon(
                    painter = painterResource(R.drawable.ic_gps),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(gpsIconAlpha),
                )
            } else {
                Icon(
                    painter = painterResource(location?.drawableInt ?: R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }

        },
        { // TrailingIcon
            if (isLoading && isFocused)
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp)
                )
        },
        { // Placeholder
            Text(
                text = if(isGpsLoading)
                            stringResource(R.string.stations_searching_position)
                        else
                            placeholder
            )
        },
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
                            painter = painterResource(sug.drawableInt),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
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
    isGpsLoading: Boolean = false,
    padding: PaddingValues = PaddingValues(8.dp, 8.dp, 8.dp, 8.dp)
)
{
    LocationInputField(
        modifier = modifier,
        location = location,
        suggestions = suggestions,
        isLoading = isLoading,
        onValueChange = onValueChange,
        onAcceptSuggestion = onAcceptSuggestion,
        onFocusChange = onFocusChange,
        isGpsLoading = isGpsLoading,
        placeholder = placeholder
    ) { iModifier, iValue, iOnValueChange, iLeadingIcon, iTrailingIcon, iPlaceholder ->
        CompactTextField(
            modifier = iModifier.height(48.dp),
            value = iValue,
            onValueChange = { iOnValueChange(it) },
            leadingIcon = { iLeadingIcon() },
            trailingIcon = { iTrailingIcon() },
            label = { Text(if(isGpsLoading) stringResource(R.string.stations_searching_position) else label) },
            placeholder = { iPlaceholder() },
            singleLine = true,
            padding = padding,
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CompactLocationInput(
    modifier: Modifier = Modifier,
    location: WrapLocation?,
    suggestions: Set<WrapLocation>?,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onAcceptSuggestion: (WrapLocation) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    label: String = "",
    placeholder: String = label,
    isGpsLoading: Boolean = false,
    padding: PaddingValues = PaddingValues(8.dp, 8.dp, 8.dp, 8.dp)
) {
    var isFocused by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf(location?.fullName ?: "") }

    val infiniteTransition = rememberInfiniteTransition(label = "gpsInfiniteTransition")
    val gpsIconAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gpsIconAlpha"
    )

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
            if(isGpsLoading) {
                Icon(
                    painter = painterResource(R.drawable.ic_gps),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(gpsIconAlpha),
                )
            } else {
                Icon(
                    painter = painterResource(location?.drawableInt ?: R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }

        },
        trailingIcon = {
            if (isLoading && isFocused)
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp)
                )
        },
        label = { Text(label) },
        placeholder = {
            Text(if(isGpsLoading) stringResource(R.string.location_gps) else placeholder)
        },
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
                            painter = painterResource(sug.drawableInt),
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
                            painter = painterResource(sug.drawableInt),
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

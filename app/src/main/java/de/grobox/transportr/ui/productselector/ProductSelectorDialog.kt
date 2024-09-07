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

package de.grobox.transportr.ui.productselector

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.grobox.transportr.R
import de.grobox.transportr.data.dto.KProduct
import java.util.EnumSet

@Composable
fun ProductSelectorDialog(
    show: Boolean,
    selectedProducts: List<KProduct>?,
    onConfirmation: (EnumSet<KProduct>) -> Unit,
    onDismissRequest: () -> Unit
) {
    if(show) {
        var selected by remember { mutableStateOf(selectedProducts ?: KProduct.ALL) }

        AlertDialog(
            icon = {
                Icon(painterResource(R.drawable.product_bus), contentDescription = "Example Icon")
            },
            title = {
                Text(text = stringResource(R.string.select_products))
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(KProduct.ALL.toList()) { product ->
                        ProductOptionComposable(
                            product = product,
                            selected = product in selected,
                            onSelectedChange = {
                                selected = if (product in selected) {
                                    selected - product
                                } else {
                                    selected + product
                                }
                            }
                        )
                    }
                }
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val temp = EnumSet.noneOf(KProduct::class.java)
                        temp.addAll(selected)
                        onConfirmation(temp)
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

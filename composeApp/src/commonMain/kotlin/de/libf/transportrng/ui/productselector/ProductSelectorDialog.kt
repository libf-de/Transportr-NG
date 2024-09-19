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

package de.libf.transportrng.ui.productselector

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
import de.libf.ptek.dto.Product
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import transportr_ng.composeapp.generated.resources.Res
import transportr_ng.composeapp.generated.resources.cancel
import transportr_ng.composeapp.generated.resources.ok
import transportr_ng.composeapp.generated.resources.product_bus
import transportr_ng.composeapp.generated.resources.select_products

@Composable
fun ProductSelectorDialog(
    show: Boolean,
    selectedProducts: List<Product>?,
    onConfirmation: (Set<Product>) -> Unit,
    onDismissRequest: () -> Unit
) {
    if(show) {
        var selected by remember { mutableStateOf(selectedProducts ?: Product.ALL) }

        AlertDialog(
            icon = {
                Icon(painterResource(Res.drawable.product_bus), contentDescription = "Example Icon")
            },
            title = {
                Text(text = stringResource(Res.string.select_products))
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(Product.ALL.toList()) { product ->
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
                        onConfirmation(selected.toSet())
                    }
                ) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

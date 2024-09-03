/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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
package de.grobox.transportr.trips.search

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.select.SelectExtension
import de.grobox.transportr.R
import de.grobox.transportr.databinding.FragmentProductDialogBinding
import de.grobox.transportr.utils.TransportrUtils.getDrawableForProduct
import de.schildbach.pte.dto.Product
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.EnumSet
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
class ProductDialogFragment : DialogFragment() {
    private val viewModel: DirectionsViewModel by activityViewModel()
    private var adapter: FastItemAdapter<ProductItem>? = null
    private val okButton: Button? = null

    private var binding: FragmentProductDialogBinding? = null
    private var dialog: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = onCreateView(LayoutInflater.from(requireContext()), null, savedInstanceState)


        val mb = MaterialAlertDialogBuilder(requireContext())
        mb.setView(dialog)
        mb.setTitle(R.string.select_products)
        mb.setIcon(R.drawable.product_bus)
        mb.setNegativeButton(R.string.cancel) { dialog: DialogInterface?, which: Int ->
            getDialog()!!.cancel()
        }
        mb.setPositiveButton(R.string.ok) { dialog: DialogInterface?, which: Int ->
            val products = getProductsFromItems(
                adapter!!.selectedItems
            )
            viewModel.setProducts(products)
            getDialog()!!.cancel()
        }
        return mb.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProductDialogBinding.inflate(inflater, container, false)
        val v: View = binding!!.root

        // RecyclerView
        val productsView = binding!!.productsView
        productsView.layoutManager = LinearLayoutManager(context)
        adapter = FastItemAdapter()
        adapter!!.withSelectable(true)
        productsView.adapter = adapter
        for (product in Product.ALL) {
            adapter!!.add(ProductItem(product))
        }

        // Get view model and observe products
        //viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get<DirectionsViewModel>(DirectionsViewModel::class.java)
        if (savedInstanceState == null) {
            viewModel.products.observe(requireActivity(), ::onProductsChanged)
        } else {
            adapter!!.withSavedInstanceState(savedInstanceState)
        }

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onStart() {
        super.onStart()

        // adjust width and height to be shown properly in landscape orientation
        val dialog = getDialog()
        if (dialog != null) {
            val window = dialog.window
            if (window != null) {
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                window.setLayout(width, height)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter!!.saveInstanceState(outState)
    }

    private fun onProductsChanged(products: EnumSet<Product>) {
        var i = 0
        for (product in Product.ALL) {
            if (products.contains(product)) {
                adapter!!.select(i)
            }
            i++
        }
    }

    private fun getProductsFromItems(items: Set<ProductItem>): EnumSet<Product> {
        val products = EnumSet.noneOf(Product::class.java)
        for (item in items) {
            products.add(item.product)
        }
        return products
    }

    private fun setOkEnabled(enabled: Boolean) {
        okButton!!.isEnabled = enabled
    }

    internal inner class ProductItem(val product: Product) : AbstractItem<ProductItem?, ProductItem.ViewHolder>() {
        override fun getType(): Int {
            return product.ordinal
        }

        override fun getLayoutRes(): Int {
            return R.layout.list_item_product
        }

        override fun bindView(ui: ViewHolder, payloads: List<Any>) {
            super.bindView(ui, payloads)

            val selectExt = adapter!!.getExtension(SelectExtension::class.java)

            ui.image.setImageResource(getDrawableForProduct(product))
            ui.name.text = productToString(ui.name.context, product)
            ui.checkBox.isChecked = isSelected
            ui.layout.setOnClickListener { v: View? ->
                val position = adapter!!.getAdapterPosition(this@ProductItem)
                if(this@ProductItem.isSelected) {
                    selectExt?.deselect(position)
                } else {
                    selectExt?.select(position)
                }

                // if no products are selected, disable the ok-button
//                if (selectExt?.selectedItems?.size == 0) {
//                    setOkEnabled(false)
//                } else {
//                    setOkEnabled(true)
//                }
            }
        }

        override fun getViewHolder(view: View): ViewHolder {
            return ViewHolder(view)
        }

        internal inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val layout: ViewGroup = v as ViewGroup
            val image: ImageView = v.findViewById(R.id.productImage)
            val name: TextView = v.findViewById(R.id.productName)
            val checkBox: CheckBox = v.findViewById(R.id.productCheckBox)
        }
    }

    private fun productToString(context: Context, p: Product): String {
        return if (p == Product.HIGH_SPEED_TRAIN) context.getString(R.string.product_high_speed_train)
        else if (p == Product.REGIONAL_TRAIN) context.getString(R.string.product_regional_train)
        else if (p == Product.SUBURBAN_TRAIN) context.getString(R.string.product_suburban_train)
        else if (p == Product.SUBWAY) context.getString(R.string.product_subway)
        else if (p == Product.TRAM) context.getString(R.string.product_tram)
        else if (p == Product.BUS) context.getString(R.string.product_bus)
        else if (p == Product.FERRY) context.getString(R.string.product_ferry)
        else if (p == Product.CABLECAR) context.getString(R.string.product_cablecar)
        else if (p == Product.ON_DEMAND) context.getString(R.string.product_on_demand)
        else throw RuntimeException()
    }

    companion object {
        val TAG: String = ProductDialogFragment::class.java.simpleName
    }
}

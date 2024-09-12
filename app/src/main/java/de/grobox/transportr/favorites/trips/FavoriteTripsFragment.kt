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
package de.grobox.transportr.favorites.trips

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.data.locations.HomeLocation
import de.grobox.transportr.data.locations.WorLocation
import de.grobox.transportr.databinding.FragmentFavoritesBinding
import de.grobox.transportr.favorites.locations.HomePickerDialogFragment
import de.grobox.transportr.favorites.locations.WorkPickerDialogFragment
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.ui.LceAnimator
import de.grobox.transportr.utils.IntentUtils.findDirections
import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
abstract class FavoriteTripsFragment<VM : SavedSearchesViewModel> : TransportrFragment(), FavoriteTripListener {
    //@Inject protected ViewModelProvider.Factory viewModelFactory;
    protected abstract val viewModel: VM

    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var list: RecyclerView
    private lateinit var adapter: FavoriteTripAdapter
    private var listAlreadyUpdated = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        progressBar = binding.progressBar

        list = binding.favorites
        adapter = FavoriteTripAdapter(this)
        list.setHasFixedSize(false)
        list.setAdapter(adapter)
        list.setLayoutManager(LinearLayoutManager(getContext()))

//        viewModel.home.observe(getViewLifecycleOwner(), Observer<HomeLocation> { home: HomeLocation? -> this.onHomeLocationChanged(home) })
//        viewModel.work.observe(getViewLifecycleOwner(), Observer<WorLocation> { work: WorLocation? -> this.onWorLocationChanged(work) })
//        viewModel.favoriteTrips.observe(getViewLifecycleOwner()) { trips: List<FavoriteTripItem> -> this.onFavoriteTripsChanged(trips) }

        return binding.root
    }

    private fun onHomeLocationChanged(home: HomeLocation?) {
        val oldHome = adapter.home
        val newHome: FavoriteTripItem = FavoriteTripItem(home)
        if (oldHome == null) {
            adapter.add(newHome)
        } else {
            onSpecialLocationChanged(oldHome, newHome)
        }
    }

    private fun onWorLocationChanged(work: WorLocation?) {
        val oldWork = adapter.work
        val newWork: FavoriteTripItem = FavoriteTripItem(work)
        if (oldWork == null) {
            adapter.add(newWork)
        } else {
            onSpecialLocationChanged(oldWork, newWork)
        }
    }

    private fun onSpecialLocationChanged(oldItem: FavoriteTripItem, newItem: FavoriteTripItem) {
        val position = adapter.findItemPosition(oldItem)
        if (position == SortedList.INVALID_POSITION) return

        // animate the new location in from right to left
        list.findViewHolderForAdapterPosition(position)?.let {
            val view: View = it.itemView
            ObjectAnimator.ofFloat<View>(view, View.TRANSLATION_X, view.width.toFloat(), 0f).start()
        }

        adapter.updateItem(position, newItem)
    }

    private fun onFavoriteTripsChanged(trips: List<FavoriteTripItem>) {
        // duplicate detection does not work, so we manage list updates ourselves, no need to reload
        if (listAlreadyUpdated) {
            // be ready for the next update
            listAlreadyUpdated = false
            return
        }
        LceAnimator.showContent(progressBar, list, null)
        adapter.swap(trips)
    }

    override fun onFavoriteChanged(item: FavoriteTripItem, isFavorite: Boolean) {
        item.isFavorite = isFavorite
        val position = adapter.findItemPosition(item)
        if (position != SortedList.INVALID_POSITION) {
            adapter.updateItem(position, item)
        }
        listAlreadyUpdated = true
        viewModel!!.updateFavoriteState(item)
    }

    override fun changeHome() {
        val f: HomePickerDialogFragment = homePickerDialogFragment
        f.listener = this
        activity?.let { f.show(it.supportFragmentManager, HomePickerDialogFragment::class.java.getSimpleName()) }
    }

    protected abstract val homePickerDialogFragment: HomePickerDialogFragment

    override fun changeWork() {
        val f: WorkPickerDialogFragment = workPickerDialogFragment
        f.listener = this
        activity?.let { f.show(it.supportFragmentManager, WorkPickerDialogFragment::class.java.getSimpleName()) }
    }

    protected abstract val workPickerDialogFragment: WorkPickerDialogFragment
    override fun onFavoriteClicked(item: FavoriteTripItem) {
        when(item.type) {
            FavoriteTripType.HOME -> item.to.let {
                if(it == null) changeHome()
                else onSpecialLocationClicked(it)
            }

            FavoriteTripType.WORK -> item.to.let {
                if(it == null) changeWork()
                else onSpecialLocationClicked(it)
            }

            FavoriteTripType.TRIP -> {
                findDirections(getContext(), item.from!!, item.via, item.to!!, true, true)
            }

            else -> throw IllegalArgumentException("item.type is null")
        }
    }

    protected abstract fun onSpecialLocationClicked(location: WrapLocation)

    override fun onFavoriteDeleted(item: FavoriteTripItem) {
        adapter.remove(item)
        listAlreadyUpdated = true
        viewModel!!.removeFavoriteTrip(item)
    }
}

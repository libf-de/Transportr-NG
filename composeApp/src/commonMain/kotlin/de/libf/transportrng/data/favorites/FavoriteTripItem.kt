package de.libf.transportrng.data.favorites

import de.libf.ptek.NetworkId
import de.libf.transportrng.data.locations.HomeLocation
import de.libf.transportrng.data.locations.WorkLocation
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.searches.StoredSearch
import kotlinx.datetime.Clock

data class FavoriteTripItem private constructor(
    val type: FavoriteTripType,
    val from: WrapLocation,
    val via: WrapLocation? = null,
    val to: WrapLocation? = null,
    override val uid: Long = 0,
    override val networkId: NetworkId? = null,
    override val fromId: Long = 0,
    override val viaId: Long? = null,
    override val toId: Long = 0,
    override var count: Int = 1,
    override var lastUsed: Long = Clock.System.now().toEpochMilliseconds(),
    override var favorite: Boolean = false
) : StoredSearch(), Comparable<FavoriteTripItem> {

    constructor(
        uid: Long,
        from: WrapLocation,
        via: WrapLocation? = null,
        to: WrapLocation
    ) : this(
        type = FavoriteTripType.TRIP,
        from = from,
        via = via,
        to = to,
        uid = uid
    )

    constructor(
        storedSearch: StoredSearch,
        from: WrapLocation,
        via: WrapLocation? = null,
        to: WrapLocation
    ) : this(
        type = FavoriteTripType.TRIP,
        from = from,
        via = via,
        to = to,
        uid = storedSearch.uid,
        networkId = storedSearch.networkId,
        fromId = storedSearch.fromId,
        viaId = storedSearch.viaId,
        toId = storedSearch.toId,
        count = storedSearch.count,
        lastUsed = storedSearch.lastUsed,
        favorite = storedSearch.favorite
    )

    constructor(to: HomeLocation?) : this(
        type = FavoriteTripType.HOME,
        from = WrapLocation(WrapLocation.WrapType.GPS),
        to = to
    )

    constructor(to: WorkLocation?) : this(
        type = FavoriteTripType.WORK,
        from = WrapLocation(WrapLocation.WrapType.GPS),
        to = to
    )

    override fun compareTo(other: FavoriteTripItem): Int {
        if (this == other) return 0
        if (type == FavoriteTripType.HOME) return -1
        if (other.type == FavoriteTripType.HOME) return 1
        if (type == FavoriteTripType.WORK) return -1
        if (other.type == FavoriteTripType.WORK) return 1
        if (favorite && !other.favorite) return -1
        if (!favorite && other.favorite) return 1
        if (favorite) {
            if (count == other.count) return lastUsed.compareTo(other.lastUsed)
            return if (count > other.count) -1 else 1
        } else {
            return lastUsed.compareTo(other.lastUsed) * -1
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FavoriteTripItem) return false
        return uid == other.uid && type == other.type
    }

    fun equalsAllFields(o: Any): Boolean {
        if (o === this) return true
        if (o !is FavoriteTripItem) return false
        val other = o
        if (this.type != other.type) return false
        if (this.from != other.from) return false
        if (this.to != other.to) return false
        if (this.via != other.via) return false
        if (this.count != other.count) return false
        if (this.favorite != other.favorite) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + uid.hashCode()
        return result
    }


}

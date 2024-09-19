package de.libf.transportrng.data.searches

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.libf.ptek.NetworkId
import de.libf.transportrng.data.locations.FavoriteLocation
import kotlinx.datetime.Clock

@Entity(
    tableName = "searches",
    foreignKeys = [
        ForeignKey(
            entity = FavoriteLocation::class,
            parentColumns = ["uid"],
            childColumns = ["from_id"]
        ),
        ForeignKey(
            entity = FavoriteLocation::class,
            parentColumns = ["uid"],
            childColumns = ["via_id"]
        ),
        ForeignKey(
            entity = FavoriteLocation::class,
            parentColumns = ["uid"],
            childColumns = ["to_id"]
        )
    ],
    indices = [
        Index("networkId"),
        Index("from_id"),
        Index("via_id"),
        Index("to_id"),
        Index(value = ["from_id", "via_id", "to_id"], unique = true)
    ]
)
open class StoredSearch(
    @PrimaryKey(autoGenerate = true) open val uid: Long,
    open val networkId: NetworkId? = null,
    @ColumnInfo(name = "from_id") open val fromId: Long,
    @ColumnInfo(name = "via_id") open val viaId: Long? = null,
    @ColumnInfo(name = "to_id") open val toId: Long,
    open var count: Int,
    open var lastUsed: Long,
    open var favorite: Boolean
) {
    constructor() : this(0, null, 0, 0L, 0, 1,
        Clock.System.now().toEpochMilliseconds(), false)

    constructor(networkId: NetworkId, from: FavoriteLocation, via: FavoriteLocation?, to: FavoriteLocation) : this(
        0, networkId, from.uid, via?.uid, to.uid, 1,
        Clock.System.now().toEpochMilliseconds(), false
    )
}

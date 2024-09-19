package de.libf.transportrng.data.gps

import de.libf.ptek.dto.Point
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class iOsGpsRepository : GpsRepository {
    override fun getGpsState(): Flow<GpsState> = flow {
        emit(GpsState.DENIED)
    }

    override fun getLocationFlow(): Flow<Result<Point>> = flow {
        emit(Result.failure(Exception("not implemented")))
    }
}
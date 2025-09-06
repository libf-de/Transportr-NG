package de.libf.transportrng.data

import de.libf.ptek.dto.Trip
import de.libf.transportrng.data.locations.WrapLocation
import de.libf.transportrng.data.utils.getName
import de.libf.transportrng.data.utils.toShareString
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.memScoped
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStoreRequestAccessCompletionHandler
import platform.UIKit.UIDevice
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class iOsPlatformTool : PlatformTool {
    override fun showLocationOnMap(loc: WrapLocation) {
        val coordinate = CLLocationCoordinate2DMake(
            latitude = loc.latLng.latitude,
            longitude = loc.latLng.longitude
        )

        val mapItem = MKMapItem(
            placemark = MKPlacemark(
                coordinate = coordinate
            )
        )

        mapItem.name = loc._getName()

        mapItem.openInMapsWithLaunchOptions(launchOptions = null)
    }

    override fun shareText(text: String) {
        val activityItems = listOf(text)
        val activityViewController = UIActivityViewController(activityItems, null)

        val application = UIApplication.sharedApplication
        application.keyWindow?.rootViewController?.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    }

    override suspend fun addToCalendar(trip: Trip): String? {
        return _addToCalendar(
            title = trip.from.name + " → " + trip.to.name,
            notes = trip.toShareString(),
            startDate = trip.firstDepartureTime,
            endDate = trip.lastArrivalTime,
            location = trip.from.getName()
        )
    }

//    @OptIn(BetaInteropApi::class)
//    private suspend fun _addToCalendar(trip: Trip): String? {
//        EKEventStore()
//            .requestWriteOnlyAccess()
//            .onFailure { return it.message }
//            .onSuccess {
//                val event = EKEvent.eventWithEventStore(it)
//                event.title = trip.from.name + " → " + trip.to.name
//                event.notes = trip.toShareString()
//                event.startDate = NSDate.dateWithTimeIntervalSince1970(trip.firstDepartureTime / 1000.0)
//                event.endDate = NSDate.dateWithTimeIntervalSince1970(trip.lastArrivalTime / 1000.0)
//                event.calendar = it.defaultCalendarForNewEvents
//                trip.from.getName()?.let { event.location = it }
//
//                try {
//                    memScoped {
//                        val errPtr = alloc<ObjCObjectVar<NSError?>>()
//                        it.saveEvent(event, EKSpan.EKSpanThisEvent, errPtr.ptr)
//                        return errPtr.value?.localizedDescription
//                    }
//                } catch(e: Exception) {
//                    return e.message
//                }
//            }
//
//        return "Unknown Failure :("
//    }

    @OptIn(BetaInteropApi::class)
    private suspend fun _addToCalendar(
        title: String,
        notes: String,
        startDate: Long,
        endDate: Long,
        location: String?
    ): String? = suspendCoroutine { cont ->
        dispatch_async(dispatch_get_main_queue()) {
            val eventStore = EKEventStore()

            val callback = object : EKEventStoreRequestAccessCompletionHandler {
                override fun invoke(b: Boolean, nsError: NSError?) {
                    if(!b) { cont.resume(nsError?.localizedDescription); return }

                    val event = EKEvent.eventWithEventStore(eventStore)
                    event.title = title
                    event.notes = notes
                    event.startDate = NSDate.dateWithTimeIntervalSince1970(startDate / 1000.0)
                    event.endDate = NSDate.dateWithTimeIntervalSince1970(endDate / 1000.0)
                    event.calendar = eventStore.defaultCalendarForNewEvents
                    location?.let { event.location = it }

                    try {
//                        eventStore.saveEvent(event, EKSpan.EKSpanThisEvent, null)

                        memScoped {
                            val errPtr = alloc<ObjCObjectVar<NSError?>>()
                            eventStore.saveEvent(event, EKSpan.EKSpanThisEvent, true, errPtr.ptr)
                            cont.resume(errPtr.value?.localizedDescription)
                        }
                    } catch(e: Exception) {
                        cont.resume(e.message)
                    }
                }
            }

            if(UIDevice.currentDevice.systemVersion.split(".")[0].toInt() >= 17) {
                eventStore.requestWriteOnlyAccessToEventsWithCompletion(callback)
            } else {
                eventStore.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent, callback)
            }
        }
    }

//    private suspend fun EKEventStore.requestWriteOnlyAccess(): Result<EKEventStore> = suspendCoroutine { cont ->
//        dispatch_async(dispatch_get_main_queue()) {
//            this.requestWriteOnlyAccessToEventsWithCompletion { b, nsError ->
//                if(!b) cont.resume(Result.failure(Exception(nsError?.localizedDescription)))
//                else cont.resume(Result.success(this))
//            }
//        }
//    }
}
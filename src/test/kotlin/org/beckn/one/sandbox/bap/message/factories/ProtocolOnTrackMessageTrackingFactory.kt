package org.beckn.one.sandbox.bap.message.factories

import org.beckn.protocol.schemas.ProtocolOnTrackMessageTracking
import org.beckn.protocol.schemas.ProtocolOnTrackMessageTracking.ProtocolTrackingStatus.Active

object ProtocolOnTrackMessageTrackingFactory {

  fun create(index: Int = 1): ProtocolOnTrackMessageTracking {
    return ProtocolOnTrackMessageTracking(
      url = "www.tracking-url-$index.com", status = Active
    )
  }
}
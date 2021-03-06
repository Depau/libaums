package com.github.mjdev.libaums.usb

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.os.Build
import android.util.Log

/**
 * Created by magnusja on 21/12/16.
 */

object UsbCommunicationFactory {

    private val TAG = UsbCommunicationFactory::class.java.simpleName

    var underlyingUsbCommunication = UnderlyingUsbCommunication.DEVICE_CONNECTION_SYNC

    enum class UnderlyingUsbCommunication {
        USB_REQUEST_ASYNC,
        DEVICE_CONNECTION_SYNC
    }

    fun createUsbCommunication(deviceConnection: UsbDeviceConnection, outEndpoint: UsbEndpoint, inEndpoint: UsbEndpoint): UsbCommunication {
        val communication: UsbCommunication

        if (underlyingUsbCommunication == UnderlyingUsbCommunication.DEVICE_CONNECTION_SYNC) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                communication = JellyBeanMr2Communication(deviceConnection, outEndpoint, inEndpoint)
            } else {
                Log.i(TAG, "using workaround usb communication")
                communication = HoneyCombMr1Communication(deviceConnection, outEndpoint, inEndpoint)
            }
        } else {
            communication = UsbRequestCommunication(deviceConnection, outEndpoint, inEndpoint)
        }

        return communication
    }
}

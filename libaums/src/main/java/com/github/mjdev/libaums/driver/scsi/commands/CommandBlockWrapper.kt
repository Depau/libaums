/*
 * (C) Copyright 2014 mjahnen <jahnen@in.tum.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.github.mjdev.libaums.driver.scsi.commands

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * This class represents the command block wrapper (CBW) which is always wrapped
 * around a specific SCSI command in the SCSI transparent command set standard.
 *
 *
 * Every SCSI command shall extend this class, call the constructor
 * [.CommandBlockWrapper] with the desired
 * information. When transmitting the command, the
 * [.serialize] method has to be called!
 *
 * @author mjahnen
 */
abstract class CommandBlockWrapper
/**
 * Constructs a new command block wrapper with the given information which
 * can than easily be serialized with [.serialize].
 *
 * @param transferLength
 * The bytes which should be transferred in the following data
 * phase (Zero if no data phase).
 * @param direction
 * The direction the data shall be transferred in the data phase.
 * If there is no data phase it should be
 * [#NONE][com.github.mjdev.libaums.driver.scsi.commands.CommandBlockWrapper.Direction]
 * @param lun
 * The logical unit number the command is directed to.
 * @param cbwcbLength
 * The length in bytes of the scsi command.
 */
protected constructor(protected var dCbwDataTransferLength: Int,
                      /**
                       * Returns the direction in the data phase.
                       *
                       * @return The direction.
                       * @see com.github.mjdev.libaums.driver.scsi.commands.CommandBlockWrapper.Direction
                       * Direction
                       */
                      val direction: Direction,
                      private val bCbwLun: Byte,
                      private val bCbwcbLength: Byte) {

    private var dCbwTag: Int = 0
    private var bmCbwFlags: Byte = 0

    /**
     * The direction of the data phase of the SCSI command.
     *
     * @author mjahnen
     */
    enum class Direction {
        /**
         * Means from device to host (Android).
         */
        IN,
        /**
         * Means from host (Android) to device.
         */
        OUT,
        /**
         * There is no data phase
         */
        NONE
    }

    init {
        if (direction == Direction.IN)
            bmCbwFlags = 0x80.toByte()
    }

    /**
     * Serializes the command block wrapper for transmission.
     *
     *
     * This method should be called in every subclass right before the specific
     * SCSI command serializes itself to the buffer!
     *
     * @param buffer
     * The buffer were the serialized data should be copied to.
     */
    open fun serialize(buffer: ByteBuffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(D_CBW_SIGNATURE)
        buffer.putInt(dCbwTag)
        buffer.putInt(dCbwDataTransferLength)
        buffer.put(bmCbwFlags)
        buffer.put(bCbwLun)
        buffer.put(bCbwcbLength)
    }

    /**
     * Returns the tag which can be used to determine the corresponding
     * [ CBW][com.github.mjdev.libaums.driver.scsi.commands.CommandStatusWrapper].
     *
     * @return The command block wrapper tag.
     * @see com.github.mjdev.libaums.driver.scsi.commands.CommandStatusWrapper
     * .getdCswTag
     */
    fun getdCbwTag(): Int {
        return dCbwTag
    }

    /**
     * Sets the tag which can be used to determine the corresponding
     * [ CBW][com.github.mjdev.libaums.driver.scsi.commands.CommandStatusWrapper].
     *
     * @param dCbwTag The command block wrapper tag
     * @see com.github.mjdev.libaums.driver.scsi.commands.CommandStatusWrapper
     * .getdCswTag
     */
    fun setdCbwTag(dCbwTag: Int) {
        this.dCbwTag = dCbwTag
    }

    /**
     * Returns the amount of bytes which should be transmitted in the data
     * phase.
     *
     * @return The length in bytes.
     */
    fun getdCbwDataTransferLength(): Int {
        return dCbwDataTransferLength
    }

    companion object {

        private val D_CBW_SIGNATURE = 0x43425355
    }

}

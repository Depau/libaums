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
 * SCSI command to read from the mass storage device. The 10 means that the
 * transfer length is two byte and the logical block address field is four byte.
 * Thus the hole command takes 10 byte when serialized.
 *
 *
 * The actual data is transferred in the data phase.
 *
 * @author mjahnen
 */
class ScsiRead10 : CommandBlockWrapper {

    private var blockAddress: Int = 0
    private var transferBytes: Int = 0
    private var blockSize: Int = 0
    private var transferBlocks: Short = 0

    /**
     * Constructs a new read command without any information.
     * Be sure to call [.init] before transfering command to device.
     */
    constructor(lun: Byte) : super(0, Direction.IN, lun, LENGTH)

    /**
     * Constructs a new read command with the given information.
     *
     * @param blockAddress
     * The logical block address the read should start.
     * @param transferBytes
     * The bytes which should be transferred.
     * @param blockSize
     * The block size of the mass storage device.
     */
    constructor(blockAddress: Int, transferBytes: Int, blockSize: Int) : super(transferBytes, Direction.IN, 0.toByte(), LENGTH) {
        init(blockAddress, transferBytes, blockSize)
    }

    fun init(blockAddress: Int, transferBytes: Int, blockSize: Int) {
        super.dCbwDataTransferLength = transferBytes
        this.blockAddress = blockAddress
        this.transferBytes = transferBytes
        this.blockSize = blockSize
        val transferBlocks = (transferBytes / blockSize).toShort()
        if (transferBytes % blockSize != 0) {
            throw IllegalArgumentException("transfer bytes is not a multiple of block size")
        }
        this.transferBlocks = transferBlocks
    }

    override fun serialize(buffer: ByteBuffer) {
        super.serialize(buffer)
        buffer.order(ByteOrder.BIG_ENDIAN)
        buffer.put(OPCODE)
        buffer.put(0.toByte())
        buffer.putInt(blockAddress)
        buffer.put(0.toByte())
        buffer.putShort(transferBlocks)
    }

    override fun toString(): String {
        return ("ScsiRead10 [blockAddress=" + blockAddress + ", transferBytes=" + transferBytes
                + ", blockSize=" + blockSize + ", transferBlocks=" + transferBlocks
                + ", getdCbwDataTransferLength()=" + getdCbwDataTransferLength() + "]")
    }

    companion object {

        // private static final String TAG = ScsiRead10.class.getSimpleName();
        private val LENGTH: Byte = 10
        private val OPCODE: Byte = 0x28
    }

}

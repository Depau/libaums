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

package com.github.mjdev.libaums.fs.fat32

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * This class represents the FAT32 boot sector which is always located at the
 * beginning of every FAT32 file system. It holds important information about
 * the file system such as the cluster size and the start cluster of the root
 * directory.
 *
 * @author mjahnen
 */
/* package */internal class Fat32BootSector private constructor() {

    /**
     * Returns the number of bytes in one single sector of a FAT32 file system.
     *
     * @return Number of bytes.
     */
    /* package */ var bytesPerSector: Short = 0
        private set
    /**
     * Returns the number of sectors in one single cluster of a FAT32 file
     * system.
     *
     * @return Number of bytes.
     */
    /* package */ var sectorsPerCluster: Short = 0
        private set
    /**
     * Returns the number of reserved sectors at the beginning of the FAT32 file
     * system. This includes one sector for the boot sector.
     *
     * @return Number of sectors.
     */
    /* package */ var reservedSectors: Short = 0
        private set
    /**
     * Returns the number of the FATs in the FAT32 file system. This is mostly
     * 2.
     *
     * @return Number of FATs.
     */
    /* package */ var fatCount: Byte = 0
        private set
    /**
     * Returns the total number of sectors in the file system.
     *
     * @return Total number of sectors.
     */
    /* package */ var totalNumberOfSectors: Long = 0
        private set
    /**
     * Returns the total number of sectors in one file allocation table. The
     * FATs have a fixed size.
     *
     * @return Number of sectors in one FAT.
     */
    /* package */ var sectorsPerFat: Long = 0
        private set
    /**
     * Returns the start cluster of the root directory in the FAT32 file system.
     *
     * @return Root directory start cluster.
     */
    /* package */ var rootDirStartCluster: Long = 0
        private set
    /**
     * Returns the start sector of the file system info structure.
     *
     * @return FSInfo Structure start sector.
     */
    /* package */ var fsInfoStartSector: Short = 0
        private set
    /**
     * Returns if the different FATs in the file system are mirrored, ie. all of
     * them are holding the same data. This is used for backup purposes.
     *
     * @return True if the FAT is mirrored.
     * @see .getValidFat
     * @see .getFatCount
     */
    /* package */ var isFatMirrored: Boolean = false
        private set
    /**
     * Returns the valid FATs which shall be used if the FATs are not mirrored.
     *
     * @return Number of the valid FAT.
     * @see .isFatMirrored
     * @see .getFatCount
     */
    /* package */ var validFat: Byte = 0
        private set
    /**
     * This returns the volume label stored in the boot sector. This is mostly
     * not used and you should instead use [FatDirectory.getVolumeLabel]
     * of the root directory.
     *
     * @return The volume label.
     */
    /* package */ var volumeLabel: String? = null
        private set

    /**
     * Returns the amount in bytes in one cluster.
     *
     * @return Amount of bytes.
     */
    /* package */ val bytesPerCluster: Int
        get() = sectorsPerCluster * bytesPerSector

    /**
     * Returns the offset in bytes from the beginning of the file system of the
     * data area. The data area is the area where the contents of directories
     * and files are saved.
     *
     * @return Offset in bytes.
     */
    /* package */ val dataAreaOffset: Long
        get() = getFatOffset(0) + fatCount.toLong() * sectorsPerFat * bytesPerSector.toLong()

    /**
     * Returns the FAT offset in bytes from the beginning of the file system for
     * the given FAT number.
     *
     * @param fatNumber
     * The number of the FAT.
     * @return Offset in bytes.
     * @see .isFatMirrored
     * @see .getFatCount
     * @see .getValidFat
     */
    /* package */ fun getFatOffset(fatNumber: Int): Long {
        return bytesPerSector * (reservedSectors + fatNumber * sectorsPerFat)
    }

    override fun toString(): String {
        return "Fat32BootSector{" +
                "bytesPerSector=" + bytesPerSector +
                ", sectorsPerCluster=" + sectorsPerCluster +
                ", reservedSectors=" + reservedSectors +
                ", fatCount=" + fatCount +
                ", totalNumberOfSectors=" + totalNumberOfSectors +
                ", sectorsPerFat=" + sectorsPerFat +
                ", rootDirStartCluster=" + rootDirStartCluster +
                ", fsInfoStartSector=" + fsInfoStartSector +
                ", fatMirrored=" + isFatMirrored +
                ", validFat=" + validFat +
                ", volumeLabel='" + volumeLabel + '\''.toString() +
                '}'.toString()
    }

    companion object {
        private val BYTES_PER_SECTOR_OFF = 11
        private val SECTORS_PER_CLUSTER_OFF = 13
        private val RESERVED_COUNT_OFF = 14
        private val FAT_COUNT_OFF = 16
        private val TOTAL_SECTORS_OFF = 32
        private val SECTORS_PER_FAT_OFF = 36
        private val FLAGS_OFF = 40
        private val ROOT_DIR_CLUSTER_OFF = 44
        private val FS_INFO_SECTOR_OFF = 48
        private val VOLUME_LABEL_OFF = 48

        /**
         * Reads a FAT32 boot sector from the given buffer. The buffer has to be 512
         * (the size of a boot sector) bytes.
         *
         * @param buffer
         * The data where the boot sector is located.
         * @return A newly created boot sector.
         */
        /* package */ fun read(buffer: ByteBuffer): Fat32BootSector {
            val result = Fat32BootSector()
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            result.bytesPerSector = buffer.getShort(BYTES_PER_SECTOR_OFF)
            result.sectorsPerCluster = (buffer.get(SECTORS_PER_CLUSTER_OFF) and 0xff).toShort()
            result.reservedSectors = buffer.getShort(RESERVED_COUNT_OFF)
            result.fatCount = buffer.get(FAT_COUNT_OFF)
            result.totalNumberOfSectors = buffer.getInt(TOTAL_SECTORS_OFF) and 0xffffffffL
            result.sectorsPerFat = buffer.getInt(SECTORS_PER_FAT_OFF) and 0xffffffffL
            result.rootDirStartCluster = buffer.getInt(ROOT_DIR_CLUSTER_OFF) and 0xffffffffL
            result.fsInfoStartSector = buffer.getShort(FS_INFO_SECTOR_OFF)
            val flag = buffer.getShort(FLAGS_OFF)
            result.isFatMirrored = flag.toByte() and 0x80 == 0
            result.validFat = (flag.toByte() and 0x7).toByte()

            val builder = StringBuilder()

            for (i in 0..10) {
                val b = buffer.get(VOLUME_LABEL_OFF + i)
                if (b.toInt() == 0)
                    break
                builder.append(b.toChar())
            }

            result.volumeLabel = builder.toString()

            return result
        }
    }
}

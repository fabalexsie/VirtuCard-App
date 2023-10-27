package de.siebes.fabian.virtucard

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.cardemulation.HostApduService
import android.os.Bundle

// based on https://github.com/MichaelsPlayground/NfcHceNdefEmulator/blob/fa2ca23d78004ef4a3823010ef7838ea1597f3cb/app/src/main/java/de/androidcrypto/nfchcendefemulator/MyHostApduService.java
class NdefHostApduService : HostApduService() {
    // Variable for NDEF record file.
    private lateinit var mNdefRecordFile: ByteArray

    // Flag to determine if the app is selected or not.
    private var mAppSelected = false

    // Flag to determine if the CC file is selected or not.
    private var mCcSelected = false

    // Flag to determine if the NDEF record file is selected or not.
    private var mNdefSelected = false
    override fun onCreate() {
        super.onCreate()

        // Clearing the state.
        mAppSelected = false
        mCcSelected = false
        mNdefSelected = false

        // default url to share
        // the maximum length is 246 so do not extend this value
        val ndefDefaultMessage = getNdefUrlMessage("https://github.com/fabalexsie/")
        val nlen = ndefDefaultMessage!!.byteArrayLength
        mNdefRecordFile = ByteArray(nlen + 2)
        mNdefRecordFile[0] = ((nlen and 0xff00) / 256).toByte()
        mNdefRecordFile[1] = (nlen and 0xff).toByte()
        System.arraycopy(
            ndefDefaultMessage.toByteArray(),
            0,
            mNdefRecordFile,
            2,
            ndefDefaultMessage.byteArrayLength
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            // intent contains an URL
            if (intent.hasExtra("ndefUrl")) {
                val ndefMessage: NdefMessage? = getNdefUrlMessage(intent.getStringExtra("ndefUrl"))
                if (ndefMessage != null) {
                    val nlen = ndefMessage.byteArrayLength
                    mNdefRecordFile = ByteArray(nlen + 2)
                    mNdefRecordFile[0] = ((nlen and 0xff00) / 256).toByte()
                    mNdefRecordFile[1] = (nlen and 0xff).toByte()
                    System.arraycopy(
                        ndefMessage.toByteArray(),
                        0,
                        mNdefRecordFile,
                        2,
                        ndefMessage.byteArrayLength
                    )
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getNdefUrlMessage(ndefData: String?): NdefMessage? {
        if(ndefData == null) return null
        if (ndefData.isEmpty()) {
            return null
        }
        val ndefRecord: NdefRecord = NdefRecord.createUri(ndefData)
        return NdefMessage(ndefRecord)
    }

    /**
     * Perform operations behaving as NFC Forum Tag Type 4.
     * Receive C-APDU and return the corresponding R-APDU.
     */
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle): ByteArray {
        if (SELECT_APP.contentEquals(commandApdu)) {
            // Selecting the application.
            mAppSelected = true
            mCcSelected = false
            mNdefSelected = false
            return SUCCESS_SW // Success
        } else if (mAppSelected && SELECT_CC_FILE.contentEquals(commandApdu)) {
            // Selecting the CC file.
            mCcSelected = true
            mNdefSelected = false
            return SUCCESS_SW // Success
        } else if (mAppSelected && SELECT_NDEF_FILE.contentEquals(commandApdu)) {
            // Selecting the NDEF file.
            mCcSelected = false
            mNdefSelected = true
            return SUCCESS_SW // Success.
        } else if (commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xb0.toByte()) {
            // READ_BINARY (File Read)

            // Retrieve the offset and length.
            val offset =
                (0x00ff and commandApdu[2].toInt()) * 256 + (0x00ff and commandApdu[3].toInt())
            val le = 0x00ff and commandApdu[4].toInt()

            // Generate a buffer for R-APDU.
            val responseApdu = ByteArray(le + SUCCESS_SW.size)
            if (mCcSelected && offset == 0 && le == CC_FILE.size) {
                // When selecting CC, the offset must be 0, and the length must match the file length (15).
                System.arraycopy(CC_FILE, offset, responseApdu, 0, le)
                System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.size)
                return responseApdu
            } else if (mNdefSelected) {
                if (offset + le <= mNdefRecordFile.size) {
                    System.arraycopy(mNdefRecordFile, offset, responseApdu, 0, le)
                    System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.size)
                    return responseApdu
                }
            }
        }

        // Return an error.
        // Normally, in a smart card application, you should change the error value according to the error type,
        // but here, we are omitting that and returning only one type of error.
        return FAILURE_SW
    }

    /*
complete sequence:
commandApdu: 00a4040007d276000085010100
responseApdu: 9000
commandApdu: 00a4000c02e103
responseApdu: 9000
commandApdu: 00b000000f
responseApdu: 000f20003b00340406e10400ff00ff9000
commandApdu: 00a4000c02e104
responseApdu: 9000
commandApdu: 00b0000002
responseApdu: 002e9000
commandApdu: 00b000022e
responseApdu: d1012a55046769746875622e636f6d2f416e64726f696443727970746f3f7461623d7265706f7369746f726965739000
 */

    /**
     * Called when the card application is in a deselected state.
     * In this application, it resets the state to return to the initial state.
     */
    override fun onDeactivated(reason: Int) {
        mAppSelected = false
        mCcSelected = false
        mNdefSelected = false
    }

    companion object {
        // C-APDU for application selection.
        private val SELECT_APP = byteArrayOf(
            0x00.toByte(),
            0xa4.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0x07.toByte(),
            0xd2.toByte(),
            0x76.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x85.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x00.toByte()
        )

        // C-APDU for CC file selection.
        private val SELECT_CC_FILE = byteArrayOf(
            0x00.toByte(),
            0xa4.toByte(),
            0x00.toByte(),
            0x0c.toByte(),
            0x02.toByte(),
            0xe1.toByte(),
            0x03.toByte()
        )

        // C-APDU for NDEF record file selection.
        private val SELECT_NDEF_FILE = byteArrayOf(
            0x00.toByte(),
            0xa4.toByte(),
            0x00.toByte(),
            0x0c.toByte(),
            0x02.toByte(),
            0xe1.toByte(),
            0x04.toByte()
        )

        // Status Word for success (used in the response).
        private val SUCCESS_SW = byteArrayOf(0x90.toByte(), 0x00.toByte())

        // Status Word for failure (used in the response).
        private val FAILURE_SW = byteArrayOf(0x6a.toByte(), 0x82.toByte())

        // Data of the CC file.
        private val CC_FILE = byteArrayOf(
            0x00, 0x0f,  // CCLEN
            0x20,  // Mapping Version
            0x00, 0x3b,  // Maximum R-APDU data size
            0x00, 0x34,  // Maximum C-APDU data size
            0x04, 0x06, 0xe1.toByte(), 0x04, 0x00.toByte(), 0xff.toByte(),  // Maximum NDEF size
            0x00, 0xff.toByte()
        )
    }
}
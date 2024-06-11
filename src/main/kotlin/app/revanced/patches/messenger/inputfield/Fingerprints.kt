package app.revanced.patches.messenger.inputfield

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.dexbacked.value.DexBackedStringEncodedValue

internal val sendTypingIndicatorFingerprint = methodFingerprint {
    returns("V")
    parameters()
    custom { methodDef, classDef ->
        methodDef.name == "run" && classDef.fields.any {
            it.name == "__redex_internal_original_name" &&
                (it.initialValue as? DexBackedStringEncodedValue)?.value == "ConversationTypingContext\$sendActiveStateRunnable\$1"
        }
    }
}

internal val switchMessangeInputEmojiButtonFingerprint = methodFingerprint {
    returns("V")
    parameters("L", "Z")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.CONST_STRING,
        Opcode.GOTO,
        Opcode.CONST_STRING,
        Opcode.GOTO,
    )
    strings("afterTextChanged", "expression_search")
}

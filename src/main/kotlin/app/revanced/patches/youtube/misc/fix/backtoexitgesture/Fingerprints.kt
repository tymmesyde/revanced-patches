package app.revanced.patches.youtube.misc.fix.backtoexitgesture

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val onBackPressedFingerprint = methodFingerprint {
    returns("V")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    opcodes(Opcode.RETURN_VOID)
    custom { method, classDef ->
        method.name == "onBackPressed" &&
            // Old versions of YouTube called this class "WatchWhileActivity" instead.
            (classDef.endsWith("MainActivity;") || classDef.endsWith("WatchWhileActivity;"))
    }
}

internal val recyclerViewScrollingFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_LEZ,
        Opcode.IGET_OBJECT,
        Opcode.CONST_4,
    )
}

internal val recyclerViewTopScrollingFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters()
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.GOTO,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
    )
}

internal val recyclerViewTopScrollingParentFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L", "L", "Landroid/view/ViewGroup;", "Landroid/view/ViewGroup;")
    opcodes(
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL,
        Opcode.NEW_INSTANCE,
    )
}

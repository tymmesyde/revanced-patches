package app.revanced.patches.twitch.chat.antidelete

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.twitch.misc.settings.PreferenceScreen
import app.revanced.patches.twitch.misc.settings.settingsPatch
import app.revanced.patches.youtube.misc.integrations.integrationsPatch

@Suppress("unused")
val showDeletedMessagesPatch = bytecodePatch(
    name = "Show deleted messages",
    description = "Shows deleted chat messages behind a clickable spoiler.",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith("tv.twitch.android.app"("15.4.1", "16.1.0", "16.9.1"))

    fun createSpoilerConditionInstructions(register: String = "v0") = """
        invoke-static {}, Lapp/revanced/integrations/twitch/patches/ShowDeletedMessagesPatch;->shouldUseSpoiler()Z
        move-result $register
        if-eqz $register, :no_spoiler
    """

    val setHasModAccessResult by setHasModAccessFingerprint
    val deletedMessageClickableSpanCtorResult by deletedMessageClickableSpanCtorFingerprint
    val chatUtilCreateDeletedSpanResult by chatUtilCreateDeletedSpanFingerprint

    execute {
        addResources("twitch", "chat.antidelete.showDeletedMessagesPatch")

        PreferenceScreen.CHAT.GENERAL.addPreferences(
            ListPreference(
                key = "revanced_show_deleted_messages",
                summaryKey = null,
            ),
        )

        // Spoiler mode: Force set hasModAccess member to true in constructor
        deletedMessageClickableSpanCtorResult.mutableMethod.apply {
            addInstructionsWithLabels(
                implementation!!.instructions.lastIndex, /* place in front of return-void */
                """
                    ${createSpoilerConditionInstructions()}
                    const/4 v0, 1
                    iput-boolean v0, p0, $definingClass->hasModAccess:Z
                """,
                ExternalLabel("no_spoiler", getInstruction(implementation!!.instructions.lastIndex)),
            )
        }

        // Spoiler mode: Disable setHasModAccess setter
        setHasModAccessResult.mutableMethod.addInstruction(0, "return-void")

        // Cross-out mode: Reformat span of deleted message
        chatUtilCreateDeletedSpanResult.mutableMethod.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static {p2}, Lapp/revanced/integrations/twitch/patches/ShowDeletedMessagesPatch;->reformatDeletedMessage(Landroid/text/Spanned;)Landroid/text/Spanned;
                    move-result-object v0
                    if-eqz v0, :no_reformat
                    return-object v0
                """,
                ExternalLabel("no_reformat", getInstruction(0)),
            )
        }
    }
}

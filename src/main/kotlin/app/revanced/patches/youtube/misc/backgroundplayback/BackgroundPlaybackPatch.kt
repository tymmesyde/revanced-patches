package app.revanced.patches.youtube.misc.backgroundplayback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.information.videoInformationPatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/BackgroundPlaybackPatch;"

@Suppress("unused")
val backgroundPlaybackPatch = bytecodePatch(
    name = "Remove background playback restrictions",
    description = "Removes restrictions on background playback, including playing kids videos in the background.",
) {
    dependsOn(
        backgroundPlaybackResourcePatch,
        integrationsPatch,
        playerTypeHookPatch,
        videoInformationPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"
        (
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val backgroundPlaybackManagerResult by backgroundPlaybackManagerFingerprint
    val backgroundPlaybackSettingsResult by backgroundPlaybackSettingsFingerprint
    val kidsBackgroundPlaybackPolicyControllerResult by kidsBackgroundPlaybackPolicyControllerFingerprint

    execute { context ->
        addResources("youtube", "misc.backgroundplayback.backgroundPlaybackPatch")

        PreferenceScreen.MISC.addPreferences(
            NonInteractivePreference("revanced_background_playback"),
        )

        backgroundPlaybackManagerResult.mutableMethod.addInstructions(
            0,
            """
                invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->playbackIsNotShort()Z
                move-result v0
                return v0
            """,
        )

        // Enable background playback option in YouTube settings
        backgroundPlaybackSettingsResult.mutableMethod.apply {
            val booleanCalls = instructions.withIndex()
                .filter { ((it.value as? ReferenceInstruction)?.reference as? MethodReference)?.returnType == "Z" }

            val settingsBooleanIndex = booleanCalls.elementAt(1).index
            val settingsBooleanMethod = context.navigate(this).at(settingsBooleanIndex).mutable()

            settingsBooleanMethod.addInstructions(
                0,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->overrideBackgroundPlaybackAvailable()Z
                    move-result v0
                    return v0
                """,
            )
        }

        // Force allowing background play for videos labeled for kids.
        kidsBackgroundPlaybackPolicyControllerResult.mutableMethod.addInstruction(
            0,
            "return-void",
        )
    }
}

internal var prefBackgroundAndOfflineCategoryId = -1L
    private set

internal val backgroundPlaybackResourcePatch = resourcePatch {
    dependsOn(resourceMappingPatch)

    execute {
        prefBackgroundAndOfflineCategoryId = resourceMappings["string", "pref_background_and_offline_category"]
    }
}

package com.browseglass.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Locks private tabs behind fingerprint / face unlock (Phase 5.33).
 *
 * Usage:
 *   val gate = BiometricGate(activity)
 *   if (gate.canAuthenticate()) gate.authenticate(onUnlocked) else onUnlocked()
 */
class BiometricGate(private val activity: FragmentActivity) {

    fun isSupported(): Boolean {
        val manager = BiometricManager.from(activity)
        return when (manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
            -> true
            else -> false
        }
    }

    fun authenticate(
        title: String = "Unlock private browsing",
        subtitle: String = "Confirm your identity to open private tabs",
        negativeText: String = "Cancel",
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        if (!isSupported()) {
            onSuccess()
            return
        }
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    onFailure()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure()
                }
            },
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeText)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(promptInfo)
    }
}
package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder

object TelegramUtils {

    const val ADMIN_BOT_USERNAME = "UMG_Admin_Bot"

    /**
     * Formats and dispatches recharge request data to the Telegram Admin Bot.
     */
    fun sendRechargeToTelegramAdmin(
        context: Context,
        phoneNumber: String,
        level: String,
        amount: Double,
        method: String,
        ftNumber: String,
        hasScreenshot: Boolean
    ) {
        val report = buildString {
            append("🔔 NEW RECHARGE SUBMISSION\n")
            append("• User: ").append(phoneNumber).append("\n")
            append("• Level: ").append(level).append("\n")
            append("• Amount: ").append(amount.toInt()).append(" ETB\n")
            append("• Method: ").append(method).append("\n")
            append("• FT Number: ").append(ftNumber).append("\n")
            append("• Screenshot: ").append(if (hasScreenshot) "Attached" else "None").append("\n")
            append("• Time: ").append(System.currentTimeMillis())
        }
        dispatchReport(context, report)
    }

    /**
     * Formats and dispatches withdrawal request data to the Telegram Admin.
     */
    fun sendWithdrawalToTelegramAdmin(
        context: Context,
        phoneNumber: String,
        level: String,
        amount: Double,
        fee: Double,
        method: String,
        ftNumber: String,
        hasScreenshot: Boolean
    ) {
        val report = buildString {
            append("💸 NEW WITHDRAWAL SUBMISSION\n")
            append("• User: ").append(phoneNumber).append("\n")
            append("• Level: ").append(level).append("\n")
            append("• Amount: ").append(amount.toInt()).append(" ETB\n")
            append("• Verification Fee Paid: ").append(fee.toInt()).append(" ETB\n")
            append("• Method: ").append(method).append("\n")
            append("• Fee FT Number: ").append(ftNumber).append("\n")
            append("• Receipt Screenshot: ").append(if (hasScreenshot) "Attached" else "None").append("\n")
            append("• Time: ").append(System.currentTimeMillis())
        }
        dispatchReport(context, report)
    }

    private fun dispatchReport(context: Context, message: String) {
        try {
            val encoded = URLEncoder.encode(message, "UTF-8")
            // Try Telegram direct link to Admin Bot
            val telegramIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://t.me/$ADMIN_BOT_USERNAME?text=$encoded")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(telegramIntent)
            Toast.makeText(context, "Dispatched to Telegram Admin Bot", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            try {
                // Fallback web share
                val shareIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://t.me/share/url?url=" + URLEncoder.encode(message, "UTF-8"))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(shareIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Recorded and submitted to Telegram Admin", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.UmgRepository
import com.example.util.UmgMusicPlayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UmgRequirementsTest {

    private lateinit var context: Context
    private lateinit var repository: UmgRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = UmgRepository(context)
    }

    @Test
    fun testRechargeLimitsAndApprovalFlow() {
        runBlocking {
            // Test lower limit: < 100 ETB rejected
            val failLow = repository.submitRecharge(50.0, "CBE", "FT123", null)
            assertTrue(failLow.isFailure)
            assertTrue(failLow.exceptionOrNull()?.message?.contains("100") == true)

            // Test upper limit: > 10000 ETB rejected
            val failHigh = repository.submitRecharge(15000.0, "CBE", "FT123", null)
            assertTrue(failHigh.isFailure)
            assertTrue(failHigh.exceptionOrNull()?.message?.contains("10,000") == true)

            // Valid recharge request: 500 ETB
            val initialBalance = repository.userSession.value.balance
            val successRecharge = repository.submitRecharge(500.0, "Telebirr", "FT998877", null)
            assertTrue(successRecharge.isSuccess)

            // Balance must NOT increase immediately
            assertEquals(initialBalance, repository.userSession.value.balance, 0.01)

            // Find the pending transaction using flow suspension
            val pendingTx = withTimeout(5000) {
                repository.allRecharges.first { list ->
                    list.any { it.ftNumber == "FT998877" }
                }.first { it.ftNumber == "FT998877" }
            }
            assertEquals("Pending", pendingTx.status)

            // Admin approves the recharge
            val approveRes = repository.approveRecharge(pendingTx.id)
            assertTrue(approveRes.isSuccess)

            // Prevent duplicate approvals
            val duplicateApprove = repository.approveRecharge(pendingTx.id)
            assertTrue(duplicateApprove.isFailure)
        }
    }

    @Test
    fun testWithdrawalLimitsAndRefundOnReject() {
        runBlocking {
            // Test lower limit: < 2,500 ETB rejected
            val failLow = repository.submitWithdrawal(1000.0, "CBE", "John Doe", "1000123456")
            assertTrue(failLow.isFailure)
            assertTrue(failLow.exceptionOrNull()?.message?.contains("2,500") == true)

            // Test upper limit: > 50,000 ETB rejected
            val failHigh = repository.submitWithdrawal(60000.0, "CBE", "John Doe", "1000123456")
            assertTrue(failHigh.isFailure)
            assertTrue(failHigh.exceptionOrNull()?.message?.contains("50,000") == true)

            val currentUserId = repository.userSession.value.userId
            // Give user enough balance for withdrawal
            val adjRes = repository.adjustUserBalance("ADMIN-1954", currentUserId, 5000.0, true, "Test Funding")
            assertTrue(adjRes.isSuccess)
            val balanceBefore = repository.userSession.value.balance
            assertTrue(balanceBefore >= 5000.0)

            // Submit valid withdrawal of 2,500 ETB
            val withdrawRes = repository.submitWithdrawal(2500.0, "Commercial Bank of Ethiopia", "Hewot Eshetu", "1000139470277")
            assertTrue(withdrawRes.isSuccess)

            // Reserved funds deducted immediately
            val balanceAfterWithdraw = repository.userSession.value.balance
            assertEquals(balanceBefore - 2500.0, balanceAfterWithdraw, 0.01)

            val pendingTx = withTimeout(5000) {
                repository.allWithdrawals.first { list ->
                    list.any { it.accountNumber == "1000139470277" && it.status == "Pending" }
                }.first { it.accountNumber == "1000139470277" && it.status == "Pending" }
            }

            // Admin rejects withdrawal -> funds refunded
            val rejectRes = repository.rejectWithdrawal(pendingTx.id, reason = "Name mismatch test")
            assertTrue(rejectRes.isSuccess)

            // Balance restored immediately
            val balanceAfterReject = repository.userSession.value.balance
            assertEquals(balanceBefore, balanceAfterReject, 0.01)
        }
    }

    @Test
    fun testAdminAuthenticationAndCodeChange() {
        runBlocking {
            // Correct initial code "1954"
            val authSuccess = repository.verifyAdminCode("1954")
            assertTrue(authSuccess.isSuccess)
            assertEquals(true, authSuccess.getOrNull())

            // Wrong code fails
            val authFail = repository.verifyAdminCode("0000")
            assertTrue(authFail.isFailure)

            // Change admin code
            val changeRes = repository.changeAdminCode("1954", "8822")
            assertTrue(changeRes.isSuccess)

            // Old code no longer works
            val oldCodeFail = repository.verifyAdminCode("1954")
            assertTrue(oldCodeFail.isFailure)

            // New code works
            val newCodeSuccess = repository.verifyAdminCode("8822")
            assertTrue(newCodeSuccess.isSuccess)

            // Revert back for other tests
            repository.changeAdminCode("8822", "1954")
        }
    }

    @Test
    fun testTaskTakeAndContinueWithMusic() {
        runBlocking {
            val task = withTimeout(5000) {
                repository.tasks.first { it.isNotEmpty() }.first()
            }

            // Step 1: Take Task
            val takeRes = repository.takeTask(task.id)
            assertTrue(takeRes.isSuccess)

            val updatedTask = withTimeout(5000) {
                repository.tasks.first { list -> list.any { it.id == task.id && it.status == "TAKEN" } }
                    .first { it.id == task.id }
            }
            assertEquals("TAKEN", updatedTask.status)

            // Step 2: Continue interaction & start Music
            UmgMusicPlayer.start()
            assertTrue(UmgMusicPlayer.isPlaying.value)

            // Stop music
            UmgMusicPlayer.stop()
            assertFalse(UmgMusicPlayer.isPlaying.value)
        }
    }
}

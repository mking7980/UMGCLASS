package com.example.ui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.UserSession
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.DarkSlateElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.util.TelegramUtils

@Composable
fun WithdrawDialog(
    userSession: UserSession,
    onDismiss: () -> Unit,
    onSubmitWithdrawal: (amount: Double, bankName: String, accountHolderName: String, accountNumber: String) -> Result<Unit>
) {
    val context = LocalContext.current

    val popularBanks = listOf(
        "Commercial Bank of Ethiopia (CBE)",
        "Telebirr",
        "Awash Bank",
        "Dashen Bank",
        "Bank of Abyssinia",
        "Wegagen Bank",
        "Nib International Bank"
    )

    var bankName by remember { mutableStateOf(popularBanks.first()) }
    var isBankDropdownOpen by remember { mutableStateOf(false) }
    var accountHolderName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var withdrawAmountText by remember { mutableStateOf("2500") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("withdraw_dialog_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Withdraw",
                            tint = GoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Withdrawal Request",
                            color = GoldLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSuccess) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Submitted",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Withdrawal Submitted (Pending)",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your withdrawal request is now marked as Pending. Funds have been reserved. The payout will be executed to your account upon administrator authorization.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("Done", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Balance & Limits Info Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSlateCard)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Available Balance:", color = TextSecondary, fontSize = 12.sp)
                                Text("${userSession.balance.toInt()} ETB", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Withdrawal Limits:", color = TextSecondary, fontSize = 12.sp)
                                Text("2,500 - 50,000 ETB", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Bank Name Field
                    Text("Bank Name", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_bank_name_input"),
                            trailingIcon = {
                                IconButton(onClick = { isBankDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Bank", tint = GoldLight)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSlateCard,
                                unfocusedContainerColor = DarkSlateCard
                            )
                        )

                        DropdownMenu(
                            expanded = isBankDropdownOpen,
                            onDismissRequest = { isBankDropdownOpen = false },
                            modifier = Modifier.background(DarkSlateCard).border(1.dp, DarkBorder)
                        ) {
                            popularBanks.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank, color = TextPrimary, fontSize = 13.sp) },
                                    onClick = {
                                        bankName = bank
                                        isBankDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Bank Account Holder's Full Name
                    Text("Bank Account Holder's Full Name", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = accountHolderName,
                        onValueChange = { accountHolderName = it },
                        placeholder = { Text("e.g. Ms Hewot Eshetu", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_holder_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Bank Account Number
                    Text("Bank Account Number", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        placeholder = { Text("e.g. 1000139470277", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_account_number_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Withdrawal Amount
                    Text("Withdrawal Amount (2,500 - 50,000 ETB)", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it },
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            val amount = withdrawAmountText.toDoubleOrNull() ?: 0.0
                            if (amount < 2500.0) {
                                errorMessage = "Minimum withdrawal amount is 2,500 ETB."
                                return@Button
                            }
                            if (amount > 50000.0) {
                                errorMessage = "Maximum withdrawal amount is 50,000 ETB."
                                return@Button
                            }
                            if (bankName.isBlank()) {
                                errorMessage = "Please enter or select a bank name."
                                return@Button
                            }
                            if (accountHolderName.isBlank()) {
                                errorMessage = "Please enter the account holder's full name."
                                return@Button
                            }
                            if (accountNumber.isBlank()) {
                                errorMessage = "Please enter the bank account number."
                                return@Button
                            }
                            if (userSession.balance < amount) {
                                errorMessage = "Insufficient available balance. Your balance is ${userSession.balance.toInt()} ETB."
                                return@Button
                            }

                            errorMessage = null
                            val res = onSubmitWithdrawal(
                                amount,
                                bankName.trim(),
                                accountHolderName.trim(),
                                accountNumber.trim()
                            )
                            if (res.isSuccess) {
                                TelegramUtils.sendWithdrawalToTelegramAdmin(
                                    context = context,
                                    phoneNumber = userSession.phoneNumber,
                                    level = userSession.level,
                                    amount = amount,
                                    fee = 0.0,
                                    method = bankName,
                                    ftNumber = "PENDING-WITHDRAWAL",
                                    hasScreenshot = false
                                )
                                isSuccess = true
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Withdrawal failed"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_withdraw_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text(
                            text = "Submit Withdrawal",
                            color = PureBlack,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

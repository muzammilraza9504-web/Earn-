package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayoutMethod
import com.example.data.model.RedeemPackage
import com.example.data.model.UserWalletEntity
import com.example.data.model.getPackagesForMethod
import com.example.ui.theme.AmazonOrange
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.FlipkartBlue
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PayPalBlue
import com.example.ui.theme.PlayStoreBlue
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.PrimaryPurpleLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UpiOrange

@Composable
fun RedeemScreen(
    wallet: UserWalletEntity,
    selectedMethod: PayoutMethod,
    selectedPackage: RedeemPackage,
    targetAddress: String,
    userName: String,
    errorMessage: String?,
    isRedeeming: Boolean,
    onMethodSelected: (PayoutMethod) -> Unit,
    onPackageSelected: (RedeemPackage) -> Unit,
    onTargetAddressChanged: (String) -> Unit,
    onUserNameChanged: (String) -> Unit,
    onSubmitRedeem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val packages = getPackagesForMethod(selectedMethod)
    val methodColor = when (selectedMethod) {
        PayoutMethod.UPI -> UpiOrange
        PayoutMethod.FLIPKART -> FlipkartBlue
        PayoutMethod.AMAZON -> AmazonOrange
        PayoutMethod.PLAY_STORE_CODE -> PlayStoreBlue
        PayoutMethod.PAYPAL -> PayPalBlue
    }

    val methodBgColor = when (selectedMethod) {
        PayoutMethod.UPI -> Color(0xFF26180B)
        PayoutMethod.FLIPKART -> Color(0xFF0C1B38)
        PayoutMethod.AMAZON -> Color(0xFF2B1A0A)
        PayoutMethod.PLAY_STORE_CODE -> Color(0xFF0C2236)
        PayoutMethod.PAYPAL -> Color(0xFF0A1C33)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .testTag("redeem_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF192A40), Color(0xFF133E33))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Redeem & Payouts",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Min Payout: 10,000 Coins (${selectedMethod.currencySymbol}${selectedPackage.amountInRupees})",
                                    fontSize = 12.sp,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color(0x33000000), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${wallet.coins} Coins",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldGreenLight
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "Select Payment Gateway",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )

                // Horizontal scrollable Payout Method Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("payout_method_tabs"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PayoutMethod.entries.forEach { method ->
                        val isSelected = selectedMethod == method
                        val chipColor = when (method) {
                            PayoutMethod.UPI -> UpiOrange
                            PayoutMethod.FLIPKART -> FlipkartBlue
                            PayoutMethod.AMAZON -> AmazonOrange
                            PayoutMethod.PLAY_STORE_CODE -> PlayStoreBlue
                            PayoutMethod.PAYPAL -> PayPalBlue
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) chipColor.copy(alpha = 0.2f) else DarkSurfaceElevated,
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) chipColor else Color(0xFF282D42),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onMethodSelected(method) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = method.iconEmoji,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = method.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Selected Method Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = methodBgColor),
                border = BorderStroke(1.dp, methodColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedMethod.iconEmoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = selectedMethod.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedMethod.description,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Select ${selectedMethod.shortName} Package",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Package selection grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                packages.chunked(2).forEach { rowPackages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPackages.forEach { pkg ->
                            val isSelected = pkg.coins == selectedPackage.coins
                            val isUnlocked = wallet.coins >= pkg.coins

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onPackageSelected(pkg) }
                                    .testTag("package_${pkg.coins}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) methodBgColor else DarkSurfaceElevated
                                ),
                                border = if (isSelected) {
                                    BorderStroke(2.dp, methodColor)
                                } else {
                                    BorderStroke(1.dp, Color(0xFF282D42))
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (pkg.isPopular) {
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldGreen, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "POPULAR",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    Text(
                                        text = "${selectedMethod.currencySymbol}${pkg.amountInRupees}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = methodColor
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${String.format("%,d", pkg.coins)} Coins",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (isUnlocked) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Unlocked",
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Ready to Withdraw",
                                                fontSize = 10.sp,
                                                color = EmeraldGreenLight,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Need ${pkg.coins - wallet.coins} more",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Target address input form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Enter ${selectedMethod.displayName} Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name Input
                    OutlinedTextField(
                        value = userName,
                        onValueChange = onUserNameChanged,
                        label = { Text("Your Full Name") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name",
                                tint = TextSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("redeem_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurpleLight,
                            unfocusedBorderColor = Color(0xFF333A52),
                            focusedLabelColor = PrimaryPurpleLight,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Payout Address Input (UPI ID or Email)
                    OutlinedTextField(
                        value = targetAddress,
                        onValueChange = onTargetAddressChanged,
                        label = {
                            Text(selectedMethod.addressLabel)
                        },
                        placeholder = {
                            Text(selectedMethod.placeholder)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (selectedMethod) {
                                    PayoutMethod.UPI -> Icons.Default.PhoneAndroid
                                    PayoutMethod.FLIPKART -> Icons.Default.ShoppingCart
                                    PayoutMethod.AMAZON -> Icons.Default.ShoppingBag
                                    PayoutMethod.PLAY_STORE_CODE -> Icons.Default.Email
                                    PayoutMethod.PAYPAL -> Icons.Default.Language
                                },
                                contentDescription = "Address",
                                tint = methodColor
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (selectedMethod == PayoutMethod.UPI) KeyboardType.Text else KeyboardType.Email
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("redeem_address_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = methodColor,
                            unfocusedBorderColor = Color(0xFF333A52),
                            focusedLabelColor = methodColor,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = errorMessage,
                                fontSize = 12.sp,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Redeem Button
                    Button(
                        onClick = onSubmitRedeem,
                        enabled = !isRedeeming,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_redeem_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = methodColor,
                            disabledContainerColor = Color(0xFF2E334D)
                        )
                    ) {
                        if (isRedeeming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Processing Payout...")
                        } else {
                            Text(
                                text = "WITHDRAW ${selectedMethod.currencySymbol}${selectedPackage.amountInRupees} (${String.format("%,d", selectedPackage.coins)} Coins)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Trust & Security Notice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Safe",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "100% Secure & Genuine Payouts",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Instant UPI: Transferred directly to your bank account via NPCI rails.\n• Flipkart & Amazon: Digital Gift Card voucher codes delivered to your email.\n• Google Play: Instant redeem codes sent directly to your registered email.\n• PayPal: Direct USD/INR international transfers within 2 to 24 hours.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

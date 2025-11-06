package com.chronoplan.ui.akun

// --- IMPORTS ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronoplan.R
import com.chronoplan.di.AppViewModelFactory
import com.chronoplan.ui.components.AuraAnimatedAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AkunScreen(
    modifier: Modifier = Modifier,
    onLogoutSuccess: () -> Unit = {}
) {
    // PASTIKAN kita pakai factory agar ViewModel dibuat dengan dependency
    val viewModel: AkunViewModel = viewModel(factory = AppViewModelFactory())
    val uiState by viewModel.uiState.collectAsState()

    // logout callback
    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogoutSuccess()
    }

    // WARNA
    val colorBlueSoft = Color(0xFFD9EAFD)
    val colorGraySoft = Color(0xFF6A786A)
    val colorRedMaroon = Color(0xFF8B0000)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F5FF))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_border),
                        contentDescription = null,
                        modifier = Modifier
                            .width(200.dp)
                            .height(150.dp)
                            .align(Alignment.TopEnd),
                        contentScale = ContentScale.FillBounds
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 70.dp)
                            .size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AuraAnimatedAvatar()
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profil User",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(8.dp),
                            tint = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileInfoCard(
                        username = uiState.username,
                        level = uiState.level,
                        modifier = Modifier.weight(1f),
                        colorBlueSoft = colorBlueSoft,
                        colorGraySoft = colorGraySoft
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { /* TODO: Edit Profil */ },
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorBlueSoft),
                        modifier = Modifier
                            .weight(1f)
                            .height(63.dp)
                    ) {
                        Text(
                            text = "Edit Profil",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Achievement",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(160.dp),
                    colors = CardDefaults.cardColors(containerColor = colorBlueSoft),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AchievementGridPlaceholder()
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .background(colorBlueSoft, RoundedCornerShape(15.dp))
                        .height(46.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = uiState.isCalendarSynced,
                            onCheckedChange = { viewModel.toggleCalendarSync(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF428F9C),
                                uncheckedThumbColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sinkronkan Kalender",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF428F9C)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Button(
                    onClick = { viewModel.logout() },
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(46.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorRedMaroon,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            text = "Keluar Akun",
                            color = colorRedMaroon,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

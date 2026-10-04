package now.abfahrt.transit.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import now.abfahrt.transit.R

@Composable
fun OnboardingScreen(
    initialApiKey: String = "",
    initialOrsApiKey: String = "",
    initialApiKeyRejected: Boolean = false,
    onContinue: (String, String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var apiKey by remember(initialApiKey) { mutableStateOf(initialApiKey) }
    var orsApiKey by remember(initialOrsApiKey) { mutableStateOf(initialOrsApiKey) }
    var apiKeyRejected by remember(initialApiKeyRejected) { mutableStateOf(initialApiKeyRejected) }
    var apiVisible by remember { mutableStateOf(false) }
    var orsVisible by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Text(text = "🚌", fontSize = 56.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (step == 1) stringResource(R.string.onboarding_welcome_title)
                else stringResource(R.string.onboarding_ors_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_step_counter, step, 2),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (step == 1) {
                        Text(
                            text = stringResource(R.string.onboarding_welcome_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = {
                                apiKey = it.trim()
                                apiKeyRejected = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.api_key_label)) },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { apiVisible = !apiVisible }) {
                                    Icon(
                                        imageVector = if (apiVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (apiVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            isError = apiKeyRejected,
                            supportingText = if (apiKeyRejected) {
                                { Text(stringResource(R.string.apikey_error_short)) }
                            } else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedButton(
                            onClick = {
                                val subject = Uri.encode("API-Key Anfrage für persönliche Nutzung in der Android App")
                                val body = Uri.encode("Hallo,\n\nich möchte einen abfahrt.now API-Key für die persönliche Nutzung in der Android-App anfragen.\n\nViele Grüße")
                                uriHandler.openUri("mailto:hi@abfahrt.now?subject=$subject&body=$body")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.api_key_request))
                        }

                        Button(
                            onClick = { step = 2 },
                            enabled = apiKey.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.next_step))
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.onboarding_ors_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = orsApiKey,
                            onValueChange = { orsApiKey = it.trim() },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.ors_api_key_label)) },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { orsVisible = !orsVisible }) {
                                    Icon(
                                        imageVector = if (orsVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (orsVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = {
                                uriHandler.openUri("https://account.heigit.org/info/plans")
                            }) {
                                Text(stringResource(R.string.ors_plans))
                            }
                            TextButton(onClick = {
                                uriHandler.openUri("https://account.heigit.org/signup")
                            }) {
                                Text(stringResource(R.string.ors_registration))
                            }
                        }

                        Button(
                            onClick = { onContinue(apiKey, orsApiKey) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (orsApiKey.isBlank()) {
                                    stringResource(R.string.next_without_ors_key)
                                } else {
                                    stringResource(R.string.show_departures)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

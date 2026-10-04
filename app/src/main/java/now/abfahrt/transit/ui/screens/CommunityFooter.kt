package now.abfahrt.transit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import now.abfahrt.transit.R
import now.abfahrt.transit.util.AppVersionInfo

private const val COMMUNITY_REPOSITORY_URL = "https://github.com/K-Cosmo/abfahrt-now-android"
private const val ABFAHRT_PRIVACY_URL = "https://www.abfahrt.now/privacy"
private const val ABFAHRT_TERMS_URL = "https://www.abfahrt.now/terms"

@Composable
internal fun CommunityFooter() {
    val context = LocalContext.current
    val openUrl: (String) -> Unit = { url ->
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
    val disclaimerSentences = stringResource(R.string.community_disclaimer)
        .split(Regex("(?<=[.!?])\\s+"))
        .map(String::trim)
        .filter(String::isNotBlank)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🚌", fontSize = 28.sp)
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.app_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.community_data_source),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))
        TextButton(
            onClick = { openUrl(COMMUNITY_REPOSITORY_URL) },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Text(
                text = stringResource(R.string.community_project_link),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CommunityFooterLink(stringResource(R.string.community_api_privacy)) {
                openUrl(ABFAHRT_PRIVACY_URL)
            }
            Text(" · ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            CommunityFooterLink(stringResource(R.string.community_api_terms)) {
                openUrl(ABFAHRT_TERMS_URL)
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            disclaimerSentences.forEach { sentence ->
                Text(
                    text = sentence,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = context.getString(R.string.version_label, AppVersionInfo.displayVersion),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun CommunityFooterLink(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

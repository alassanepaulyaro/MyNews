package com.yaropaul.mynews.ui.screen.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.ClickableText
import com.yaropaul.mynews.domain.model.Article
import com.yaropaul.mynews.ui.components.ArticleImage
import com.yaropaul.mynews.ui.components.CategoryPillTag
import com.yaropaul.mynews.ui.theme.NewsBlue
import com.yaropaul.mynews.ui.theme.NewsTextGray
import com.yaropaul.mynews.ui.theme.SerifFontFamily

private val TRUNCATION_REGEX = Regex("""\s*\[\+\d+\s*chars]""")

@Composable
fun DetailContent(
    article: Article,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                // Title (single — above image)
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hero image
                if (article.urlToImage.isNotBlank()) {
                    ArticleImage(
                        url = article.urlToImage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Category + Author row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (article.sourceName.isNotBlank()) {
                        CategoryPillTag(label = article.sourceName.take(20).lowercase())
                    }
                    if (article.author.isNotBlank()) {
                        Text(
                            text = "  By ${article.author}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NewsTextGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description / content
                // Prefer content (longer) over description; clean the API truncation marker.
                val rawText = article.content.takeIf { it.isNotBlank() }
                    ?: article.description
                val cleanText = rawText.replace(TRUNCATION_REGEX, "").trim()

                if (cleanText.isNotBlank()) {
                    var expanded by rememberSaveable { mutableStateOf(false) }

                    Text(
                        text = cleanText,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = if (expanded) Int.MAX_VALUE else 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Show toggle only when the text is long enough to need it
                    if (cleanText.length > 2000) {
                        TextButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (expanded) "Show less" else "Read more")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Read the full story link
                if (article.url.isNotBlank()) {
                    Text(
                        text = "Read the full story",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val urlAnnotated = buildAnnotatedString {
                        pushStringAnnotation(tag = "URL", annotation = article.url)
                        withStyle(
                            style = SpanStyle(
                                color = NewsBlue,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(article.url)
                        }
                        pop()
                    }
                    ClickableText(
                        text = urlAnnotated,
                        style = MaterialTheme.typography.bodySmall,
                        onClick = { offset ->
                            urlAnnotated.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

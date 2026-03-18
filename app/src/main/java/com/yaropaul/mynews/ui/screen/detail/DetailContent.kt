package com.yaropaul.mynews.ui.screen.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.yaropaul.mynews.R
import com.yaropaul.mynews.domain.model.Article
import com.yaropaul.mynews.ui.components.ArticleImage
import com.yaropaul.mynews.ui.components.CategoryPillTag
import com.yaropaul.mynews.ui.components.ScreenSurface
import com.yaropaul.mynews.ui.theme.Dimens

private val TRUNCATION_REGEX = Regex("""\s*\[\+\d+\s*chars]""")

// Minimum character count before a "Read more / Show less" toggle appears.
private const val CONTENT_EXPAND_THRESHOLD = 2000

// Maximum characters shown for source name to keep the pill tag compact.
private const val MAX_SOURCE_NAME_LENGTH = 20

// Number of visible lines when content is collapsed.
private const val COLLAPSED_MAX_LINES = 4

@Composable
fun DetailContent(
    article: Article,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    ScreenSurface(modifier = modifier) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.TopBarPadding, vertical = Dimens.TopBarPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
        }

        // Scrollable content — navigationBarsPadding ensures content is not hidden
        // behind the system navigation bar on gesture-nav and 3-button devices.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.ScreenPaddingHorizontal)
                .navigationBarsPadding()
        ) {
            // Title (single — above image)
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(Dimens.ContentSpacing))

            // Hero image
            if (article.urlToImage.isNotBlank()) {
                ArticleImage(
                    url = article.urlToImage,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ImageHeight)
                )
                Spacer(modifier = Modifier.height(Dimens.ContentSpacing))
            }

            // Category + Author row
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (article.sourceName.isNotBlank()) {
                    CategoryPillTag(
                        label = article.sourceName.take(MAX_SOURCE_NAME_LENGTH).lowercase()
                    )
                }
                if (article.author.isNotBlank()) {
                    Spacer(modifier = Modifier.width(Dimens.SmallSpacing))
                    Text(
                        text = stringResource(R.string.detail_by_author, article.author),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.DetailBodySpacing))

            // Description / content
            // Prefer content (longer) over description; clean the API truncation marker.
            val rawText = article.content.takeIf { it.isNotBlank() } ?: article.description
            val cleanText = rawText.replace(TRUNCATION_REGEX, "").trim()

            if (cleanText.isNotBlank()) {
                // Only apply collapse/expand when the text is long enough to warrant it.
                // For shorter content, show everything without truncation.
                val needsToggle = cleanText.length > CONTENT_EXPAND_THRESHOLD
                var expanded by rememberSaveable { mutableStateOf(false) }

                Text(
                    text = cleanText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = if (needsToggle && !expanded) COLLAPSED_MAX_LINES else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )

                if (needsToggle) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            stringResource(
                                if (expanded) R.string.detail_show_less else R.string.detail_read_more
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.ContentSpacing))

            // Read the full story link
            if (article.url.isNotBlank()) {
                Text(
                    text = stringResource(R.string.detail_full_story_label),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(Dimens.TopBarPadding))

                val linkColor = MaterialTheme.colorScheme.primary
                val urlAnnotated = buildAnnotatedString {
                    pushLink(
                        LinkAnnotation.Url(
                            url = article.url,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        )
                    )
                    append(article.url)
                    pop()
                }
                Text(
                    text = urlAnnotated,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(Dimens.LargeSpacing))
        }
    }
}

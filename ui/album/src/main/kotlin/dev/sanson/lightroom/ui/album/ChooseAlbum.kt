package dev.sanson.lightroom.ui.album

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import dev.sanson.lightroom.common.ui.component.DarkModePreviews
import dev.sanson.lightroom.common.ui.component.StepHeader
import dev.sanson.lightroom.core.ui.MuzeiLightroomTheme
import dev.sanson.lightroom.screens.ChooseAlbumScreen
import dev.sanson.lightroom.sdk.model.Album
import dev.sanson.lightroom.sdk.model.AlbumId
import dev.sanson.lightroom.sdk.model.AlbumTreeItem
import dev.sanson.lightroom.sdk.model.AssetId
import dev.sanson.lightroom.sdk.model.CatalogId
import dev.sanson.lightroom.sdk.model.CollectionSet
import dev.sanson.lightroom.sdk.model.CollectionSetId
import dev.sanson.lightroom.sdk.model.Rendition
import nz.sanson.lightroom.coil.rememberImageRequest

@CircuitInject(ChooseAlbumScreen::class, SingletonComponent::class)
@Composable
fun ChooseAlbum(
    state: ChooseAlbumState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            StepHeader(
                stepNumber = 2,
                stepName = stringResource(R.string.choose_an_album),
                modifier = Modifier
                    .padding(24.dp)
                    .padding(top = 64.dp),
            )
        },
        modifier = modifier
            .fillMaxSize(),
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .systemBarsPadding(),
        ) {
            when (state) {
                is ChooseAlbumState.Loaded ->
                    LazyColumn(
                        Modifier
                            .padding(horizontal = 16.dp),
                    ) {
                        collectionSet(
                            children = state.albumTree,
                            selectedAlbum = state.selectedAlbum,
                            onAlbumClick = { state.eventSink(ChooseAlbumEvent.SelectAlbum(it)) },
                        )

                        item {
                            Spacer(Modifier.height(96.dp))
                        }
                    }

                else ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        CircularProgressIndicator()
                    }
            }

            if (state is ChooseAlbumState.Loaded) {
                Button(
                    onClick = { state.eventSink(ChooseAlbumEvent.Confirm) },
                    enabled = state.selectedAlbum != null,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

private fun LazyListScope.collectionSet(
    children: List<AlbumTreeItem>,
    selectedAlbum: AlbumId?,
    onAlbumClick: (AlbumId) -> Unit,
    name: String? = null,
    inset: Dp = 0.dp,
) {
    if (name != null) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .padding(top = 16.dp, bottom = 8.dp)
                    .padding(start = inset - 8.dp),
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_folder),
                    contentDescription = null,
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(text = name)
            }
        }
    }

    for (child in children) {
        when (child) {
            is Album -> {
                item {
                    Row(Modifier.padding(start = inset)) {
                        // TODO: Apply this effect to nested collection sets, to avoid line breaks
                        // when presenting nested folders
                        val dashColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
                        Canvas(
                            modifier = Modifier
                                .size(width = 2.dp, height = 74.dp),
                        ) {
                            drawLine(
                                color = dashColor,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 4f,
                                cap = StrokeCap.Round,
                            )
                        }

                        AlbumRow(
                            isSelected = selectedAlbum == child.id,
                            onClick = { onAlbumClick(child.id) },
                            name = child.name,
                            coverAsset = child.cover,
                            catalogId = child.catalogId,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .padding(start = 8.dp, end = 8.dp),
                        )
                    }
                }
            }

            is CollectionSet -> {
                collectionSet(
                    name = child.name,
                    children = child.children,
                    selectedAlbum = selectedAlbum,
                    onAlbumClick = onAlbumClick,
                    inset = inset + 16.dp,
                )
            }
        }
    }
}

@Composable
private fun AlbumRow(
    isSelected: Boolean,
    onClick: () -> Unit,
    name: String,
    coverAsset: AssetId?,
    catalogId: CatalogId,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "Album selection background",
    )

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (coverAsset != null) {
                AssetThumbnail(
                    asset = coverAsset,
                    catalogId = catalogId,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(48.dp),
                )
            } else {
                Box(
                    Modifier
                        .padding(8.dp)
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f),
                            shape = RoundedCornerShape(6.dp),
                        ),
                )
            }

            Spacer(Modifier.size(12.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AssetThumbnail(
    asset: AssetId,
    catalogId: CatalogId,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = rememberImageRequest(
            assetId = asset,
            catalogId = catalogId,
            rendition = Rendition.Thumbnail,
        ),
        contentDescription = "Album cover photo",
        modifier = modifier
            .clip(shape = RoundedCornerShape(6.dp)),
        contentScale = ContentScale.Crop,
    )
}

@DarkModePreviews
@Composable
fun ChooseAlbumScreenPreview() {
    MuzeiLightroomTheme {
        ChooseAlbum(
            state = ChooseAlbumState.Loaded(
                albumTree = listOf(
                    CollectionSet(
                        id = CollectionSetId("0"),
                        catalogId = CatalogId(""),
                        name = "Travel",
                        children = listOf(
                            Album(
                                id = AlbumId("1"),
                                catalogId = CatalogId(""),
                                name = "Portugal",
                                cover = null,
                                assets = List(168) { AssetId("") },
                            ),
                            Album(
                                id = AlbumId("2"),
                                catalogId = CatalogId(""),
                                name = "Spain",
                                cover = null,
                                assets = List(340) { AssetId("") },
                            ),
                            Album(
                                id = AlbumId("3"),
                                catalogId = CatalogId(""),
                                name = "Morocco",
                                cover = null,
                                assets = List(210) { AssetId("") },
                            ),
                        ),
                    ),
                    Album(
                        id = AlbumId("4"),
                        catalogId = CatalogId(""),
                        name = "NZ Birdlife",
                        cover = null,
                        assets = List(10) { AssetId("") },
                    ),
                ),
                selectedAlbum = AlbumId("3"),
                eventSink = {},
            ),
        )
    }
}

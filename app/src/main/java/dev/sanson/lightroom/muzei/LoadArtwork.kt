package dev.sanson.lightroom.muzei

import androidx.core.net.toUri
import com.google.android.apps.muzei.api.provider.Artwork
import dev.sanson.lightroom.core.config.Config
import dev.sanson.lightroom.core.config.permitsAsset
import dev.sanson.lightroom.sdk.Lightroom
import dev.sanson.lightroom.sdk.model.Asset
import dev.sanson.lightroom.sdk.model.Rendition
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Extension function for loading all relevant Muzei [Artwork] from Lightroom
 * given a [config]. This function is responsible for selecting the source of the artwork,
 * as well as formatting Artwork-related information into something user-visible in Muzei.
 *
 * @receiver Lightroom instance
 * @param config Config to use when loading artwork
 * @return List of artwork matching [config]
 */
suspend fun Lightroom.loadArtwork(config: Config): List<Artwork> {
    /**
     * Map [Asset] to [Artwork]
     *
     * The expected format is as such:
     *
     * title = Date (London - 9 Nov 2022)
     * byline = Camera & lens (Fujifilm X-T3, XF16-55mm etc.)
     * attribution = Capture specs (ISO 160 55mm f/4.0 1/160s)
     * token = <asset_id>
     * persistentUrl = <asset_id URI>
     * webUrl = <lightroom web URL> (https://lightroom.adobe.com/libraries/<catalog_id>/assets/<asset_id>)
     * metadata = <catalog_id>
     */
    suspend fun Asset.toArtwork(): Artwork {
        fun LocalDateTime.format(): String =
            "$dayOfMonth ${
                month.getDisplayName(
                    TextStyle.SHORT,
                    Locale.getDefault(),
                )
            } $year"

        val catalogId = getCatalog().id

        return Artwork(
            title = captureDate.toLocalDateTime(TimeZone.currentSystemDefault()).format(),
            byline = "$cameraBody, $lens",
            attribution = "ISO $iso - $focalLength - $aperture - $shutterSpeed",
            token = id.id,
            persistentUri = id.asUrl(rendition = Rendition.Full).toUri(),
            webUri = "https://lightroom.adobe.com/libraries/${catalogId.id}/assets/${id.id}".toUri(),
            metadata = catalogId.id,
        )
    }

    val assets = when (val source = config.source) {
        is Config.Source.Album ->
            getAlbumAssets(source.requireId())

        is Config.Source.Catalog ->
            getCatalogAssets()
    }

    return assets.filter { config.permitsAsset(it) }
        .map { asset -> asset.toArtwork() }
}

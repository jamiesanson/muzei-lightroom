package dev.sanson.lightroom.ui.confirmation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract.getProviderClient
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import dev.sanson.lightroom.circuit.FinishActivityScreen
import dev.sanson.lightroom.common.config.ConfigRepository
import dev.sanson.lightroom.muzei.LightroomAlbumProvider
import dev.sanson.lightroom.muzei.loadArtwork
import dev.sanson.lightroom.sdk.Lightroom
import dev.sanson.lightroom.sdk.model.AssetId
import dev.sanson.lightroom.sdk.model.Rendition
import dev.sanson.lightroom.ui.confirmation.ConfirmationScreen.State
import kotlinx.coroutines.flow.firstOrNull
import nz.sanson.lightroom.coil.LocalLightroomImageLoader

class ConfirmationPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val configRepository: ConfigRepository,
    private val lightroom: Lightroom,
) : Presenter<State> {

    @Composable
    override fun present(): State {
        val context = LocalContext.current
        val imageLoader = LocalLightroomImageLoader.current

        val providerClient = remember { getProviderClient<LightroomAlbumProvider>(context) }

        val artwork by produceState<List<Artwork>?>(initialValue = null) {
            val config = requireNotNull(configRepository.config.firstOrNull()) {
                "Config is required on confirmation"
            }

            value = lightroom
                .loadArtwork(config)
                .also(providerClient::setArtwork)
        }

        var firstArtworkId by remember { mutableStateOf<AssetId?>(null) }

        LaunchedEffect(artwork) {
            val art = artwork ?: return@LaunchedEffect

            val firstArtwork = art.first()

            val assetId =
                AssetId(requireNotNull(firstArtwork.token) { "No token found for artwork: $firstArtwork" })

            lightroom.generateRendition(
                asset = assetId,
                rendition = Rendition.Full,
            )

            with(imageLoader) {
                newRequest(assetId = assetId, rendition = Rendition.Full).await()
            }

            firstArtworkId = assetId
        }

        return when (val art = artwork) {
            null ->
                State.LoadingArtwork

            else -> when (val firstImage = firstArtworkId) {
                null ->
                    State.LoadingFirstImage(art)

                else ->
                    State.Loaded(
                        artwork = art,
                        firstWallpaperId = firstImage,
                        firstArtworkCaptureDate = requireNotNull(art.first().title),
                        eventSink = { event ->
                            when (event) {
                                ConfirmationScreen.Event.OnFinish ->
                                    navigator.goTo(FinishActivityScreen(requestCode = Activity.RESULT_OK))
                            }
                        },
                    )
            }
        }
    }

    @CircuitInject(ConfirmationScreen::class, SingletonComponent::class)
    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): ConfirmationPresenter
    }
}

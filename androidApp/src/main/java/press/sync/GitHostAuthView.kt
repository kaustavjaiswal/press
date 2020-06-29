package press.sync

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.widget.ProgressBar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers
import me.saket.press.shared.sync.GitHostAuthEvent.GitRepositoryClicked
import me.saket.press.shared.sync.git.GitHostAuthPresenter
import me.saket.press.shared.sync.GitHostAuthUiEffect
import me.saket.press.shared.sync.GitHostAuthUiEffect.OpenAuthorizationUrl
import me.saket.press.shared.sync.GitHostAuthUiModel
import me.saket.press.shared.sync.GitHostAuthUiModel.FullscreenError
import me.saket.press.shared.sync.GitHostAuthUiModel.Loading
import me.saket.press.shared.sync.GitHostAuthUiModel.SelectRepo
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.theme.themeAware
import press.theme.themed
import press.widgets.PressToolbar

class GitHostAuthView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted onDismiss: () -> Unit,
  private val presenter: GitHostAuthPresenter
) : ContourLayout(context) {

  private val toolbar = themed(PressToolbar(context)).apply {
    title = "GitHub"
    setNavigationOnClickListener { onDismiss() }
    applyLayout(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }

  private val repoAdapter = GitRepositoryAdapter()
  private val recyclerView = themed(RecyclerView(context)).apply {
    layoutManager = LinearLayoutManager(context)
    adapter = repoAdapter
    applyLayout(
        x = matchParentX(),
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  private val progressView = themed(ProgressBar(context)).apply {
    isGone = true
    isIndeterminate = true
    applyLayout(
        x = centerHorizontallyTo { parent.centerX() }.widthOf { 60.xdip },
        y = centerVerticallyTo { parent.centerY() }.heightOf { 60.ydip }
    )
  }

  private val errorView = ErrorView(context).also {
    it.isGone = true
    it.applyLayout(
        x = matchParentX(),
        y = centerVerticallyTo { parent.centerY() }
    )
  }

  init {
    themeAware {
      background = ColorDrawable(it.window.backgroundColor)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    repoAdapter.onClick = {
      presenter.dispatch(GitRepositoryClicked(it))
    }

    presenter.uiUpdates()
        .takeUntil(detaches())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(models = ::render, effects = ::render)
  }

  private fun render(model: GitHostAuthUiModel) {
    TransitionManager.beginDelayedTransition(this, AutoTransition().apply {
      addTarget(progressView)
      addTarget(errorView)
    })

    progressView.isGone = model !is Loading
    errorView.isGone = model !is FullscreenError

    return when (model) {
      is Loading -> Unit
      is SelectRepo -> repoAdapter.submitList(model.repositories)
      is FullscreenError -> {
        errorView.retryButton.setOnClickListener {
          model.onRetry()
        }
      }
    }
  }

  private fun render(effect: GitHostAuthUiEffect) {
    return when (effect) {
      is OpenAuthorizationUrl -> CustomTabsIntent.Builder()
          .addDefaultShareMenuItem()
          .build()
          .launchUrl(context, Uri.parse(effect.url))
    }
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(context: Context, onDismiss: () -> Unit): GitHostAuthView
  }
}

package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.rushi.mitavyay.util.hapticLight

/**
 * Standard Material 3 PullToRefresh container wrapper.
 * Wraps list contents with nested scrolling and positions the refresh indicator
 * with accent green primary spinner on neutral surface container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable BoxScope.() -> Unit = {
        PullToRefreshContainer(
            state = state,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    },
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current

    if (state.isRefreshing) {
        LaunchedEffect(true) {
            context.hapticLight()
            onRefresh()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            state.startRefresh()
        } else {
            state.endRefresh()
        }
    }

    Box(
        modifier = modifier.nestedScroll(state.nestedScrollConnection),
        contentAlignment = contentAlignment
    ) {
        content()
        indicator()
    }
}

package com.rushi.mitavyay.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.rushi.mitavyay.ui.theme.spacing

@Composable
fun VerticalSpacer(height: Dp, modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.height(height))
}

@Composable
fun HorizontalSpacer(width: Dp, modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.width(width))
}

@Composable
fun SpacerXs(modifier: Modifier = Modifier) = VerticalSpacer(MaterialTheme.spacing.xs, modifier)

@Composable
fun SpacerSm(modifier: Modifier = Modifier) = VerticalSpacer(MaterialTheme.spacing.sm, modifier)

@Composable
fun SpacerMd(modifier: Modifier = Modifier) = VerticalSpacer(MaterialTheme.spacing.md, modifier)

@Composable
fun SpacerLg(modifier: Modifier = Modifier) = VerticalSpacer(MaterialTheme.spacing.lg, modifier)

@Composable
fun SpacerXl(modifier: Modifier = Modifier) = VerticalSpacer(MaterialTheme.spacing.xl, modifier)

@Composable
fun HSpacerXs(modifier: Modifier = Modifier) = HorizontalSpacer(MaterialTheme.spacing.xs, modifier)

@Composable
fun HSpacerSm(modifier: Modifier = Modifier) = HorizontalSpacer(MaterialTheme.spacing.sm, modifier)

@Composable
fun HSpacerMd(modifier: Modifier = Modifier) = HorizontalSpacer(MaterialTheme.spacing.md, modifier)

@Composable
fun HSpacerLg(modifier: Modifier = Modifier) = HorizontalSpacer(MaterialTheme.spacing.lg, modifier)

@Composable
fun HSpacerXl(modifier: Modifier = Modifier) = HorizontalSpacer(MaterialTheme.spacing.xl, modifier)

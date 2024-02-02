/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.compose.chart.pie

import android.graphics.RectF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.chart.ChartHostBox
import com.patrykandpatrick.vico.compose.model.collectAsState
import com.patrykandpatrick.vico.compose.model.defaultPieDiffAnimationSpec
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.draw.drawContext
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.extension.spToPx
import com.patrykandpatrick.vico.core.formatter.PieValueFormatter
import com.patrykandpatrick.vico.core.legend.Legend
import com.patrykandpatrick.vico.core.model.PieChartModelProducer
import com.patrykandpatrick.vico.core.model.PieModel
import com.patrykandpatrick.vico.core.pie.PieChart
import com.patrykandpatrick.vico.core.pie.Size
import com.patrykandpatrick.vico.core.pie.Slice

/**
 * Draws a pie chart.
 *
 * @param modifier the modifier to be applied to the chart.
 * @param slices the [List] of [Slice]s which define the appearance of each slice of the pie chart.
 * @param modelProducer creates and updates the [PieChartModelProducer] for the chart.
 * @param spacing defines spacing between [slices].
 * @param outerSize defines the size of the chart.
 * @param innerSize defines the size of the hole in the middle of the chart.
 * @param startAngle defines the angle at which the first slice starts.
 * @param valueFormatter formats the values of the pie chart.
 * @param legend the legend for the chart.
 * @param diffAnimationSpec the animation spec used for difference animations.
 * @param elevationOverlayColor the color to use for the elevation overlay.
 * @param runInitialAnimation whether to run the initial animation.
 * @param placeholder the composable to display when the chart is empty.
 */
@Composable
public fun PieChartHost(
    modelProducer: PieChartModelProducer,
    modifier: Modifier = Modifier,
    slices: List<Slice> = currentChartStyle.pieChart.slices,
    spacing: Dp = currentChartStyle.pieChart.spacing,
    outerSize: Size.OuterSize = currentChartStyle.pieChart.outerSize,
    innerSize: Size.InnerSize = currentChartStyle.pieChart.innerSize,
    startAngle: Float = currentChartStyle.pieChart.startAngle,
    valueFormatter: PieValueFormatter = PieValueFormatter.Default,
    legend: Legend? = null,
    diffAnimationSpec: AnimationSpec<Float> = defaultPieDiffAnimationSpec,
    elevationOverlayColor: Color = currentChartStyle.elevationOverlayColor,
    runInitialAnimation: Boolean = true,
    placeholder: @Composable BoxScope.() -> Unit = {},
) {
    val pieChart = rememberPieChart(slices, spacing, outerSize, innerSize, startAngle, valueFormatter)

    val model by
        modelProducer.collectAsState(
            chart = pieChart,
            producerKey = modelProducer,
            animationSpec = diffAnimationSpec,
            runInitialAnimation = runInitialAnimation,
        )

    ChartHostBox(
        modifier = modifier,
        legend = legend,
        hasModel = model != null,
        desiredHeight = { constraints -> constraints.maxWidth },
    ) {
        model?.also { model ->
            PieChartHost(
                model = model,
                pieChart = pieChart,
                legend = legend,
                elevationOverlayColor = elevationOverlayColor,
            )
        } ?: Box { placeholder() }
    }
}

/**
 * Draws a pie chart.
 *
 * @param modifier the modifier to be applied to the chart.
 * @param slices the [List] of [Slice]s which define the appearance of each slice of the pie chart.
 * @param model the [PieModel].
 * @param spacing defines spacing between [slices].
 * @param outerSize defines the size of the chart.
 * @param innerSize defines the size of the hole in the middle of the chart.
 * @param startAngle defines the angle at which the first slice starts.
 * @param valueFormatter formats the values of the pie chart.
 * @param legend the legend for the chart.
 * @param elevationOverlayColor the color to use for the elevation overlay.
 */
@Composable
public fun PieChartHost(
    model: PieModel,
    modifier: Modifier = Modifier,
    slices: List<Slice> = currentChartStyle.pieChart.slices,
    spacing: Dp = currentChartStyle.pieChart.spacing,
    outerSize: Size.OuterSize = currentChartStyle.pieChart.outerSize,
    innerSize: Size.InnerSize = currentChartStyle.pieChart.innerSize,
    startAngle: Float = currentChartStyle.pieChart.startAngle,
    valueFormatter: PieValueFormatter = PieValueFormatter.Default,
    legend: Legend? = null,
    elevationOverlayColor: Color = currentChartStyle.elevationOverlayColor,
) {
    val pieChart = rememberPieChart(slices, spacing, outerSize, innerSize, startAngle, valueFormatter)

    ChartHostBox(
        modifier = modifier,
        legend = legend,
        hasModel = true,
        desiredHeight = { constraints -> constraints.maxWidth },
    ) {
        PieChartHost(
            model = model,
            pieChart = pieChart,
            legend = legend,
            elevationOverlayColor = elevationOverlayColor,
        )
    }
}

@Composable
private fun rememberPieChart(
    slices: List<Slice> = currentChartStyle.pieChart.slices,
    spacing: Dp = currentChartStyle.pieChart.spacing,
    outerSize: Size.OuterSize = currentChartStyle.pieChart.outerSize,
    innerSize: Size.InnerSize = currentChartStyle.pieChart.innerSize,
    startAngle: Float = currentChartStyle.pieChart.startAngle,
    valueFormatter: PieValueFormatter = PieValueFormatter.Default,
) = remember {
    PieChart(
        slices = slices,
        spacingDp = spacing.value,
        innerSize = innerSize,
        outerSize = outerSize,
        startAngle = startAngle,
    )
}.apply {
    this.slices = slices
    this.spacingDp = spacing.value
    this.innerSize = innerSize
    this.outerSize = outerSize
    this.startAngle = startAngle
    this.valueFormatter = valueFormatter
}

@Composable
internal fun PieChartHost(
    model: PieModel,
    pieChart: PieChart,
    legend: Legend?,
    elevationOverlayColor: Color,
    modifier: Modifier = Modifier,
) {
    val bounds = remember { RectF() }
    val density = LocalDensity.current.density
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val context = LocalContext.current

    Canvas(modifier = modifier) {
        bounds.set(left = 0, top = 0, right = size.width, bottom = size.height)
        val pieChartHeight = minOf(bounds.width(), bounds.height())
        pieChart.setBounds(bounds.left, bounds.top, pieChartHeight, pieChartHeight)

        val drawContext =
            drawContext(
                canvas = drawContext.canvas.nativeCanvas,
                density = density,
                isLtr = isLtr,
                elevationOverlayColor = elevationOverlayColor.toArgb().toLong(),
                spToPx = context::spToPx,
            )

        pieChart.draw(
            context = drawContext,
            model = model,
        )

        legend?.apply {
            setBounds(
                left = pieChart.bounds.left,
                top = pieChart.bounds.bottom,
                right = pieChart.bounds.right,
                bottom = pieChart.bounds.bottom + getHeight(drawContext, bounds.width()),
            )
            draw(
                context = drawContext,
                chartBounds = pieChart.bounds,
            )
        }
    }
}

/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.chart

import android.content.Context
import android.util.AttributeSet
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.util.SampleCandlestickEntryProvider
import com.patrykandpatrick.vico.views.theme.ThemeHandler

/**
 * A subclass of [BaseChartView] that displays charts that use [ChartEntryModel].
 */
public class CandlestickChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseChartView<CandlestickEntryModel>(
    context = context,
    attrs = attrs,
    defStyleAttr = defStyleAttr,
    chartType = ThemeHandler.ChartType.Candlestick,
) {
    init {
        chart = themeHandler.candlestickChart
        if (isInEditMode) setModel(model = SampleCandlestickEntryProvider.sampleModel)
    }
}

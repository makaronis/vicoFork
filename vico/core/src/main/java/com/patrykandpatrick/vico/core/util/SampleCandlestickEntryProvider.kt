/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.util

import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryModel
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntry
import com.patrykandpatrick.vico.core.candlestickentry.candlestickEntryModelOf

/**
 * An object used to provide a sample [CandlestickEntryModel] instance. Used solely for previewing purposes.
 */
public object SampleCandlestickEntryProvider {

    /**
     * The sample [CandlestickEntryModel] instance. Used solely for previewing purposes.
     */
    public val sampleModel: CandlestickEntryModel =
        candlestickEntryModelOf(
            CandlestickEntry(
                x = 0f,
                low = 2f,
                open = 4f,
                close = 8f,
                high = 10f,
            ),
            CandlestickEntry(
                x = 1f,
                low = 6f,
                open = 8f,
                close = 12f,
                high = 14f,
            ),
            CandlestickEntry(
                x = 2f,
                low = 10f,
                open = 12f,
                close = 14f,
                high = 16f,
            ),
            CandlestickEntry(
                x = 3f,
                low = 8f,
                open = 16f,
                close = 10f,
                high = 18f,
            ),
            CandlestickEntry(
                x = 4f,
                low = 14f,
                open = 16f,
                close = 16f,
                high = 18f,
            ),
            CandlestickEntry(
                x = 5f,
                low = 8f,
                open = 16f,
                close = 10f,
                high = 18f,
            ),
        )
}

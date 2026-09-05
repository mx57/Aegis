package com.example.engine.vectorizer

/**
 * Tracing algorithm modes for the vectorizer.
 */
enum class TracingMode(val titleRu: String, val descriptionRu: String) {
    CENTERLINE(
        "Осевая линия (Скелетонизация)",
        "Вычисляет топологический скелет штрихов (алгоритм Чжана-Суэня). Идеально для рун, тату-контуров и рукописных эскизов"
    ),
    OUTLINE(
        "Контурный силуэт (Potrace / Изолинии)",
        "Точный захват внешних границ и внутренних отверстий с правилом evenodd. Идеально для сплошных заливок и сложных пятен"
    ),
    MULTILAYER_TONAL(
        "Многослойный полутоновый (Градиенты)",
        "Постеризация и разделение на несколько тональных слоёв (от светлого к тёмному). Идеально для эскизов со штриховкой и тенями"
    )
}

/**
 * Binarization and thresholding techniques.
 */
enum class ThresholdMode(val titleRu: String) {
    OTSU_AUTO("Оцу (Автоматический расчёт гистограммы)"),
    ADAPTIVE_LOCAL("Адаптивный локальный (Интегральное изображение)"),
    MANUAL("Ручной порог яркости")
}

/**
 * Curve approximation and fitting type.
 */
enum class CurveFittingType(val titleRu: String) {
    CUBIC_BEZIER("Кубические кривые Безье (Плавные дуги C)"),
    QUADRATIC("Квадратичные сплайны (Q)"),
    POLYLINE("Ломаные линии (Максимальная строгость L)")
}

/**
 * Background style for SVG canvas.
 */
enum class VectorizerBg(val titleRu: String, val colorHex: String?) {
    TRANSPARENT("Прозрачный (Для тату и печати)", null),
    OBSIDIAN_BLACK("Тёмный обсидиан (#0E1117)", "#0E1117"),
    PURE_WHITE("Чистый белый (#FFFFFF)", "#FFFFFF"),
    PARCHMENT("Древний пергамент (#F4ECD8)", "#F4ECD8")
}

/**
 * Predefined configurations optimized for common artistic and technical workflows.
 */
enum class VectorizerPreset(val titleRu: String, val descriptionRu: String) {
    ULTRA_FIDELITY_100(
        "100% Максимальная точность",
        "Прецизионный захват каждого пикселя без потерь деталей с сохранением острых углов"
    ),
    TATTOO_STENCIL_LINEWORK(
        "Тату-трафарет (Line Art)",
        "Чёткие осевые линии заданной толщины для термопринтера и трансфера на кожу"
    ),
    SMOOTH_BEZIER_CALLIGRAPHY(
        "Шелковистое Безье-сглаживание",
        "Мягкие кубические сплайны, устраняющие шум сканирования и ступеньки растра"
    ),
    MULTI_TONE_REALISTIC(
        "Многослойный объём (Полутона)",
        "4-уровневая тональная постеризация для передачи объёмных теней и гравюрной штриховки"
    ),
    HISTORICAL_MANUSCRIPT(
        "Скан старинного манускрипта",
        "Адаптивная локальная фильтрация неоднородного освещения и текстуры пергамента"
    )
}

/**
 * Complete set of configurable parameters for the raster-to-SVG vectorizer engine.
 */
data class VectorizerConfig(
    // Mode
    val mode: TracingMode = TracingMode.CENTERLINE,

    // Preprocessing & Binarization
    val thresholdMode: ThresholdMode = ThresholdMode.OTSU_AUTO,
    val manualThreshold: Int = 128,         // 0..255
    val adaptiveBlockSize: Int = 19,        // 5..51 (odd)
    val adaptiveC: Int = 8,                 // -30..30
    val contrast: Float = 1.25f,            // 0.5f..2.5f
    val brightness: Float = 0.0f,           // -50f..50f
    val denoiseRadius: Int = 1,             // 0..4 (0=none, 1=3x3, 2=5x5)
    val invertPolarity: Boolean = false,    // true: white lines on dark; false: dark lines on light
    val tonalLayers: Int = 4,               // 2..8 for MULTILAYER_TONAL

    // Curve Fitting & Geometry
    val detailLevel: Float = 0.10f,         // Epsilon tolerance: 0.02f (100% detail) to 1.5f (simplified)
    val curveFitting: CurveFittingType = CurveFittingType.CUBIC_BEZIER,
    val smoothFactor: Float = 0.55f,        // 0.0f (straight) to 1.0f (full curvature)
    val cornerThresholdAngle: Float = 60f,  // 20°..120° (angles sharper than this stay sharp corners)
    val minPathArea: Int = 3,               // Minimum pixel length / area to filter noise specks

    // Styling & SVG Appearance
    val strokeColorHex: String = "#E5C158", // Sacred gold by default
    val fillColorHex: String = "#E5C158",
    val strokeWidth: Float = 2.2f,          // 0.5f..8.0f
    val canvasBackground: VectorizerBg = VectorizerBg.OBSIDIAN_BLACK,
    val exportScale: Float = 1.0f
) {
    companion object {
        fun fromPreset(preset: VectorizerPreset): VectorizerConfig = when (preset) {
            VectorizerPreset.ULTRA_FIDELITY_100 -> VectorizerConfig(
                mode = TracingMode.OUTLINE,
                thresholdMode = ThresholdMode.OTSU_AUTO,
                detailLevel = 0.02f, // Extreme 100% precision
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.35f,
                cornerThresholdAngle = 45f,
                minPathArea = 1,
                denoiseRadius = 0,
                strokeWidth = 1.5f,
                strokeColorHex = "#E5C158",
                fillColorHex = "#E5C158",
                canvasBackground = VectorizerBg.OBSIDIAN_BLACK
            )

            VectorizerPreset.TATTOO_STENCIL_LINEWORK -> VectorizerConfig(
                mode = TracingMode.CENTERLINE,
                thresholdMode = ThresholdMode.OTSU_AUTO,
                detailLevel = 0.12f,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.50f,
                cornerThresholdAngle = 60f,
                minPathArea = 4,
                denoiseRadius = 1,
                strokeWidth = 2.4f,
                strokeColorHex = "#000000",
                fillColorHex = "#000000",
                canvasBackground = VectorizerBg.PURE_WHITE
            )

            VectorizerPreset.SMOOTH_BEZIER_CALLIGRAPHY -> VectorizerConfig(
                mode = TracingMode.CENTERLINE,
                thresholdMode = ThresholdMode.OTSU_AUTO,
                detailLevel = 0.25f,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.75f,
                cornerThresholdAngle = 75f,
                minPathArea = 5,
                denoiseRadius = 1,
                strokeWidth = 2.5f,
                strokeColorHex = "#E5C158",
                fillColorHex = "#E5C158",
                canvasBackground = VectorizerBg.OBSIDIAN_BLACK
            )

            VectorizerPreset.MULTI_TONE_REALISTIC -> VectorizerConfig(
                mode = TracingMode.MULTILAYER_TONAL,
                thresholdMode = ThresholdMode.OTSU_AUTO,
                tonalLayers = 4,
                detailLevel = 0.08f,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.45f,
                cornerThresholdAngle = 55f,
                minPathArea = 3,
                denoiseRadius = 1,
                strokeWidth = 1.0f,
                strokeColorHex = "#E5C158",
                fillColorHex = "#E5C158",
                canvasBackground = VectorizerBg.OBSIDIAN_BLACK
            )

            VectorizerPreset.HISTORICAL_MANUSCRIPT -> VectorizerConfig(
                mode = TracingMode.OUTLINE,
                thresholdMode = ThresholdMode.ADAPTIVE_LOCAL,
                adaptiveBlockSize = 25,
                adaptiveC = 10,
                contrast = 1.35f,
                detailLevel = 0.06f,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.40f,
                cornerThresholdAngle = 50f,
                minPathArea = 4,
                denoiseRadius = 1,
                strokeWidth = 1.8f,
                strokeColorHex = "#D4AF37",
                fillColorHex = "#D4AF37",
                canvasBackground = VectorizerBg.PARCHMENT
            )
        }
    }
}

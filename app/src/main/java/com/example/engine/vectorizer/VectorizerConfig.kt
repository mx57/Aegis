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
    ),
    COLOR_QUANTIZED(
        "Цветной вектор (K-Means палитра)",
        "Кластеризация цветов и разделение на векторные цветовые слои. Идеально для цветных рисунков и цветных тату"
    ),
    ENGRAVING_HATCHING(
        "Гравюрная штриховка (Woodcut)",
        "Генерация векторных гравюрных линий и штриховки с модуляцией толщины по яркости. Классический стиль гравюры"
    )
}

/**
 * Binarization and thresholding techniques.
 */
enum class ThresholdMode(val titleRu: String) {
    OTSU_AUTO("Оцу (Автоматический расчёт гистограммы)"),
    ADAPTIVE_LOCAL("Адаптивный локальный (Интегральное изображение)"),
    SAUVOLA_LOCAL("Sauvola (Локальный порог с дисперсией)"),
    MANUAL("Ручной порог яркости")
}

/**
 * Morphological filtering operations.
 */
enum class MorphologyOp(val titleRu: String, val descriptionRu: String) {
    NONE("Без морфологии", "Прямая бинаризация пикселей"),
    DILATE_1PX("Дилатация (Утолщение)", "Заполняет микро-трещины и утолщает волосяные штрихи"),
    ERODE_1PX("Эрозия (Утоньшение)", "Устраняет слипание близких параллельных линий"),
    CLOSE_GAP("Замыкание (Closing)", "Устраняет разрывы контура без раздувания толщины")
}

/**
 * Visual styling and metallic shader palettes for SVG and UI.
 */
enum class ColorStyle(val titleRu: String, val gradientId: String?, val primaryHex: String) {
    SACRED_GOLD_3D("Сакральное золото 3D", "sacredGoldGrad", "#E5C158"),
    SILVER_CHROME("Серебряный хром", "silverChromeGrad", "#CBD5E1"),
    ANTIQUE_BRONZE("Античная бронза", "antiqueBronzeGrad", "#CD7F32"),
    TATTOO_STENCIL_PURPLE("Фиолетовый трансфер (Tattoo)", null, "#581C87"),
    OBSIDIAN_CHARCOAL("Угольный обсидиан", null, "#1C1917"),
    PURE_BLACK("Чистый чёрный", null, "#000000"),
    NORDIC_TEAL("Северная бирюза", null, "#0D9488"),
    BLOOD_CRIMSON("Кровавый кармин", null, "#991B1B"),
    CUSTOM_COLOR("Выбранный цвет", null, "#E5C158")
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
    PARCHMENT("Древний пергамент (#F4ECD8)", "#F4ECD8"),
    NAVY_SLATE("Тёмный сланец (#0F172A)", "#0F172A")
}

/**
 * Predefined configurations optimized for common artistic and technical workflows.
 */
enum class VectorizerPreset(val titleRu: String, val descriptionRu: String) {
    ULTRA_FIDELITY_100(
        "100% Максимальная точность",
        "Прецизионный субпиксельный захват каждого контура без потерь деталей с сохранением острых углов"
    ),
    TATTOO_STENCIL_LINEWORK(
        "Тату-трафарет (Line Art)",
        "Чёткие осевые линии заданной толщины для термопринтера и трансфера на кожу"
    ),
    HISTORICAL_MANUSCRIPT(
        "Скан старинного манускрипта",
        "Адаптивная локальная фильтрация неоднородного освещения и текстуры пергамента"
    ),
    SAUVOLA_PRECISION(
        "Sauvola Манускрипт (PRO)",
        "Двумерный локальный анализ дисперсии для выявления тончайших выцветших чернил"
    ),
    COLOR_PALETTE_KMEANS(
        "Цветной вектор (K-Means)",
        "Полноцветная векторизация с выделением адаптивной палитры оттенков"
    ),
    SACRED_GOLD_ENGRAVING(
        "Сакральная 3D гравировка",
        "Трёхмерный металлический золотой рельеф с фаской тени"
    ),
    SMOOTH_BEZIER_CALLIGRAPHY(
        "Шелковистое Безье-сглаживание",
        "Мягкие кубические сплайны, устраняющие шум сканирования и ступеньки растра"
    ),
    MULTI_TONE_REALISTIC(
        "Многослойный объём (Полутона)",
        "4-уровневая тональная постеризация для передачи объёмных теней и гравюрной штриховки"
    ),
    WOODCUT_HATCHING(
        "Гравюрная штриховка",
        "Векторные линии штриховки гравюры по дереву"
    )
}

/**
 * Complete set of configurable parameters for the raster-to-SVG vectorizer engine.
 */
data class VectorizerConfig(
    // Mode
    val mode: TracingMode = TracingMode.CENTERLINE,

    // Preprocessing & Image Enhancement
    val maxResolution: Int = 1600,          // 800..4096 (prevents aggressive downsampling for 100% fidelity)
    val sharpnessBoost: Float = 0.5f,       // 0.0f..2.5f (Unsharp Mask edge sharpening)
    val contrast: Float = 1.25f,            // 0.5f..2.5f
    val brightness: Float = 0.0f,           // -50f..50f
    val gamma: Float = 1.0f,                // 0.5f..2.0f
    val denoiseRadius: Int = 1,             // 0..4 (0=none, 1=3x3, 2=5x5)
    val morphology: MorphologyOp = MorphologyOp.NONE,
    val invertPolarity: Boolean = false,    // true: white lines on dark; false: dark lines on light

    // Binarization & Thresholding
    val thresholdMode: ThresholdMode = ThresholdMode.OTSU_AUTO,
    val manualThreshold: Int = 128,         // 0..255
    val adaptiveBlockSize: Int = 19,        // 5..51 (odd)
    val adaptiveC: Int = 8,                 // -30..30
    val sauvolaK: Float = 0.35f,            // 0.1f..0.8f
    val tonalLayers: Int = 4,               // 2..8 for MULTILAYER_TONAL
    val colorClusters: Int = 4,             // 2..12 for COLOR_QUANTIZED

    // Curve Fitting & Geometry
    val detailLevel: Float = 0.10f,         // Epsilon tolerance: 0.005f (100% extreme precision) to 1.5f (simplified)
    val subPixelInterpolation: Boolean = true, // Sub-pixel boundary estimation
    val curveFitting: CurveFittingType = CurveFittingType.CUBIC_BEZIER,
    val smoothFactor: Float = 0.55f,        // 0.0f (straight) to 1.0f (full curvature)
    val cornerThresholdAngle: Float = 60f,  // 20°..120° (angles sharper than this stay sharp corners)
    val minPathArea: Int = 3,               // Minimum pixel length / area to filter noise specks

    // Engraving & Hatching
    val hatchingAngle: Float = 45f,         // 0..180 degrees
    val hatchingDensity: Int = 8,           // 4..20 px spacing

    // Styling & SVG Appearance
    val colorStyle: ColorStyle = ColorStyle.SACRED_GOLD_3D,
    val strokeColorHex: String = "#E5C158", // Sacred gold by default
    val fillColorHex: String = "#E5C158",
    val strokeWidth: Float = 2.2f,          // 0.5f..8.0f
    val enableSvgDropShadow: Boolean = true,
    val canvasBackground: VectorizerBg = VectorizerBg.OBSIDIAN_BLACK,
    val exportScale: Float = 1.0f
) {
    companion object {
        fun fromPreset(preset: VectorizerPreset): VectorizerConfig = when (preset) {
            VectorizerPreset.ULTRA_FIDELITY_100 -> VectorizerConfig(
                mode = TracingMode.OUTLINE,
                thresholdMode = ThresholdMode.OTSU_AUTO,
                maxResolution = 2400,
                sharpnessBoost = 0.6f,
                detailLevel = 0.02f, // Extreme 100% precision
                subPixelInterpolation = true,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.35f,
                cornerThresholdAngle = 45f,
                minPathArea = 1,
                denoiseRadius = 0,
                strokeWidth = 1.5f,
                colorStyle = ColorStyle.SACRED_GOLD_3D,
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
                colorStyle = ColorStyle.TATTOO_STENCIL_PURPLE,
                strokeColorHex = "#581C87",
                fillColorHex = "#581C87",
                canvasBackground = VectorizerBg.PURE_WHITE
            )

            VectorizerPreset.HISTORICAL_MANUSCRIPT -> VectorizerConfig(
                mode = TracingMode.OUTLINE,
                thresholdMode = ThresholdMode.ADAPTIVE_LOCAL,
                adaptiveBlockSize = 25,
                adaptiveC = 10,
                contrast = 1.35f,
                sharpnessBoost = 0.5f,
                detailLevel = 0.06f,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.40f,
                cornerThresholdAngle = 50f,
                minPathArea = 4,
                denoiseRadius = 1,
                strokeWidth = 1.8f,
                colorStyle = ColorStyle.SACRED_GOLD_3D,
                strokeColorHex = "#D4AF37",
                fillColorHex = "#D4AF37",
                canvasBackground = VectorizerBg.PARCHMENT
            )

            VectorizerPreset.SAUVOLA_PRECISION -> VectorizerConfig(
                mode = TracingMode.OUTLINE,
                thresholdMode = ThresholdMode.SAUVOLA_LOCAL,
                adaptiveBlockSize = 27,
                sauvolaK = 0.34f,
                contrast = 1.30f,
                sharpnessBoost = 0.8f,
                morphology = MorphologyOp.CLOSE_GAP,
                detailLevel = 0.03f,
                subPixelInterpolation = true,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.35f,
                cornerThresholdAngle = 45f,
                minPathArea = 2,
                denoiseRadius = 1,
                colorStyle = ColorStyle.SACRED_GOLD_3D,
                strokeColorHex = "#E5C158",
                fillColorHex = "#E5C158",
                canvasBackground = VectorizerBg.OBSIDIAN_BLACK
            )

            VectorizerPreset.COLOR_PALETTE_KMEANS -> VectorizerConfig(
                mode = TracingMode.COLOR_QUANTIZED,
                colorClusters = 5,
                detailLevel = 0.05f,
                subPixelInterpolation = true,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.40f,
                cornerThresholdAngle = 50f,
                minPathArea = 3,
                denoiseRadius = 1,
                canvasBackground = VectorizerBg.TRANSPARENT
            )

            VectorizerPreset.SACRED_GOLD_ENGRAVING -> VectorizerConfig(
                mode = TracingMode.OUTLINE,
                thresholdMode = ThresholdMode.OTSU_AUTO,
                sharpnessBoost = 0.7f,
                detailLevel = 0.02f,
                subPixelInterpolation = true,
                curveFitting = CurveFittingType.CUBIC_BEZIER,
                smoothFactor = 0.35f,
                cornerThresholdAngle = 45f,
                minPathArea = 2,
                denoiseRadius = 0,
                colorStyle = ColorStyle.SACRED_GOLD_3D,
                strokeColorHex = "#E5C158",
                fillColorHex = "#E5C158",
                enableSvgDropShadow = true,
                canvasBackground = VectorizerBg.OBSIDIAN_BLACK
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
                colorStyle = ColorStyle.SACRED_GOLD_3D,
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
                colorStyle = ColorStyle.SACRED_GOLD_3D,
                strokeColorHex = "#E5C158",
                fillColorHex = "#E5C158",
                canvasBackground = VectorizerBg.OBSIDIAN_BLACK
            )

            VectorizerPreset.WOODCUT_HATCHING -> VectorizerConfig(
                mode = TracingMode.ENGRAVING_HATCHING,
                hatchingDensity = 7,
                hatchingAngle = 45f,
                contrast = 1.30f,
                sharpnessBoost = 0.5f,
                detailLevel = 0.10f,
                strokeWidth = 1.6f,
                colorStyle = ColorStyle.SACRED_GOLD_3D,
                strokeColorHex = "#E5C158",
                fillColorHex = "#E5C158",
                canvasBackground = VectorizerBg.OBSIDIAN_BLACK
            )
        }
    }
}

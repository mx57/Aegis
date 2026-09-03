package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import com.example.data.model.StrokePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class SketchStyle(val titleRu: String, val descriptionRu: String) {
    ODIN_TOTEM("Тотем Одина (Волк и Ворон)", "Оберег со стражами Асгарда: Волк Фенрир, Ворон Хугин, кованая цепь и рунический обелиск"),
    VIKING_CHAIN("Кованая цепь викингов", "Шипованная цепь Глейпнир, кельтский медальон и гранёные лучи"),
    WOODCUT_ENGRAVING("Гравюра и штриховка", "Художественный карандашный эскиз, штриховка тушью и резьба по камню"),
    RUNIC_OBELISK("Руническая стела", "Обелиск с вертикальными рунами, окруженный гранёной звездой"),
    SACRED_GOLD("Сакральное Золото", "3D объемное червленое золото, небесные орбиты и сияющая астролябия"),
    EMERALD_BRONZE("Древняя Бронза и Изумруд", "Благородная патинированная бронза с глубоким изумрудным свечением"),
    FROST_CRYSTAL("Ледяной Кристалл", "Кристаллический морозный рельеф с выпуклой платиновой гранью"),
    NORDIC_TATTOO("Тату-Блэкворк", "Четкая тату-графика, акцентные ромбы, плотные линии и чистый контраст"),
    VALKYRIE_SILVER("Серебро Валькирий", "Лунная платина, тончайшая вязь и морозное сияние севера"),
    ORNAMENTAL("Орнаментальный", "Северные узоры, засечки, трезубцы Агисхьяльма и узлы"),
    WOODCARVE("Резьба по камню", "Аутентичные 3D срезы и глубокая высечка рунических камней Еллинге"),
    CELTIC_KNOT("Кельтская вязь", "Сакральные петли, трикветры и переплетающиеся узлы"),
    AEGISHJALMUR("Шлем Ужаса", "Исландские гальдраставы, тройные вилы и защитные кресты"),
    DOTWORK("Сакральный Дотворк", "Сакральная геометрия, звездные точки и растушевка"),
    BLACKWORK("Блэкворк (Классик)", "Массивные контрастные линии, ромбы и стреловидные наконечники"),
    STRICT("Строгий манускрипт", "Чистая геометрия, археологическая строгость линий")
}

enum class FrameStyle(val titleRu: String) {
    NONE("Без рамки"),
    SPIKED_CHAIN("Шипованная цепь"),
    CELTIC_MEDALLION("Кельтский медальон"),
    SOLAR_CIRCLE("Солнечный круг"),
    CELESTIAL_ASTROLABE("Небесная астролябия"),
    YGGDRASIL_BRANCHES("Ветви Иггдрасиля"),
    NORDIC_BRAID("Скандинавская плетёнка"),
    RUNIC_SERPENT("Змей Мидгарда"),
    COMPASS_RAYS("Лучи Вегвизира"),
    SACRED_OCTAGON("Сакральный октагон")
}

enum class FinialType(val titleRu: String) {
    DEFAULT("Классические"),
    TRIDENT("Трезубцы (Вилы)"),
    ARROWS("Стрелы Тюра"),
    CIRCLES_DOTS("Кольца и точки"),
    CROSSBARS("Защитные засечки"),
    SPIRALS("Спирали и рога")
}

enum class CenterEmblem(val titleRu: String) {
    NONE("Без символа"),
    BEASTS_OF_ODIN("Волк и Ворон"),
    FACETED_STAR("Гранёная звезда"),
    RUNIC_STELE("Стела-обелиск"),
    YGGDRASIL_TREE("Древо Иггдрасиль"),
    VALKNUT("Валькнут"),
    TRIQUETRA("Трикветр"),
    SOLAR_CROSS("Солнечный крест"),
    INGUZ_DIAMOND("Око Ингуз"),
    AEGISHJALMUR_CORE("Ядро Агисхьяльма")
}

enum class CornerStyle(val titleRu: String) {
    NONE("Без углов"),
    NORSE_KNOTS("Северные узлы"),
    RUNIC_BINDS("Бинд-руны"),
    SHIELD_STUDS("Заклёпки щита"),
    SUN_RAYS("Солнечные лучи")
}

enum class CanvasTheme(
    val titleRu: String,
    val descriptionRu: String,
    val bgHex: String,
    val bgEdgeHex: String,
    val strokeHex: String,
    val accentHex: String,
    val glowHex: String,
    val highlightHex: String = "#FFF5D1",
    val shadowHex: String = "#000000"
) {
    GRAPHITE_SKETCH("Графитовый эскиз", "Художественный рисунок карандашом и тушью как на фото", "#F4EFE6", "#E4DAC8", "#1E1A16", "#423830", "#332B25", "#FFFFFF", "#1E1A16"),
    GOLDEN_EMBER("Небесное Золото", "Космический обсидиан с благородным червонным золотом", "#090B10", "#030407", "#E5C158", "#F3D882", "#E5C158", "#FFF3BC", "#000000"),
    EMERALD_PATINA("Изумрудная Бронза", "Византийская бронза с окисленной изумрудной патиной", "#0A1310", "#030806", "#CD9B51", "#52B788", "#2A9D8F", "#FFE8B6", "#000000"),
    AURORA_NIGHT("Северное Сияние", "Глубокая полярная ночь с изумрудно-лунным сиянием", "#0A1017", "#04070B", "#7EE0D2", "#89DDFF", "#64FFDA", "#E0FFFF", "#000000"),
    VALKYRIE_MITHRIL("Серебро Валькирий", "Темный антрацит с лунным платиновым серебром", "#121418", "#08090B", "#E2E8F0", "#94A3B8", "#CBD5E1", "#FFFFFF", "#000000"),
    FROST_ICE("Ледяная Платина", "Арктический морозный лед со скандинавской платиной", "#0C141D", "#050A0F", "#B0E0E6", "#708090", "#87CEFA", "#F0F8FF", "#000000"),
    DARK_SLATE("Тёмный сланец", "Мистический графит с теплым античным золотом", "#14181F", "#0A0D12", "#E5C07B", "#D19A66", "#E5C07B", "#FFE6A7", "#000000"),
    ANCIENT_PARCHMENT("Древний пергамент", "Состаренная бумага с тёмными ореховыми чернилами", "#F5EEDC", "#D9C6A5", "#2B190E", "#7A4924", "#8C5835", "#FFFDF7", "#1A0F08"),
    CHARCOAL_DARK("Тёмный уголь", "Глубокая гравюра углём на темном сланце", "#161311", "#0C0A09", "#E8DFD0", "#C8B69B", "#E8DFD0", "#FFFFFF", "#000000"),
    RUNESTONE_GRAY("Скандинавский гранит", "Высеченный в северном камне рельеф с лазурью", "#1C2128", "#12151A", "#88C0D0", "#5E81AC", "#81A1C1", "#E0F7FA", "#000000"),
    STENCIL("Трафарет для тату", "Чистый черно-белый вектор для перевода на кожу", "#FFFFFF", "#FFFFFF", "#000000", "#222222", "#000000", "#FFFFFF", "#000000")
}

val ELDER_FUTHARK_RUNES = listOf(
    "ᚠ", "ᚢ", "ᚦ", "ᚨ", "ᚱ", "ᚲ", "ᚷ", "ᚹ",
    "ᚺ", "ᚾ", "ᛁ", "ᛃ", "ᛇ", "ᛈ", "ᛉ", "ᛊ",
    "ᛏ", "ᛒ", "ᛖ", "ᛗ", "ᛚ", "ᛜ", "ᛞ", "ᛟ"
)

data class SketchConfig(
    val style: SketchStyle = SketchStyle.ORNAMENTAL,
    val theme: CanvasTheme = CanvasTheme.DARK_SLATE,
    val lineWidth: Float = 3.5f,
    val hasFrameCircle: Boolean = true,
    val frameStyle: FrameStyle = FrameStyle.SOLAR_CIRCLE,
    val finialType: FinialType = FinialType.TRIDENT,
    val centerEmblem: CenterEmblem = CenterEmblem.SOLAR_CROSS,
    val cornerStyle: CornerStyle = CornerStyle.NORSE_KNOTS,
    val hasSymmetryAccents: Boolean = true,
    val hasBranchNotches: Boolean = true,
    val hasRayBurst: Boolean = false,
    val hasRunering: Boolean = false,
    val hasGlowEffect: Boolean = false,
    val wobbleAmount: Float = 0.20f, // 0.0f..1.0f
    val seed: Long = 1337L,
    val isStencil: Boolean = false,
    val hasVolumetricShading: Boolean = true,
    val hasTextureGrain: Boolean = true,
    val runeChiselDepth: Float = 1.0f
) {
    val effectiveTheme: CanvasTheme
        get() = if (isStencil) CanvasTheme.STENCIL else theme
}

object SvgStaveRenderer {

    /**
     * Generates a fully compliant, highly detailed SVG string from a composed stave.
     */
    fun renderSvg(stave: ComposedStave, config: SketchConfig): String {
        val prng = Random(config.seed)
        val sb = StringBuilder()

        val theme = config.effectiveTheme
        val bgColor = theme.bgHex
        val strokeColor = if (config.isStencil) "#000000" else when (config.theme) {
            CanvasTheme.DARK_SLATE -> when (config.style) {
                SketchStyle.SACRED_GOLD -> "#E5C158"
                SketchStyle.EMERALD_BRONZE -> "#CD9B51"
                SketchStyle.FROST_CRYSTAL -> "#B0E0E6"
                SketchStyle.NORDIC_TATTOO -> "#F8FAFC"
                SketchStyle.VALKYRIE_SILVER -> "#E2E8F0"
                SketchStyle.STRICT -> "#E5E9F0"
                SketchStyle.ORNAMENTAL -> "#E5C07B"
                SketchStyle.WOODCARVE -> "#D19A66"
                SketchStyle.CELTIC_KNOT -> "#98C379"
                SketchStyle.AEGISHJALMUR -> "#61AFEF"
                SketchStyle.DOTWORK -> "#D8DEE9"
                SketchStyle.BLACKWORK -> "#ECEFF4"
                else -> theme.strokeHex
            }
            CanvasTheme.GRAPHITE_SKETCH -> theme.strokeHex
            CanvasTheme.CHARCOAL_DARK -> theme.strokeHex
            CanvasTheme.GOLDEN_EMBER -> "#E5C158"
            CanvasTheme.EMERALD_PATINA -> "#CD9B51"
            CanvasTheme.AURORA_NIGHT -> "#7EE0D2"
            CanvasTheme.VALKYRIE_MITHRIL -> "#E2E8F0"
            CanvasTheme.FROST_ICE -> "#B0E0E6"
            CanvasTheme.ANCIENT_PARCHMENT -> theme.strokeHex
            CanvasTheme.RUNESTONE_GRAY -> theme.strokeHex
            CanvasTheme.STENCIL -> "#000000"
        }

        val effectiveStrokeWidth = when (config.style) {
            SketchStyle.BLACKWORK -> (config.lineWidth * 2.2f).coerceAtLeast(5.5f)
            SketchStyle.NORDIC_TATTOO -> (config.lineWidth * 1.35f).coerceAtLeast(3.8f)
            SketchStyle.ODIN_TOTEM -> (config.lineWidth * 1.25f).coerceAtLeast(3.2f)
            SketchStyle.VIKING_CHAIN -> (config.lineWidth * 1.30f).coerceAtLeast(3.4f)
            SketchStyle.WOODCUT_ENGRAVING -> config.lineWidth * 1.15f
            SketchStyle.RUNIC_OBELISK -> config.lineWidth * 1.20f
            SketchStyle.WOODCARVE -> config.lineWidth * 1.25f
            SketchStyle.SACRED_GOLD -> config.lineWidth * 0.95f
            SketchStyle.VALKYRIE_SILVER -> config.lineWidth * 0.90f
            SketchStyle.STRICT -> config.lineWidth * 0.85f
            else -> config.lineWidth
        }

        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500" width="500" height="500">""").append("\n")

        // Definitions: Vignette gradient, Gold/Metallic 3D gradients, Shadow & Glow filters
        sb.append("  <defs>\n")
        if (!config.isStencil) {
            sb.append("""    <radialGradient id="bgVignette" cx="50%" cy="50%" r="72%">""").append("\n")
            sb.append("""      <stop offset="0%" stop-color="${theme.bgHex}"/>""").append("\n")
            sb.append("""      <stop offset="100%" stop-color="${theme.bgEdgeHex}"/>""").append("\n")
            sb.append("""    </radialGradient>""").append("\n")

            // Metallic Gold / Bronze 3D Linear Gradient for volumetric strokes
            sb.append("""    <linearGradient id="gold3dGrad" x1="0%" y1="0%" x2="100%" y2="100%">""").append("\n")
            sb.append("""      <stop offset="0%" stop-color="${theme.highlightHex}"/>""").append("\n")
            sb.append("""      <stop offset="35%" stop-color="${theme.strokeHex}"/>""").append("\n")
            sb.append("""      <stop offset="70%" stop-color="${theme.accentHex}"/>""").append("\n")
            sb.append("""      <stop offset="100%" stop-color="${theme.strokeHex}"/>""").append("\n")
            sb.append("""    </linearGradient>""").append("\n")

            // Volumetric Drop Shadow filter for carved/embossed depth
            sb.append("""    <filter id="chiselDropShadow" x="-20%" y="-20%" width="140%" height="140%">""").append("\n")
            sb.append("""      <feDropShadow dx="1.8" dy="2.4" stdDeviation="1.5" flood-color="${theme.shadowHex}" flood-opacity="0.85"/>""").append("\n")
            sb.append("""    </filter>""").append("\n")
        }
        if (config.hasGlowEffect && !config.isStencil) {
            sb.append("""    <filter id="sacredGlow" x="-20%" y="-20%" width="140%" height="140%">""").append("\n")
            sb.append("""      <feGaussianBlur stdDeviation="3.0" result="blur"/>""").append("\n")
            sb.append("""      <feMerge>""").append("\n")
            sb.append("""        <feMergeNode in="blur"/>""").append("\n")
            sb.append("""        <feMergeNode in="SourceGraphic"/>""").append("\n")
            sb.append("""      </feMerge>""").append("\n")
            sb.append("""    </filter>""").append("\n")
        }
        sb.append("  </defs>\n")

        // Background
        if (!config.isStencil) {
            sb.append("""  <rect width="100%" height="100%" fill="url(#bgVignette)"/>""").append("\n")
        } else {
            sb.append("""  <rect width="100%" height="100%" fill="$bgColor"/>""").append("\n")
        }

        // Sacred Celestial Geometry background (for Sacred Gold style)
        if (config.style == SketchStyle.SACRED_GOLD && !config.isStencil) {
            val cx = 250f
            val cy = 250f
            val orbitRadii = listOf(46f, 92f, 142f, 236f)
            for (r in orbitRadii) {
                sb.append("""  <circle cx="250" cy="250" r="${r.format()}" fill="none" stroke="$strokeColor" stroke-width="0.7" opacity="0.26"/>""").append("\n")
            }
            for (i in 0 until 8) {
                val a = (PI / 4 * i).toFloat()
                val x1 = cx + 38f * cos(a)
                val y1 = cy + 38f * sin(a)
                val x2 = cx + 236f * cos(a)
                val y2 = cy + 236f * sin(a)
                sb.append("""  <line x1="${x1.format()}" y1="${y1.format()}" x2="${x2.format()}" y2="${y2.format()}" stroke="$strokeColor" stroke-width="0.6" opacity="0.16"/>""").append("\n")
            }
        }

        // 1. Ray burst aura background (if enabled)
        if (config.hasRayBurst && !config.isStencil) {
            val cx = 250f
            val cy = 250f
            val rays = 32
            for (i in 0 until rays) {
                val angle = (2 * PI * i / rays).toFloat()
                val r1 = 50f
                val r2 = 210f
                val x1 = cx + r1 * cos(angle)
                val y1 = cy + r1 * sin(angle)
                val x2 = cx + r2 * cos(angle)
                val y2 = cy + r2 * sin(angle)
                sb.append("""  <line x1="${x1.format()}" y1="${y1.format()}" x2="${x2.format()}" y2="${y2.format()}" stroke="$strokeColor" stroke-width="0.8" opacity="0.18"/>""").append("\n")
            }
        }

        // 1.1 Outer Elder Futhark Rune Ring
        if (config.hasRunering && !config.isStencil) {
            renderFutharkRuneringSvg(sb, strokeColor, effectiveStrokeWidth)
        }

        // 2. Decorative Frame
        val effectiveFrame = if (!config.hasFrameCircle) FrameStyle.NONE else config.frameStyle
        if (effectiveFrame != FrameStyle.NONE) {
            val frameOrnaments = OrnamentGeometry.generateFrame(effectiveFrame, effectiveStrokeWidth)
            renderGeneratedOrnamentsSvg(sb, frameOrnaments, strokeColor, effectiveStrokeWidth)
        }

        // 3. Stave Strokes (optionally wrapped in glow filter)
        val filterAttr = if (config.hasGlowEffect && !config.isStencil) """ filter="url(#sacredGlow)"""" else ""
        if (filterAttr.isNotEmpty()) {
            sb.append("""  <g$filterAttr>""").append("\n")
        }
        for (stroke in stave.strokes) {
            val pts = if (config.wobbleAmount > 0.01f) {
                applyWobble(stroke.points, config.wobbleAmount, prng)
            } else {
                stroke.points
            }

            if (pts.size < 2) continue

            val strokeW = if (stroke.isHairlineGuide) {
                (effectiveStrokeWidth * 0.45f).coerceAtLeast(0.8f)
            } else {
                effectiveStrokeWidth
            }
            val strokeOpacity = if (stroke.isHairlineGuide) "0.55" else "1.0"

            when (config.style) {
                SketchStyle.DOTWORK -> {
                    renderDotworkStrokeSvg(sb, pts, strokeColor, prng)
                }
                SketchStyle.WOODCARVE -> {
                    // Double-carved stone incision with chiseled facets
                    renderCarvedStrokeSvg(sb, pts, strokeColor, strokeW)
                }
                SketchStyle.SACRED_GOLD, SketchStyle.EMERALD_BRONZE, SketchStyle.FROST_CRYSTAL -> {
                    // 3D Volumetric Metallic Embossed Stroke with Specular Highlight line
                    renderVolumetricMetallicStrokeSvg(sb, pts, theme, strokeW, strokeOpacity, config)
                }
                else -> {
                    // Standard Path rendering with optional 3D drop shadow
                    val shadowAttr = if (config.hasVolumetricShading && !config.isStencil) """ filter="url(#chiselDropShadow)"""" else ""
                    sb.append("""  <path d="M ${pts[0].x.format()} ${pts[0].y.format()} """)
                    for (i in 1 until pts.size) {
                        sb.append("""L ${pts[i].x.format()} ${pts[i].y.format()} """)
                    }
                    sb.append("""" fill="none" stroke="$strokeColor" stroke-width="${strokeW.format()}" opacity="$strokeOpacity"$shadowAttr stroke-linecap="round" stroke-linejoin="round"/>""").append("\n")

                    if ((config.style == SketchStyle.BLACKWORK || config.style == SketchStyle.NORDIC_TATTOO) && stroke.isOuterPole) {
                        // Diamond joint caps at outer endpoints
                        renderDiamondCapSvg(sb, pts.first().x, pts.first().y, strokeColor, 5.0f)
                        renderDiamondCapSvg(sb, pts.last().x, pts.last().y, strokeColor, 5.0f)
                    }
                }
            }

            // Protective branch charm notches along stroke line - ONLY on central stems to prevent clutter
            if (config.hasBranchNotches && stroke.isStem && pts.size >= 2) {
                val notches = OrnamentGeometry.generateStrokeNotches(pts.first(), pts.last())
                for (notch in notches) {
                    sb.append("""  <line x1="${notch.x1.format()}" y1="${notch.y1.format()}" x2="${notch.x2.format()}" y2="${notch.y2.format()}" stroke="$strokeColor" stroke-width="${(effectiveStrokeWidth * 0.75f).format()}"/>""").append("\n")
                }
            }

            // Terminal Finials at the end of the stroke (facing outward) - ONLY on outer poles
            if (config.finialType != FinialType.DEFAULT && stroke.isOuterPole && pts.size >= 2) {
                val pLast = pts.last()
                val pPrev = pts[pts.size - 2]
                val dirX = pLast.x - pPrev.x
                val dirY = pLast.y - pPrev.y
                val finials = OrnamentGeometry.generateFinial(pLast, dirX, dirY, config.finialType)
                renderGeneratedOrnamentsSvg(sb, finials, strokeColor, effectiveStrokeWidth)
            } else if (config.style == SketchStyle.ORNAMENTAL && stroke.isOuterPole && pts.size >= 2) {
                // Subtle ornamental dot terminals
                renderOrnamentalAccentsSvg(sb, pts.first(), pts.last(), strokeColor)
            }
        }

        // 4. Central Sacred Emblem
        if (config.centerEmblem != CenterEmblem.NONE) {
            val centerOrnaments = OrnamentGeometry.generateCenterEmblem(config.centerEmblem, effectiveStrokeWidth)
            renderGeneratedOrnamentsSvg(sb, centerOrnaments, strokeColor, effectiveStrokeWidth)
        }

        // 5. Corner Accents
        if (config.hasSymmetryAccents && !config.isStencil && config.cornerStyle != CornerStyle.NONE) {
            val cornerOrnaments = OrnamentGeometry.generateCorners(config.cornerStyle, effectiveStrokeWidth)
            renderGeneratedOrnamentsSvg(sb, cornerOrnaments, strokeColor, effectiveStrokeWidth)
        }

        if (filterAttr.isNotEmpty()) {
            sb.append("  </g>\n")
        }

        sb.append("</svg>")
        return sb.toString()
    }

    private fun renderFutharkRuneringSvg(sb: StringBuilder, color: String, baseWidth: Float) {
        val rInner = 218f
        val rOuter = 244f
        val rText = 231f
        val sw = (baseWidth * 0.45f).coerceAtLeast(0.8f).format()
        sb.append("""  <circle cx="250" cy="250" r="$rInner" fill="none" stroke="$color" stroke-width="$sw" opacity="0.6"/>""").append("\n")
        sb.append("""  <circle cx="250" cy="250" r="$rOuter" fill="none" stroke="$color" stroke-width="$sw" opacity="0.6"/>""").append("\n")
        val totalRunes = ELDER_FUTHARK_RUNES.size
        for (i in 0 until totalRunes) {
            val deg = i * (360f / totalRunes)
            val rune = ELDER_FUTHARK_RUNES[i]
            sb.append("""  <text x="250" y="${(250 - rText + 5.5f).format()}" transform="rotate(${deg.format()}, 250, 250)" text-anchor="middle" font-size="14" font-family="serif" font-weight="bold" fill="$color" opacity="0.9">$rune</text>""").append("\n")
        }
    }

    private fun renderGeneratedOrnamentsSvg(
        sb: StringBuilder,
        ornaments: GeneratedOrnaments,
        color: String,
        baseStrokeWidth: Float
    ) {
        for (line in ornaments.lines) {
            val sw = (baseStrokeWidth * line.widthFactor).coerceAtLeast(0.8f).format()
            val op = if (line.alpha < 0.99f) """ opacity="${line.alpha.format()}"""" else ""
            sb.append("""  <line x1="${line.x1.format()}" y1="${line.y1.format()}" x2="${line.x2.format()}" y2="${line.y2.format()}" stroke="$color" stroke-width="$sw"$op stroke-linecap="round"/>""").append("\n")
        }

        for (circle in ornaments.circles) {
            val op = if (circle.alpha < 0.99f) """ opacity="${circle.alpha.format()}"""" else ""
            if (circle.isFilled) {
                sb.append("""  <circle cx="${circle.cx.format()}" cy="${circle.cy.format()}" r="${circle.radius.format()}" fill="$color"$op/>""").append("\n")
            } else {
                val sw = (baseStrokeWidth * circle.widthFactor).coerceAtLeast(0.8f).format()
                sb.append("""  <circle cx="${circle.cx.format()}" cy="${circle.cy.format()}" r="${circle.radius.format()}" fill="none" stroke="$color" stroke-width="$sw"$op/>""").append("\n")
            }
        }

        for (poly in ornaments.polygons) {
            if (poly.points.isEmpty()) continue
            val ptsStr = poly.points.joinToString(" ") { "${it.x.format()},${it.y.format()}" }
            val op = if (poly.alpha < 0.99f) """ opacity="${poly.alpha.format()}"""" else ""
            if (poly.isFilled) {
                sb.append("""  <polygon points="$ptsStr" fill="$color"$op/>""").append("\n")
            } else {
                val sw = (baseStrokeWidth * poly.widthFactor).coerceAtLeast(0.8f).format()
                sb.append("""  <polygon points="$ptsStr" fill="none" stroke="$color" stroke-width="$sw"$op stroke-linejoin="round"/>""").append("\n")
            }
        }

        for (path in ornaments.paths) {
            if (path.points.size < 2) continue
            val sw = (baseStrokeWidth * path.widthFactor).coerceAtLeast(0.8f).format()
            val op = if (path.alpha < 0.99f) """ opacity="${path.alpha.format()}"""" else ""
            sb.append("""  <path d="M ${path.points[0].x.format()} ${path.points[0].y.format()} """)
            for (i in 1 until path.points.size) {
                sb.append("""L ${path.points[i].x.format()} ${path.points[i].y.format()} """)
            }
            if (path.isClosed) sb.append("Z ")
            val fillVal = if (path.isFilled) color else "none"
            sb.append("""" fill="$fillVal" stroke="$color" stroke-width="$sw"$op stroke-linecap="round" stroke-linejoin="round"/>""").append("\n")
        }
    }

    private fun renderVolumetricMetallicStrokeSvg(
        sb: StringBuilder,
        pts: List<StrokePoint>,
        theme: CanvasTheme,
        sw: Float,
        opacity: String,
        config: SketchConfig
    ) {
        val pathD = StringBuilder()
        pathD.append("M ${pts[0].x.format()} ${pts[0].y.format()} ")
        for (i in 1 until pts.size) {
            pathD.append("L ${pts[i].x.format()} ${pts[i].y.format()} ")
        }

        // 1. Base Ambient/Shadow Layer for 3D depth
        if (config.hasVolumetricShading && !config.isStencil) {
            sb.append("""  <path d="$pathD" fill="none" stroke="${theme.shadowHex}" stroke-width="${(sw * 1.5f).format()}" opacity="0.55" stroke-linecap="round" stroke-linejoin="round" transform="translate(1.2, 1.6)"/>""").append("\n")
        }

        // 2. Main Metallic Body Path (Gradient / Stroke Color)
        val strokeFill = if (!config.isStencil) "url(#gold3dGrad)" else theme.strokeHex
        sb.append("""  <path d="$pathD" fill="none" stroke="$strokeFill" stroke-width="${sw.format()}" opacity="$opacity" stroke-linecap="round" stroke-linejoin="round"/>""").append("\n")

        // 3. Specular Highlight Core Line
        if (config.hasVolumetricShading && !config.isStencil && sw > 1.5f) {
            val hlWidth = (sw * 0.35f).coerceAtLeast(0.6f)
            sb.append("""  <path d="$pathD" fill="none" stroke="${theme.highlightHex}" stroke-width="${hlWidth.format()}" opacity="0.85" stroke-linecap="round" stroke-linejoin="round" transform="translate(-0.4, -0.5)"/>""").append("\n")
        }
    }

    private fun renderCarvedStrokeSvg(sb: StringBuilder, pts: List<StrokePoint>, color: String, sw: Float) {
        val pathD = StringBuilder()
        pathD.append("M ${pts[0].x.format()} ${pts[0].y.format()} ")
        for (i in 1 until pts.size) {
            pathD.append("L ${pts[i].x.format()} ${pts[i].y.format()} ")
        }

        // 1. Chiseled Deep Ambient Shadow
        sb.append("""  <path d="$pathD" fill="none" stroke="#000000" stroke-width="${(sw * 1.6f).format()}" opacity="0.65" stroke-linecap="round" stroke-linejoin="round" transform="translate(1.5, 1.8)"/>""").append("\n")

        // 2. Main chiseled groove body
        sb.append("""  <path d="$pathD" fill="none" stroke="$color" stroke-width="${sw.format()}" stroke-linecap="round" stroke-linejoin="round"/>""").append("\n")

        // 3. Inner Bevel Light Specular Reflection
        sb.append("""  <path d="$pathD" fill="none" stroke="#FFFFFF" stroke-width="${(sw * 0.30f).coerceAtLeast(0.6f).format()}" opacity="0.50" stroke-linecap="round" stroke-linejoin="round" transform="translate(-0.6, -0.6)"/>""").append("\n")
    }

    private fun renderDotworkStrokeSvg(sb: StringBuilder, pts: List<StrokePoint>, strokeColor: String, prng: Random) {
        for (i in 0 until pts.size - 1) {
            val p1 = pts[i]
            val p2 = pts[i + 1]
            val dist = sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))
            val steps = (dist / 6.5f).toInt().coerceAtLeast(2)

            for (s in 0..steps) {
                val t = s.toFloat() / steps
                val baseX = p1.x + t * (p2.x - p1.x)
                val baseY = p1.y + t * (p2.y - p1.y)

                // Central sacred dot
                sb.append("""  <circle cx="${baseX.format()}" cy="${baseY.format()}" r="2.0" fill="$strokeColor"/>""").append("\n")

                // Flanking stippled satellite dots
                if (prng.nextFloat() > 0.35f) {
                    val jx = baseX + (prng.nextFloat() - 0.5f) * 6.5f
                    val jy = baseY + (prng.nextFloat() - 0.5f) * 6.5f
                    sb.append("""  <circle cx="${jx.format()}" cy="${jy.format()}" r="1.1" fill="$strokeColor" opacity="0.75"/>""").append("\n")
                }
            }
        }
    }

    private fun renderOrnamentalAccentsSvg(sb: StringBuilder, p1: StrokePoint, p2: StrokePoint, color: String) {
        sb.append("""  <circle cx="${p1.x.format()}" cy="${p1.y.format()}" r="3.2" fill="$color"/>""").append("\n")
        sb.append("""  <circle cx="${p2.x.format()}" cy="${p2.y.format()}" r="3.2" fill="$color"/>""").append("\n")
    }

    private fun renderDiamondCapSvg(sb: StringBuilder, x: Float, y: Float, color: String, s: Float = 4.5f) {
        sb.append("""  <polygon points="${x.format()},${(y - s).format()} ${(x + s).format()},${y.format()} ${x.format()},${(y + s).format()} ${(x - s).format()},${y.format()}" fill="$color"/>""").append("\n")
    }

    private fun applyWobble(points: List<StrokePoint>, wobble: Float, prng: Random): List<StrokePoint> {
        val result = mutableListOf<StrokePoint>()
        for (i in points.indices) {
            val pt = points[i]
            val factor = if (i == 0 || i == points.size - 1) 0.25f else 1.0f
            val jx = pt.x + (prng.nextFloat() - 0.5f) * 6f * wobble * factor
            val jy = pt.y + (prng.nextFloat() - 0.5f) * 6f * wobble * factor
            result.add(StrokePoint(jx, jy))
        }
        return result
    }

    /**
     * Offscreen rendering of the stave into high resolution Bitmap (1024, 2048, 4096).
     */
    suspend fun renderToBitmap(
        stave: ComposedStave,
        config: SketchConfig,
        targetSize: Int = 2048
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = targetSize / 500f

        val theme = config.effectiveTheme
        if (!config.isStencil) {
            val gradient = RadialGradient(
                targetSize / 2f, targetSize / 2f, targetSize * 0.72f,
                Color.parseColor(theme.bgHex),
                Color.parseColor(theme.bgEdgeHex),
                Shader.TileMode.CLAMP
            )
            val bgPaint = Paint().apply { shader = gradient }
            canvas.drawRect(0f, 0f, targetSize.toFloat(), targetSize.toFloat(), bgPaint)
        } else {
            val bgPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, targetSize.toFloat(), targetSize.toFloat(), bgPaint)
        }

        val prng = Random(config.seed)
        val strokeColorInt = if (config.isStencil) Color.BLACK else when (config.theme) {
            CanvasTheme.DARK_SLATE -> when (config.style) {
                SketchStyle.SACRED_GOLD -> Color.parseColor("#E5C158")
                SketchStyle.EMERALD_BRONZE -> Color.parseColor("#CD9B51")
                SketchStyle.FROST_CRYSTAL -> Color.parseColor("#B0E0E6")
                SketchStyle.NORDIC_TATTOO -> Color.parseColor("#F8FAFC")
                SketchStyle.VALKYRIE_SILVER -> Color.parseColor("#E2E8F0")
                SketchStyle.STRICT -> Color.parseColor("#E5E9F0")
                SketchStyle.ORNAMENTAL -> Color.parseColor("#E5C07B")
                SketchStyle.WOODCARVE -> Color.parseColor("#D19A66")
                SketchStyle.CELTIC_KNOT -> Color.parseColor("#98C379")
                SketchStyle.AEGISHJALMUR -> Color.parseColor("#61AFEF")
                SketchStyle.DOTWORK -> Color.parseColor("#D8DEE9")
                SketchStyle.BLACKWORK -> Color.parseColor("#ECEFF4")
                else -> Color.parseColor(theme.strokeHex)
            }
            CanvasTheme.GRAPHITE_SKETCH -> Color.parseColor(theme.strokeHex)
            CanvasTheme.CHARCOAL_DARK -> Color.parseColor(theme.strokeHex)
            CanvasTheme.GOLDEN_EMBER -> Color.parseColor("#E5C158")
            CanvasTheme.EMERALD_PATINA -> Color.parseColor("#CD9B51")
            CanvasTheme.AURORA_NIGHT -> Color.parseColor("#7EE0D2")
            CanvasTheme.VALKYRIE_MITHRIL -> Color.parseColor("#E2E8F0")
            CanvasTheme.FROST_ICE -> Color.parseColor("#B0E0E6")
            CanvasTheme.ANCIENT_PARCHMENT -> Color.parseColor(theme.strokeHex)
            CanvasTheme.RUNESTONE_GRAY -> Color.parseColor(theme.strokeHex)
            CanvasTheme.STENCIL -> Color.BLACK
        }

        val effectiveStrokeWidth = when (config.style) {
            SketchStyle.BLACKWORK -> (config.lineWidth * 2.2f).coerceAtLeast(5.5f)
            SketchStyle.NORDIC_TATTOO -> (config.lineWidth * 1.35f).coerceAtLeast(3.8f)
            SketchStyle.ODIN_TOTEM -> (config.lineWidth * 1.25f).coerceAtLeast(3.2f)
            SketchStyle.VIKING_CHAIN -> (config.lineWidth * 1.30f).coerceAtLeast(3.4f)
            SketchStyle.WOODCUT_ENGRAVING -> config.lineWidth * 1.15f
            SketchStyle.RUNIC_OBELISK -> config.lineWidth * 1.20f
            SketchStyle.WOODCARVE -> config.lineWidth * 1.25f
            SketchStyle.SACRED_GOLD -> config.lineWidth * 0.95f
            SketchStyle.VALKYRIE_SILVER -> config.lineWidth * 0.90f
            SketchStyle.STRICT -> config.lineWidth * 0.85f
            else -> config.lineWidth
        } * scale

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColorInt
            style = Paint.Style.STROKE
            strokeWidth = effectiveStrokeWidth
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val glowPaint = Paint(linePaint).apply {
            strokeWidth = effectiveStrokeWidth * 2.4f
            alpha = 55
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColorInt
            style = Paint.Style.FILL
        }

        // Helper to draw generated ornaments on Bitmap canvas
        fun drawOrnamentsOnBitmap(ornaments: GeneratedOrnaments) {
            for (line in ornaments.lines) {
                val lp = Paint(linePaint).apply {
                    strokeWidth = (effectiveStrokeWidth * line.widthFactor).coerceAtLeast(1f * scale)
                    alpha = (line.alpha * 255).toInt().coerceIn(0, 255)
                }
                canvas.drawLine(line.x1 * scale, line.y1 * scale, line.x2 * scale, line.y2 * scale, lp)
            }
            for (c in ornaments.circles) {
                val alphaVal = (c.alpha * 255).toInt().coerceIn(0, 255)
                if (c.isFilled) {
                    val fp = Paint(fillPaint).apply { alpha = alphaVal }
                    canvas.drawCircle(c.cx * scale, c.cy * scale, c.radius * scale, fp)
                } else {
                    val lp = Paint(linePaint).apply {
                        strokeWidth = (effectiveStrokeWidth * c.widthFactor).coerceAtLeast(1f * scale)
                        alpha = alphaVal
                    }
                    canvas.drawCircle(c.cx * scale, c.cy * scale, c.radius * scale, lp)
                }
            }
            for (poly in ornaments.polygons) {
                if (poly.points.size < 3) continue
                val path = Path().apply {
                    moveTo(poly.points[0].x * scale, poly.points[0].y * scale)
                    for (i in 1 until poly.points.size) {
                        lineTo(poly.points[i].x * scale, poly.points[i].y * scale)
                    }
                    close()
                }
                val alphaVal = (poly.alpha * 255).toInt().coerceIn(0, 255)
                if (poly.isFilled) {
                    val fp = Paint(fillPaint).apply { alpha = alphaVal }
                    canvas.drawPath(path, fp)
                } else {
                    val lp = Paint(linePaint).apply {
                        strokeWidth = (effectiveStrokeWidth * poly.widthFactor).coerceAtLeast(1f * scale)
                        alpha = alphaVal
                    }
                    canvas.drawPath(path, lp)
                }
            }
            for (pathGeom in ornaments.paths) {
                if (pathGeom.points.size < 2) continue
                val path = Path().apply {
                    moveTo(pathGeom.points[0].x * scale, pathGeom.points[0].y * scale)
                    for (i in 1 until pathGeom.points.size) {
                        lineTo(pathGeom.points[i].x * scale, pathGeom.points[i].y * scale)
                    }
                    if (pathGeom.isClosed) close()
                }
                val alphaVal = (pathGeom.alpha * 255).toInt().coerceIn(0, 255)
                if (pathGeom.isFilled) {
                    val fp = Paint(fillPaint).apply { alpha = alphaVal }
                    canvas.drawPath(path, fp)
                } else {
                    val lp = Paint(linePaint).apply {
                        strokeWidth = (effectiveStrokeWidth * pathGeom.widthFactor).coerceAtLeast(1f * scale)
                        alpha = alphaVal
                    }
                    canvas.drawPath(path, lp)
                }
            }
        }

        // Celestial Astrolabe background (Sacred Gold)
        if (config.style == SketchStyle.SACRED_GOLD && !config.isStencil) {
            val cx = 250f * scale
            val cy = 250f * scale
            val orbitPaint = Paint(linePaint).apply {
                strokeWidth = 0.7f * scale
                alpha = 65
            }
            val orbitRadii = listOf(46f, 92f, 142f, 236f)
            for (r in orbitRadii) {
                canvas.drawCircle(cx, cy, r * scale, orbitPaint)
            }
            val rayPaint = Paint(linePaint).apply {
                strokeWidth = 0.6f * scale
                alpha = 40
            }
            for (i in 0 until 8) {
                val a = (PI / 4 * i).toFloat()
                canvas.drawLine(
                    cx + 38f * scale * cos(a), cy + 38f * scale * sin(a),
                    cx + 236f * scale * cos(a), cy + 236f * scale * sin(a),
                    rayPaint
                )
            }
        }

        // 1. Ray burst aura
        if (config.hasRayBurst && !config.isStencil) {
            val auraPaint = Paint(linePaint).apply {
                strokeWidth = 0.8f * scale
                alpha = 45
            }
            val cx = 250f * scale
            val cy = 250f * scale
            val rays = 32
            for (i in 0 until rays) {
                val angle = 2 * PI * i / rays
                canvas.drawLine(
                    (cx + 50f * scale * cos(angle)).toFloat(),
                    (cy + 50f * scale * sin(angle)).toFloat(),
                    (cx + 210f * scale * cos(angle)).toFloat(),
                    (cy + 210f * scale * sin(angle)).toFloat(),
                    auraPaint
                )
            }
        }

        // 1.1 Outer Elder Futhark Rune Ring
        if (config.hasRunering && !config.isStencil) {
            val rInner = 218f * scale
            val rOuter = 244f * scale
            val ringPaint = Paint(linePaint).apply {
                strokeWidth = (effectiveStrokeWidth * 0.45f).coerceAtLeast(1f * scale)
                alpha = 150
            }
            canvas.drawCircle(250f * scale, 250f * scale, rInner, ringPaint)
            canvas.drawCircle(250f * scale, 250f * scale, rOuter, ringPaint)

            val runeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = strokeColorInt
                textSize = 14f * scale
                textAlign = Paint.Align.CENTER
                typeface = Typeface.SERIF
                isFakeBoldText = true
                alpha = 230
            }
            val totalRunes = ELDER_FUTHARK_RUNES.size
            for (i in 0 until totalRunes) {
                val deg = i * (360f / totalRunes)
                val rune = ELDER_FUTHARK_RUNES[i]
                canvas.save()
                canvas.rotate(deg, 250f * scale, 250f * scale)
                canvas.drawText(rune, 250f * scale, (250f - 231f + 5.5f) * scale, runeTextPaint)
                canvas.restore()
            }
        }

        // 2. Decorative Frame
        val effectiveFrame = if (!config.hasFrameCircle) FrameStyle.NONE else config.frameStyle
        if (effectiveFrame != FrameStyle.NONE) {
            val frameOrnaments = OrnamentGeometry.generateFrame(effectiveFrame, config.lineWidth)
            drawOrnamentsOnBitmap(frameOrnaments)
        }

        // 2.1 Pre-pass for sacred glow effect
        if (config.hasGlowEffect && !config.isStencil) {
            for (stroke in stave.strokes) {
                val pts = stroke.points
                if (pts.size < 2) continue
                val glowPath = Path().apply {
                    moveTo(pts[0].x * scale, pts[0].y * scale)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x * scale, pts[i].y * scale)
                    }
                }
                canvas.drawPath(glowPath, glowPaint)
            }
        }

        // 3. Stave Strokes
        for (stroke in stave.strokes) {
            val pts = if (config.wobbleAmount > 0.01f) {
                applyWobble(stroke.points, config.wobbleAmount, prng)
            } else {
                stroke.points
            }
            if (pts.size < 2) continue

            if (config.style == SketchStyle.DOTWORK) {
                for (i in 0 until pts.size - 1) {
                    val p1 = pts[i]
                    val p2 = pts[i + 1]
                    val dist = sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y))
                    val steps = (dist / 6.5f).toInt().coerceAtLeast(2)
                    for (s in 0..steps) {
                        val t = s.toFloat() / steps
                        val bx = (p1.x + t * (p2.x - p1.x)) * scale
                        val by = (p1.y + t * (p2.y - p1.y)) * scale
                        canvas.drawCircle(bx, by, 2.0f * scale, fillPaint)
                        if (prng.nextFloat() > 0.35f) {
                            val jx = bx + (prng.nextFloat() - 0.5f) * 6.5f * scale
                            val jy = by + (prng.nextFloat() - 0.5f) * 6.5f * scale
                            val satPaint = Paint(fillPaint).apply { alpha = 190 }
                            canvas.drawCircle(jx, jy, 1.1f * scale, satPaint)
                        }
                    }
                }
            } else {
                val path = Path().apply {
                    moveTo(pts[0].x * scale, pts[0].y * scale)
                    for (i in 1 until pts.size) {
                        lineTo(pts[i].x * scale, pts[i].y * scale)
                    }
                }
                val curPaint = if (stroke.isHairlineGuide) {
                    Paint(linePaint).apply {
                        strokeWidth = (effectiveStrokeWidth * 0.45f).coerceAtLeast(1f * scale)
                        alpha = 140
                    }
                } else {
                    linePaint
                }
                canvas.drawPath(path, curPaint)

                if (config.style == SketchStyle.WOODCARVE) {
                    val shadowPath = Path().apply {
                        val off = effectiveStrokeWidth * 0.45f
                        moveTo(pts[0].x * scale + off, pts[0].y * scale + off)
                        for (i in 1 until pts.size) {
                            lineTo(pts[i].x * scale + off, pts[i].y * scale + off)
                        }
                    }
                    val sp = Paint(linePaint).apply {
                        strokeWidth = effectiveStrokeWidth * 0.4f
                        alpha = 150
                    }
                    canvas.drawPath(shadowPath, sp)
                }

                if ((config.style == SketchStyle.BLACKWORK || config.style == SketchStyle.NORDIC_TATTOO) && stroke.isOuterPole) {
                    val dSize = 5.0f * scale
                    for (pt in listOf(pts.first(), pts.last())) {
                        val diamondPath = Path().apply {
                            moveTo(pt.x * scale, pt.y * scale - dSize)
                            lineTo(pt.x * scale + dSize, pt.y * scale)
                            lineTo(pt.x * scale, pt.y * scale + dSize)
                            lineTo(pt.x * scale - dSize, pt.y * scale)
                            close()
                        }
                        canvas.drawPath(diamondPath, fillPaint)
                    }
                }
            }

            // Branch Notches - ONLY on central stems to prevent clutter
            if (config.hasBranchNotches && stroke.isStem && pts.size >= 2) {
                val notches = OrnamentGeometry.generateStrokeNotches(pts.first(), pts.last())
                val np = Paint(linePaint).apply { strokeWidth = effectiveStrokeWidth * 0.75f }
                for (notch in notches) {
                    canvas.drawLine(notch.x1 * scale, notch.y1 * scale, notch.x2 * scale, notch.y2 * scale, np)
                }
            }

            // Finials - ONLY on outer poles
            if (config.finialType != FinialType.DEFAULT && stroke.isOuterPole && pts.size >= 2) {
                val pLast = pts.last()
                val pPrev = pts[pts.size - 2]
                val finials = OrnamentGeometry.generateFinial(pLast, pLast.x - pPrev.x, pLast.y - pPrev.y, config.finialType)
                drawOrnamentsOnBitmap(finials)
            } else if (config.style == SketchStyle.ORNAMENTAL && stroke.isOuterPole && pts.size >= 2) {
                canvas.drawCircle(pts.first().x * scale, pts.first().y * scale, 3.2f * scale, fillPaint)
                canvas.drawCircle(pts.last().x * scale, pts.last().y * scale, 3.2f * scale, fillPaint)
            }
        }

        // 4. Central Sacred Emblem
        if (config.centerEmblem != CenterEmblem.NONE) {
            val centerOrnaments = OrnamentGeometry.generateCenterEmblem(config.centerEmblem, config.lineWidth)
            drawOrnamentsOnBitmap(centerOrnaments)
        }

        // 5. Corner Accents
        if (config.hasSymmetryAccents && !config.isStencil && config.cornerStyle != CornerStyle.NONE) {
            val cornerOrnaments = OrnamentGeometry.generateCorners(config.cornerStyle, config.lineWidth)
            drawOrnamentsOnBitmap(cornerOrnaments)
        }

        // 6. Sacred golden ember dust
        if (theme == CanvasTheme.GOLDEN_EMBER && !config.isStencil) {
            val emberPaint = Paint(fillPaint).apply {
                color = Color.parseColor(theme.glowHex)
            }
            for (i in 0 until 42) {
                val ex = (40f + prng.nextFloat() * 420f) * scale
                val ey = (40f + prng.nextFloat() * 420f) * scale
                emberPaint.alpha = (prng.nextFloat() * 190 + 35).toInt()
                canvas.drawCircle(ex, ey, (prng.nextFloat() * 2.2f + 0.8f) * scale, emberPaint)
            }
        }

        bitmap
    }

    suspend fun savePngForSharing(context: Context, bitmap: Bitmap, fileName: String = "runic_stave.png"): File = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file
    }

    suspend fun saveSvgForSharing(context: Context, svgContent: String, fileName: String = "runic_stave.svg"): File = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val file = File(cacheDir, fileName)
        file.writeText(svgContent)
        file
    }

    private fun Float.format(): String = String.format(java.util.Locale.US, "%.1f", this)
}

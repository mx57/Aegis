package com.example.data.catalog

data class IntentionPreset(
    val id: String,
    val titleRu: String,
    val category: String,
    val descriptionRu: String,
    val runeIds: List<String>,
    val explanationRu: String,
    val recommendedPlacement: String
)

data class HistoricalStaveTemplate(
    val id: String,
    val nameRu: String,
    val sourceEra: String,
    val category: String,
    val runeIds: List<String>,
    val historicalContextRu: String,
    val applicationRu: String,
    val inscriptionNote: String
)

object StaveCatalog {

    val intentions: List<IntentionPreset> = listOf(
        IntentionPreset(
            id = "protection",
            titleRu = "Щит Воина (Защита)",
            category = "Защита",
            descriptionRu = "Мощнейший защитный барьер, отражающий внешний негатив и охраняющий границы личности.",
            runeIds = listOf("algiz", "thurisaz", "tiwaz", "algiz"),
            explanationRu = "Альгиз призывает высшее покровительство богов, Турисаз сокрушает направленные стрелы зла, а Тейваз дарует стойкость и справедливость воина.",
            recommendedPlacement = "Плечо или предплечье доминирующей руки"
        ),
        IntentionPreset(
            id = "love",
            titleRu = "Пламя Союза (Любовь и Гармония)",
            category = "Отношения",
            descriptionRu = "Привлечение родственной души, углубление взаимного доверия и разжигание сердечного тепла.",
            runeIds = listOf("gebo", "kenaz", "wunjo", "berkano"),
            explanationRu = "Гебо символизирует священный равноправный союз, Кеназ разжигает чистое пламя страсти, Вуньо дарит совместную радость, а Беркана бережет тепло очага.",
            recommendedPlacement = "Запястье или область ключицы/груди"
        ),
        IntentionPreset(
            id = "wealth",
            titleRu = "Златой Урожай (Деньги и Изобилие)",
            category = "Богатство",
            descriptionRu = "Приток материальных благ, развитие коммерческого чутья и надежное сохранение накопленного.",
            runeIds = listOf("fehu", "othala", "jera", "sowilo"),
            explanationRu = "Феху привлекает денежные потоки, Отала закрепляет материальный базис в семье, Йера обеспечивает циклический урожай, а Соуло озаряет путь успехом.",
            recommendedPlacement = "Предплечье или запястье рабочей руки"
        ),
        IntentionPreset(
            id = "luck",
            titleRu = "Поток Благодати (Удача)",
            category = "Удача",
            descriptionRu = "Внезапные счастливые совпадения, легкость на жизненном пути и содействие высших сил.",
            runeIds = listOf("ansuz", "laguz", "wunjo"),
            explanationRu = "Ансуз открывает божественные знаки судьбы, Лагуз несет в гармоничном потоке событий, а Вуньо венчает дело триумфом и ликованием.",
            recommendedPlacement = "Запястье или шея сзади"
        ),
        IntentionPreset(
            id = "health",
            titleRu = "Живой Источник (Здоровье и Сила)",
            category = "Здоровье",
            descriptionRu = "Восстановление сил, укрепление иммунитета, регенерация тканей и внутренняя бодрость.",
            runeIds = listOf("uruz", "ingwaz", "berkano", "dagaz"),
            explanationRu = "Уруз активизирует первозданные резервы тела, Ингуз пробуждает клеточное обновление, Беркана мягко исцеляет, а Дагаз сменяет недуг на здоровье.",
            recommendedPlacement = "Спина вдоль позвоночника или область груди"
        ),
        IntentionPreset(
            id = "journey",
            titleRu = "Светлый Путь (Безопасная Дорога)",
            category = "Путешествия",
            descriptionRu = "Оберег в путешествиях, командировках, на транспорте и при кардинальной смене места жительства.",
            runeIds = listOf("raidho", "algiz", "sowilo"),
            explanationRu = "Райдо выстраивает безупречный маршрут без задержек, Альгиз предотвращает аварии и опасность, а Соуло освещает дорогу ясным светом.",
            recommendedPlacement = "Голень, щиколотка или предплечье"
        ),
        IntentionPreset(
            id = "strength",
            titleRu = "Несокрушимый Дух (Воля и Сила)",
            category = "Сила",
            descriptionRu = "Преодоление тяжелых жизненных кризисов, закалка характера, дисциплина и лидерство.",
            runeIds = listOf("tiwaz", "uruz", "nauthiz", "sowilo"),
            explanationRu = "Тейваз ведет к цели как стрела, Уруз дает неисчерпаемую силу мышц и духа, Наутиз помогает вытерпеть лишения, а Соуло гарантирует победу.",
            recommendedPlacement = "Плечо, спина или предплечье"
        ),
        IntentionPreset(
            id = "creativity",
            titleRu = "Дар Скальда (Творчество и Разум)",
            category = "Творчество",
            descriptionRu = "Преодоление творческого ступора, ораторское мастерство, ясность мысли и поток идей.",
            runeIds = listOf("kenaz", "ansuz", "laguz", "wunjo"),
            explanationRu = "Кеназ зажигает искру гениальности, Ансуз дарует поэтическое красноречие, Лагуз раскрывает свободный поток фантазии, а Вуньо приносит восторг творчества.",
            recommendedPlacement = "Внутренняя сторона предплечья или за ухом"
        )
    )

    val historicalTemplates: List<HistoricalStaveTemplate> = listOf(
        HistoricalStaveTemplate(
            id = "alu",
            nameRu = "ALU (Сакральная Защита)",
            sourceEra = "Брактеаты V–VII вв., брактеат Вадстена",
            category = "Оберег",
            runeIds = listOf("ansuz", "laguz", "uruz"),
            historicalContextRu = "Самая часто встречающаяся сакральная формула на древнескандинавских амулетах и брактеатах. Обозначает экстатическую силу и защиту богов.",
            applicationRu = "Универсальный щит от колдовства, порчи и внезапных опасностей. Наносился на металлические диски и оружие.",
            inscriptionNote = "Ансуз + Лагуз + Уруз. В ставе часто соединяются в единую связку-биндруну."
        ),
        HistoricalStaveTemplate(
            id = "laukaz",
            nameRu = "LAUKAZ (Дикий Лук / Жизненная Сила)",
            sourceEra = "Надписи на брактеатах и фибулах Сконе (VI в.)",
            category = "Здоровье",
            runeIds = listOf("laguz", "ansuz", "uruz", "kenaz", "ansuz", "algiz"),
            historicalContextRu = "Слово 'Laukaz' символизировало лук-порей — священное растение с мощнейшей целебной силой, пробивающее тьму и холод.",
            applicationRu = "Стимуляция плодородия, ускорение выздоровления от тяжелых болезней, защита от ядов.",
            inscriptionNote = "Классическая линейная надпись или биндруна вокруг центрального стебля."
        ),
        HistoricalStaveTemplate(
            id = "auja",
            nameRu = "AUJA (Дарующая Счастье)",
            sourceEra = "Серебряный брактеат Зеландии (ок. 500 г.)",
            category = "Удача",
            runeIds = listOf("ansuz", "uruz", "jera", "ansuz"),
            historicalContextRu = "Прагерманское заклинание счастья и доброй доли. Вырезалось кузнецами для благословения владельца оберега.",
            applicationRu = "Привлечение удачи в торговых делах, охоте и повседневной жизни.",
            inscriptionNote = "Ансуз-Уруз-Йера-Ансуз. Симметрия крайних рун Ансуз усиливает божественную опеку."
        ),
        HistoricalStaveTemplate(
            id = "triple_tiwaz",
            nameRu = "Копьё Тюра (Т-Т-Т)",
            sourceEra = "Старшая Эдда («Речи Сигрдривы»), надписи на копьях",
            category = "Победа",
            runeIds = listOf("tiwaz", "tiwaz", "tiwaz"),
            historicalContextRu = "«Руны победы познай, если к ней ты стремишься, — вырежи их на меча рукояти... и дважды пометь именем Тюра!»",
            applicationRu = "Призыв непобедимой отваги воина, победа в судебных спорах и отстаивание чести.",
            inscriptionNote = "Три руны Тейваз, вписанные одна под другой на едином стрежне копья."
        ),
        HistoricalStaveTemplate(
            id = "fehu_uruz_othala",
            nameRu = "Процветание Рода (F-U-O)",
            sourceEra = "Готландские рунические камни (VIII в.)",
            category = "Богатство",
            runeIds = listOf("fehu", "uruz", "othala"),
            historicalContextRu = "Формула нерушимого материального изобилия, передаваемого по наследству.",
            applicationRu = "Сохранение дома, защита земли и приумножение семейного капитала.",
            inscriptionNote = "Триада благополучия: энергия скота (Феху), мощь земли (Уруз) и священный очаг (Отала)."
        ),
        HistoricalStaveTemplate(
            id = "salu",
            nameRu = "SALU (Солнце и Поток)",
            sourceEra = "Фибула из Верлёсе (Дания, III в.)",
            category = "Оберег",
            runeIds = listOf("sowilo", "ansuz", "laguz", "uruz"),
            historicalContextRu = "Акроним благословения и исцеления, связанный со священным приветствием и солнечным щитом.",
            applicationRu = "Рассеивание морока, депрессии, обретение гармонии души и тела.",
            inscriptionNote = "Соуло-Ансуз-Лагуз-Уруз."
        ),
        HistoricalStaveTemplate(
            id = "erilaz",
            nameRu = "ERILAZ (Слово Эриля)",
            sourceEra = "Камень из Крагехуля, Lindholmen (VI в.)",
            category = "Мудрость",
            runeIds = listOf("ehwaz", "raidho", "isa", "laguz", "ansuz", "algiz"),
            historicalContextRu = "Титул посвященного мастера рун ('Я, эриль, резчик рун...'). Обозначает авторитет и знание законов мироздания.",
            applicationRu = "Усиление магического восприятия, интуиции и концентрации воли мастера.",
            inscriptionNote = "Священная подпись знатока тайн."
        ),
        HistoricalStaveTemplate(
            id = "algiz_sowilo_algiz",
            nameRu = "Солнечный Панцирь",
            sourceEra = "Амулеты эпохи викингов (Бирка, Швеция)",
            category = "Защита",
            runeIds = listOf("algiz", "sowilo", "algiz"),
            historicalContextRu = "Зеркальная формула абсолютной безопасности: центральное солнце Соуло под защитой двух крыльев Альгиз.",
            applicationRu = "Отражение зависти, порчи, сглаза и защита жизненного пространства.",
            inscriptionNote = "Идеально подходит для зеркальной или круговой компоновки."
        ),
        HistoricalStaveTemplate(
            id = "gas",
            nameRu = "G-A-S (Дар Божественного Света)",
            sourceEra = "Раннесредневековые фибулы и кольца",
            category = "Удача",
            runeIds = listOf("gebo", "ansuz", "sowilo"),
            historicalContextRu = "Триада щедрости: взаимный дар (Гебо), вдохновение свыше (Ансуз) и победоносное солнце (Соуло).",
            applicationRu = "Заключение успешных союзов, благосклонность покровителей и признание заслуг.",
            inscriptionNote = "Лаконичная гармоничная формула из трех рун."
        ),
        HistoricalStaveTemplate(
            id = "wunjo_sowilo_wunjo",
            nameRu = "Триумф Радости",
            sourceEra = "Обережные надписи на питьевых рогах",
            category = "Гармония",
            runeIds = listOf("wunjo", "sowilo", "wunjo"),
            historicalContextRu = "Светлая комбинация для изгнания печали, примирения враждующих и празднования победы.",
            applicationRu = "Снятие тревожности, преодоление тоски, наполнение сердца ликованием.",
            inscriptionNote = "Зеркальная симметричная композиция."
        ),
        HistoricalStaveTemplate(
            id = "othala_fehu_jera",
            nameRu = "Обильная Жатва Рода",
            sourceEra = "Скандинавские аграрные обереги",
            category = "Богатство",
            runeIds = listOf("othala", "fehu", "jera"),
            historicalContextRu = "Оберег амбаров, кузниц и усадеб скандинавских бондов.",
            applicationRu = "Защита дома от разорения, стабильный доход от своего дела и ремесла.",
            inscriptionNote = "Отала-Феху-Йера."
        ),
        HistoricalStaveTemplate(
            id = "tiwaz_algiz",
            nameRu = "Страж Порога",
            sourceEra = "Навершия мечей и щитов воинов Уппсалы",
            category = "Защита",
            runeIds = listOf("tiwaz", "algiz", "tiwaz"),
            historicalContextRu = "Священный воинский оберег защитника родной земли и своей чести.",
            applicationRu = "Отражение прямых агрессивных нападений, непоколебимость в суде и споре.",
            inscriptionNote = "Тейваз-Альгиз-Тейваз."
        )
    )
}

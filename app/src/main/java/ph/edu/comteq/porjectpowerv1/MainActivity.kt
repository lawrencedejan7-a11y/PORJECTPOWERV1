package ph.edu.comteq.porjectpowerv1

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.edit
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ---------- Colors ----------
val DarkBackground = Color(0xFF0F0E17)
val DarkCardColor = Color(0xFF1B1930)
val DarkCardElevated = Color(0xFF28243D)
val PurpleAccent = Color(0xFF9D4EDD)
val PurpleLight = Color(0xFFC77DFF)
val PurpleDeep = Color(0xFF5A189A)
val FlameOrange = Color(0xFFFF9F1C)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF9CA3AF)
val BorderColor = Color(0xFF332E4C)
val ErrorRed = Color(0xFFFF6B6B)

// ---------- Power scale ----------
data class PowerRank(val name: String, val min: Int, val max: Int)

val powerRanks = listOf(
    PowerRank("Knight", 10, 50),
    PowerRank("Gladiator", 60, 100),
    PowerRank("Warrior", 110, 180),
    PowerRank("Tank", 180, 250),
    PowerRank("Demigod", 260, 350),
    PowerRank("Eternal", 360, 450),
    PowerRank("Olympian", 450, 650),
    PowerRank("Viltrumite", 660, 750),
    PowerRank("Kryptonian", 760, 850),
    PowerRank("Saiyan", 850, 1000)
)


fun rankFor(total: Int): PowerRank? = powerRanks.lastOrNull { total >= it.min }

fun nextRankFor(total: Int): PowerRank? = powerRanks.firstOrNull { it.min > total }


fun rankProgress(total: Int): Float {
    val current = rankFor(total)
    val next = nextRankFor(total)
    return when {
        next == null -> ((total - 850) / 150f).coerceIn(0f, 1f) // Saiyan: progress toward 1000
        current == null -> (total / next.min.toFloat()).coerceIn(0f, 1f)
        else -> ((total - current.min).toFloat() / (next.min - current.min)).coerceIn(0f, 1f)
    }
}


fun rankIcon(rank: PowerRank?): Int = when (rank?.name) {
    "Knight" -> R.drawable.knight
    "Gladiator" -> R.drawable.gladiator
    "Warrior" -> R.drawable.warrior
    "Tank" -> R.drawable.tank
    "Demigod" -> R.drawable.demigod
    "Eternal" -> R.drawable.eternal
    "Olympian" -> R.drawable.olympian
    "Viltrumite" -> R.drawable.viltrumempirelogo
    "Kryptonian" -> R.drawable.kyrptonian // file is spelled "kyrptonian" - rename it, then fix this line
    "Saiyan" -> R.drawable.saiyan
    else -> R.drawable.common // Unranked (below 10 lbs)
}


fun todayEpochDay(): Long {
    val cal = Calendar.getInstance()
    return (cal.timeInMillis + cal.timeZone.getOffset(cal.timeInMillis)) / 86_400_000L
}


data class LiftEntry(val day: Long, val bench: Int, val deadlift: Int, val squat: Int) {
    val total: Int get() = bench + deadlift + squat
}


fun historyToString(list: List<LiftEntry>): String =
    list.joinToString(";") { "${it.day},${it.bench},${it.deadlift},${it.squat}" }

fun historyFromString(text: String): List<LiftEntry> =
    text.split(";").mapNotNull { part ->
        val p = part.split(",")
        if (p.size != 4) return@mapNotNull null
        val day = p[0].toLongOrNull() ?: return@mapNotNull null
        LiftEntry(day, p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 0, p[3].toIntOrNull() ?: 0)
    }


fun rankIndexFor(total: Int): Int = powerRanks.indexOfLast { total >= it.min }


fun formatDay(day: Long): String {
    val format = SimpleDateFormat("MMM d", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(day * 86_400_000L))
}


fun activityToString(map: Map<Long, Int>): String =
    map.entries.sortedBy { it.key }.joinToString(";") { "${it.key}:${it.value}" }

fun activityFromString(text: String): Map<Long, Int> =
    text.split(";").mapNotNull { part ->
        val p = part.split(":")
        val day = p.getOrNull(0)?.toLongOrNull()
        val count = p.getOrNull(1)?.toIntOrNull()
        if (day != null && count != null) Pair(day, count) else null
    }.toMap()


fun bestLiftOf(history: List<LiftEntry>): Pair<String, Int>? {
    var bestName: String? = null
    var best = 0
    for (entry in history) {
        if (entry.bench > best) { best = entry.bench; bestName = "Bench" }
        if (entry.deadlift > best) { best = entry.deadlift; bestName = "Deadlift" }
        if (entry.squat > best) { best = entry.squat; bestName = "Squat" }
    }
    return bestName?.let { Pair(it, best) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUiShell()
        }
    }
}

// ---------- Shared building blocks ----------
private data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = BorderColor,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val colors = CardDefaults.cardColors(containerColor = DarkCardColor)
    val border = Modifier.border(1.dp, borderColor, shape)
    if (onClick != null) {
        Card(onClick = onClick, shape = shape, colors = colors, modifier = modifier.then(border), content = content)
    } else {
        Card(shape = shape, colors = colors, modifier = modifier.then(border), content = content)
    }
}

@Composable
fun IconBadge(icon: ImageVector, tint: Color = PurpleLight, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.5f).dp))
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(title, color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, color = TextGray, fontSize = 14.sp)
    }
}

// ---------- App shell ----------
@Composable
fun AppUiShell() {
    var currentTab by remember { mutableIntStateOf(0) }

    // Saved total is remembered on the device, so the rank survives closing the app
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val prefs = remember { context.getSharedPreferences("power_prefs", android.content.Context.MODE_PRIVATE) }
    var savedTotal by remember { mutableIntStateOf(if (inPreview) 0 else prefs.getInt("saved_total", 0)) }
    var streak by remember { mutableIntStateOf(if (inPreview) 0 else prefs.getInt("streak", 0)) }
    var lastActiveDay by remember { mutableLongStateOf(if (inPreview) -1L else prefs.getLong("last_active_day", -1L)) }

    var history by remember {
        mutableStateOf(if (inPreview) emptyList() else historyFromString(prefs.getString("history", "") ?: ""))
    }

    var workoutsDone by remember { mutableIntStateOf(if (inPreview) 0 else prefs.getInt("workouts_done", 0)) }
    var activity by remember {
        mutableStateOf(if (inPreview) emptyMap() else activityFromString(prefs.getString("activity", "") ?: ""))
    }
    var rankUp by remember { mutableStateOf<PowerRank?>(null) }


    var profileName by remember { mutableStateOf(if (inPreview) "Lifter" else prefs.getString("profile_name", "") ?: "") }
    var fitnessLevel by remember { mutableStateOf(if (inPreview) "Casual" else prefs.getString("fitness_level", "") ?: "") }
    var onboarded by remember { mutableStateOf(inPreview || prefs.getBoolean("onboarded", false)) }
    var showProfileDialog by remember { mutableStateOf(false) }


    fun recordActivity(isWorkout: Boolean = false) {
        val today = todayEpochDay()
        if (lastActiveDay != today) {
            streak = if (lastActiveDay == today - 1) streak + 1 else 1 // yesterday -> +1, otherwise restart at 1
            lastActiveDay = today
        }
        activity = (activity + Pair(today, (activity[today] ?: 0) + 1)).filterKeys { it >= today - 60 }
        if (isWorkout) workoutsDone += 1
        if (!inPreview) prefs.edit {
            putInt("streak", streak)
            putLong("last_active_day", lastActiveDay)
            putInt("workouts_done", workoutsDone)
            putString("activity", activityToString(activity))
        }
    }


    val shownStreak = if (lastActiveDay >= 0 && todayEpochDay() - lastActiveDay > 1) 0 else streak


    if (!onboarded) {
        OnboardingUi(onFinish = { name, level ->
            profileName = name
            fitnessLevel = level
            onboarded = true
            if (!inPreview) prefs.edit {
                putString("profile_name", name)
                putString("fitness_level", level)
                putBoolean("onboarded", true)
            }
        })
        return
    }

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Quests", Icons.Default.EmojiEvents),
        NavItem("Workout", Icons.Default.FitnessCenter),
        NavItem("History", Icons.Default.History)
    )

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(containerColor = DarkCardColor, tonalElevation = 0.dp) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextWhite,
                            selectedTextColor = PurpleLight,
                            indicatorColor = PurpleAccent,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            when (currentTab) {
                0 -> DashboardUi(
                    totalLifts = savedTotal,
                    dayStreak = shownStreak,
                    bestLift = bestLiftOf(history),
                    profileName = profileName,
                    fitnessLevel = fitnessLevel,
                    onEditProfile = { showProfileDialog = true },
                    onOpenWorkouts = { currentTab = 2 }
                )
                1 -> QuestBoardUi(lastLifts = history.lastOrNull(), onSave = { bench, deadlift, squat ->
                    val total = bench + deadlift + squat

                    // Moving up to a higher rank shows the rank-up pop-up
                    val oldRank = rankIndexFor(savedTotal)
                    val newRank = rankIndexFor(total)
                    if (newRank > oldRank) rankUp = powerRanks[newRank]

                    savedTotal = total


                    val entry = LiftEntry(todayEpochDay(), bench, deadlift, squat)
                    history = (history.filter { it.day != entry.day } + entry).sortedBy { it.day }.takeLast(60)

                    if (!inPreview) prefs.edit {
                        putInt("saved_total", total)
                        putString("history", historyToString(history))
                    }
                    recordActivity()
                })
                2 -> PreSavedWorkoutUi(
                    onWorkoutFinished = { recordActivity(isWorkout = true) },
                    defaultDifficulty = difficultyFor(fitnessLevel) // your fitness level picks the starting difficulty
                )
                3 -> ProgressHistoryUi(
                    history = history,
                    workoutsDone = workoutsDone,
                    dayStreak = shownStreak,
                    activity = activity
                )
            }
        }
    }

    rankUp?.let { RankUpDialog(rank = it, onDismiss = { rankUp = null }) }

    if (showProfileDialog) {
        ProfileDialog(
            name = profileName,
            level = fitnessLevel,
            onSave = { name, level ->
                profileName = name
                fitnessLevel = level
                showProfileDialog = false
                if (!inPreview) prefs.edit {
                    putString("profile_name", name)
                    putString("fitness_level", level)
                }
            },
            onDismiss = { showProfileDialog = false }
        )
    }
}

// Profile + intro screens
// Fitness level
fun difficultyFor(level: String): String = when (level) {
    "Beginner" -> "Easy"
    "Advanced" -> "Hard"
    else -> "Medium"
}

val fitnessLevels = listOf("Beginner", "Casual", "Advanced")

@Composable
fun appFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PurpleAccent,
    unfocusedBorderColor = BorderColor,
    focusedLabelColor = PurpleLight,
    unfocusedLabelColor = TextGray,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    cursorColor = PurpleLight
)

@Composable
fun OnboardingUi(onFinish: (name: String, level: String) -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    val lastPage = 2

    // You can't go on until the name
    val canContinue = when (page) {
        0 -> name.isNotBlank()
        1 -> level.isNotEmpty()
        else -> true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .systemBarsPadding()
            .padding(24.dp)
    ) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(lastPage + 1) { i ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(if (i == page) 24.dp else 8.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(if (i == page) PurpleAccent else DarkCardElevated)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            when (page) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(DarkCardColor)
                            .border(2.dp, PurpleAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.common),
                            contentDescription = "Project Power logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Welcome to Project Power",
                        color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Lift. Rank up. Repeat.", color = TextGray, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 20) name = it },
                        label = { Text("What should we call you?") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = appFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You can change this later by tapping your picture on the home screen.",
                        color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center
                    )
                }

                1 -> {
                    Text(
                        "What's your fitness level?",
                        color = TextWhite, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This sets your starting workout difficulty. You can still change it any time.",
                        color = TextGray, fontSize = 14.sp, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val options = listOf(
                        Triple("Beginner", "New to lifting, or getting back into it", Icons.Default.Star),
                        Triple("Casual", "You train now and then and know the basics", Icons.Default.Bolt),
                        Triple("Advanced", "You train regularly and lift heavy", Icons.Default.LocalFireDepartment)
                    )
                    options.forEach { (title, description, icon) ->
                        val selected = level == title
                        AppCard(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            onClick = { level = title },
                            borderColor = if (selected) PurpleAccent else BorderColor
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconBadge(icon)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                    Text(description, color = TextGray, fontSize = 13.sp)
                                    Text(
                                        "Starts on ${difficultyFor(title)} workouts",
                                        color = PurpleLight, fontSize = 12.sp
                                    )
                                }
                                if (selected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = PurpleLight)
                                }
                            }
                        }
                    }
                }

                else -> {
                    Text(
                        "How it works",
                        color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val steps = listOf(
                        Triple(Icons.Default.EmojiEvents, "Quest Board", "Log your bench, deadlift and squat. Your total sets your power rank, from Knight to Saiyan. Each new lift must beat your last one."),
                        Triple(Icons.Default.FitnessCenter, "Workouts", "Pick a plan, watch the exercise videos and tick off each exercise as you go."),
                        Triple(Icons.Default.LocalFireDepartment, "Streaks", "Save your lifts or finish a workout every day to build your streak."),
                        Triple(Icons.Default.History, "History", "See your personal records and how your rank grows over time.")
                    )
                    steps.forEach { (icon, title, description) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            IconBadge(icon)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(description, color = TextGray, fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (page > 0) {
                TextButton(onClick = { page -= 1 }) {
                    Text("Back", color = TextGray)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Button(
                onClick = { if (page == lastPage) onFinish(name.trim(), level) else page += 1 },
                enabled = canContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleAccent,
                    disabledContainerColor = DarkCardElevated
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(52.dp).widthIn(min = 140.dp)
            ) {
                Text(
                    if (page == lastPage) "Get Started" else "Next",
                    color = if (canContinue) TextWhite else TextGray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Three buttons in a row to pick a fitness level
@Composable
fun LevelSelector(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardElevated)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        fitnessLevels.forEach { option ->
            val isSelected = selected == option
            Button(
                onClick = { onSelect(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) PurpleAccent else Color.Transparent
                ),
                elevation = null,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    option,
                    color = if (isSelected) TextWhite else TextGray,
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ProfileDialog(
    name: String,
    level: String,
    onSave: (name: String, level: String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(name) }
    var newLevel by remember { mutableStateOf(level.ifEmpty { "Casual" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardColor,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Edit profile", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= 20) newName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = appFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fitness level", color = TextGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LevelSelector(selected = newLevel, onSelect = { newLevel = it })
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(newName.trim(), newLevel) },
                enabled = newName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleAccent,
                    disabledContainerColor = DarkCardElevated
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextGray) }
        }
    )
}

// ---------- Rank-up pop-up ----------
@Composable
fun RankUpDialog(rank: PowerRank, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCardColor,
        shape = RoundedCornerShape(24.dp),
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Let's go", color = TextWhite, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(DarkBackground)
                        .border(3.dp, PurpleAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = rankIcon(rank)),
                        contentDescription = "${rank.name} rank icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("RANK UP!", color = PurpleLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("You reached ${rank.name}", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("${rank.min} - ${rank.max} lbs total", color = TextGray, fontSize = 14.sp)
            }
        }
    )
}

// ---------- Dashboard ----------
@Composable
fun DashboardUi(
    totalLifts: Int,
    dayStreak: Int,
    bestLift: Pair<String, Int>?,
    profileName: String,
    fitnessLevel: String,
    onEditProfile: () -> Unit,
    onOpenWorkouts: () -> Unit
) {
    val rank = rankFor(totalLifts)
    val nextRank = nextRankFor(totalLifts)
    val progress = rankProgress(totalLifts)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Welcome back, $profileName",
                        color = TextGray, fontSize = 14.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (fitnessLevel.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            fitnessLevel.uppercase(),
                            color = PurpleLight, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PurpleAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                Text("Project Power", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            // Tap your initial to edit your profile
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PurpleAccent.copy(alpha = 0.2f))
                    .border(1.dp, PurpleAccent, CircleShape)
                    .clickable { onEditProfile() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    profileName.trim().firstOrNull()?.uppercase() ?: "?",
                    color = PurpleLight, fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Rank hero card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(PurpleDeep, PurpleAccent)))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkBackground)
                            .border(2.dp, TextWhite.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = rankIcon(rank)),
                            contentDescription = "${rank?.name ?: "Unranked"} rank icon",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("CURRENT RANK", color = TextWhite.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                        Text(
                            if (rank != null) "${rank.name} Tier" else "Unranked",
                            color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (rank != null) "${rank.min} - ${rank.max} lbs total" else "Log 10 lbs total to earn Knight",
                            color = TextWhite.copy(alpha = 0.85f), fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (nextRank != null) "Progress to ${nextRank.name}" else "Max rank progress",
                        color = TextWhite.copy(alpha = 0.85f), fontSize = 12.sp
                    )
                    Text("${(progress * 100).toInt()}%", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = TextWhite,
                    trackColor = TextWhite.copy(alpha = 0.25f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Your Stats", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), // keeps the 3 tiles the same height
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(Modifier.weight(1f).fillMaxHeight(), Icons.Default.FitnessCenter, PurpleLight, "$totalLifts", "Total Lifts (lbs)")
            StatTile(Modifier.weight(1f).fillMaxHeight(), Icons.Default.LocalFireDepartment, FlameOrange, "$dayStreak", "Day Streak")
            StatTile(
                Modifier.weight(1f).fillMaxHeight(), Icons.Default.Bolt, PurpleLight,
                "${bestLift?.second ?: 0}", "Best Lift (lbs)",
                subLabel = bestLift?.first ?: "None yet" // Bench, Deadlift or Squat
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Today's Quest", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        AppCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenWorkouts) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(Icons.Default.FitnessCenter)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pick a workout", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("Chest, Back, Arms or Legs", color = TextGray, fontSize = 13.sp)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        QuoteOfTheDayCard()
    }
}

// ---------- Quote of the day ----------
data class Quote(val text: String, val author: String, val category: String)

val bodybuilderQuotes = listOf(
    // Mindset & Determination
    Quote("The mind is the limit. As long as the mind can envision the fact that you can do something, you can do it, as long as you really believe 100%.", "Arnold Schwarzenegger", "Mindset"),
    Quote("Vision creates faith and faith creates willpower. With faith there is no anxiety, no doubt—just absolute belief in your ability to achieve.", "Arnold Schwarzenegger", "Mindset"),
    Quote("If you're capable of sending a message to your mind to have clear self-discipline, then standard muscle building will take care of itself.", "Lee Haney", "Mindset"),
    // Intensity & Pushing Through Pain
    Quote("The last three or four reps is what makes the muscle grow. This area of pain divides the champion from someone else who is not a champion.", "Arnold Schwarzenegger", "Intensity"),
    Quote("Everybody wants to be a bodybuilder, but nobody wants to lift no heavy-ass weights.", "Ronnie Coleman", "Intensity"),
    Quote("Light weight, baby!", "Ronnie Coleman", "Intensity"),
    Quote("If you work hard, you're going to get results. It's that simple.", "Dorian Yates", "Intensity"),
    Quote("To be successful in bodybuilding, you have to be a good observer of your own body.", "Dorian Yates", "Intensity"),
    // Consistency & Longevity
    Quote("Exercise to stimulate, not to annihilate. The world wasn't formed in a day, and neither were we. Set small goals and build upon them.", "Lee Haney", "Consistency"),
    Quote("Consistency is the key to achieving any goal in life, especially in bodybuilding. Show up every day and put in the work.", "Phil Heath", "Consistency"),
    Quote("I don't eat for taste, I eat for function.", "Jay Cutler", "Consistency"),
    // Work Ethic & Discipline
    Quote("There are no shortcuts. Everything is reps, reps, reps.", "Arnold Schwarzenegger", "Discipline"),
    Quote("I'm not the genetically most gifted guy, but I work harder than anyone else.", "Jay Cutler", "Discipline"),
    Quote("Clear your mind of can't.", "Samuel Wilson", "Discipline"),
    Quote("Surround yourself with people who challenge you, teach you, and push you to be better.", "Kai Greene", "Discipline"),
    Quote("If you do what you've always done, you'll get what you've always gotten.", "Rich Piana", "Discipline"),
    // Extra quotes
    Quote("The worst thing I can be is the same as everybody else. I hate that.", "Arnold Schwarzenegger", "Mindset"),
    Quote("Strength does not come from winning. Your struggles develop your strengths.", "Arnold Schwarzenegger", "Mindset")
)

// Picks one quote per calendar day (local time), so it changes every day
fun quoteOfTheDay(): Quote {
    val cal = Calendar.getInstance()
    val dayNumber = cal.get(Calendar.YEAR) * 366 + cal.get(Calendar.DAY_OF_YEAR)
    return bodybuilderQuotes[dayNumber % bodybuilderQuotes.size]
}

@Composable
fun QuoteOfTheDayCard() {
    val quote = remember { quoteOfTheDay() }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Default.FormatQuote, size = 36)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("QUOTE OF THE DAY", color = PurpleLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                    Text(quote.category, color = TextGray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "\u201C${quote.text}\u201D",
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 25.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(PurpleAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(quote.author, color = PurpleLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun StatTile(modifier: Modifier, icon: ImageVector, tint: Color, value: String, label: String, subLabel: String? = null) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            IconBadge(icon, tint, size = 36)
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextGray, fontSize = 11.sp)
            if (subLabel != null) {
                Text(subLabel, color = PurpleLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// Quest board
@Composable
fun QuestBoardUi(lastLifts: LiftEntry?, onSave: (bench: Int, deadlift: Int, squat: Int) -> Unit) {
    var saved by remember { mutableStateOf(false) }
    var benchPressInput by remember { mutableStateOf("") }
    var deadliftInput by remember { mutableStateOf("") }
    var squatInput by remember { mutableStateOf("") }

    // Your last saved lifts
    val lastBench = lastLifts?.bench ?: 0
    val lastDeadlift = lastLifts?.deadlift ?: 0
    val lastSquat = lastLifts?.squat ?: 0


    val newBench = benchPressInput.toIntOrNull()
    val newDeadlift = deadliftInput.toIntOrNull()
    val newSquat = squatInput.toIntOrNull()

    val benchError = newBench != null && newBench <= lastBench
    val deadliftError = newDeadlift != null && newDeadlift <= lastDeadlift
    val squatError = newSquat != null && newSquat <= lastSquat

    val benchImproved = newBench != null && !benchError
    val deadliftImproved = newDeadlift != null && !deadliftError
    val squatImproved = newSquat != null && !squatError


    val bench = if (newBench != null && !benchError) newBench else lastBench
    val deadlift = if (newDeadlift != null && !deadliftError) newDeadlift else lastDeadlift
    val squat = if (newSquat != null && !squatError) newSquat else lastSquat
    val total = bench + deadlift + squat


    val canSave = (benchImproved || deadliftImproved || squatImproved) &&
            !(benchError || deadliftError || squatError)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        ScreenHeader("Quest Board", "Beat your last lift to save a new record")

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Power Ranking Inputs", color = PurpleLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                LiftField("Bench Press (lbs)", benchPressInput, lastBench, benchError) { benchPressInput = it; saved = false }
                Spacer(modifier = Modifier.height(4.dp))
                LiftField("Deadlift (lbs)", deadliftInput, lastDeadlift, deadliftError) { deadliftInput = it; saved = false }
                Spacer(modifier = Modifier.height(4.dp))
                LiftField("Back Squat (lbs)", squatInput, lastSquat, squatError) { squatInput = it; saved = false }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkCardElevated)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", color = TextGray, fontSize = 14.sp)
                    Text("$total lbs", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rank", color = TextGray, fontSize = 14.sp)
                    Text(
                        rankFor(total)?.name ?: "Unranked",
                        color = PurpleLight, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSave(bench, deadlift, squat)
                        // Clear the boxes: the saved numbers are now your "last" lifts
                        benchPressInput = ""
                        deadliftInput = ""
                        squatInput = ""
                        saved = true
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleAccent,
                        disabledContainerColor = DarkCardElevated
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (saved) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (saved) "Saved" else "Save Power Lifts",
                        color = if (canSave || saved) TextWhite else TextGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LiftField(
    label: String,
    value: String,
    lastValue: Int,
    isError: Boolean,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter { it.isDigit() }) },
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = {
            Text(
                when {
                    isError && lastValue > 0 -> "Must be higher than your last lift: $lastValue lbs"
                    isError -> "Enter more than 0 lbs"
                    lastValue > 0 -> "Last saved: $lastValue lbs"
                    else -> "No lift saved yet"
                }
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PurpleAccent,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = PurpleLight,
            unfocusedLabelColor = TextGray,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = PurpleLight,
            focusedSupportingTextColor = TextGray,
            unfocusedSupportingTextColor = TextGray,
            errorBorderColor = ErrorRed,
            errorLabelColor = ErrorRed,
            errorTextColor = TextWhite,
            errorCursorColor = ErrorRed,
            errorSupportingTextColor = ErrorRed
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

// Workouts
@Composable
fun PreSavedWorkoutUi(onWorkoutFinished: () -> Unit, defaultDifficulty: String = "Medium") {
    val context = LocalContext.current
    var selectedWorkoutTitle by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf(defaultDifficulty) }

    if (selectedWorkoutTitle == null) {
        val workoutPlans = listOf(
            Pair("Chest Day Quest", "Pecs & Triceps"),
            Pair("Back Day Quest", "Lats & Upper Back"),
            Pair("Arm Day Quest", "Biceps & Triceps"),
            Pair("Leg Day Quest", "Quads, Glutes & Hamstrings")
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp)) {
            ScreenHeader("Workouts", "Choose a plan, then set your difficulty")

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(workoutPlans) { (title, summary) ->
                    AppCard(modifier = Modifier.fillMaxWidth(), onClick = { selectedWorkoutTitle = title }) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconBadge(Icons.Default.FitnessCenter, size = 48)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(summary, color = TextGray, fontSize = 13.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    } else {
        val exercises = getExercisesForWorkout(selectedWorkoutTitle!!, selectedDifficulty)
        // Which exercise's video is showing
        var selectedExerciseIndex by remember(selectedWorkoutTitle, selectedDifficulty) { mutableIntStateOf(0) }
        val currentExercise = exercises[selectedExerciseIndex.coerceIn(0, exercises.lastIndex)].first
        // Exercises you have ticked off
        var doneSet by remember(selectedWorkoutTitle, selectedDifficulty) { mutableStateOf(setOf<Int>()) }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedWorkoutTitle = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Text(selectedWorkoutTitle!!, color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardColor)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Easy", "Medium", "Hard").forEach { difficulty ->
                    val isSelected = selectedDifficulty == difficulty
                    Button(
                        onClick = { selectedDifficulty = difficulty },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PurpleAccent else Color.Transparent
                        ),
                        elevation = null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            difficulty,
                            color = if (isSelected) TextWhite else TextGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Video for the selected exercise
                item {
                    ExerciseVideoPlayer(exerciseName = currentExercise)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Routine", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${doneSet.size}/${exercises.size} done - tap to watch", color = TextGray, fontSize = 13.sp)
                    }
                }

                itemsIndexed(exercises) { index, (exerciseName, setsReps) ->
                    val isDone = index in doneSet
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedExerciseIndex = index },
                        borderColor = if (index == selectedExerciseIndex) PurpleAccent else BorderColor
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tap the circle to tick the exercise off
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) PurpleAccent else PurpleAccent.copy(alpha = 0.2f))
                                    .clickable { doneSet = if (isDone) doneSet - index else doneSet + index },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        Icons.Default.Check, contentDescription = "Done",
                                        tint = TextWhite, modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text("${index + 1}", color = PurpleLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    exerciseName,
                                    color = if (isDone) TextGray else TextWhite,
                                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                                )
                                Text(setsReps, color = TextGray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            onWorkoutFinished()
                            Toast.makeText(context, "Workout complete! Streak updated", Toast.LENGTH_SHORT).show()
                            doneSet = emptySet()
                        },
                        enabled = doneSet.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleAccent,
                            disabledContainerColor = DarkCardElevated
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (doneSet.isNotEmpty()) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = TextWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (doneSet.isEmpty()) "Tick exercises to finish"
                            else "Finish Workout (${doneSet.size}/${exercises.size})",
                            color = if (doneSet.isEmpty()) TextGray else TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// list of workouts based on difficulty
fun getExercisesForWorkout(workoutTitle: String, difficulty: String): List<Pair<String, String>> {
    return when (workoutTitle) {
        "Chest Day Quest" -> when (difficulty) {
            "Easy" -> listOf(
                Pair("Push-Ups", "3 Sets x 10 Reps"),
                Pair("Flat Dumbbell Press", "3 Sets x 10 Reps"),
                Pair("Incline Dumbbell Flyes", "3 Sets x 12 Reps"),
                Pair("Chest Dips (Assisted)", "3 Sets x 8 Reps"),
                Pair("Cable Crossover (High)", "3 Sets x 12 Reps")
            )
            "Medium" -> listOf(
                Pair("Barbell Bench Press", "4 Sets x 8 Reps"),
                Pair("Incline Dumbbell Press", "4 Sets x 10 Reps"),
                Pair("Chest Dips (Bodyweight)", "3 Sets x 10 Reps"),
                Pair("Cable Crossover (Mid)", "3 Sets x 12 Reps"),
                Pair("Decline Dumbbell Press", "3 Sets x 10 Reps"),
                Pair("Push-Up Finisher", "2 Sets to Failure")
            )
            else -> listOf(
                Pair("Heavy Barbell Bench Press", "5 Sets x 5 Reps"),
                Pair("Incline Barbell Bench Press", "4 Sets x 8 Reps"),
                Pair("Weighted Chest Dips", "4 Sets x 8 Reps"),
                Pair("Low-to-High Cable Flyes", "4 Sets x 12 Reps"),
                Pair("Dumbbell Pullover", "3 Sets x 10 Reps"),
                Pair("Incline Diamond Push-Ups", "3 Sets to Failure")
            )
        }
        "Back Day Quest" -> when (difficulty) {
            "Easy" -> listOf(
                Pair("Lat Pulldowns", "3 Sets x 10 Reps"),
                Pair("Seated Cable Rows", "3 Sets x 12 Reps"),
                Pair("Dumbbell Single-Arm Rows", "3 Sets x 10 Reps"),
                Pair("Face Pulls", "3 Sets x 15 Reps"),
                Pair("Hyperextensions", "3 Sets x 12 Reps")
            )
            "Medium" -> listOf(
                Pair("Barbell Bent-Over Rows", "4 Sets x 8 Reps"),
                Pair("Wide-Grip Lat Pulldowns", "4 Sets x 10 Reps"),
                Pair("T-Bar Rows", "3 Sets x 10 Reps"),
                Pair("Single-Arm Cable Lat Pulls", "3 Sets x 12 Reps"),
                Pair("Reverse Pec Deck Flyes", "3 Sets x 12 Reps"),
                Pair("Pull-Ups", "3 Sets to Failure")
            )
            else -> listOf(
                Pair("Conventional Deadlifts", "5 Sets x 5 Reps"),
                Pair("Weighted Pull-Ups", "4 Sets x 6 Reps"),
                Pair("Heavy Barbell Pendlay Rows", "4 Sets x 8 Reps"),
                Pair("Close-Grip Lat Pulldowns", "4 Sets x 10 Reps"),
                Pair("Meadows Rows", "3 Sets x 10 Reps"),
                Pair("Rack Pulls", "3 Sets x 8 Reps")
            )
        }
        "Arm Day Quest" -> when (difficulty) {
            "Easy" -> listOf(
                Pair("Standing Barbell Curls", "3 Sets x 10 Reps"),
                Pair("Tricep Rope Pushdowns", "3 Sets x 12 Reps"),
                Pair("Dumbbell Hammer Curls", "3 Sets x 10 Reps"),
                Pair("Bench Dips", "3 Sets x 12 Reps"),
                Pair("Concentration Curls", "3 Sets x 12 Reps")
            )
            "Medium" -> listOf(
                Pair("EZ-Bar Preacher Curls", "4 Sets x 10 Reps"),
                Pair("Skullcrushers (EZ-Bar)", "4 Sets x 10 Reps"),
                Pair("Incline Dumbbell Curls", "3 Sets x 12 Reps"),
                Pair("Overhead Dumbbell Extension", "3 Sets x 10 Reps"),
                Pair("Cable Hammer Curls", "3 Sets x 12 Reps"),
                Pair("Single-Arm Tricep Pushdowns", "3 Sets x 12 Reps")
            )
            else -> listOf(
                Pair("Heavy Cheat Curls (Barbell)", "4 Sets x 6 Reps"),
                Pair("Close-Grip Bench Press", "4 Sets x 8 Reps"),
                Pair("Spider Curls", "4 Sets x 10 Reps"),
                Pair("Weighted Overhead Cable Extension", "4 Sets x 10 Reps"),
                Pair("Reverse Grip Barbell Curls", "3 Sets x 12 Reps"),
                Pair("Tricep Dips to Burnout", "3 Sets to Failure")
            )
        }
        else -> when (difficulty) {
            "Easy" -> listOf(
                Pair("Goblet Squats", "3 Sets x 12 Reps"),
                Pair("Leg Press", "3 Sets x 10 Reps"),
                Pair("Lying Leg Curls", "3 Sets x 12 Reps"),
                Pair("Walking Lunges", "3 Sets x 10 Steps/leg"),
                Pair("Standing Calf Raises", "3 Sets x 15 Reps")
            )
            "Medium" -> listOf(
                Pair("Barbell Back Squats", "4 Sets x 8 Reps"),
                Pair("Romanian Deadlifts (RDL)", "4 Sets x 10 Reps"),
                Pair("Leg Press", "3 Sets x 12 Reps"),
                Pair("Leg Extensions", "3 Sets x 12 Reps"),
                Pair("Seated Leg Curls", "3 Sets x 12 Reps"),
                Pair("Seated Calf Raises", "4 Sets x 15 Reps")
            )
            else -> listOf(
                Pair("Heavy Barbell Back Squats", "5 Sets x 5 Reps"),
                Pair("Barbell Hip Thrusts", "4 Sets x 8 Reps"),
                Pair("Bulgarian Split Squats", "4 Sets x 8 Reps/leg"),
                Pair("Heavy Romanian Deadlifts", "4 Sets x 8 Reps"),
                Pair("Single-Leg Extensions", "3 Sets x 12 Reps"),
                Pair("Donkey Calf Raises", "4 Sets x 20 Reps")
            )
        }
    }
}

// Exercise videos
val exerciseVideos = mapOf(
    "Push-Ups" to "push_ups",
    "Flat Dumbbell Press" to "flatdumbellpress",
    "Incline Dumbbell Flyes" to "incline_dumbbell_flyes",
    "Chest Dips (Assisted)" to "dips",
    "Cable Crossover (High)" to "cable_crossover_high",
    "Barbell Bench Press" to "barbell_bench_press",
    "Incline Dumbbell Press" to "incline_dumbbell_press",
    "Chest Dips (Bodyweight)" to "chest_dips_bodyweight",
    "Cable Crossover (Mid)" to "cable_crossover_mid",
    "Decline Dumbbell Press" to "decline_dumbbell_press",
    "Push-Up Finisher" to "push_up_finisher",
    "Heavy Barbell Bench Press" to "heavy_barbell_bench_press",
    "Incline Barbell Bench Press" to "incline_barbell_bench_press",
    "Weighted Chest Dips" to "weighted_chest_dips",
    "Low-to-High Cable Flyes" to "low_to_high_cable_flyes",
    "Dumbbell Pullover" to "dumbbell_pullover",
    "Incline Diamond Push-Ups" to "incline_diamond_push_ups",
    "Lat Pulldowns" to "latpulldowns",
    "Seated Cable Rows" to "seated_cable_rows",
    "Dumbbell Single-Arm Rows" to "dumbbell_single_arm_rows",
    "Face Pulls" to "face_pulls",
    "Hyperextensions" to "hyperextension",
    "Barbell Bent-Over Rows" to "barbell_bent_over_rows",
    "Wide-Grip Lat Pulldowns" to "wide_grip_lat_pulldowns",
    "T-Bar Rows" to "t_bar_rows",
    "Single-Arm Cable Lat Pulls" to "single_arm_cable_lat_pulls",
    "Reverse Pec Deck Flyes" to "reverse_pec_deck_flyes",
    "Pull-Ups" to "pull_ups",
    "Conventional Deadlifts" to "conventional_deadlifts",
    "Weighted Pull-Ups" to "weighted_pull_ups",
    "Heavy Barbell Pendlay Rows" to "heavy_barbell_pendlay_rows",
    "Close-Grip Lat Pulldowns" to "close_grip_lat_pulldowns",
    "Meadows Rows" to "meadows_rows",
    "Rack Pulls" to "rack_pulls",
    "Standing Barbell Curls" to "standing_barbell_curls",
    "Tricep Rope Pushdowns" to "tricep_rope_pushdowns",
    "Dumbbell Hammer Curls" to "dumbbell_hammer_curls",
    "Bench Dips" to "bench_dips",
    "Concentration Curls" to "concentration_curls",
    "EZ-Bar Preacher Curls" to "ez_bar_preacher_curls",
    "Skullcrushers (EZ-Bar)" to "skullcrushers_ez_bar",
    "Incline Dumbbell Curls" to "incline_dumbbell_curls",
    "Overhead Dumbbell Extension" to "overhead_dumbbell_extension",
    "Cable Hammer Curls" to "cable_hammer_curls",
    "Single-Arm Tricep Pushdowns" to "single_arm_tricep_pushdowns",
    "Heavy Cheat Curls (Barbell)" to "heavy_cheat_curls_barbell",
    "Close-Grip Bench Press" to "close_grip_bench_press",
    "Spider Curls" to "spider_curls",
    "Weighted Overhead Cable Extension" to "weighted_overhead_cable_extension",
    "Reverse Grip Barbell Curls" to "reverse_grip_barbell_curls",
    "Tricep Dips to Burnout" to "tricep_dips_to_burnout",
    "Goblet Squats" to "goblet_squats",
    "Leg Press" to "leg_press",
    "Lying Leg Curls" to "lying_leg_curls",
    "Walking Lunges" to "walking_lunges",
    "Standing Calf Raises" to "standing_calf_raises",
    "Barbell Back Squats" to "squat",
    "Romanian Deadlifts (RDL)" to "rdl",
    "Leg Extensions" to "leg_extensions",
    "Seated Leg Curls" to "seated_leg_curls",
    "Seated Calf Raises" to "seated_calf_raises",
    "Heavy Barbell Back Squats" to "squat",
    "Barbell Hip Thrusts" to "barbell_hip_thrusts",
    "Bulgarian Split Squats" to "bulgarian_split_squats",
    "Heavy Romanian Deadlifts" to "heavy_romanian_deadlifts",
    "Single-Leg Extensions" to "single_leg_extensions",
    "Donkey Calf Raises" to "donkey_calf_raises"
)

@Composable
fun ExerciseVideoPlayer(exerciseName: String) {
    val context = LocalContext.current
    val videoName = exerciseVideos[exerciseName]
    val resId = remember(videoName) {
        if (videoName.isNullOrBlank()) 0
        else context.resources.getIdentifier(videoName, "raw", context.packageName)
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (resId != 0 && !LocalInspectionMode.current) {
                val uri = remember(resId) { Uri.parse("android.resource://${context.packageName}/$resId") }
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setOnPreparedListener { player ->
                                player.isLooping = true
                                start()
                            }
                            // Tap the video to pause / resume
                            setOnClickListener { if (isPlaying) pause() else start() }
                        }
                    },
                    update = { view ->
                        if (view.tag != resId) {
                            view.tag = resId
                            view.setVideoURI(uri)
                        }
                    },
                    onRelease = { it.stopPlayback() },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Video placeholder",
                        tint = PurpleLight,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Video coming soon", color = TextGray, fontSize = 13.sp)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                exerciseName,
                color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (resId != 0) Text("Tap video to pause", color = TextGray, fontSize = 11.sp)
        }
    }
}

// Personal records
@Composable
fun PersonalRecordsCard(history: List<LiftEntry>) {
    val latest = history.lastOrNull()
    val earlier = history.dropLast(1)

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Default.EmojiEvents, size = 36)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Personal Records", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (latest == null) {
                Text(
                    "Save your lifts on the Quest Board to set your first records.",
                    color = TextGray, fontSize = 13.sp
                )
            } else {
                // A lift is a "new PR" when your latest save beats everything saved before it
                RecordRow("Bench Press", history.maxOf { it.bench },
                    earlier.isNotEmpty() && latest.bench > earlier.maxOf { it.bench })
                Spacer(modifier = Modifier.height(12.dp))
                RecordRow("Deadlift", history.maxOf { it.deadlift },
                    earlier.isNotEmpty() && latest.deadlift > earlier.maxOf { it.deadlift })
                Spacer(modifier = Modifier.height(12.dp))
                RecordRow("Back Squat", history.maxOf { it.squat },
                    earlier.isNotEmpty() && latest.squat > earlier.maxOf { it.squat })
            }
        }
    }
}

@Composable
fun RecordRow(label: String, best: Int, isNewPr: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardElevated)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isNewPr) {
                Text(
                    "NEW PR",
                    color = FlameOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FlameOrange.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text("$best lbs", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Rank progress
@Composable
fun RankProgressCard(history: List<LiftEntry>) {
    val shown = history.takeLast(7)


    val promotions = history.mapIndexedNotNull { i, entry ->
        val now = rankIndexFor(entry.total)
        val before = if (i == 0) -1 else rankIndexFor(history[i - 1].total)
        if (now > before) Pair(powerRanks[now].name, entry.day) else null
    }.takeLast(2).reversed()

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Rank Progress", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            if (shown.isEmpty()) {
                Text(
                    "Your total over time will appear here after your first save.",
                    color = TextGray, fontSize = 13.sp
                )
            } else {
                Text("Total (lbs) from your last ${shown.size} saves", color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(20.dp))

                val maxTotal = shown.maxOf { it.total }.coerceAtLeast(1)

                Row(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    shown.forEachIndexed { index, entry ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text("${entry.total}", color = TextGray, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(maxOf(4f, 100f * entry.total / maxTotal).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (index == shown.lastIndex) PurpleAccent else DarkCardElevated)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                formatDay(entry.day),
                                color = if (index == shown.lastIndex) PurpleLight else TextGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (promotions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    promotions.forEach { (rankName, day) ->
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward, contentDescription = null,
                                tint = PurpleLight, modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reached $rankName", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(formatDay(day), color = TextGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------- History ----------
@Composable
fun ProgressHistoryUi(
    history: List<LiftEntry>,
    workoutsDone: Int,
    dayStreak: Int,
    activity: Map<Long, Int>
) {

    val today = todayEpochDay()
    val todayIndex = ((today + 3) % 7).toInt()
    val weekStart = today - todayIndex
    val counts = List(7) { activity[weekStart + it] ?: 0 }
    val maxCount = (counts.maxOrNull() ?: 1).coerceAtLeast(1)
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        ScreenHeader("History", "Your training activity this week")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(Modifier.weight(1f), Icons.Default.CalendarMonth, PurpleLight, "$workoutsDone", "Workouts")
            StatTile(Modifier.weight(1f), Icons.Default.LocalFireDepartment, FlameOrange, "$dayStreak", "Day Streak")
        }

        Spacer(modifier = Modifier.height(16.dp))
        PersonalRecordsCard(history)

        Spacer(modifier = Modifier.height(16.dp))
        RankProgressCard(history)

        Spacer(modifier = Modifier.height(16.dp))

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Weekly Activity", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Workouts finished + lift saves per day", color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEachIndexed { index, day ->
                        val count = counts[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            if (count > 0) {
                                Text("$count", color = TextGray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(if (count == 0) 6.dp else maxOf(10f, 90f * count / maxCount).dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (index == todayIndex) PurpleAccent else DarkCardElevated)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                day,
                                color = if (index == todayIndex) PurpleLight else TextGray,
                                fontSize = 12.sp,
                                fontWeight = if (index == todayIndex) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AppUiShell()
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    OnboardingUi(onFinish = { _, _ -> })
}
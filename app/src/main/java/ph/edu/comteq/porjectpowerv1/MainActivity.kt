package ph.edu.comteq.porjectpowerv1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DarkBackground = Color(0xFF0F0E17)
val DarkCardColor = Color(0xFF1E1B2E)
val DarkCardElevated = Color(0xFF28243D)
val PurpleAccent = Color(0xFF9D4EDD)
val PurpleLight = Color(0xFFC77DFF)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF9CA3AF)
val BorderColor = Color(0xFF332E4C)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUiShell()
        }
    }
}

@Composable
fun AppUiShell() {
    var currentTab by remember { mutableIntStateOf(1) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            Surface(
                color = DarkCardColor,
                modifier = Modifier.fillMaxWidth().height(65.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { currentTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentTab == 1) PurpleAccent else Color.Transparent
                        )
                    ) { Text("Dashboard", color = TextWhite, fontSize = 11.sp) }

                    Button(
                        onClick = { currentTab = 2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentTab == 2) PurpleAccent else Color.Transparent
                        )
                    ) { Text("Quests", color = TextWhite, fontSize = 11.sp) }

                    Button(
                        onClick = { currentTab = 3 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentTab == 3) PurpleAccent else Color.Transparent
                        )
                    ) { Text("Workout", color = TextWhite, fontSize = 11.sp) }

                    Button(
                        onClick = { currentTab = 4 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentTab == 4) PurpleAccent else Color.Transparent
                        )
                    ) { Text("History", color = TextWhite, fontSize = 11.sp) }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp)
        ) {
            when (currentTab) {
                1 -> DashboardUi()
                2 -> QuestBoardUi()
                3 -> PreSavedWorkoutUi()
                4 -> ProgressHistoryUi()
            }
        }
    }
}

// For dashboard
@Composable
fun DashboardUi() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Project Power", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(DarkCardColor)
                    .border(2.dp, PurpleAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.viltrumempirelogo),
                    contentDescription = "Viltrum Empire Rank Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardColor),
            modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CURRENT RANK", color = PurpleLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Saiyan Tier (850-1000 lbs)", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.85f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = PurpleAccent,
                    trackColor = DarkCardElevated
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Categories", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(3) { index ->
                val title = if (index == 0) "Total Lifts (650 Lbs)" else if (index == 1) "Lift Streak (3 Days)" else "Last Lift (250 Lbs)"
                Card(
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCardColor),
                    modifier = Modifier.fillMaxWidth().height(65.dp).border(1.dp, BorderColor, RoundedCornerShape(30.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(PurpleAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = PurpleLight)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(title, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// for quest board
@Composable
fun QuestBoardUi() {
    var benchPressInput by remember { mutableStateOf("") }
    var deadliftInput by remember { mutableStateOf("") }
    var squatInput by remember { mutableStateOf("") }

    Column {
        Text("Quest Board", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Log your big 3 lifts to calculate future rank", color = TextGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardColor),
            modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Power Ranking Inputs", color = PurpleLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = benchPressInput,
                    onValueChange = { benchPressInput = it },
                    label = { Text("Bench Press (lbs)", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deadliftInput,
                    onValueChange = { deadliftInput = it },
                    label = { Text("Deadlifts (lbs)", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = squatInput,
                    onValueChange = { squatInput = it },
                    label = { Text("Back Squats (lbs)", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Design only */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Power Lifts", color = TextWhite)
                }
            }
        }
    }
}

// for workouts amd pre saved workouts
@Composable
fun PreSavedWorkoutUi() {
    var selectedWorkoutTitle by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf("Medium") }

    if (selectedWorkoutTitle == null) {
        // --- VIEW 1: WORKOUT CATEGORY LIST ---
        val workoutPlans = listOf(
            Pair("Chest Day Quest", "Push Lifts Focus • Pecs & Triceps"),
            Pair("Back Day Quest", "Pull Lifts Focus • Lats & Upper Back"),
            Pair("Arm Day Quest", "Isolation Focus • Biceps & Triceps"),
            Pair("Leg Day Quest", "Lower Body Focus • Quads, Glutes & Hamstrings")
        )

        Column {
            Text("Pre-Saved Workouts", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Select a plan to configure difficulty", color = TextGray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(workoutPlans.size) { index ->
                    val (title, summary) = workoutPlans[index]
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardColor),
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, color = PurpleLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(summary, color = TextGray, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { selectedWorkoutTitle = title },
                                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Start", color = TextWhite, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // for workout details and difficulty
        val exercises = getExercisesForWorkout(selectedWorkoutTitle!!, selectedDifficulty)

        Column {
            // Header with Back Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { selectedWorkoutTitle = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedWorkoutTitle!!, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty Selection Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Easy", "Medium", "Hard").forEach { difficulty ->
                    val isSelected = selectedDifficulty == difficulty
                    Button(
                        onClick = { selectedDifficulty = difficulty },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PurpleAccent else DarkCardColor
                        ),
                        modifier = Modifier.weight(1f).border(1.dp, if (isSelected) PurpleLight else BorderColor, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(difficulty, color = TextWhite, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // for future video holder
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = "Workout Guide Video Placeholder",
                            tint = PurpleLight,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Workout Guide Video Container", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Interactive demonstration video player", color = TextGray, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // exercise list
            Text("Routine ($selectedDifficulty Level - ${exercises.size} Exercises)", color = PurpleLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exercises.size) { index ->
                    val (exerciseName, setsReps) = exercises[index]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardColor),
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}. $exerciseName", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(setsReps, color = TextGray, fontSize = 12.sp)
                        }
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

// for progress and history
@Composable
fun ProgressHistoryUi() {
    Column {
        Text("History & Records", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCardColor),
            modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Weekly Activity", color = TextGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val heights = listOf(0.4f, 0.9f, 0.5f, 0.8f, 0.3f, 0.7f, 1.0f)
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")

                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(heights[index])
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (index == 6) PurpleAccent else DarkCardElevated)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(day, color = TextGray, fontSize = 12.sp)
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
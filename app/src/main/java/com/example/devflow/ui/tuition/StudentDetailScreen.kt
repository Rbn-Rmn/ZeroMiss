package com.example.devflow.ui.tuition
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.FlowRow
import com.example.devflow.data.model.Attendance
import com.example.devflow.data.model.Student
import com.example.devflow.ui.components.GlassCard
import com.example.devflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Int,
    navController: NavController,
    viewModel: TuitionViewModel = viewModel()
) {
    val context = LocalContext.current
    val students by viewModel.allStudents.collectAsState()
    val student = students.find { it.id == studentId }

    val monthlyAttendance by viewModel.monthlyAttendance.collectAsState()
    val presentCount by viewModel.presentCount.collectAsState()
    val absentCount by viewModel.absentCount.collectAsState()
    val extraCount by viewModel.extraCount.collectAsState()
    val viewMonth by viewModel.viewMonth.collectAsState()
    val viewYear by viewModel.viewYear.collectAsState()

    var showMarkDialog by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingAttendance by remember { mutableStateOf<Attendance?>(null) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showEditStudentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(studentId) { viewModel.loadStudent(studentId) }
    LaunchedEffect(students) {
        if (students.isNotEmpty()) viewModel.loadStudent(studentId)
    }

    if (student == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LGPrimary)
        }
        return
    }

    val studentColor = try {
        Color(android.graphics.Color.parseColor(student.colorLabel))
    } catch (e: Exception) { LGPrimary }

    val initials = student.name.split(" ")
        .take(2).joinToString("") { it.first().uppercase() }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        .format(Calendar.getInstance().apply { set(viewYear, viewMonth, 1) }.time)

    if (showShareDialog) {
        EnhancedShareReportDialog(
            student = student,
            viewMonth = viewMonth,
            viewYear = viewYear,
            onDismiss = { showShareDialog = false },
            onShare = { report ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, report)
                }
                context.startActivity(Intent.createChooser(intent, "Share Report"))
                showShareDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showEditStudentDialog) {
        EditStudentDialog(
            student = student,
            onDismiss = { showEditStudentDialog = false },
            onSave = { updatedStudent ->
                viewModel.updateStudent(updatedStudent)
                showEditStudentDialog = false
            }
        )
    }

    if (showMarkDialog) {
        MarkAttendanceDialog(
            dateMillis = selectedDateMillis,
            onDismiss = { showMarkDialog = false },
            onMark = { status, note ->
                viewModel.markAttendance(studentId, selectedDateMillis, status, note)
                showMarkDialog = false
            }
        )
    }

    if (showEditDialog && editingAttendance != null) {
        EditAttendanceDialog(
            attendance = editingAttendance!!,
            onDismiss = { showEditDialog = false },
            onSave = { status, note ->
                viewModel.markAttendance(studentId, editingAttendance!!.date, status, note)
                showEditDialog = false
            },
            onDelete = {
                viewModel.deleteAttendance(editingAttendance!!)
                showEditDialog = false
            }
        )
    }

    if (showNotesDialog) {
        NotesDialog(
            student = student,
            onDismiss = { showNotesDialog = false },
            onSave = { updatedNotes ->
                viewModel.updateStudent(student.copy(notes = updatedNotes))
                showNotesDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back",
                                modifier = Modifier.size(20.dp))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showEditStudentDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit",
                            tint = LGPrimary)
                    }
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share",
                            tint = LGPrimary)
                    }
                    if (student.guardianPhone.isNotBlank()) {
                        IconButton(onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${student.guardianPhone}")
                                }
                            )
                        }) {
                            Icon(Icons.Filled.Call, contentDescription = "Call",
                                tint = LGSecondary)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Profile header card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = studentColor.copy(alpha = 0.2f),
                        spotColor = studentColor.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                studentColor.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                studentColor.copy(alpha = 0.4f),
                                studentColor.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(8.dp, CircleShape,
                                ambientColor = studentColor.copy(alpha = 0.3f),
                                spotColor = studentColor.copy(alpha = 0.3f))
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(studentColor, studentColor.copy(alpha = 0.6f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (student.photoUri.isNotBlank()) {
                            val bitmap = remember(student.photoUri) {
                                try {
                                    val uri = Uri.parse(student.photoUri)
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    android.graphics.BitmapFactory.decodeStream(inputStream)
                                } catch (e: Exception) { null }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(initials, color = Color.White,
                                    fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(initials, color = Color.White,
                                fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (student.subject.isNotBlank())
                            Text(student.subject, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (student.grade.isNotBlank())
                            Text("Class: ${student.grade}", fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (student.fee.isNotBlank())
                            Text("Fee: ${student.fee}", fontSize = 13.sp,
                                color = LGSecondary, fontWeight = FontWeight.Medium)
                        val days = student.scheduleDays.split(",")
                            .filter { it.isNotBlank() }
                        if (days.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                days.forEach { day ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(studentColor.copy(alpha = 0.15f))
                                            .border(0.5.dp, studentColor.copy(alpha = 0.4f),
                                                RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(day.trim(), fontSize = 9.sp,
                                            color = studentColor,
                                            fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Month navigation
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply {
                            set(viewYear, viewMonth, 1)
                            add(Calendar.MONTH, -1)
                        }
                        viewModel.setViewMonth(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
                    }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous")
                    }
                    Text(monthName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply {
                            set(viewYear, viewMonth, 1)
                            add(Calendar.MONTH, 1)
                        }
                        viewModel.setViewMonth(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR))
                    }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next")
                    }
                }
            }

            // Stats row with gradient cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidStatCard("Present", presentCount,
                    listOf(Color(0xFF34C759), Color(0xFF00ACC1)),
                    Modifier.weight(1f))
                LiquidStatCard("Absent", absentCount,
                    listOf(Color(0xFFFF3B30), Color(0xFFFF6B35)),
                    Modifier.weight(1f))
                LiquidStatCard("Extra", extraCount,
                    listOf(Color(0xFF007AFF), Color(0xFF5856D6)),
                    Modifier.weight(1f))
                LiquidStatCard("Total", presentCount + extraCount,
                    listOf(Color(0xFF5856D6), Color(0xFFFF2D55)),
                    Modifier.weight(1f))
            }

            // Attendance Calendar
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    AttendanceCalendar(
                        year = viewYear,
                        month = viewMonth,
                        attendanceList = monthlyAttendance,
                        scheduleDays = student.scheduleDays,
                        onDayClick = { dateMillis, existingAttendance ->
                            if (existingAttendance != null) {
                                editingAttendance = existingAttendance
                                showEditDialog = true
                            } else {
                                selectedDateMillis = dateMillis
                                showMarkDialog = true
                            }
                        }
                    )
                }
            }

            // Notes section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(
                            onClick = { showNotesDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit notes",
                                modifier = Modifier.size(18.dp),
                                tint = LGPrimary)
                        }
                    }
                    if (student.notes.isBlank()) {
                        Text("No notes yet. Tap ✏️ to add.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(text = student.notes,
                            fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditStudentDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    val context = LocalContext.current
    val colorOptions = listOf(
        "#007AFF", "#5856D6", "#34C759",
        "#FF9500", "#FF3B30", "#00ACC1", "#FF2D55"
    )
    val allDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    var name by remember { mutableStateOf(student.name) }
    var subject by remember { mutableStateOf(student.subject) }
    var grade by remember { mutableStateOf(student.grade) }
    var guardianPhone by remember { mutableStateOf(student.guardianPhone) }
    var fee by remember { mutableStateOf(student.fee) }
    var notes by remember { mutableStateOf(student.notes) }
    var selectedDays = remember {
        mutableStateListOf<String>().apply {
            addAll(student.scheduleDays.split(",").filter { it.isNotBlank() })
        }
    }
    var selectedColor by remember { mutableStateOf(student.colorLabel) }
    var photoUri by remember { mutableStateOf(student.photoUri) }
    var nameError by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { e.printStackTrace() }
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Student", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(selectedColor))
                                } catch (e: Exception) { LGPrimary }
                            )
                            .clickable { photoPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotBlank()) {
                            val bitmap = remember(photoUri) {
                                try {
                                    val uri = Uri.parse(photoUri)
                                    val inputStream = context.contentResolver
                                        .openInputStream(uri)
                                    android.graphics.BitmapFactory.decodeStream(inputStream)
                                } catch (e: Exception) { null }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Filled.AddAPhoto,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Icon(Icons.Filled.AddAPhoto,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp))
                        }
                    }
                    Column {
                        Text("Tap to change photo", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (photoUri.isNotBlank()) {
                            TextButton(
                                onClick = { photoUri = "" },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Remove photo", fontSize = 11.sp, color = LGError)
                            }
                        }
                    }
                }

                OutlinedTextField(value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Name *") }, isError = nameError,
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Class/Grade") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text("Guardian Phone") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = fee,
                    onValueChange = { fee = it },
                    label = { Text("Monthly Fee") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2, maxLines = 4)

                Text("Schedule Days", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allDays.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) LGPrimary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                                .border(1.dp,
                                    if (isSelected) LGPrimary
                                    else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isSelected) selectedDays.remove(day)
                                    else selectedDays.add(day)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(day, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) LGPrimary
                                else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Text("Color", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selectedColor == hex)
                                        Modifier.border(3.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) { nameError = true; return@Button }
                onSave(student.copy(
                    name = name.trim(),
                    subject = subject.trim(),
                    grade = grade.trim(),
                    guardianPhone = guardianPhone.trim(),
                    fee = fee.trim(),
                    notes = notes.trim(),
                    scheduleDays = selectedDays.joinToString(","),
                    colorLabel = selectedColor,
                    photoUri = photoUri
                ))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedShareReportDialog(
    student: Student,
    viewMonth: Int,
    viewYear: Int,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit,
    viewModel: TuitionViewModel
) {
    var selectedOption by remember { mutableStateOf(0) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val fromDateState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().apply {
            set(viewYear, viewMonth, 1)
        }.timeInMillis
    )
    val toDateState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showFromPicker) {
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("OK") }
            }
        ) { DatePicker(state = fromDateState) }
    }

    if (showToPicker) {
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = { showToPicker = false }) { Text("OK") }
            }
        ) { DatePicker(state = toDateState) }
    }

    val fromFormatted = fromDateState.selectedDateMillis?.let {
        SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Select"
    val toFormatted = toDateState.selectedDateMillis?.let {
        SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(Date(it))
    } ?: "Select"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Report", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ReportOptionCard(
                    title = "📅 Current Month",
                    subtitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        .format(Calendar.getInstance().apply {
                            set(viewYear, viewMonth, 1)
                        }.time),
                    isSelected = selectedOption == 0,
                    onClick = { selectedOption = 0 }
                )
                ReportOptionCard(
                    title = "📆 Custom Range",
                    subtitle = "$fromFormatted → $toFormatted",
                    isSelected = selectedOption == 1,
                    onClick = { selectedOption = 1 }
                )
                if (selectedOption == 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFromPicker = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("From: $fromFormatted", fontSize = 11.sp) }
                        OutlinedButton(
                            onClick = { showToPicker = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("To: $toFormatted", fontSize = 11.sp) }
                    }
                }
                ReportOptionCard(
                    title = "📊 All Time",
                    subtitle = "Complete attendance history",
                    isSelected = selectedOption == 2,
                    onClick = { selectedOption = 2 }
                )
                HorizontalDivider()
                Text("Report Format", fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Summary button
                    Button(
                        onClick = {
                            scope.launch {
                                val report = when (selectedOption) {
                                    0 -> viewModel.generateMonthlyReport(
                                        student, viewMonth, viewYear)
                                    1 -> viewModel.generateRangeReportSuspend(
                                        student,
                                        fromDateState.selectedDateMillis ?: 0L,
                                        toDateState.selectedDateMillis
                                            ?: System.currentTimeMillis())
                                    else -> viewModel.generateAllTimeReportSuspend(student)
                                }
                                onShare(report)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LGPrimary)
                    ) { Text("Summary", fontSize = 12.sp) }

                    // Detailed button
                    Button(
                        onClick = {
                            scope.launch {
                                val report = when (selectedOption) {
                                    0 -> viewModel.generateDetailedReport(
                                        student, viewMonth, viewYear)
                                    1 -> viewModel.generateRangeReportSuspend(
                                        student,
                                        fromDateState.selectedDateMillis ?: 0L,
                                        toDateState.selectedDateMillis
                                            ?: System.currentTimeMillis())
                                    else -> viewModel.generateAllTimeReportSuspend(student)
                                }
                                onShare(report)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LGPurple)
                    ) { Text("Detailed", fontSize = 12.sp) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ReportOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) LGPrimary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) LGPrimary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LiquidStatCard(
    label: String,
    count: Int,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = gradientColors.first().copy(alpha = 0.3f),
                spotColor = gradientColors.first().copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(colors = gradientColors)
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), fontSize = 24.sp,
                fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium)
        }
    }
}
@Composable
fun ShareReportDialog(
    onDismiss: () -> Unit,
    onShareSummary: () -> Unit,
    onShareDetailed: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Report", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Choose report format:", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onShareSummary() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Summary Report", fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold)
                        Text("Present / Absent / Extra counts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onShareDetailed() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Detailed Report", fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold)
                        Text("Day by day attendance log",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MarkAttendanceDialog(
    dateMillis: Long,
    onDismiss: () -> Unit,
    onMark: (String, String) -> Unit
) {
    val dateStr = SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault())
        .format(Date(dateMillis))
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark Attendance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(dateStr, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Text("Mark as:", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onMark("PRESENT", note) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34C759)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Present", fontSize = 12.sp) }
                    Button(
                        onClick = { onMark("ABSENT", note) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF3B30)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Absent", fontSize = 12.sp) }
                    Button(
                        onClick = { onMark("EXTRA", note) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Extra", fontSize = 12.sp) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditAttendanceDialog(
    attendance: Attendance,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault())
        .format(Date(attendance.date))
    var note by remember { mutableStateOf(attendance.note) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Attendance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(dateStr, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Currently: ${attendance.status}", fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (attendance.status) {
                        "PRESENT" -> Color(0xFF34C759)
                        "ABSENT"  -> Color(0xFFFF3B30)
                        else      -> Color(0xFF007AFF)
                    })
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Text("Change to:", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onSave("PRESENT", note) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF34C759)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Present", fontSize = 12.sp) }
                    Button(
                        onClick = { onSave("ABSENT", note) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF3B30)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Absent", fontSize = 12.sp) }
                    Button(
                        onClick = { onSave("EXTRA", note) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Extra", fontSize = 12.sp) }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun NotesDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(student.notes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notes for ${student.name}", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(160.dp),
                maxLines = 8
            )
        },
        confirmButton = {
            Button(onClick = { onSave(notes) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AttendanceCalendar(
    year: Int,
    month: Int,
    attendanceList: List<Attendance>,
    scheduleDays: String,
    onDayClick: (Long, Attendance?) -> Unit
) {
    val scheduledDayNames = scheduleDays.split(",").filter { it.isNotBlank() }
    val dayNameMap = mapOf(
        Calendar.MONDAY to "MON", Calendar.TUESDAY to "TUE",
        Calendar.WEDNESDAY to "WED", Calendar.THURSDAY to "THU",
        Calendar.FRIDAY to "FRI", Calendar.SATURDAY to "SAT",
        Calendar.SUNDAY to "SUN"
    )
    val attendanceByDay = attendanceList.associateBy { att ->
        Calendar.getInstance().apply { timeInMillis = att.date }
            .get(Calendar.DAY_OF_MONTH)
    }
    val firstDayOfMonth = Calendar.getInstance().apply {
        set(year, month, 1)
    }.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = Calendar.getInstance().apply {
        set(year, month, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Attendance Calendar", fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Su","Mo","Tu","We","Th","Fr","Sa").forEach { day ->
                Text(text = day, modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
            }
        }
        val rows = ((firstDayOfMonth + daysInMonth) + 6) / 7
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNumber = row * 7 + col - firstDayOfMonth + 1
                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(38.dp))
                    } else {
                        val calForDay = Calendar.getInstance().apply {
                            set(year, month, dayNumber, 12, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dayName = dayNameMap[calForDay.get(Calendar.DAY_OF_WEEK)] ?: ""
                        val isScheduled = scheduledDayNames.contains(dayName)
                        val attendance = attendanceByDay[dayNumber]
                        val bgColor = when (attendance?.status) {
                            "PRESENT" -> Color(0xFF34C759)
                            "ABSENT"  -> Color(0xFFFF3B30)
                            "EXTRA"   -> Color(0xFF007AFF)
                            else -> if (isScheduled)
                                MaterialTheme.colorScheme.surfaceVariant
                            else Color.Transparent
                        }
                        val textColor = when {
                            attendance != null -> Color.White
                            isScheduled -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        }
                        val isToday = Calendar.getInstance().let { now ->
                            now.get(Calendar.DAY_OF_MONTH) == dayNumber &&
                                    now.get(Calendar.MONTH) == month &&
                                    now.get(Calendar.YEAR) == year
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f).height(38.dp).padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .then(
                                    if (isToday) Modifier.border(
                                        2.dp, LGPrimary, RoundedCornerShape(8.dp)
                                    ) else Modifier
                                )
                                .clickable {
                                    onDayClick(calForDay.timeInMillis, attendance)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(dayNumber.toString(), fontSize = 12.sp,
                                fontWeight = if (isToday) FontWeight.Bold
                                else FontWeight.Normal,
                                color = textColor)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()) {
            LegendItem(Color(0xFF34C759), "Present")
            LegendItem(Color(0xFFFF3B30), "Absent")
            LegendItem(Color(0xFF007AFF), "Extra")
            LegendItem(MaterialTheme.colorScheme.surfaceVariant, "Scheduled")
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp)
            .clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
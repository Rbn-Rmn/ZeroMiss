package com.example.devflow.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.devflow.data.local.AppDatabase
import com.example.devflow.data.model.Attendance
import com.example.devflow.data.model.Student
import com.example.devflow.data.model.Task
import com.example.devflow.data.repository.ContestRepository
import com.example.devflow.data.repository.TaskRepository
import com.example.devflow.data.repository.TuitionRepository
import com.example.devflow.data.model.Contest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = TaskRepository(
        AppDatabase.getDatabase(application).taskDao()
    )

    private val tuitionRepository = TuitionRepository(
        AppDatabase.getDatabase(application).studentDao(),
        AppDatabase.getDatabase(application).attendanceDao()
    )

    private val contestRepository = ContestRepository()

    // Tasks
    val todayTasks: StateFlow<List<Task>> = taskRepository.getTodayTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tomorrowTasks: StateFlow<List<Task>> = taskRepository.getTomorrowTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missedTasks: StateFlow<List<Task>> = taskRepository.getMissedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Students scheduled for today
    private val _todayStudents = MutableStateFlow<List<Student>>(emptyList())
    val todayStudents: StateFlow<List<Student>> = _todayStudents

    // Today's attendance map studentId -> Attendance?
    private val _todayAttendance = MutableStateFlow<Map<Int, Attendance?>>(emptyMap())
    val todayAttendance: StateFlow<Map<Int, Attendance?>> = _todayAttendance

    // Today's CF contests
    private val _todayContests = MutableStateFlow<List<Contest>>(emptyList())
    val todayContests: StateFlow<List<Contest>> = _todayContests

    // Day name map
    private val dayNameMap = mapOf(
        Calendar.MONDAY to "MON",
        Calendar.TUESDAY to "TUE",
        Calendar.WEDNESDAY to "WED",
        Calendar.THURSDAY to "THU",
        Calendar.FRIDAY to "FRI",
        Calendar.SATURDAY to "SAT",
        Calendar.SUNDAY to "SUN"
    )

    init {
        loadTodayStudents()
        loadTodayContests()
    }

    private fun loadTodayStudents() {
        viewModelScope.launch {
            tuitionRepository.getAllStudents().collect { students ->
                val today = Calendar.getInstance()
                val todayName = dayNameMap[today.get(Calendar.DAY_OF_WEEK)] ?: ""

                val scheduled = students.filter { student ->
                    student.scheduleDays.split(",")
                        .map { it.trim() }
                        .contains(todayName)
                }
                _todayStudents.value = scheduled
                loadTodayAttendance(scheduled)
            }
        }
    }

    private fun loadTodayAttendance(students: List<Student>) {
        viewModelScope.launch {
            val today = Calendar.getInstance()
            val todayMillis = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val attendanceMap = mutableMapOf<Int, Attendance?>()
            students.forEach { student ->
                val att = tuitionRepository.getAttendanceForDay(student.id, todayMillis)
                attendanceMap[student.id] = att
            }
            _todayAttendance.value = attendanceMap
        }
    }

    private fun loadTodayContests() {
        viewModelScope.launch {
            try {
                val contests = contestRepository.fetchContests()
                val now = System.currentTimeMillis()
                val endOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                }.timeInMillis

                _todayContests.value = contests.filter { contest ->
                    contest.startTimeMillis in now..endOfDay
                }
            } catch (e: Exception) {
                _todayContests.value = emptyList()
            }
        }
    }

    fun markAttendance(
        studentId: Int,
        status: String,
        note: String = ""
    ) {
        viewModelScope.launch {
            val todayMillis = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val existing = tuitionRepository.getAttendanceForDay(studentId, todayMillis)
            if (existing != null) {
                tuitionRepository.updateAttendance(
                    existing.copy(status = status, note = note)
                )
            } else {
                tuitionRepository.markAttendance(
                    Attendance(
                        studentId = studentId,
                        date = todayMillis,
                        status = status,
                        note = note
                    )
                )
            }
            // Refresh attendance map
            loadTodayAttendance(_todayStudents.value)
        }
    }

    fun autoMarkAbsent() {
        viewModelScope.launch {
            val todayMillis = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            _todayStudents.value.forEach { student ->
                val existing = tuitionRepository.getAttendanceForDay(
                    student.id, todayMillis
                )
                if (existing == null) {
                    tuitionRepository.markAttendance(
                        Attendance(
                            studentId = student.id,
                            date = todayMillis,
                            status = "ABSENT",
                            note = "Auto-marked absent"
                        )
                    )
                }
            }
            loadTodayAttendance(_todayStudents.value)
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch { taskRepository.updateTask(task.copy(isCompleted = true)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task) }
    }
}
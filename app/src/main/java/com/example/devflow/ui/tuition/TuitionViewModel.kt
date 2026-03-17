package com.example.devflow.ui.tuition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.devflow.data.local.AppDatabase
import com.example.devflow.data.model.Attendance
import com.example.devflow.data.model.Student
import com.example.devflow.data.repository.TuitionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TuitionRepository(
        AppDatabase.getDatabase(application).studentDao(),
        AppDatabase.getDatabase(application).attendanceDao()
    )

    // All students
    val allStudents: StateFlow<List<Student>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected student for detail screen
    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent

    // Monthly attendance for selected student
    private val _monthlyAttendance = MutableStateFlow<List<Attendance>>(emptyList())
    val monthlyAttendance: StateFlow<List<Attendance>> = _monthlyAttendance

    // Current viewing month/year
    private val _viewMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val viewMonth: StateFlow<Int> = _viewMonth

    private val _viewYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val viewYear: StateFlow<Int> = _viewYear

    // Monthly stats
    private val _presentCount = MutableStateFlow(0)
    val presentCount: StateFlow<Int> = _presentCount

    private val _absentCount = MutableStateFlow(0)
    val absentCount: StateFlow<Int> = _absentCount

    private val _extraCount = MutableStateFlow(0)
    val extraCount: StateFlow<Int> = _extraCount

    // ── Student operations ────────────────────────────────
    fun addStudent(student: Student) {
        viewModelScope.launch { repository.addStudent(student) }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch { repository.updateStudent(student) }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch { repository.deleteStudent(student) }
    }

    // ── Load student detail ───────────────────────────────
    fun loadStudent(studentId: Int) {
        viewModelScope.launch {
            allStudents.collect { students ->
                val found = students.find { it.id == studentId }
                if (found != null && _selectedStudent.value?.id != studentId) {
                    _selectedStudent.value = found
                    loadMonthlyData()
                }
            }
        }
    }

    fun setViewMonth(month: Int, year: Int) {
        _viewMonth.value = month
        _viewYear.value = year
        loadMonthlyData()
    }

    private fun loadMonthlyData() {
        val studentId = _selectedStudent.value?.id ?: return
        val month = _viewMonth.value
        val year = _viewYear.value
        viewModelScope.launch {
            repository.getMonthlyAttendance(studentId, year, month).collect { list ->
                _monthlyAttendance.value = list
                _presentCount.value = list.count { it.status == "PRESENT" }
                _absentCount.value = list.count { it.status == "ABSENT" }
                _extraCount.value = list.count { it.status == "EXTRA" }
            }
        }
    }

    // ── Attendance operations ─────────────────────────────
    fun markAttendance(studentId: Int, dateMillis: Long, status: String, note: String = "") {
        viewModelScope.launch {
            val existing = repository.getAttendanceForDay(studentId, dateMillis)
            if (existing != null) {
                repository.updateAttendance(existing.copy(status = status, note = note))
            } else {
                repository.markAttendance(
                    Attendance(
                        studentId = studentId,
                        date = dateMillis,
                        status = status,
                        note = note
                    )
                )
            }
            loadMonthlyData()
        }
    }

    fun deleteAttendance(attendance: Attendance) {
        viewModelScope.launch {
            repository.deleteAttendance(attendance)
            loadMonthlyData()
        }
    }

    // ── Monthly report ────────────────────────────────────
    fun generateMonthlyReport(student: Student, month: Int, year: Int): String {
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(Calendar.getInstance().apply { set(year, month, 1) }.time)
        val present = _presentCount.value
        val absent = _absentCount.value
        val extra = _extraCount.value
        val total = present + extra
        return """
📋 *Tuition Report — $monthName*
👤 Student: ${student.name}
📚 Subject: ${student.subject}
🎓 Class: ${student.grade}
${if (student.fee.isNotBlank()) "💰 Fee: ${student.fee}" else ""}

✅ Present: $present days
❌ Absent: $absent days
➕ Extra: $extra days
📊 Total taught: $total days

_Generated by ZeroMiss_
        """.trimIndent()
    }

    // ── Detailed report ───────────────────────────────────
    fun generateDetailedReport(student: Student, month: Int, year: Int): String {
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(Calendar.getInstance().apply { set(year, month, 1) }.time)
        val present = _presentCount.value
        val absent = _absentCount.value
        val extra = _extraCount.value
        val total = present + extra

        val attendance = _monthlyAttendance.value.sortedBy { it.date }
        val dayLines = attendance.joinToString("\n") { att ->
            val dayStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                .format(Date(att.date))
            val statusIcon = when (att.status) {
                "PRESENT" -> "✅"
                "ABSENT"  -> "❌"
                "EXTRA"   -> "➕"
                else      -> "•"
            }
            val noteStr = if (att.note.isNotBlank()) " — ${att.note}" else ""
            "$statusIcon $dayStr$noteStr"
        }

        return """
📋 *Detailed Report — $monthName*
👤 Student: ${student.name}
📚 Subject: ${student.subject}
🎓 Class: ${student.grade}
${if (student.fee.isNotBlank()) "💰 Fee: ${student.fee}" else ""}

📅 *Attendance Details:*
$dayLines

━━━━━━━━━━━━━━
✅ Present: $present days
❌ Absent: $absent days
➕ Extra: $extra days
📊 Total taught: $total days

_Generated by ZeroMiss_
        """.trimIndent()
    }

    // ── Range report ──────────────────────────────────────
    fun generateRangeReport(
        student: Student,
        fromMillis: Long,
        toMillis: Long
    ): String {
        val fromStr = SimpleDateFormat("MMM d yyyy", Locale.getDefault())
            .format(Date(fromMillis))
        val toStr = SimpleDateFormat("MMM d yyyy", Locale.getDefault())
            .format(Date(toMillis))

        val rangeAttendance = _monthlyAttendance.value
            .filter { it.date in fromMillis..toMillis }
            .sortedBy { it.date }

        val present = rangeAttendance.count { it.status == "PRESENT" }
        val absent = rangeAttendance.count { it.status == "ABSENT" }
        val extra = rangeAttendance.count { it.status == "EXTRA" }
        val total = present + extra

        val dayLines = rangeAttendance.joinToString("\n") { att ->
            val dayStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                .format(Date(att.date))
            val statusIcon = when (att.status) {
                "PRESENT" -> "✅"
                "ABSENT"  -> "❌"
                "EXTRA"   -> "➕"
                else      -> "•"
            }
            val noteStr = if (att.note.isNotBlank()) " — ${att.note}" else ""
            "$statusIcon $dayStr$noteStr"
        }

        return """
📋 *Attendance Report — ${student.name}*
📅 Period: $fromStr → $toStr
📚 Subject: ${student.subject}
🎓 Class: ${student.grade}
${if (student.fee.isNotBlank()) "💰 Fee: ${student.fee}" else ""}

📅 *Attendance Details:*
$dayLines

━━━━━━━━━━━━━━
✅ Present: $present days
❌ Absent: $absent days
➕ Extra: $extra days
📊 Total taught: $total days

_Generated by ZeroMiss_
        """.trimIndent()
    }

    // ── All time report ───────────────────────────────────
    fun generateAllTimeReport(student: Student): String {
        viewModelScope.launch {
            try {
                repository.getAttendanceForStudent(student.id).first().let { attendance ->
                    _monthlyAttendance.value = attendance
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val attendance = _monthlyAttendance.value.sortedBy { it.date }
        val present = attendance.count { it.status == "PRESENT" }
        val absent = attendance.count { it.status == "ABSENT" }
        val extra = attendance.count { it.status == "EXTRA" }
        val total = present + extra

        val fromStr = attendance.firstOrNull()?.let {
            SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(Date(it.date))
        } ?: "N/A"
        val toStr = attendance.lastOrNull()?.let {
            SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(Date(it.date))
        } ?: "N/A"

        return """
📋 *All Time Report — ${student.name}*
📅 Period: $fromStr → $toStr
📚 Subject: ${student.subject}
🎓 Class: ${student.grade}
${if (student.fee.isNotBlank()) "💰 Fee: ${student.fee}" else ""}

━━━━━━━━━━━━━━
✅ Present: $present days
❌ Absent: $absent days
➕ Extra: $extra days
📊 Total taught: $total days

_Generated by ZeroMiss_
        """.trimIndent()
    }
}
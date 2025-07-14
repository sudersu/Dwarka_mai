
package com.dailylist.pro

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import com.dailylist.pro.databinding.DialogAddTaskBinding
import java.util.*

class EditTaskDialog(private val taskToEdit: Task) : DialogFragment() {

    private var _binding: DialogAddTaskBinding? = null
    private val binding get() = _binding!!

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddTaskBinding.inflate(layoutInflater)
        val view = binding.root

        val calendar = Calendar.getInstance()
        // Populate fields with existing task data
        val taskDateParts = taskToEdit.date.split("/")
        selectedDay = taskDateParts[0].toInt()
        selectedMonth = taskDateParts[1].toInt() - 1 // Month is 0-indexed
        selectedYear = taskDateParts[2].toInt()

        val taskTimeParts = taskToEdit.time.split(":")
        selectedHour = taskTimeParts[0].toInt()
        selectedMinute = taskTimeParts[1].toInt()

        binding.editTextTaskName.setText(taskToEdit.title)
        binding.switchReminder.isChecked = taskToEdit.reminderEnabled

        updateDateText()
        updateTimeText()

        binding.textViewDate.setOnClickListener { showDatePicker() }
        binding.textViewTime.setOnClickListener { showTimePicker() }
        binding.buttonSave.setOnClickListener { saveTask() }

        return Dialog(requireContext()).apply { setContentView(view) }
    }

    private fun updateDateText() {
        binding.textViewDate.text = "Date: $selectedDay/${selectedMonth + 1}/$selectedYear"
    }

    private fun updateTimeText() {
        binding.textViewTime.text = String.format("Time: %02d:%02d", selectedHour, selectedMinute)
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedYear = year
                selectedMonth = month
                selectedDay = dayOfMonth
                updateDateText()
            },
            selectedYear,
            selectedMonth,
            selectedDay
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                updateTimeText()
            },
            selectedHour,
            selectedMinute,
            true // 24-hour format
        )
        timePickerDialog.show()
    }

    private fun saveTask() {
        val taskName = binding.editTextTaskName.text.toString().trim()
        if (taskName.isEmpty()) {
            Toast.makeText(requireContext(), "Task name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val reminderEnabled = binding.switchReminder.isChecked

        val updatedTask = taskToEdit.copy(
            title = taskName,
            date = "$selectedDay/${selectedMonth + 1}/$selectedYear",
            time = String.format("%02d:%02d", selectedHour, selectedMinute),
            reminderEnabled = reminderEnabled
        )

        (activity as? MainActivity)?.onTaskUpdated(updatedTask)
        if (reminderEnabled) {
            AlarmScheduler.scheduleAlarm(requireContext(), updatedTask)
        } else {
            AlarmScheduler.cancelAlarm(requireContext(), updatedTask.id)
        }
        Toast.makeText(requireContext(), "Task updated: $taskName", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditTaskDialog"
    }
}



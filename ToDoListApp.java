import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

public class ToDoListApp {
    private final JFrame frame;
    private final JTextField taskField, dateField;
    private final JComboBox<String> priorityBox;
    private final JTable taskTable;
    private final DefaultTableModel tableModel;
    private final TaskManager taskManager;
    private static final String FILE_NAME = "tasks.txt"; // File to store tasks

    public ToDoListApp() {
        frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{"Task", "Due Date", "Priority", "Status"}, 0);
        taskTable = new JTable(tableModel);
        taskManager = new TaskManager(tableModel);

        // Scroll Pane for Task List
        JScrollPane scrollPane = new JScrollPane(taskTable);

        JPanel inputPanel = new JPanel(new GridLayout(2, 3));
        taskField = new JTextField();
        dateField = new JTextField();
        priorityBox = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        JButton addButton = new JButton("Add Task");
        JButton deleteButton = new JButton("Delete Task");
        JButton completeButton = new JButton("Mark as Completed");

        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(new JLabel("Due Date:"));
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(taskField);
        inputPanel.add(dateField);
        inputPanel.add(priorityBox);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(completeButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // --- Key Bindings ---
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        // Press "Enter" to Add Task
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ADD_TASK");
        actionMap.put("ADD_TASK", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        // Button Actions
        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteTask());
        completeButton.addActionListener(e -> markTaskCompleted());

        loadTasks(); // Load tasks when app starts
        frame.setVisible(true);
    }

    private void addTask() {
        String name = taskField.getText().trim();
        String dueDate = dateField.getText().trim();
        String priority = (String) priorityBox.getSelectedItem();
        if (name.isEmpty() || dueDate.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Task name and due date cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        taskManager.addTask(new Task(name, dueDate, priority));
        taskField.setText("");
        dateField.setText("");
        saveTasks();
    }

    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            taskManager.deleteTask(selectedRow);
            saveTasks();
        } else {
            JOptionPane.showMessageDialog(frame, "Select a task to delete!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markTaskCompleted() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow >= 0) {
            taskManager.markTaskCompleted(selectedRow);
            saveTasks();
        } else {
            JOptionPane.showMessageDialog(frame, "Select a task to mark as completed!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                writer.write(tableModel.getValueAt(i, 0) + "," +  // Task Name
                             tableModel.getValueAt(i, 1) + "," +  // Due Date
                             tableModel.getValueAt(i, 2) + "," +  // Priority
                             tableModel.getValueAt(i, 3));        // Status
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving tasks!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    tableModel.addRow(new Object[]{data[0], data[1], data[2], data[3]});
                }
            }
        } catch (FileNotFoundException e) {
            // No saved tasks yet (file does not exist), so ignore
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading tasks!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ToDoListApp();
    }
}

class Task {
    String name, dueDate, priority;
    boolean completed;

    public Task(String name, String dueDate, String priority) {
        this.name = name;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
    }
}

class TaskManager {
    private final DefaultTableModel tableModel;

    public TaskManager(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public void addTask(Task task) {
        tableModel.addRow(new Object[]{task.name, task.dueDate, task.priority, task.completed ? "Completed" : "Pending"});
    }

    public void deleteTask(int index) {
        if (index >= 0) {
            tableModel.removeRow(index);
        }
    }

    public void markTaskCompleted(int index) {
        if (index >= 0) {
            tableModel.setValueAt("Completed", index, 3);
        }
    }
}

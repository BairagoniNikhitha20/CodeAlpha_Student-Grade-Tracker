import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class Student {
    private String name;
    private int[] grades;

    public Student(String name, int[] grades) {
        this.name = name;
        this.grades = grades;
    }

    public String getName() { return name; }

    public int[] getGrades() { return grades; }

    public double getAverage() {
        if (grades.length == 0) return 0;
        int sum = 0;
        for (int g : grades) sum += g;
        return (double) sum / grades.length;
    }

    public int getHighest() {
        if (grades.length == 0) return 0;
        int max = grades[0];
        for (int g : grades) if (g > max) max = g;
        return max;
    }

    public int getLowest() {
        if (grades.length == 0) return 0;
        int min = grades[0];
        for (int g : grades) if (g < min) min = g;
        return min;
    }

    public String toFileString() {
        StringBuilder sb = new StringBuilder(name);
        for (int g : grades) sb.append(",").append(g);
        return sb.toString();
    }

    public static Student fromFileString(String line) {
        String[] parts = line.split(",");
        String name = parts[0];
        int[] grades = new int[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            grades[i - 1] = Integer.parseInt(parts[i]);
        }
        return new Student(name, grades);
    }
}

public class StudentGradeTrackerGUI extends JFrame {
    private ArrayList<Student> students = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable table;
    private static final String FILE_NAME = "students.txt";

    public StudentGradeTrackerGUI() {
        setTitle("Student Grade Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 400);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"Name", "Grades", "Average", "Highest", "Lowest"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        String[] buttons = {"Add Student", "Edit Student", "Delete Student", "Show Summary", "Save & Exit"};
        JButton[] btn = new JButton[buttons.length];

        for (int i = 0; i < buttons.length; i++) {
            btn[i] = new JButton(buttons[i]);
            buttonPanel.add(btn[i]);
        }

        btn[0].addActionListener(e -> addStudentDialog());
        btn[1].addActionListener(e -> editStudentDialog());
        btn[2].addActionListener(e -> deleteStudent());
        btn[3].addActionListener(e -> showSummary());
        btn[4].addActionListener(e -> {
            saveToFile();
            JOptionPane.showMessageDialog(this, "Data saved. Goodbye!");
            System.exit(0);
        });

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadFromFile();
        refreshTable();
    }

    private void addStudentDialog() {
        JTextField nameField = new JTextField();
        JTextField gradesField = new JTextField();

        Object[] inputs = {
            "Student Name:", nameField,
            "Grades (comma separated):", gradesField
        };

        int option = JOptionPane.showConfirmDialog(this, inputs, "Add Student", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int[] grades = parseGrades(gradesField.getText());
                students.add(new Student(name, grades));
                refreshTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid grade input.");
            }
        }
    }

    private void editStudentDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to edit.");
            return;
        }

        Student s = students.get(row);
        JTextField nameField = new JTextField(s.getName());
        JTextField gradesField = new JTextField(joinGrades(s.getGrades()));

        Object[] inputs = {
            "Student Name:", nameField,
            "Grades (comma separated):", gradesField
        };

        int option = JOptionPane.showConfirmDialog(this, inputs, "Edit Student", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int[] grades = parseGrades(gradesField.getText());
                students.set(row, new Student(name, grades));
                refreshTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid grade input.");
            }
        }
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete Student", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            students.remove(row);
            refreshTable();
        }
    }

    private void showSummary() {
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students in list.");
            return;
        }

        int total = 0, count = 0;
        for (Student s : students) {
            for (int g : s.getGrades()) {
                total += g;
                count++;
            }
        }

        double avg = count > 0 ? (double) total / count : 0;
        JOptionPane.showMessageDialog(this, "Class Average: " + String.format("%.2f", avg));
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                s.getName(),
                joinGrades(s.getGrades()),
                String.format("%.2f", s.getAverage()),
                s.getHighest(),
                s.getLowest()
            });
        }
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Student s : students) writer.println(s.toFileString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file.");
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                students.add(Student.fromFileString(line));
            }
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error loading student file.");
        }
    }

    private int[] parseGrades(String gradesText) throws NumberFormatException {
        String[] parts = gradesText.split(",");
        int[] grades = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            grades[i] = Integer.parseInt(parts[i].trim());
        }
        return grades;
    }

    private String joinGrades(int[] grades) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < grades.length; i++) {
            sb.append(grades[i]);
            if (i < grades.length - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentGradeTrackerGUI().setVisible(true));
    }
}

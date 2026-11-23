package week_03;

import java.awt.*;
import javax.swing.*;

public class frm_KetNoiSQL extends JFrame {
    private JTextField txt_server, txt_port, txt_database, txt_user;
    private JPasswordField txt_password;
    private JButton btn_test, btn_history;
    private JLabel lbl_status;

    public frm_KetNoiSQL() {
        setTitle("Kết nối Microsoft SQL Server");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 320);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("KẾT NỐI MICROSOFT SQL SERVER");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        gbc.gridwidth = 1;
        txt_server = addRow("Server:", "127.0.0.1", 1, gbc);
        txt_port = addRow("Port:", "14333", 2, gbc);
        txt_database = addRow("Database:", "LAB3_MaHoaKhoaCongKhai", 3, gbc);
        txt_user = addRow("User:", "lab3_user", 4, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        add(new JLabel("Password:"), gbc);
        txt_password = new JPasswordField();
        gbc.gridx = 1; gbc.weightx = 1;
        add(txt_password, gbc);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_test = new JButton("Test Connection");
        btn_history = new JButton("Xem lịch sử");
        pnlBtn.add(btn_test);
        pnlBtn.add(btn_history);
        gbc.gridx = 1; gbc.gridy = 6;
        add(pnlBtn, gbc);

        lbl_status = new JLabel("Chưa kết nối.");
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        add(lbl_status, gbc);

        btn_test.addActionListener(e -> {
            try {
                int port = Integer.parseInt(txt_port.getText().trim());
                DbConfig.set(txt_server.getText().trim(), port, txt_database.getText().trim(),
                        txt_user.getText().trim(), txt_password.getPassword());
                lbl_status.setText("<html>" + DbConfig.test() + "</html>");
            } catch (Exception ex) {
                DbConfig.clear();
                lbl_status.setText("<html>Kết nối thất bại: " + ex.getMessage() + "</html>");
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không kết nối được SQL Server", JOptionPane.ERROR_MESSAGE);
            }
        });

        btn_history.addActionListener(e -> new frm_LichSu().setVisible(true));
    }

    private JTextField addRow(String label, String val, int row, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        add(new JLabel(label), gbc);
        JTextField tf = new JTextField(val);
        gbc.gridx = 1; gbc.weightx = 1;
        add(tf, gbc);
        return tf;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new frm_KetNoiSQL().setVisible(true));
    }
}
package week_03;

import java.awt.*;
import javax.swing.*;

public class frm_Menu extends JFrame {
    public frm_Menu() {
        setTitle("LAB 3 - Mã hóa khóa công khai");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(680, 240);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        JLabel lblTitle = new JLabel("LAB 3 - MÃ HÓA KHÓA CÔNG KHAI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel lblSub = new JLabel("Chọn một bài để mở form tương ứng (bài 3.1 chạy RSADemo.java bằng Run File):");
        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        pnlGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton btn_rsa = new JButton("3.2 RSA (Swing)");
        JButton btn_ecc = new JButton("3.3 ECC");
        JButton btn_rsaaes = new JButton("3.4 RSA + AES");
        JButton btn_signature = new JButton("3.5 Chữ ký ECDSA");
        JButton btn_sql = new JButton("Kết nối SQL Server");
        JButton btn_history = new JButton("Xem lịch sử");

        pnlGrid.add(btn_rsa);
        pnlGrid.add(btn_ecc);
        pnlGrid.add(btn_rsaaes);
        pnlGrid.add(btn_signature);
        pnlGrid.add(btn_sql);
        pnlGrid.add(btn_history);
        add(pnlGrid, BorderLayout.CENTER);

        btn_rsa.addActionListener(e -> new frm_RSA().setVisible(true));
        btn_ecc.addActionListener(e -> new frm_ECC().setVisible(true));
        btn_rsaaes.addActionListener(e -> new frm_RSA_AES().setVisible(true));
        btn_signature.addActionListener(e -> new frm_ECC_Signature().setVisible(true));
        btn_sql.addActionListener(e -> new frm_KetNoiSQL().setVisible(true));
        btn_history.addActionListener(e -> new frm_LichSu().setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new frm_Menu().setVisible(true));
    }
}
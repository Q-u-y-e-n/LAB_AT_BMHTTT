package week_03;

import java.awt.*;
import javax.swing.*;

public class frm_LichSu extends JFrame {
    private JTable tbl_history;
    private JButton btn_refresh;
    private JLabel lbl_status;

    public frm_LichSu() {
        setTitle("Lịch sử thao tác (SQL Server)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 380);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("  LỊCH SỬ THAO TÁC - BẢNG DBO.HISTORY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));
        add(lblTitle, BorderLayout.NORTH);

        tbl_history = new JTable(HistoryDao.emptyModel());
        add(new JScrollPane(tbl_history), BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btn_refresh = new JButton("Tải lại");
        lbl_status = new JLabel("Đang tải...");
        pnlBottom.add(btn_refresh);
        pnlBottom.add(lbl_status);
        add(pnlBottom, BorderLayout.SOUTH);

        btn_refresh.addActionListener(e -> loadData());
        loadData();
    }

    private void loadData() {
        try {
            tbl_history.setModel(HistoryDao.loadLatest(50));
            lbl_status.setText("Đã tải " + tbl_history.getRowCount() + " dòng mới nhất từ dbo.History.");
        } catch (java.sql.SQLException ex) {
            lbl_status.setText("Không đọc được lịch sử: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new frm_LichSu().setVisible(true));
    }
}
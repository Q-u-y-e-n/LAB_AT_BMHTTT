package week_03;

import java.awt.*;
import javax.swing.*;

public class frm_ECC_Signature extends JFrame {
    private final ECCSignature ecdsa = new ECCSignature();
    private JTextArea txt_public, txt_private, txt_message, txt_signature;
    private JButton btn_generate, btn_savekeys, btn_loadkeys, btn_sign, btn_verify;
    private JLabel lbl_result, lbl_status;

    public frm_ECC_Signature() {
        setTitle("ECDSA Signature");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(740, 490);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 12, 5, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblHeader = new JLabel("CHỮ KÝ SỐ ECDSA (SHA256withECDSA)");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblHeader, gbc);

        gbc.gridwidth = 1;
        txt_public = addArea("Public key:", 2, false, 1, gbc);
        txt_private = addArea("Private key:", 2, false, 2, gbc);

        JPanel pnlKeys = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_generate = new JButton("Generate Keys");
        btn_savekeys = new JButton("Save Keys");
        btn_loadkeys = new JButton("Load Keys");
        pnlKeys.add(btn_generate);
        pnlKeys.add(btn_savekeys);
        pnlKeys.add(btn_loadkeys);
        gbc.gridx = 1; gbc.gridy = 3;
        add(pnlKeys, gbc);

        txt_message = addArea("Message:", 2, true, 4, gbc);
        txt_signature = addArea("Signature:", 2, true, 5, gbc);

        JPanel pnlAct = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btn_sign = new JButton("Sign");
        btn_verify = new JButton("Verify");
        lbl_result = new JLabel("");
        lbl_result.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pnlAct.add(btn_sign);
        pnlAct.add(btn_verify);
        pnlAct.add(lbl_result);
        gbc.gridx = 1; gbc.gridy = 6;
        add(pnlAct, gbc);

        lbl_status = new JLabel("Bấm Generate Keys hoặc Load Keys.");
        gbc.gridx = 1; gbc.gridy = 7;
        add(lbl_status, gbc);

        btn_generate.addActionListener(e -> {
            try {
                ecdsa.generateKeys();
                showKeys();
                lbl_status.setText("Đã tạo cặp khóa mới. Bản mã chữ ký cũ sẽ không dùng được với khóa mới.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không tạo được khóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btn_savekeys.addActionListener(e -> {
            if (txt_public.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có khóa để lưu.");
                return;
            }
            String content = CryptoSupport.toKeyFile(txt_public.getText(), txt_private.getText());
            String path = FileTextIO.saveString(this, content, "ecdsa_keys.txt");
            if (path != null) {
                lbl_status.setText("Đã lưu khóa vào: " + path + " (giữ bí mật dòng PRIVATE).");
            }
        });

        btn_loadkeys.addActionListener(e -> {
            String content = FileTextIO.openString(this);
            if (content == null) return;
            try {
                String[] keys = CryptoSupport.fromKeyFile(content);
                ecdsa.loadKeys(keys[0], keys[1]);
                txt_public.setText(keys[0]);
                txt_private.setText(keys[1]);
                lbl_status.setText("Đã nạp khóa từ tệp.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Tệp khóa không hợp lệ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btn_sign.addActionListener(e -> {
            String message = txt_message.getText();
            try {
                txt_signature.setText(ecdsa.sign(message));
                lbl_result.setText("");
                lbl_status.setText("Đã ký bằng KHÓA RIÊNG. "
                        + HistoryDao.tryInsert("ECDSA", "SIGN", message.length(), true));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ký thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btn_verify.addActionListener(e -> {
            String message = txt_message.getText();
            try {
                boolean ok = ecdsa.verify(message, txt_signature.getText());
                lbl_result.setText(ok ? "VALID" : "INVALID");
                lbl_result.setForeground(ok ? new Color(0, 128, 0) : Color.RED);
                lbl_status.setText("Đã xác minh bằng KHÓA CÔNG KHAI. "
                        + HistoryDao.tryInsert("ECDSA", "VERIFY", message.length(), ok));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Xác minh thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showKeys() {
        txt_public.setText(ecdsa.getPublicKeyBase64());
        txt_private.setText(ecdsa.getPrivateKeyBase64());
        txt_public.setCaretPosition(0);
        txt_private.setCaretPosition(0);
    }

    private JTextArea addArea(String label, int rows, boolean editable, int row, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        add(new JLabel(label), gbc);
        JTextArea ta = new JTextArea(rows, 45);
        ta.setLineWrap(true);
        ta.setEditable(editable);
        gbc.gridx = 1; gbc.weightx = 1;
        add(new JScrollPane(ta), gbc);
        return ta;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new frm_ECC_Signature().setVisible(true));
    }
}
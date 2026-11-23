package week_03;

import java.awt.*;
import javax.swing.*;

public class frm_RSA extends JFrame {
    private final RSACipher rsa = new RSACipher();
    private JComboBox<String> cbo_bits;
    private JButton btn_generate, btn_encrypt, btn_decrypt;
    private JLabel lbl_keyinfo, lbl_status;
    private JTextArea txt_plaintext, txt_ciphertext;

    public frm_RSA() {
        setTitle("RSA Cipher");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(740, 430);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblHeader = new JLabel("MẬT MÃ RSA (OAEP - SHA-256)");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblHeader, gbc);

        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        add(new JLabel("Key size:"), gbc);
        JPanel pnlKey = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cbo_bits = new JComboBox<>(new String[]{"2048", "3072"});
        btn_generate = new JButton("Generate Keys");
        lbl_keyinfo = new JLabel("Chưa có khóa.");
        pnlKey.add(cbo_bits);
        pnlKey.add(btn_generate);
        pnlKey.add(lbl_keyinfo);
        gbc.gridx = 1; gbc.weightx = 1;
        add(pnlKey, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        add(new JLabel("Plaintext:"), gbc);
        txt_plaintext = new JTextArea(2, 45);
        txt_plaintext.setLineWrap(true);
        gbc.gridx = 1; gbc.weightx = 1;
        add(new JScrollPane(txt_plaintext), gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        add(new JLabel("Ciphertext:"), gbc);
        txt_ciphertext = new JTextArea(4, 45);
        txt_ciphertext.setLineWrap(true);
        gbc.gridx = 1; gbc.weightx = 1;
        add(new JScrollPane(txt_ciphertext), gbc);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_encrypt = new JButton("Encrypt");
        btn_decrypt = new JButton("Decrypt");
        pnlBtn.add(btn_encrypt);
        pnlBtn.add(btn_decrypt);
        gbc.gridx = 1; gbc.gridy = 4;
        add(pnlBtn, gbc);

        lbl_status = new JLabel("Bấm Generate Keys để tạo cặp khóa.");
        gbc.gridx = 1; gbc.gridy = 5;
        add(lbl_status, gbc);

        btn_generate.addActionListener(e -> {
            try {
                int bits = Integer.parseInt((String) cbo_bits.getSelectedItem());
                long start = System.currentTimeMillis();
                rsa.generateKeys(bits);
                long ms = System.currentTimeMillis() - start;
                lbl_keyinfo.setText("RSA-" + bits + ": tạo khóa " + ms + " ms, tối đa " + rsa.maxPlaintextBytes() + " byte");
                lbl_status.setText("Đã tạo cặp khóa. Khóa công khai dùng để mã hóa, khóa riêng để giải mã.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không tạo được khóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btn_encrypt.addActionListener(e -> {
            String plaintext = txt_plaintext.getText();
            if (plaintext.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hãy nhập Plaintext trước khi mã hóa.");
                return;
            }
            try {
                String ciphertext = rsa.encrypt(plaintext);
                txt_ciphertext.setText(ciphertext);
                txt_ciphertext.setCaretPosition(0);
                lbl_status.setText("Đã mã hóa RSA (" + ciphertext.length() + " ký tự Base64). "
                        + HistoryDao.tryInsert("RSA", "ENCRYPT", plaintext.length(), true));
            } catch (Exception ex) {
                HistoryDao.tryInsert("RSA", "ENCRYPT", plaintext.length(), false);
                JOptionPane.showMessageDialog(this, "Mã hóa thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btn_decrypt.addActionListener(e -> {
            String ciphertext = txt_ciphertext.getText();
            if (ciphertext.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext trước khi giải mã.");
                return;
            }
            try {
                String plaintext = rsa.decrypt(ciphertext);
                txt_plaintext.setText(plaintext);
                txt_plaintext.setCaretPosition(0);
                lbl_status.setText("Đã giải mã RSA. " + HistoryDao.tryInsert("RSA", "DECRYPT", ciphertext.length(), true));
            } catch (Exception ex) {
                HistoryDao.tryInsert("RSA", "DECRYPT", ciphertext.length(), false);
                lbl_status.setText("Giải mã RSA THẤT BẠI (sai khóa hoặc bản mã bị sửa).");
                JOptionPane.showMessageDialog(this, "Giải mã thất bại: sai khóa riêng hoặc bản mã bị sửa.\n" + ex,
                        "Giải mã thất bại", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new frm_RSA().setVisible(true));
    }
}
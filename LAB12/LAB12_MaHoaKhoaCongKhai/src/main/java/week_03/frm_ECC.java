package week_03;

import java.awt.*;
import javax.swing.*;

public class frm_ECC extends JFrame {
    private final ECCCipher ecc = new ECCCipher();
    private JTextArea txt_public, txt_private, txt_plaintext, txt_ciphertext;
    private JButton btn_generate, btn_savekeys, btn_loadkeys, btn_encrypt, btn_decrypt;
    private JLabel lbl_status;

    public frm_ECC() {
        setTitle("ECC Cipher");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(740, 490);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 12, 5, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblHeader = new JLabel("MẬT MÃ ĐƯỜNG CONG ELLIPTIC (ECC - secp256r1)");
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

        txt_plaintext = addArea("Plaintext:", 2, true, 4, gbc);
        txt_ciphertext = addArea("Ciphertext:", 3, true, 5, gbc);

        JPanel pnlAct = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_encrypt = new JButton("Encrypt");
        btn_decrypt = new JButton("Decrypt");
        pnlAct.add(btn_encrypt);
        pnlAct.add(btn_decrypt);
        gbc.gridx = 1; gbc.gridy = 6;
        add(pnlAct, gbc);

        lbl_status = new JLabel("Bấm Generate Keys hoặc Load Keys.");
        gbc.gridx = 1; gbc.gridy = 7;
        add(lbl_status, gbc);

        btn_generate.addActionListener(e -> {
            try {
                ecc.generateKeys();
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
            String path = FileTextIO.saveString(this, content, "ecc_keys.txt");
            if (path != null) {
                lbl_status.setText("Đã lưu khóa vào: " + path + " (giữ bí mật dòng PRIVATE).");
            }
        });

        btn_loadkeys.addActionListener(e -> {
            String content = FileTextIO.openString(this);
            if (content == null) return;
            try {
                String[] keys = CryptoSupport.fromKeyFile(content);
                ecc.loadKeys(keys[0], keys[1]);
                txt_public.setText(keys[0]);
                txt_private.setText(keys[1]);
                lbl_status.setText("Đã nạp khóa từ tệp.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Tệp khóa không hợp lệ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btn_encrypt.addActionListener(e -> {
            String plaintext = txt_plaintext.getText();
            if (plaintext.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hãy nhập Plaintext trước khi mã hóa.");
                return;
            }
            try {
                String ciphertext = ecc.encrypt(plaintext);
                txt_ciphertext.setText(ciphertext);
                txt_ciphertext.setCaretPosition(0);
                lbl_status.setText("Đã mã hóa ECC (" + ciphertext.length() + " ký tự Base64). "
                        + HistoryDao.tryInsert("ECC", "ENCRYPT", plaintext.length(), true));
            } catch (Exception ex) {
                HistoryDao.tryInsert("ECC", "ENCRYPT", plaintext.length(), false);
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
                String plaintext = ecc.decrypt(ciphertext);
                txt_plaintext.setText(plaintext);
                txt_plaintext.setCaretPosition(0);
                lbl_status.setText("Đã giải mã ECC. " + HistoryDao.tryInsert("ECC", "DECRYPT", ciphertext.length(), true));
            } catch (Exception ex) {
                HistoryDao.tryInsert("ECC", "DECRYPT", ciphertext.length(), false);
                lbl_status.setText("Giải mã ECC THẤT BẠI (sai khóa hoặc bản mã bị sửa).");
                JOptionPane.showMessageDialog(this, "Giải mã thất bại: sai khóa riêng hoặc bản mã bị sửa.\n" + ex,
                        "Giải mã thất bại", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showKeys() {
        txt_public.setText(ecc.getPublicKeyBase64());
        txt_private.setText(ecc.getPrivateKeyBase64());
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
        SwingUtilities.invokeLater(() -> new frm_ECC().setVisible(true));
    }
}
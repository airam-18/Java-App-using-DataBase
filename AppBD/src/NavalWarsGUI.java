import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class NavalWarsGUI extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;

    // DB settings
    private static final String URL = "jdbc:mysql://localhost:3306/navalwarsbd?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Eugenia.18";

    public NavalWarsGUI() {
        setTitle("Naval Wars");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel principal cu fundal
        JPanel backgroundPanel = new JPanel() {
            Image bg = new ImageIcon(getClass().getResource("/icons/ship.png")).getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // JTable central
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel stanga pentru toate butoanele
        JPanel leftPanel = new JPanel(new GridLayout(9, 1, 5, 5));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Creare butoane cu emoji color (Segoe UI Emoji)
        JButton btnClaseCR = createButton("🛳 Clase tip CR");
        JButton btnNaveIntreAni = createButton("⚓ Nave intre ani");
        JButton btnJoinBattle = createButton("🏴‍☠️ Join Nave/Clase/Consecinte");
        JButton btnPerechiNave = createButton("🧭 Perechi nave acelasi an");
        JButton btnPrimaBatalie = createButton("🕰️ Prima batalie");
        JButton btnClaseAlaska = createButton("🗂️ Clase ca Alaska");
        JButton btnAgregateClase = createButton("📊 Agregate clase");
        JButton btnMaxNave = createButton("🚢 Max nave/batalie");
        JButton btnExceptii = createButton("⚠️ Detecteaza Exceptii");

        // Adauga butoanele in panel stanga
        leftPanel.add(btnClaseCR);
        leftPanel.add(btnNaveIntreAni);
        leftPanel.add(btnJoinBattle);
        leftPanel.add(btnPerechiNave);
        leftPanel.add(btnPrimaBatalie);
        leftPanel.add(btnClaseAlaska);
        leftPanel.add(btnAgregateClase);
        leftPanel.add(btnMaxNave);
        leftPanel.add(btnExceptii);

        backgroundPanel.add(leftPanel, BorderLayout.WEST);

        // Action listeners pentru fiecare buton

        // 1. Clase tip CR – parametrizat
        btnClaseCR.addActionListener(e -> {
            String tipNava = JOptionPane.showInputDialog(this, "Introduceti tipul navei (ex: cr):");
            if (tipNava != null && !tipNava.isEmpty()) {
                tipNava = tipNava.toLowerCase(); // case-insensitive
                runQuery("SELECT clasa, tara, deplasament FROM Clase " +
                        "WHERE tip='" + tipNava.replace("'", "''") + "' AND deplasament>10000 ORDER BY deplasament");
            } else {
                JOptionPane.showMessageDialog(this, "Nu ati introdus niciun tip de nava!");
            }
        });

        // 2. Nave intre ani
        btnNaveIntreAni.addActionListener(e -> {
            try {
                String startYearStr = JOptionPane.showInputDialog(this, "Introduceti anul de inceput:");
                String endYearStr = JOptionPane.showInputDialog(this, "Introduceti anul de sfarsit:");
                int startYear = Integer.parseInt(startYearStr);
                int endYear = Integer.parseInt(endYearStr);

                runQuery("SELECT nume, anul_lansarii FROM Nave " +
                        "WHERE anul_lansarii BETWEEN " + startYear + " AND " + endYear +
                        " ORDER BY anul_lansarii ASC, clasa DESC");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ani invalidi!");
            }
        });

        // 3. Join Nave/Clase/Consecinte
        btnJoinBattle.addActionListener(e -> {
            String battleName = JOptionPane.showInputDialog(this, "Introduceti numele bataliei:");
            if (battleName != null && !battleName.isEmpty()) {
                runQuery("SELECT n.nume, c.deplasament, c.nr_arme, b.nume AS batalie, b.data, b.locatie, con.rezultat " +
                        "FROM Nave n JOIN Clase c ON n.clasa=c.clasa " +
                        "JOIN Consecinte con ON n.nume=con.nava " +
                        "JOIN Batalii b ON con.batalie=b.nume " +
                        "WHERE b.nume='" + battleName.replace("'", "''") + "'");
            }
        });

        // 4. Perechi nave acelasi an
        btnPerechiNave.addActionListener(e -> runQuery(
                "SELECT n1.nume AS nume1, n2.nume AS nume2, n1.anul_lansarii " +
                        "FROM Nave n1 JOIN Nave n2 ON n1.anul_lansarii=n2.anul_lansarii AND n1.clasa<>n2.clasa AND n1.nume<n2.nume " +
                        "ORDER BY n1.anul_lansarii, n1.nume, n2.nume"));

        // 5. Prima batalie
        btnPrimaBatalie.addActionListener(e -> runQuery(
                "SELECT * FROM Batalii b WHERE NOT EXISTS (SELECT 1 FROM Batalii b2 WHERE b2.data < b.data)"));

        // 6. Clase ca Alaska
        btnClaseAlaska.addActionListener(e -> runQuery(
                "SELECT * FROM Clase WHERE nr_arme IN (SELECT nr_arme FROM Clase WHERE clasa='Alaska')"));

        // 7. Agregate clase
        btnAgregateClase.addActionListener(e -> runQuery(
                "SELECT tara, tip, COUNT(*) AS nr_clase, MIN(diametru_tun) AS diametru_minim, " +
                        "AVG(diametru_tun) AS diametru_mediu, MAX(diametru_tun) AS diametru_maxim " +
                        "FROM Clase GROUP BY tara, tip ORDER BY tara, tip"));

        // 8. Max nave/batalie
        btnMaxNave.addActionListener(e -> runQuery(
                "SELECT batalie, COUNT(nava) AS nr_nave FROM Consecinte GROUP BY batalie"));

        // 9. Detecteaza Exceptii
        btnExceptii.addActionListener(e -> {
            callProcedure();
            runQuery("SELECT * FROM Exceptii");
        });
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btn.setForeground(Color.BLACK);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(Color.GRAY, 2));
        btn.setFocusPainted(false);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(220,220,220)); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private void runQuery(String query) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++)
                tableModel.addColumn(meta.getColumnName(i));

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++)
                    row[i] = rs.getObject(i + 1);
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la interogare: " + e.getMessage());
        }
    }

    private void callProcedure() {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             CallableStatement cs = con.prepareCall("{CALL DetecteazaExceptii()}")) {
            cs.execute();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Eroare la procedura: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NavalWarsGUI gui = new NavalWarsGUI();
            gui.setVisible(true);
        });
    }
}

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Smart Inventory For Cafe (SIC)
 * Complete executable file containing Database, UI, and Business Logic.
 */
public class SIC_System {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            Font modernFont = new Font("Segoe UI", Font.PLAIN, 14);
            Font boldFont = new Font("Segoe UI", Font.BOLD, 14);
            UIManager.put("Label.font", modernFont);
            UIManager.put("TextField.font", modernFont);
            UIManager.put("PasswordField.font", modernFont);
            UIManager.put("ComboBox.font", modernFont);
            UIManager.put("Table.font", modernFont);
            UIManager.put("TableHeader.font", boldFont);
            UIManager.put("OptionPane.messageFont", modernFont);
            UIManager.put("TabbedPane.font", boldFont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DBHelper.initializeDatabase();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

/* =========================================================================
 * UI UTILITY LAYER (For Consistent Attractive Styling)
 * ========================================================================= */
class NonEditableTableModel extends DefaultTableModel {
    public NonEditableTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; 
    }
}

class StyledButton extends JButton {
    private Color bg;

    public StyledButton(String text, Color bg, Color fg) {
        super(text);
        this.bg = bg;
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setForeground(fg);
        setFocusPainted(false);
        setBorderPainted(true);
        setContentAreaFilled(false); 
        setOpaque(false);            
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 120), 1), 
            new EmptyBorder(8, 15, 8, 15)
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (getModel().isPressed()) {
            g2.setColor(bg.darker().darker()); 
        } else if (getModel().isRollover()) {
            g2.setColor(bg.darker()); 
        } else {
            g2.setColor(bg);
        }
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}

class UIUtils {
    public static JButton createStyledButton(String text, Color bg, Color fg) {
        return new StyledButton(text, bg, fg);
    }

    public static JPanel createTopHeaderContainer(String title, String subtitle, JButton actionButton) {
        JPanel mainHeader = new JPanel(new BorderLayout());
        mainHeader.setBackground(new Color(41, 128, 185)); 
        mainHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel textPanel = new JPanel(new GridLayout(subtitle != null ? 2 : 1, 1, 0, 5));
        textPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        textPanel.add(lblTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblSubtitle = new JLabel(subtitle);
            lblSubtitle.setForeground(Color.WHITE);
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            textPanel.add(lblSubtitle);
        }
        mainHeader.add(textPanel, BorderLayout.WEST);

        if (actionButton != null) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            actionPanel.setOpaque(false);
            
            actionButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            actionButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 120), 1),
                new EmptyBorder(6, 12, 6, 12)
            ));
            
            actionPanel.setBorder(new EmptyBorder(subtitle != null ? 8 : 0, 0, 0, 0));
            actionPanel.add(actionButton);
            mainHeader.add(actionPanel, BorderLayout.EAST);
        }

        return mainHeader;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setBackground(Color.WHITE); 
        table.setForeground(Color.BLACK); 
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);
        
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 35));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(new Color(44, 62, 80));
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerRenderer.setHorizontalAlignment(JLabel.LEFT);
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        header.setDefaultRenderer(headerRenderer);
    }

    public static String formatSmartDateOnly(String dbDateStr) {
        if (dbDateStr == null || dbDateStr.isEmpty()) return "";
        try {
            SimpleDateFormat dbSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss 'IST'");
            dbSdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            Date dbDate = dbSdf.parse(dbDateStr);

            SimpleDateFormat checkFormat = new SimpleDateFormat("dd/MM/yyyy");
            checkFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            String dbDay = checkFormat.format(dbDate);

            Date now = new Date();
            String todayDay = checkFormat.format(now);

            long yesterdayMillis = now.getTime() - (24 * 60 * 60 * 1000);
            String yesterdayDay = checkFormat.format(new Date(yesterdayMillis));

            SimpleDateFormat dayOfWeekFmt = new SimpleDateFormat("EEEE");
            dayOfWeekFmt.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            String dayOfWeek = dayOfWeekFmt.format(dbDate);

            if (dbDay.equals(todayDay)) {
                return "Today (" + dbDay + ") " + dayOfWeek;
            } else if (dbDay.equals(yesterdayDay)) {
                return "Yesterday (" + dbDay + ") " + dayOfWeek;
            } else {
                return dbDay + " " + dayOfWeek;
            }
        } catch (Exception e) {
            return dbDateStr.split(" ")[0]; 
        }
    }

    public static String formatTimeOnly(String dbDateStr) {
        if (dbDateStr == null || dbDateStr.isEmpty()) return "";
        try {
            SimpleDateFormat dbSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss 'IST'");
            dbSdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            Date dbDate = dbSdf.parse(dbDateStr);

            SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm:ss a");
            timeFmt.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            return timeFmt.format(dbDate);
        } catch (Exception e) {
            String[] parts = dbDateStr.split(" ");
            return parts.length > 1 ? parts[1] : dbDateStr; 
        }
    }

    public static JPanel buildGroupedBills(ResultSet rs, boolean isMember, Consumer<String> onBillClick) throws SQLException {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(UIManager.getColor("Panel.background"));
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        String lastSmartDate = null;
        DefaultTableModel currentModel = null;
        JTable currentTable = null;
        JPanel currentTableWrapper = null;

        String[] cols = isMember ? 
            new String[]{"Bill No", "Destination", "Discount Type", "Discount (₹)", "Total (₹)", "Time"} : 
            new String[]{"Bill No", "Billed By Staff", "Destination", "Member ID", "Discount Type", "Discount (₹)", "Total (₹)", "Time"};

        while (rs.next()) {
            String rawDateStr = rs.getString("date");
            String currentSmartDate = formatSmartDateOnly(rawDateStr);
            String timeOnly = formatTimeOnly(rawDateStr);

            if (!currentSmartDate.equals(lastSmartDate)) {
                lastSmartDate = currentSmartDate;

                if (container.getComponentCount() > 0) {
                    container.add(Box.createRigidArea(new Dimension(0, 25))); 
                }

                JLabel lblDate = new JLabel(currentSmartDate);
                lblDate.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblDate.setForeground(new Color(41, 128, 185));
                lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
                container.add(lblDate);
                container.add(Box.createRigidArea(new Dimension(0, 5)));

                currentModel = new NonEditableTableModel(cols, 0);
                currentTable = new JTable(currentModel);
                styleTable(currentTable);
                currentTable.setToolTipText("Double-click a row to view exact bill order details");

                // Attach double-click listener to open exact bill items
                final JTable activeTable = currentTable;
                currentTable.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2 && onBillClick != null) {
                            int r = activeTable.getSelectedRow();
                            if (r != -1) {
                                int colIdx = activeTable.getColumnModel().getColumnIndex("Bill No");
                                String bNo = (String) activeTable.getValueAt(r, colIdx);
                                onBillClick.accept(bNo);
                            }
                        }
                    }
                });
                
                currentTableWrapper = new JPanel(new BorderLayout());
                currentTableWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
                currentTableWrapper.add(currentTable.getTableHeader(), BorderLayout.NORTH);
                currentTableWrapper.add(currentTable, BorderLayout.CENTER);
                currentTableWrapper.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                
                container.add(currentTableWrapper);
            }

            String dest = rs.getInt("table_no") == 0 ? "Takeaway" : "Table " + rs.getInt("table_no");
            String staffId = rs.getString("staff_id");
            if (staffId == null) staffId = rs.getString("user_id"); 

            String memberId = "NA";
            if (rs.getString("user_id") != null && rs.getString("user_id").startsWith("MEM-")) {
                memberId = rs.getString("user_id");
            }

            String discountType = rs.getString("discount_type");
            if (discountType == null || discountType.isEmpty()) discountType = "None";

            if (isMember) {
                currentModel.addRow(new Object[]{
                    rs.getString("bill_no"), dest, discountType,
                    "₹" + String.format("%.2f", rs.getDouble("discount")),
                    "₹" + String.format("%.2f", rs.getDouble("total_amount")),
                    timeOnly
                });
            } else {
                currentModel.addRow(new Object[]{
                    rs.getString("bill_no"), staffId, dest, memberId, discountType,
                    "₹" + String.format("%.2f", rs.getDouble("discount")),
                    "₹" + String.format("%.2f", rs.getDouble("total_amount")),
                    timeOnly
                });
            }

            int headerHeight = currentTable.getTableHeader().getPreferredSize().height;
            int tableHeight = currentTable.getRowHeight() * currentModel.getRowCount();
            Dimension preferredSize = new Dimension(Integer.MAX_VALUE, headerHeight + tableHeight);
            currentTableWrapper.setMaximumSize(preferredSize);
            currentTableWrapper.setPreferredSize(new Dimension(1000, headerHeight + tableHeight));
        }

        if (container.getComponentCount() == 0) {
            JLabel lblEmpty = new JLabel("No order history found.");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            lblEmpty.setForeground(Color.GRAY);
            container.add(lblEmpty);
        }

        return container;
    }

    public static void showBillDetails(Component parent, String billNo) {
        DefaultTableModel model = new NonEditableTableModel(new String[]{"Item Name", "Qty", "Total Price (₹)"}, 0);
        JTable table = new JTable(model);
        styleTable(table);
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Bill_Items WHERE bill_no=?")) {
            ps.setString(1, billNo);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    "₹" + String.format("%.2f", rs.getDouble("price"))
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "No detailed items found for this bill (Older generated bill).", "Order Details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(450, 250));
        JOptionPane.showMessageDialog(parent, scroll, "Exact Order Details: " + billNo, JOptionPane.PLAIN_MESSAGE);
    }
}

/* =========================================================================
 * DATABASE UTILITY LAYER
 * ========================================================================= */
class DBHelper {
    private static final String DB_URL = "jdbc:sqlite:cafe_inventory.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Users (id TEXT PRIMARY KEY, username TEXT UNIQUE, password TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Menu (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price REAL, type TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Bills (bill_no TEXT PRIMARY KEY, user_id TEXT, table_no INTEGER, total_amount REAL, discount REAL, date TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Feedback (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, bill_no TEXT, feedback_text TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Tables_Config (id INTEGER PRIMARY KEY, total_tables INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Coupons (code TEXT PRIMARY KEY, discount_percent REAL, max_uses INTEGER, current_uses INTEGER DEFAULT 0)");
            
            // NEW TABLE: Logs exact items inside a bill
            stmt.execute("CREATE TABLE IF NOT EXISTS Bill_Items (id INTEGER PRIMARY KEY AUTOINCREMENT, bill_no TEXT, item_name TEXT, quantity INTEGER, price REAL)");

            try { stmt.execute("ALTER TABLE Bills ADD COLUMN staff_id TEXT"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE Bills ADD COLUMN discount_type TEXT DEFAULT 'None'"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE Menu ADD COLUMN original_price REAL DEFAULT 0"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE Menu ADD COLUMN initial_stock INTEGER DEFAULT 0"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE Menu ADD COLUMN current_stock INTEGER DEFAULT 0"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE Coupons ADD COLUMN type TEXT DEFAULT 'GLOBAL'"); } catch(Exception ignored) {}

            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM Users");
            if (rs.next() && rs.getInt(1) == 0) {
                String adminId = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String staffId = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                String memberId = "MEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                PreparedStatement ps = conn.prepareStatement("INSERT INTO Users (id, username, password, role) VALUES (?, ?, ?, ?)");
                ps.setString(1, adminId); ps.setString(2, "admin"); ps.setString(3, "admin123"); ps.setString(4, "ADMIN"); ps.executeUpdate();
                ps.setString(1, staffId); ps.setString(2, "staff"); ps.setString(3, "staff123"); ps.setString(4, "STAFF"); ps.executeUpdate();
                ps.setString(1, memberId); ps.setString(2, "member"); ps.setString(3, "member123"); ps.setString(4, "MEMBER"); ps.executeUpdate();

                stmt.execute("INSERT INTO Tables_Config (id, total_tables) VALUES (1, 10)");
                
                stmt.execute("INSERT INTO Menu (name, price, original_price, type, initial_stock, current_stock) VALUES ('Espresso', 150.00, 150.00, 'REGULAR', 50, 50)");
                stmt.execute("INSERT INTO Menu (name, price, original_price, type, initial_stock, current_stock) VALUES ('Cappuccino', 220.00, 220.00, 'REGULAR', 50, 50)");
                stmt.execute("INSERT INTO Menu (name, price, original_price, type, initial_stock, current_stock) VALUES ('Truffle Cake', 175.00, 350.00, 'TRIAL_DISH', 20, 20)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

/* =========================================================================
 * SESSION MANAGEMENT
 * ========================================================================= */
class UserSession {
    public static String loggedInId;
    public static String username;
    public static String role;

    public static void clear() {
        loggedInId = null; username = null; role = null;
    }
}

/* =========================================================================
 * GUI COMPONENTS (VIEWS)
 * ========================================================================= */

// 1. LOGIN FRAME
class LoginFrame extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;

    public LoginFrame() {
        setTitle("Smart Inventory For Cafe - Secure Login");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIUtils.createTopHeaderContainer("Welcome to SIC System", "Please login or proceed as guest", null), BorderLayout.NORTH);

        JPanel wrapperPanel = new JPanel(new GridBagLayout());

        JPanel centerPanel = new JPanel(new GridLayout(3, 2, 10, 25));
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        centerPanel.setPreferredSize(new Dimension(400, 220)); 

        centerPanel.add(new JLabel("Username:"));
        txtUser = new JTextField();
        centerPanel.add(txtUser);

        centerPanel.add(new JLabel("Password:"));
        txtPass = new JPasswordField();
        centerPanel.add(txtPass);

        JButton btnLogin = UIUtils.createStyledButton("Login", new Color(46, 204, 113), Color.WHITE);
        JButton btnGuest = UIUtils.createStyledButton("Guest Feedback", new Color(149, 165, 166), Color.WHITE);

        centerPanel.add(btnLogin);
        centerPanel.add(btnGuest);

        wrapperPanel.add(centerPanel);
        add(wrapperPanel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> authenticate());
        btnGuest.addActionListener(e -> {
            new GuestFeedbackFrame().setVisible(true);
            this.dispose();
        });
    }

    private void authenticate() {
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, role FROM Users WHERE username=? AND password=?")) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UserSession.loggedInId = rs.getString("id");
                UserSession.username = user;
                UserSession.role = rs.getString("role");

                this.dispose();
                switch (UserSession.role) {
                    case "ADMIN": new AdminFrame().setVisible(true); break;
                    case "STAFF": new StaffFrame().setVisible(true); break;
                    case "MEMBER": new MemberFrame().setVisible(true); break;
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

// 2. ADMIN FRAME
class AdminFrame extends JFrame {
    private DefaultTableModel stdMenuModel, trialMenuModel, feedbackModel, couponModel, staffModel;
    private JTable staffTable, stdMenuTable, trialMenuTable, couponTable, feedbackTable;
    private JLabel lblTotalRev, lblTotalBills, lblTotalStaff, lblLowStock;
    private JScrollPane billsScrollPane;

    public AdminFrame() {
        setTitle("Admin Dashboard");
        setSize(1100, 800); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JButton btnLogout = UIUtils.createStyledButton("Logout", new Color(231, 76, 60), Color.WHITE);
        btnLogout.addActionListener(e -> {
            UserSession.clear();
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        add(UIUtils.createTopHeaderContainer("Admin Dashboard", "Logged in as: " + UserSession.username, btnLogout), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        // --- 1. Split Menu Management & Inventory Tab ---
        JPanel menuContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        menuContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel stdPanel = new JPanel(new BorderLayout(5, 5));
        stdPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Standard Menu", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 16)));
        
        JPanel stdActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddStd = UIUtils.createStyledButton("Add", new Color(46, 204, 113), Color.WHITE);
        JButton btnEditStd = UIUtils.createStyledButton("Edit", new Color(52, 152, 219), Color.WHITE);
        JButton btnDelStd = UIUtils.createStyledButton("Delete", new Color(231, 76, 60), Color.WHITE);
        JButton btnStockStd = UIUtils.createStyledButton("Configure Stock", new Color(155, 89, 182), Color.WHITE);
        stdActionPanel.add(btnAddStd); stdActionPanel.add(btnEditStd); stdActionPanel.add(btnDelStd); stdActionPanel.add(btnStockStd);
        stdPanel.add(stdActionPanel, BorderLayout.NORTH);

        stdMenuModel = new NonEditableTableModel(new String[]{"ID", "Item Name", "Price (₹)", "Start Stock", "Left Over"}, 0);
        stdMenuTable = new JTable(stdMenuModel);
        UIUtils.styleTable(stdMenuTable);
        stdPanel.add(new JScrollPane(stdMenuTable), BorderLayout.CENTER);

        JPanel trialPanel = new JPanel(new BorderLayout(5, 5));
        trialPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(230, 126, 34)), "Trial Dish Menu (50% Off)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 16)));
        
        JPanel trialActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddTrial = UIUtils.createStyledButton("Add", new Color(46, 204, 113), Color.WHITE);
        JButton btnEditTrial = UIUtils.createStyledButton("Edit", new Color(52, 152, 219), Color.WHITE);
        JButton btnDelTrial = UIUtils.createStyledButton("Delete", new Color(231, 76, 60), Color.WHITE);
        JButton btnStockTrial = UIUtils.createStyledButton("Configure Stock", new Color(155, 89, 182), Color.WHITE);
        trialActionPanel.add(btnAddTrial); trialActionPanel.add(btnEditTrial); trialActionPanel.add(btnDelTrial); trialActionPanel.add(btnStockTrial);
        trialPanel.add(trialActionPanel, BorderLayout.NORTH);

        trialMenuModel = new NonEditableTableModel(new String[]{"ID", "Dish Name", "Original Price (₹)", "Trial Price (₹)", "Start Stock", "Left Over"}, 0);
        trialMenuTable = new JTable(trialMenuModel);
        UIUtils.styleTable(trialMenuTable);
        trialPanel.add(new JScrollPane(trialMenuTable), BorderLayout.CENTER);

        menuContainer.add(stdPanel);
        menuContainer.add(trialPanel);
        loadMenuData();

        btnAddStd.addActionListener(e -> addMenuItem(false));
        btnEditStd.addActionListener(e -> editMenuItem(stdMenuTable, stdMenuModel, false));
        btnDelStd.addActionListener(e -> deleteMenuItem(stdMenuTable, stdMenuModel));
        btnStockStd.addActionListener(e -> configureStock(stdMenuTable, stdMenuModel, 4));

        btnAddTrial.addActionListener(e -> addMenuItem(true));
        btnEditTrial.addActionListener(e -> editMenuItem(trialMenuTable, trialMenuModel, true));
        btnDelTrial.addActionListener(e -> deleteMenuItem(trialMenuTable, trialMenuModel));
        btnStockTrial.addActionListener(e -> configureStock(trialMenuTable, trialMenuModel, 5));

        // --- 2. View All Bills Tab ---
        JPanel billsPanel = new JPanel(new BorderLayout(10, 10));
        billsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        billsScrollPane = new JScrollPane();
        billsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        billsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        billsPanel.add(billsScrollPane, BorderLayout.CENTER);
        
        JPanel billActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefreshBills = UIUtils.createStyledButton("Refresh Bills", new Color(52, 152, 219), Color.WHITE);
        btnRefreshBills.addActionListener(e -> loadBillsData());
        billActionPanel.add(btnRefreshBills);
        billsPanel.add(billActionPanel, BorderLayout.NORTH);
        loadBillsData();

        // --- 3. Manage Staff/Users Tab ---
        JPanel staffPanel = new JPanel(new BorderLayout(10, 10));
        staffPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel staffActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        JButton btnAddStaff = UIUtils.createStyledButton("Add New User", new Color(46, 204, 113), Color.WHITE);
        JButton btnDelStaff = UIUtils.createStyledButton("Delete Selected User", new Color(231, 76, 60), Color.WHITE);
        
        btnAddStaff.addActionListener(e -> addStaff());
        btnDelStaff.addActionListener(e -> deleteStaff());
        
        staffActionPanel.add(btnAddStaff);
        staffActionPanel.add(btnDelStaff);
        staffPanel.add(staffActionPanel, BorderLayout.NORTH);
        
        staffModel = new NonEditableTableModel(new String[]{"User ID", "Username", "Role"}, 0);
        staffTable = new JTable(staffModel);
        UIUtils.styleTable(staffTable);
        loadStaffData();
        staffPanel.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        // --- 4. Feedback Tab (WITH DOUBLE CLICK SUPPORT) ---
        JPanel feedbackPanel = new JPanel(new BorderLayout());
        feedbackPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        feedbackModel = new NonEditableTableModel(new String[]{"Guest Name", "Bill No.", "Feedback Comments"}, 0);
        feedbackTable = new JTable(feedbackModel);
        UIUtils.styleTable(feedbackTable);
        
        feedbackTable.setToolTipText("Double-click to read full feedback");
        feedbackTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = feedbackTable.getSelectedRow();
                    if (r != -1) {
                        String name = (String) feedbackModel.getValueAt(r, 0);
                        String bill = (String) feedbackModel.getValueAt(r, 1);
                        String text = (String) feedbackModel.getValueAt(r, 2);
                        
                        JTextArea ta = new JTextArea(text);
                        ta.setLineWrap(true);
                        ta.setWrapStyleWord(true);
                        ta.setEditable(false);
                        ta.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                        ta.setMargin(new Insets(10, 10, 10, 10));
                        
                        JScrollPane sp = new JScrollPane(ta);
                        sp.setPreferredSize(new Dimension(400, 250));
                        JOptionPane.showMessageDialog(AdminFrame.this, sp, "Feedback: " + name + " (" + bill + ")", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        
        loadFeedbackData();
        feedbackPanel.add(new JScrollPane(feedbackTable), BorderLayout.CENTER);

        // --- 5. Coupon Management Tab (ADVANCED CRUD) ---
        JPanel couponPanel = new JPanel(new BorderLayout(10, 10));
        couponPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel couponActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddCoupon = UIUtils.createStyledButton("Create", new Color(46, 204, 113), Color.WHITE);
        JButton btnEditCoupon = UIUtils.createStyledButton("Edit", new Color(52, 152, 219), Color.WHITE);
        JButton btnDelCoupon = UIUtils.createStyledButton("Delete", new Color(231, 76, 60), Color.WHITE);
        
        btnAddCoupon.addActionListener(e -> addCoupon());
        btnEditCoupon.addActionListener(e -> editCoupon());
        btnDelCoupon.addActionListener(e -> deleteCoupon());
        
        couponActionPanel.add(btnAddCoupon); 
        couponActionPanel.add(btnEditCoupon); 
        couponActionPanel.add(btnDelCoupon);
        couponPanel.add(couponActionPanel, BorderLayout.NORTH);
        
        couponModel = new NonEditableTableModel(new String[]{"Coupon Code", "Coupon Type", "Discount (%)", "Max Uses", "Current Uses"}, 0);
        couponTable = new JTable(couponModel);
        UIUtils.styleTable(couponTable);
        loadCouponData();
        couponPanel.add(new JScrollPane(couponTable), BorderLayout.CENTER);

        // --- 6. Settings Tab ---
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        settingsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JButton btnSetTables = UIUtils.createStyledButton("Configure Total Tables", new Color(155, 89, 182), Color.WHITE);
        btnSetTables.addActionListener(e -> configureTables());
        settingsPanel.add(btnSetTables);

        // --- 7. Cafe Summary Tab ---
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        summaryPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        lblTotalRev = new JLabel("Total Revenue: ₹0.00");
        lblTotalRev.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalRev.setForeground(new Color(39, 174, 96));
        
        lblTotalBills = new JLabel("Total Bills Generated: 0");
        lblTotalBills.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        lblTotalStaff = new JLabel("Active Users/Staff: 0");
        lblTotalStaff.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        lblLowStock = new JLabel("Items Out of Stock: 0");
        lblLowStock.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLowStock.setForeground(new Color(231, 76, 60)); 
        
        summaryPanel.add(lblTotalRev);
        summaryPanel.add(lblTotalBills);
        summaryPanel.add(lblTotalStaff);
        summaryPanel.add(lblLowStock);
        loadSummaryData();

        // Add all tabs
        tabbedPane.addTab("Menu & Inventory", menuContainer);
        tabbedPane.addTab("View All Bills", billsPanel);
        tabbedPane.addTab("Manage Users", staffPanel);
        tabbedPane.addTab("Manage Coupons", couponPanel);
        tabbedPane.addTab("Customer Feedback", feedbackPanel);
        tabbedPane.addTab("Settings", settingsPanel);
        tabbedPane.addTab("Cafe Summary", summaryPanel);

        tabbedPane.addChangeListener(e -> {
            loadBillsData();
            loadStaffData();
            loadMenuData();
            loadSummaryData();
        });

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- Tab Actions & Loaders ---

    private void loadSummaryData() {
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT SUM(total_amount), COUNT(*) FROM Bills");
            if (rs1.next()) {
                lblTotalRev.setText("Total Revenue: ₹" + String.format("%.2f", rs1.getDouble(1)));
                lblTotalBills.setText("Total Bills Generated: " + rs1.getInt(2));
            }
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) FROM Users WHERE role != 'MEMBER'");
            if (rs2.next()) lblTotalStaff.setText("Active Users/Staff: " + rs2.getInt(1));
            
            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) FROM Menu WHERE current_stock <= 0");
            if (rs3.next()) lblLowStock.setText("Items Out of Stock: " + rs3.getInt(1));
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void configureStock(JTable table, DefaultTableModel model, int stockColIdx) {
        int row = table.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        int currentStock = (int) model.getValueAt(row, stockColIdx);
        
        String qtyStr = JOptionPane.showInputDialog(this, "Configure exact current stock for '" + name + "':", currentStock);
        if(qtyStr != null && !qtyStr.trim().isEmpty()) {
            try {
                int newQty = Integer.parseInt(qtyStr.trim());
                if (newQty < 0) throw new Exception("Cannot be negative");
                try(Connection conn = DBHelper.getConnection();
                    PreparedStatement ps = conn.prepareStatement("UPDATE Menu SET current_stock = ? WHERE id = ?")) {
                    ps.setInt(1, newQty);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Stock configured successfully.");
                    loadMenuData();
                    loadSummaryData();
                }
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid quantity entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addMenuItem(boolean isTrial) {
        JTextField txtName = new JTextField();
        JTextField txtPrice = new JTextField();
        JTextField txtStock = new JTextField("0");

        Object[] msg = {
            "Item Name:", txtName, 
            isTrial ? "Original Full Price (₹) [50% Trial Auto-Applied]:" : "Standard Price (₹):", txtPrice,
            "Initial Stock Quantity:", txtStock
        };
        
        int res = JOptionPane.showConfirmDialog(this, msg, "Add New " + (isTrial ? "Trial" : "Standard") + " Item", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String name = txtName.getText().trim();
                String priceStr = txtPrice.getText().trim();
                String stockStr = txtStock.getText().trim();
                String type = isTrial ? "TRIAL_DISH" : "REGULAR";
                
                if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Fields cannot be empty!", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double standardPrice = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                
                double sellingPrice = isTrial ? (standardPrice / 2.0) : standardPrice;

                try (Connection conn = DBHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO Menu (name, price, original_price, type, initial_stock, current_stock) VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, name);
                    ps.setDouble(2, sellingPrice);
                    ps.setDouble(3, standardPrice);
                    ps.setString(4, type);
                    ps.setInt(5, stock);
                    ps.setInt(6, stock);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Item added successfully!\nSelling Price set to: ₹" + sellingPrice);
                    loadMenuData(); 
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding item. Ensure price and stock are valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editMenuItem(JTable table, DefaultTableModel model, boolean isTrial) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        
        String priceVal = "";
        if (isTrial) {
            priceVal = ((String) model.getValueAt(row, 2)).replace("₹", "").replace(",", ""); 
        } else {
            priceVal = ((String) model.getValueAt(row, 2)).replace("₹", "").replace(",", ""); 
        }
        
        JTextField txtName = new JTextField(name);
        JTextField txtPrice = new JTextField(priceVal);

        Object[] msg = {
            "Item Name:", txtName, 
            isTrial ? "Original Full Price (₹) [Updates Trial Automatically]:" : "Standard Price (₹):", txtPrice
        };
        
        int res = JOptionPane.showConfirmDialog(this, msg, "Edit Menu Item", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String newName = txtName.getText().trim();
                double newPrice = Double.parseDouble(txtPrice.getText().trim());
                
                double sellPrice = isTrial ? (newPrice / 2.0) : newPrice;
                double origPrice = isTrial ? newPrice : newPrice; 

                try (Connection conn = DBHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE Menu SET name=?, price=?, original_price=? WHERE id=?")) {
                    ps.setString(1, newName);
                    ps.setDouble(2, sellPrice);
                    ps.setDouble(3, origPrice);
                    ps.setInt(4, id);
                    ps.executeUpdate();
                    loadMenuData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Ensure price is a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteMenuItem(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to completely delete '" + name + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM Menu WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadMenuData();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void loadMenuData() {
        stdMenuModel.setRowCount(0);
        trialMenuModel.setRowCount(0);
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Menu")) {
            while (rs.next()) {
                String type = rs.getString("type");
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int initStock = rs.getInt("initial_stock");
                int currStock = rs.getInt("current_stock");
                
                if ("TRIAL_DISH".equals(type)) {
                    trialMenuModel.addRow(new Object[]{
                        id, name, 
                        "₹" + String.format("%.2f", rs.getDouble("original_price")), 
                        "₹" + String.format("%.2f", rs.getDouble("price")), 
                        initStock, currStock
                    });
                } else {
                    stdMenuModel.addRow(new Object[]{
                        id, name, 
                        "₹" + String.format("%.2f", rs.getDouble("price")), 
                        initStock, currStock
                    });
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void addStaff() {
        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"STAFF", "ADMIN", "MEMBER"});
        
        Object[] msg = {"New Username:", txtUser, "Password:", txtPass, "Assign Role:", cbRole};
        
        int res = JOptionPane.showConfirmDialog(this, msg, "Add New User", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String role = (String) cbRole.getSelectedItem();
            
            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String prefix = role.equals("MEMBER") ? "MEM-" : "EMP-";
            String newId = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO Users (id, username, password, role) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, newId);
                ps.setString(2, user);
                ps.setString(3, pass);
                ps.setString(4, role);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "User added successfully!\nNew ID: " + newId + "\nRole: " + role);
                loadStaffData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding user. Username might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table first to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String id = (String) staffModel.getValueAt(row, 0);
        String role = (String) staffModel.getValueAt(row, 2);
        
        if (id.equals(UserSession.loggedInId)) {
            JOptionPane.showMessageDialog(this, "You cannot delete yourself!", "Action Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("ADMIN".equals(role)) {
            JOptionPane.showMessageDialog(this, "Cannot delete other Admin accounts.", "Action Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete User ID: " + id + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM Users WHERE id=?")) {
                ps.setString(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "User successfully deleted.");
                loadStaffData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadStaffData() {
        staffModel.setRowCount(0);
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, role FROM Users WHERE role IN ('STAFF', 'ADMIN', 'MEMBER')")) {
            while (rs.next()) {
                staffModel.addRow(new Object[]{rs.getString("id"), rs.getString("username"), rs.getString("role")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadBillsData() {
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Bills ORDER BY rowid DESC")) { 
            
            JPanel groupedPanel = UIUtils.buildGroupedBills(rs, false, billNo -> {
                UIUtils.showBillDetails(AdminFrame.this, billNo);
            });
            billsScrollPane.setViewportView(groupedPanel);
            
            SwingUtilities.invokeLater(() -> billsScrollPane.getVerticalScrollBar().setValue(0));
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void addCoupon() {
        JTextField txtCode = new JTextField();
        JComboBox<String> cbType = new JComboBox<>(new String[]{"GLOBAL", "MEMBER_ONLY"});
        JTextField txtPct = new JTextField();
        JTextField txtUses = new JTextField();
        Object[] msg = {
            "Coupon Code (e.g., FESTIVE50):", txtCode, 
            "Coupon Access Type:", cbType,
            "Discount Percentage (0-100):", txtPct, 
            "Maximum Uses Allowed:", txtUses
        };
        
        int res = JOptionPane.showConfirmDialog(this, msg, "Create New Coupon", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String code = txtCode.getText().trim().toUpperCase();
                String type = (String) cbType.getSelectedItem();
                double pct = Double.parseDouble(txtPct.getText().trim());
                int uses = Integer.parseInt(txtUses.getText().trim());
                
                if (code.isEmpty() || pct < 0 || pct > 100 || uses < 1) throw new Exception("Invalid parameters");
                
                try (Connection conn = DBHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement("INSERT INTO Coupons (code, discount_percent, max_uses, type) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, code); ps.setDouble(2, pct); ps.setInt(3, uses); ps.setString(4, type);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Coupon created successfully.");
                    loadCouponData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating coupon. Check values and ensure code is unique.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editCoupon() {
        int row = couponTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a coupon to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String oldCode = (String) couponModel.getValueAt(row, 0);
        String oldType = (String) couponModel.getValueAt(row, 1);
        String pctStr = ((String) couponModel.getValueAt(row, 2)).replace("%", "");
        int uses = (int) couponModel.getValueAt(row, 3);

        JTextField txtCode = new JTextField(oldCode);
        JComboBox<String> cbType = new JComboBox<>(new String[]{"GLOBAL", "MEMBER_ONLY"});
        cbType.setSelectedItem(oldType);
        JTextField txtPct = new JTextField(pctStr);
        JTextField txtUses = new JTextField(String.valueOf(uses));
        
        Object[] msg = {
            "Coupon Code:", txtCode, 
            "Coupon Access Type:", cbType,
            "Discount Percentage (0-100):", txtPct, 
            "Maximum Uses Allowed:", txtUses
        };

        int res = JOptionPane.showConfirmDialog(this, msg, "Edit Coupon", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String code = txtCode.getText().trim().toUpperCase();
                String type = (String) cbType.getSelectedItem();
                double pct = Double.parseDouble(txtPct.getText().trim());
                int newUses = Integer.parseInt(txtUses.getText().trim());
                
                if (code.isEmpty() || pct < 0 || pct > 100 || newUses < 1) throw new Exception("Invalid");

                try (Connection conn = DBHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE Coupons SET code=?, type=?, discount_percent=?, max_uses=? WHERE code=?")) {
                    ps.setString(1, code); ps.setString(2, type); ps.setDouble(3, pct); ps.setInt(4, newUses); ps.setString(5, oldCode);
                    ps.executeUpdate();
                    loadCouponData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating. Check limits and code uniqueness.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCoupon() {
        int row = couponTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a coupon to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String code = (String) couponModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Permanently delete coupon '" + code + "'?", "Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM Coupons WHERE code=?")) {
                ps.setString(1, code);
                ps.executeUpdate();
                loadCouponData();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void loadCouponData() {
        couponModel.setRowCount(0);
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Coupons")) {
            while (rs.next()) {
                couponModel.addRow(new Object[]{
                    rs.getString("code"), 
                    rs.getString("type"), 
                    rs.getDouble("discount_percent") + "%", 
                    rs.getInt("max_uses"), 
                    rs.getInt("current_uses")
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadFeedbackData() {
        feedbackModel.setRowCount(0);
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Feedback")) {
            while (rs.next()) {
                feedbackModel.addRow(new Object[]{rs.getString("name"), rs.getString("bill_no"), rs.getString("feedback_text")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void configureTables() {
        String numStr = JOptionPane.showInputDialog(this, "Enter Total Number of Tables:");
        if (numStr != null) {
            try {
                int tables = Integer.parseInt(numStr);
                try (Connection conn = DBHelper.getConnection();
                     PreparedStatement ps = conn.prepareStatement("UPDATE Tables_Config SET total_tables=? WHERE id=1")) {
                    ps.setInt(1, tables);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Total tables configured successfully.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

// 3. STAFF FRAME
class StaffFrame extends JFrame {
    private JComboBox<String> cbMenu, cbTables;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JScrollPane staffHistoryScrollPane;
    private JLabel lblTotal;
    private JCheckBox chkStaffDiscount;
    private JTextField txtCoupon, txtMemberId;
    private JButton btnApplyCoupon, btnVerifyMember;
    
    private double currentTotal = 0.0;
    private String appliedCoupon = null;
    private double couponPct = 0.0;
    private String verifiedMemberId = null;
    
    public StaffFrame() {
        setTitle("Smart Inventory For Cafe");
        setSize(1000, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JButton btnLogout = UIUtils.createStyledButton("Logout", new Color(231, 76, 60), Color.WHITE);
        btnLogout.addActionListener(e -> {
            UserSession.clear();
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        add(UIUtils.createTopHeaderContainer("Smart Inventory For Cafe", "👤 ID: " + UserSession.loggedInId + " | Name: " + UserSession.username, btnLogout), BorderLayout.NORTH);

        // --- Tab 1: Point of Sale ---
        JPanel posPanel = new JPanel(new BorderLayout(10, 10));

        // Left Panel: Actions
        JPanel leftPanel = new JPanel(new GridLayout(10, 1, 5, 10)); 
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 20, 15, 10),
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2), "Billing Controls", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 16))
        ));
        
        cbTables = new JComboBox<>();
        loadTables(cbTables);
        leftPanel.add(new JLabel("Select Destination:"));
        leftPanel.add(cbTables);

        cbMenu = new JComboBox<>();
        loadMenuDropdown();
        leftPanel.add(new JLabel("Select Menu Item:"));
        leftPanel.add(cbMenu);

        // Updated Add/Remove Buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPanel.setOpaque(false);
        JButton btnRemove = UIUtils.createStyledButton("- Remove", new Color(231, 76, 60), Color.WHITE);
        JButton btnAdd = UIUtils.createStyledButton("+ Add", new Color(52, 152, 219), Color.WHITE);
        actionPanel.add(btnRemove);
        actionPanel.add(btnAdd);
        leftPanel.add(actionPanel);

        chkStaffDiscount = new JCheckBox("Apply Staff Discount (Max 3/day)");
        chkStaffDiscount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkStaffDiscount.setBackground(UIManager.getColor("Panel.background"));
        leftPanel.add(chkStaffDiscount);

        // Coupon Panel
        JPanel couponInputPanel = new JPanel(new BorderLayout(5, 0));
        couponInputPanel.setOpaque(false);
        txtCoupon = new JTextField();
        btnApplyCoupon = UIUtils.createStyledButton("Apply", new Color(155, 89, 182), Color.WHITE);
        couponInputPanel.add(txtCoupon, BorderLayout.CENTER);
        couponInputPanel.add(btnApplyCoupon, BorderLayout.EAST);
        
        leftPanel.add(new JLabel("Special Coupon Code:"));
        leftPanel.add(couponInputPanel);

        // Member ID Panel
        JPanel memberInputPanel = new JPanel(new BorderLayout(5, 0));
        memberInputPanel.setOpaque(false);
        txtMemberId = new JTextField();
        btnVerifyMember = UIUtils.createStyledButton("Verify", new Color(230, 126, 34), Color.WHITE);
        memberInputPanel.add(txtMemberId, BorderLayout.CENTER);
        memberInputPanel.add(btnVerifyMember, BorderLayout.EAST);
        
        leftPanel.add(new JLabel("Customer Member ID:"));
        leftPanel.add(memberInputPanel);

        // Center Panel: Cart
        cartModel = new NonEditableTableModel(new String[]{"Item Name", "Qty", "Total Price"}, 0);
        cartTable = new JTable(cartModel);
        UIUtils.styleTable(cartTable);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(15, 5, 15, 20), BorderFactory.createLineBorder(new Color(189, 195, 199))));
        
        // Bottom Panel: Totals & Checkout
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 20));
        lblTotal = new JLabel("Total: ₹0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTotal.setForeground(new Color(39, 174, 96));

        JButton btnCheckout = UIUtils.createStyledButton("Generate Final Bill", new Color(46, 204, 113), Color.WHITE);
        btnCheckout.setPreferredSize(new Dimension(220, 50));
        
        bottomPanel.add(lblTotal);
        bottomPanel.add(btnCheckout);

        posPanel.add(leftPanel, BorderLayout.WEST);
        posPanel.add(scrollPane, BorderLayout.CENTER);
        posPanel.add(bottomPanel, BorderLayout.SOUTH);

        // --- Tab 2: Staff Order History ---
        JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
        historyPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        staffHistoryScrollPane = new JScrollPane();
        staffHistoryScrollPane.setBorder(BorderFactory.createEmptyBorder());
        staffHistoryScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        historyPanel.add(staffHistoryScrollPane, BorderLayout.CENTER);

        JPanel historyActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefreshHistory = UIUtils.createStyledButton("Refresh History", new Color(52, 152, 219), Color.WHITE);
        btnRefreshHistory.addActionListener(e -> loadHistoryData());
        historyActionPanel.add(btnRefreshHistory);
        historyPanel.add(historyActionPanel, BorderLayout.NORTH);
        loadHistoryData();

        // --- Tabbing Structure ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("Point of Sale", posPanel);
        tabbedPane.addTab("My Order History", historyPanel);

        tabbedPane.addChangeListener(e -> loadHistoryData());

        add(tabbedPane, BorderLayout.CENTER);

        // Listeners
        btnAdd.addActionListener(e -> modifyCartItem(1));
        btnRemove.addActionListener(e -> modifyCartItem(-1));
        
        chkStaffDiscount.addActionListener(e -> {
            if (chkStaffDiscount.isSelected()) {
                if (!canUseDiscount(UserSession.loggedInId)) {
                    JOptionPane.showMessageDialog(this, "Daily discount limit (3 times) reached for your ID!", "Limit Exceeded", JOptionPane.WARNING_MESSAGE);
                    chkStaffDiscount.setSelected(false);
                } else {
                    appliedCoupon = null; couponPct = 0.0; txtCoupon.setText("");
                    verifiedMemberId = null; txtMemberId.setText("");
                }
            }
            updateTotal();
        });

        btnApplyCoupon.addActionListener(e -> applyCoupon());
        btnVerifyMember.addActionListener(e -> verifyMember());
        btnCheckout.addActionListener(e -> processCheckout());
    }

    private void modifyCartItem(int delta) {
        String selection = (String) cbMenu.getSelectedItem();
        if (selection == null || selection.contains("ALL STOCKS OVER")) return;

        if (delta > 0 && selection.contains("[OUT OF STOCK]")) {
            JOptionPane.showMessageDialog(this, "This item is completely out of stock!", "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = selection.split(" - ₹")[0];
        
        String pricePart = selection.split(" - ₹")[1];
        if (pricePart.contains(" (Trial!")) {
            pricePart = pricePart.substring(0, pricePart.indexOf(" (Trial!"));
        } else if (pricePart.contains(" (Orig:")) {
            pricePart = pricePart.substring(0, pricePart.indexOf(" (Orig:"));
        }
        double unitPrice = Double.parseDouble(pricePart.split(" ")[0]); 

        int rowIndex = -1;
        int currentQty = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i, 0).equals(name)) {
                rowIndex = i;
                currentQty = (int) cartModel.getValueAt(i, 1);
                break;
            }
        }

        if (delta > 0) {
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT current_stock FROM Menu WHERE name=?")) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int stock = rs.getInt("current_stock");
                    if (currentQty + delta > stock) {
                        JOptionPane.showMessageDialog(this, "Cannot add! Only " + stock + " left in stock.", "Out of Stock", JOptionPane.WARNING_MESSAGE);
                        return; 
                    }
                }
            } catch (SQLException ex) { ex.printStackTrace(); }

            if (rowIndex != -1) {
                cartModel.setValueAt(currentQty + 1, rowIndex, 1);
                cartModel.setValueAt(String.format("₹%.2f", (currentQty + 1) * unitPrice), rowIndex, 2);
            } else {
                cartModel.addRow(new Object[]{name, 1, String.format("₹%.2f", unitPrice)});
            }
            currentTotal += unitPrice;
        } else if (delta < 0 && rowIndex != -1) {
            if (currentQty > 1) {
                cartModel.setValueAt(currentQty - 1, rowIndex, 1);
                cartModel.setValueAt(String.format("₹%.2f", (currentQty - 1) * unitPrice), rowIndex, 2);
            } else {
                cartModel.removeRow(rowIndex);
            }
            currentTotal -= unitPrice;
        }
        
        updateTotal();
    }

    private void applyCoupon() {
        String code = txtCoupon.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            appliedCoupon = null; couponPct = 0.0; updateTotal(); return;
        }
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT type, discount_percent, max_uses, current_uses FROM Coupons WHERE code=?")) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String cType = rs.getString("type");
                
                // Block if it's a member-only coupon and no member ID is verified
                if ("MEMBER_ONLY".equals(cType) && verifiedMemberId == null) {
                    JOptionPane.showMessageDialog(this, "This coupon is MEMBER ONLY. Please verify a valid Customer Member ID first.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                    appliedCoupon = null; couponPct = 0.0; updateTotal();
                    return;
                }

                int max = rs.getInt("max_uses");
                int cur = rs.getInt("current_uses");
                if (cur >= max) {
                    JOptionPane.showMessageDialog(this, "Coupon expired or maximum uses reached.");
                    appliedCoupon = null; couponPct = 0.0;
                } else {
                    appliedCoupon = code;
                    couponPct = rs.getDouble("discount_percent");
                    JOptionPane.showMessageDialog(this, "Coupon Applied: " + couponPct + "% off! (Overrides standard member discount if applicable)");
                    chkStaffDiscount.setSelected(false); 
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid coupon code.", "Error", JOptionPane.ERROR_MESSAGE);
                appliedCoupon = null; couponPct = 0.0;
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        updateTotal();
    }

    private void verifyMember() {
        String memId = txtMemberId.getText().trim();
        if (memId.isEmpty()) {
            verifiedMemberId = null; updateTotal(); return;
        }
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM Users WHERE id=? AND role='MEMBER'")) {
            ps.setString(1, memId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                verifiedMemberId = memId;
                JOptionPane.showMessageDialog(this, "Member Verified! 10% Member Discount will be applied.");
                chkStaffDiscount.setSelected(false);
                // We do NOT clear appliedCoupon here. If a member-only coupon was denied earlier, 
                // they can now successfully press 'Apply' again to use it.
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Member ID.", "Error", JOptionPane.ERROR_MESSAGE);
                verifiedMemberId = null;
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        updateTotal();
    }

    private boolean canUseDiscount(String userId) {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM Bills WHERE user_id=? AND discount > 0 AND date LIKE ?")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            String todayStr = sdf.format(new Date());

            ps.setString(1, userId);
            ps.setString(2, todayStr + "%"); 
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) < 3;
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return false;
    }

    private void loadTables(JComboBox<String> cb) {
        cb.addItem("Takeaway"); 
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT total_tables FROM Tables_Config WHERE id=1")) {
            if (rs.next()) {
                int count = rs.getInt(1);
                for (int i = 1; i <= count; i++) cb.addItem("Table " + i);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadMenuDropdown() {
        cbMenu.removeAllItems();
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, price, original_price, type, current_stock FROM Menu")) {
            while (rs.next()) {
                String type = rs.getString("type");
                int stock = rs.getInt("current_stock");
                String display = rs.getString("name") + " - ₹" + rs.getDouble("price");
                
                if (type.equals("TRIAL_DISH")) {
                    display += " (Trial! Orig: ₹" + rs.getDouble("original_price") + ")";
                }

                if (stock > 0) {
                    display += " [" + stock + " left]";
                } else {
                    display += " [OUT OF STOCK]";
                }

                cbMenu.addItem(display);
            }
            
            if (cbMenu.getItemCount() == 0) {
                cbMenu.addItem("ALL STOCKS OVER / NO MENU");
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void updateTotal() {
        double displayTotal = currentTotal;
        if (chkStaffDiscount.isSelected()) {
            displayTotal = currentTotal * 0.80; 
        } else if (appliedCoupon != null) {
            displayTotal = currentTotal * (1.0 - (couponPct / 100.0)); // Overrides member discount
        } else if (verifiedMemberId != null) {
            displayTotal = currentTotal * 0.90; // Default 10% Member discount
        }
        lblTotal.setText(String.format("Total: ₹%.2f", displayTotal));
    }

    private void loadHistoryData() {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Bills WHERE staff_id=? OR (staff_id IS NULL AND user_id=?) ORDER BY rowid DESC")) {
            ps.setString(1, UserSession.loggedInId);
            ps.setString(2, UserSession.loggedInId);
            ResultSet rs = ps.executeQuery();
            
            JPanel groupedPanel = UIUtils.buildGroupedBills(rs, false, billNo -> {
                UIUtils.showBillDetails(StaffFrame.this, billNo);
            });
            staffHistoryScrollPane.setViewportView(groupedPanel);
            
            SwingUtilities.invokeLater(() -> staffHistoryScrollPane.getVerticalScrollBar().setValue(0));
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void processCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        String tableSelection = (String) cbTables.getSelectedItem();
        int tableNo = tableSelection.equals("Takeaway") ? 0 : Integer.parseInt(tableSelection.replace("Table ", ""));

        String discountTypeToLog = "None";
        double discountPercent = 0.0;
        
        if (chkStaffDiscount.isSelected()) {
            discountPercent = 20.0;
            discountTypeToLog = "Staff Discount";
        } else if (appliedCoupon != null) {
            discountPercent = couponPct;
            discountTypeToLog = "Coupon (" + appliedCoupon + ")";
        } else if (verifiedMemberId != null) {
            discountPercent = 10.0;
            discountTypeToLog = "Member Discount";
        }
        
        double finalAmount = currentTotal * (1.0 - (discountPercent / 100.0));
        double discountAmount = currentTotal - finalAmount;
        
        String billNo = "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss 'IST'");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String timestampIST = sdf.format(new Date());

        String billUserId = verifiedMemberId != null ? verifiedMemberId : UserSession.loggedInId;

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO Bills (bill_no, user_id, table_no, total_amount, discount, date, staff_id, discount_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, billNo);
            ps.setString(2, billUserId);
            ps.setInt(3, tableNo);
            ps.setDouble(4, finalAmount);
            ps.setDouble(5, discountAmount);
            ps.setString(6, timestampIST);
            ps.setString(7, UserSession.loggedInId); 
            ps.setString(8, discountTypeToLog);      
            ps.executeUpdate();

            // SAVE EXACT ITEMS TO Bill_Items TABLE FOR DETAILED VIEW
            try (PreparedStatement psItems = conn.prepareStatement("INSERT INTO Bill_Items (bill_no, item_name, quantity, price) VALUES (?, ?, ?, ?)")) {
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    String itemName = (String) cartModel.getValueAt(i, 0);
                    int qty = (int) cartModel.getValueAt(i, 1);
                    String priceStr = (String) cartModel.getValueAt(i, 2);
                    double itemTotalPrice = Double.parseDouble(priceStr.replace("₹", "").replace(",", ""));
                    
                    psItems.setString(1, billNo);
                    psItems.setString(2, itemName);
                    psItems.setInt(3, qty);
                    psItems.setDouble(4, itemTotalPrice);
                    psItems.executeUpdate();
                }
            }

            if (appliedCoupon != null && !chkStaffDiscount.isSelected()) {
                try (PreparedStatement ps2 = conn.prepareStatement("UPDATE Coupons SET current_uses = current_uses + 1 WHERE code=?")) {
                    ps2.setString(1, appliedCoupon);
                    ps2.executeUpdate();
                }
            }

            try (PreparedStatement psStock = conn.prepareStatement("UPDATE Menu SET current_stock = current_stock - ? WHERE name = ?")) {
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int qty = (int) cartModel.getValueAt(i, 1);
                    String itemName = (String) cartModel.getValueAt(i, 0);
                    psStock.setInt(1, qty);
                    psStock.setString(2, itemName);
                    psStock.executeUpdate();
                }
            }

            String type = tableNo == 0 ? "Takeaway" : "Table " + tableNo;
            JOptionPane.showMessageDialog(this, "Bill Generated Successfully!\nType: " + type + "\nBill No: " + billNo + "\nAmount: ₹" + String.format("%.2f", finalAmount) + "\nTime: " + timestampIST);
            
            cartModel.setRowCount(0); 
            currentTotal = 0.0;
            chkStaffDiscount.setSelected(false);
            appliedCoupon = null;
            couponPct = 0.0;
            txtCoupon.setText("");
            verifiedMemberId = null;
            txtMemberId.setText("");
            updateTotal();
            
            loadMenuDropdown(); 

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing checkout.");
        }
    }
}

// 4. CUSTOMER / MEMBER FRAME
class MemberFrame extends JFrame {
    private DefaultTableModel standardMenuModel, trialMenuModel;
    private JScrollPane historyScrollPane;

    public MemberFrame() {
        setTitle("Cafe Member Portal");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JButton btnLogout = UIUtils.createStyledButton("Logout", new Color(231, 76, 60), Color.WHITE);
        btnLogout.addActionListener(e -> {
            UserSession.clear();
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        add(UIUtils.createTopHeaderContainer("Welcome Member!", "ID: " + UserSession.loggedInId + " | Enjoy your automatic 10% discount.", btnLogout), BorderLayout.NORTH);

        // --- Tab 1: Menu Data Panel ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        centerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel stdPanel = new JPanel(new BorderLayout());
        stdPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Standard Member Menu (10% Off)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 16)));
        standardMenuModel = new NonEditableTableModel(new String[]{"Item Name", "Standard Price", "Your Price (10% Off)"}, 0);
        JTable stdTable = new JTable(standardMenuModel);
        UIUtils.styleTable(stdTable);
        stdPanel.add(new JScrollPane(stdTable), BorderLayout.CENTER);

        JPanel trialPanel = new JPanel(new BorderLayout());
        trialPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(230, 126, 34)), "Exclusive Trial Dish Menu (50% Off)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 16)));
        trialMenuModel = new NonEditableTableModel(new String[]{"Dish Name", "Original Price", "Trial Price"}, 0);
        JTable trialTable = new JTable(trialMenuModel);
        UIUtils.styleTable(trialTable);
        trialPanel.add(new JScrollPane(trialTable), BorderLayout.CENTER);

        centerPanel.add(stdPanel);
        centerPanel.add(trialPanel);
        
        loadMemberMenu();

        // --- Tab 2: Order History Panel ---
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 20, 15, 20),
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "My Past Orders", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 16))
        ));

        historyScrollPane = new JScrollPane();
        historyScrollPane.setBorder(BorderFactory.createEmptyBorder());
        historyScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
        loadHistoryData();

        // --- Tabbing Structure ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        tabbedPane.addTab("Exclusive Menus", centerPanel);
        tabbedPane.addTab("My Order History", historyPanel);
        
        tabbedPane.addChangeListener(e -> loadHistoryData());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadMemberMenu() {
        standardMenuModel.setRowCount(0);
        trialMenuModel.setRowCount(0);
        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Menu")) {
            while (rs.next()) {
                String type = rs.getString("type");
                String name = rs.getString("name");
                double stdPrice = rs.getDouble("price");

                if ("TRIAL_DISH".equals(type)) {
                    double origPrice = rs.getDouble("original_price");
                    trialMenuModel.addRow(new Object[]{
                        name, 
                        "₹" + String.format("%.2f", origPrice), 
                        "₹" + String.format("%.2f", stdPrice)
                    });
                } else {
                    double memPrice = stdPrice * 0.90;
                    standardMenuModel.addRow(new Object[]{
                        name, 
                        "₹" + String.format("%.2f", stdPrice), 
                        "₹" + String.format("%.2f", memPrice)
                    });
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadHistoryData() {
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM Bills WHERE user_id=? ORDER BY rowid DESC")) {
            ps.setString(1, UserSession.loggedInId);
            ResultSet rs = ps.executeQuery();
            
            JPanel groupedPanel = UIUtils.buildGroupedBills(rs, true, billNo -> {
                UIUtils.showBillDetails(MemberFrame.this, billNo);
            });
            historyScrollPane.setViewportView(groupedPanel);
            
            SwingUtilities.invokeLater(() -> historyScrollPane.getVerticalScrollBar().setValue(0));
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}

// 5. GUEST FEEDBACK FRAME
class GuestFeedbackFrame extends JFrame {
    public GuestFeedbackFrame() {
        setTitle("Guest Feedback Portal");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UIUtils.createTopHeaderContainer("Guest Feedback", "We value your experience. Let us know how we did!", null), BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0); 

        JLabel lblName = new JLabel("Your Name:");
        JTextField txtName = new JTextField();
        txtName.setPreferredSize(new Dimension(0, 35));

        JLabel lblBill = new JLabel("Bill Number:");
        JTextField txtBillNo = new JTextField();
        txtBillNo.setPreferredSize(new Dimension(0, 35));

        JLabel lblFeedback = new JLabel("Feedback / Comments:");
        JTextArea txtFeedback = new JTextArea(6, 20);
        txtFeedback.setLineWrap(true);
        txtFeedback.setWrapStyleWord(true);
        JScrollPane scrollFeedback = new JScrollPane(txtFeedback);
        scrollFeedback.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(lblName, gbc);
        gbc.gridy = 1; formPanel.add(txtName, gbc);
        
        gbc.gridy = 2; formPanel.add(lblBill, gbc);
        gbc.gridy = 3; formPanel.add(txtBillNo, gbc);
        
        gbc.gridy = 4; formPanel.add(lblFeedback, gbc);
        gbc.gridy = 5; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollFeedback, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        JButton btnSubmit = UIUtils.createStyledButton("Submit Feedback", new Color(46, 204, 113), Color.WHITE);
        JButton btnBack = UIUtils.createStyledButton("Back to Login", new Color(149, 165, 166), Color.WHITE);

        btnSubmit.addActionListener(e -> {
            if (txtName.getText().trim().isEmpty() || txtFeedback.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Feedback are required!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO Feedback (name, bill_no, feedback_text) VALUES (?, ?, ?)")) {
                ps.setString(1, txtName.getText());
                ps.setString(2, txtBillNo.getText());
                ps.setString(3, txtFeedback.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Thank you! Your feedback has been recorded.");
                new LoginFrame().setVisible(true);
                this.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        bottomPanel.add(btnSubmit);
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package gui;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.SQL;
import raven.toast.Notifications;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import model.InvoiceItem;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author AmiChan
 */
public class MainFrame extends javax.swing.JFrame {

    private MainDashboard mf;

    private static final Logger logger = Logger.getLogger(UserLogin.class.getName());
    public String itemID;

    private void setLogger() {
        try {
            FileHandler fileHandler = new FileHandler("Log Reports/Main Programme Log Report.log", true);
            fileHandler.setFormatter(new SimpleFormatter());

            logger.addHandler(fileHandler);

            logger.info("EM Logger initialized");

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Failed to initialize the log file");

        }
    }

    private static HashMap<String, InvoiceItem> invoiceItemMap = new HashMap<>();
    private static HashMap<String, String> paymentMethodMap = new HashMap<>();
    private static HashMap<String, String> orderMethodMap = new HashMap<>();

    private String employeeName = "danusi";//Amnt for test @overridden by Dashboard
    private String employeeID = "7"; //1 for test @overridden by Dashboard

    private static HashMap<String, String> productIDMap = new HashMap<>();
    private String productID;

    public void setProductID(String pid) {
        this.productID = pid;
    }

    private String finalCusPoints;
    private String customerID;

    public void setCustomerID(String cid) {
        this.customerID = cid;
        jCheckBox1.setEnabled(true);
    }

    private boolean tableSelection = false;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        calculateOrderID();
        setLogger();
        loadTables();
        loadComboBox();
        loadMethodComboBox();
        setTime();
        TableListener();
        PointsListener();
    }

    private void calculateOrderID() {
        long id = System.currentTimeMillis();
        jTextField4.setText(String.valueOf(id));
    }

    public void setParent(MainDashboard mf) {
        this.mf = mf;
        this.employeeName = mf.setNameLabel().getText();
        this.employeeID = mf.employeeID;
        setEmployee();
    }

    private void setEmployee() {
        if (mf != null) {
            jTextField5.setText(employeeName);
        }
    }

    private void loadTables() {
        try {
            JLabel[] tableLabels = {jLabel8, jLabel9, jLabel10, jLabel11, jLabel12, jLabel13};
            ResultSet result = SQL.executeSearch("SELECT * FROM `table` INNER JOIN `table_status` ON `table`.`table_status_id`=`table_status`.`id`");

            int labelIndex = 0;

            while (result.next() && labelIndex < tableLabels.length) {
                String status = result.getString("table_status.name");

                JLabel currentLabel = tableLabels[labelIndex];

                if (status.equalsIgnoreCase("Available")) {
                    currentLabel.setBackground(Color.GREEN);
                } else if (status.equalsIgnoreCase("Occupied")) {
                    currentLabel.setBackground(Color.RED);
                }
                labelIndex++;

            }
        } catch (Exception e) {
        }
    }

    private void TableListener() {
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        dtm.addTableModelListener((TableModelEvent e) -> {
            double totalPrice = 0;
            for (int i = 0; i < jTable1.getRowCount(); i++) {

                double price = Double.parseDouble(jTable1.getValueAt(i, 1).toString());
                double quantity = Double.parseDouble(jTable1.getValueAt(i, 2).toString());
                double totalPricePerRow = price * quantity;
                totalPrice += totalPricePerRow;
            }

            jTextField6.setText(String.valueOf(Math.floor(totalPrice)));
        });
    }

    private void PointsListener() {
        jTextField13.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkCheckbox();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkCheckbox();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
//                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            private void checkCheckbox() {
                String integerCheck = "\"[+-]?\\d+(\\.\\d+)?\"";
                String points = jTextField13.getText();
                if (points.isEmpty()) {
                    jTextField17.setText("0");
                } else {
                    if (!points.matches(integerCheck)) {
                        double pointsPutting = Double.parseDouble(points);
                        double discount = pointsPutting * 10;
                        jTextField17.setText(String.valueOf(discount));
                    }
                }
            }
        });

    }

    private void loadComboBox() {
        try {
            ResultSet result = SQL.executeSearch("SELECT * FROM `payment_method`");
            Vector<String> vector = new Vector<>();
            vector.add("Select");
            while (result.next()) {
                vector.add(result.getString("name"));
                paymentMethodMap.put(result.getString("name"), result.getString("id"));
            }
            DefaultComboBoxModel dcm = new DefaultComboBoxModel(vector);
            jComboBox1.setModel(dcm);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("payment method combo box load error");
        }
    }

    private void loadMethodComboBox() {
        try {
            ResultSet result = SQL.executeSearch("SELECT * FROM `order_method`");
            Vector<String> vector = new Vector<>();
            vector.add("Select");
            while (result.next()) {
                vector.add(result.getString("name"));
                orderMethodMap.put(result.getString("name"), result.getString("id"));
            }
            DefaultComboBoxModel dcm = new DefaultComboBoxModel(vector);
            jComboBox2.setModel(dcm);
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("order method combo box load error");
        }
    }

    private void setTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get current date and time
                LocalDateTime currentDateTime = LocalDateTime.now();
                String dateTimeString = currentDateTime.format(formatter);

                // Set date and time to JTextField
                jTextField16.setText(dateTimeString);
            }
        });
        timer.start();
    }

//    Resets
    private void resetOne() {
        calculateOrderID();
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField1.setText("");
        jTextField7.setText("");
        jTextField8.setText("");
    }

    private void resetTwo() {
        jTextField9.setText("");
    }

    private void resetThree() {
        jTextField10.setText("");
        jTextField11.setText("");
        jTextField12.setText("");
        jCheckBox1.setSelected(false);
    }

    private void resetFinal() {
        customerID = "";
        jComboBox1.setSelectedIndex(0);
        jComboBox2.setSelectedIndex(0);
        jTextField14.setText("");
        jTextField6.setText("");
        jTextField15.setText("");
        jCheckBox1.setEnabled(false);

        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        dtm.setRowCount(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        kGradientPanel1 = new keeptoo.KGradientPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jTextField9 = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jTextField10 = new javax.swing.JTextField();
        jTextField11 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel17 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jTextField13 = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        jTextField14 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jTextField15 = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jTextField17 = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jTextField16 = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        kGradientPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 3, true));
        kGradientPanel1.setkEndColor(new java.awt.Color(204, 255, 204));
        kGradientPanel1.setkGradientFocus(250);
        kGradientPanel1.setkStartColor(new java.awt.Color(255, 204, 255));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Quantity", "Unit Price"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("UD Digi Kyokasho NK-B", 0, 36)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(102, 0, 0));
        jLabel1.setText("Culinary Resort Bakery");

        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        jButton1.setBackground(new java.awt.Color(204, 255, 255));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(0, 0, 0));
        jButton1.setText("Select Customer");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(204, 255, 255));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(0, 0, 0));
        jButton2.setText("Select Table");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(204, 255, 255));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(0, 0, 0));
        jButton3.setText("Select Food Items");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Quantity");

        jButton4.setText("Add To Order");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jTextField1.setBackground(new java.awt.Color(255, 255, 255));
        jTextField1.setForeground(new java.awt.Color(0, 0, 0));

        jTextField2.setBackground(new java.awt.Color(255, 255, 255));
        jTextField2.setForeground(new java.awt.Color(0, 0, 0));
        jTextField2.setEnabled(false);

        jTextField3.setBackground(new java.awt.Color(255, 255, 255));
        jTextField3.setForeground(new java.awt.Color(0, 0, 0));
        jTextField3.setEnabled(false);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Name");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Price");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Order ID");

        jTextField4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField4.setEnabled(false);

        jSeparator1.setBackground(new java.awt.Color(204, 255, 204));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Inventory Availability");

        jTextField7.setBackground(new java.awt.Color(255, 255, 255));
        jTextField7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField7.setForeground(new java.awt.Color(0, 0, 0));
        jTextField7.setEnabled(false);

        jTextField8.setBackground(new java.awt.Color(255, 255, 255));
        jTextField8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField8.setForeground(new java.awt.Color(0, 0, 0));
        jTextField8.setEnabled(false);

        jButton5.setBackground(new java.awt.Color(153, 153, 153));
        jButton5.setForeground(new java.awt.Color(0, 0, 0));
        jButton5.setText("R");
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jTextField9.setBackground(new java.awt.Color(255, 255, 255));
        jTextField9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField9.setForeground(new java.awt.Color(0, 0, 0));
        jTextField9.setEnabled(false);

        jSeparator2.setBackground(new java.awt.Color(204, 255, 204));

        jButton6.setBackground(new java.awt.Color(153, 153, 153));
        jButton6.setForeground(new java.awt.Color(0, 0, 0));
        jButton6.setText("R");
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setBackground(new java.awt.Color(153, 153, 153));
        jButton7.setForeground(new java.awt.Color(0, 0, 0));
        jButton7.setText("R");
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jTextField10.setBackground(new java.awt.Color(255, 255, 255));
        jTextField10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField10.setForeground(new java.awt.Color(0, 0, 0));
        jTextField10.setEnabled(false);

        jTextField11.setBackground(new java.awt.Color(255, 255, 255));
        jTextField11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField11.setForeground(new java.awt.Color(0, 0, 0));
        jTextField11.setEnabled(false);

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setText("Customer Name");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setText("Mobile");

        jTextField12.setBackground(new java.awt.Color(255, 255, 255));
        jTextField12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField12.setForeground(new java.awt.Color(0, 0, 0));
        jTextField12.setEnabled(false);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setText("Points");

        jSeparator3.setBackground(new java.awt.Color(204, 255, 204));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setText("Amantha Gamage @ Java Institute");

        jCheckBox1.setText("Use Points");
        jCheckBox1.setEnabled(false);
        jCheckBox1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox1StateChanged(evt);
            }
        });

        jTextField13.setBackground(new java.awt.Color(255, 255, 255));
        jTextField13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField13.setForeground(new java.awt.Color(0, 0, 0));
        jTextField13.setEnabled(false);
        jTextField13.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField13KeyReleased(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel25.setText("1 Point = 10rs");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField4))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jTextField1)
                                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(4, 4, 4)
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jTextField2)
                                                    .addComponent(jTextField3)))))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5))
                            .addComponent(jSeparator1)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jTextField9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox1))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField13)))
                        .addGap(18, 18, 18)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                            .addComponent(jTextField10, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator3))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(26, 26, 26)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(4, 4, 4)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Order Menu", jPanel2);

        jLabel8.setBackground(new java.awt.Color(255, 51, 51));
        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Table 1");
        jLabel8.setOpaque(true);
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        jLabel9.setBackground(new java.awt.Color(255, 51, 51));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Table 2");
        jLabel9.setOpaque(true);
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });

        jLabel10.setBackground(new java.awt.Color(255, 51, 51));
        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("Table 3");
        jLabel10.setOpaque(true);
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel10MouseClicked(evt);
            }
        });

        jLabel11.setBackground(new java.awt.Color(255, 51, 51));
        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Table 4");
        jLabel11.setOpaque(true);
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel11MouseClicked(evt);
            }
        });

        jLabel12.setBackground(new java.awt.Color(255, 51, 51));
        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Table 5");
        jLabel12.setOpaque(true);
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel12MouseClicked(evt);
            }
        });

        jLabel13.setBackground(new java.awt.Color(255, 51, 51));
        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Table 6");
        jLabel13.setOpaque(true);
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel13MouseClicked(evt);
            }
        });

        jCheckBox2.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jCheckBox2.setText("Update Mode");
        jCheckBox2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jCheckBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox2ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(66, 66, 66))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 473, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(58, 58, 58)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(46, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Table Layout", jPanel3);

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("Total Price");

        jTextField6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField6.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField6.setEnabled(false);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("Paid Amount");

        jTextField14.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField14.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField14.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField14KeyReleased(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Payment Method");

        jTextField15.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField15.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField15.setEnabled(false);

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("Balance");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Order Method");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));
        jComboBox2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox2ItemStateChanged(evt);
            }
        });

        jTextField17.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField17.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField17.setText("0");
        jTextField17.setEnabled(false);

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel26.setText("Discount");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(231, 231, 231)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextField6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(224, 224, 224)
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Employee");

        jTextField5.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField5.setEnabled(false);

        jButton8.setBackground(new java.awt.Color(255, 102, 102));
        jButton8.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jButton8.setForeground(new java.awt.Color(0, 0, 0));
        jButton8.setText("Print Invoice");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel23.setText("Date");

        jTextField16.setFont(new java.awt.Font("Segoe UI", 1, 17)); // NOI18N
        jTextField16.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField16.setEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField16)
                .addGap(18, 18, 18)
                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jLabel24.setFont(new java.awt.Font("UD Digi Kyokasho NK-B", 0, 36)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(102, 0, 0));
        jLabel24.setText("& Restaurant");

        javax.swing.GroupLayout kGradientPanel1Layout = new javax.swing.GroupLayout(kGradientPanel1);
        kGradientPanel1.setLayout(kGradientPanel1Layout);
        kGradientPanel1Layout.setHorizontalGroup(
            kGradientPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(kGradientPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(kGradientPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 512, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(kGradientPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel24)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 440, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(kGradientPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        kGradientPanel1Layout.setVerticalGroup(
            kGradientPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(kGradientPanel1Layout.createSequentialGroup()
                .addGroup(kGradientPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(kGradientPanel1Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 478, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(kGradientPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTabbedPane1)))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(kGradientPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(kGradientPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        String name = jTextField2.getText();
        String price = jTextField3.getText();
        String quantity = jTextField1.getText();
        String stockQuantity = jTextField8.getText();

        String qtyValid = "[+-]?\\d+(\\.\\d+)?";

        try {
            if (jTextField7.getText().equals("Available")) {
                if (name.isEmpty()) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Please enter an item name");
                } else if (price.isEmpty()) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Please enter a price");
                } else if (quantity.isEmpty()) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Please enter a quantity");
                } else if (!quantity.matches(qtyValid)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Please enter a valid quantity");
                } else {
                    int qtyInsert = (int) (Double.parseDouble(quantity));
                    double qtyStock = Double.parseDouble(stockQuantity);

                    if (qtyInsert > qtyStock) {
                        Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Stock is not enough to fill the order");
                    } else {
                        // Update table
                        boolean itemExists = false;
                        double dPrice = Double.parseDouble(price);

                        for (int i = 0; i < dtm.getRowCount(); i++) {
                            String existingName = (String) dtm.getValueAt(i, 0);

                            if (existingName.equals(name)) {
                                double existingQty = Double.parseDouble(dtm.getValueAt(i, 1).toString());
                                int newQty = (int) (existingQty + qtyInsert);

                                dtm.setValueAt(newQty, i, 1);
                                dtm.setValueAt(dPrice, i, 2);

                                qtyStock -= qtyInsert;
                                jTextField8.setText(String.valueOf((int) Math.floor(qtyStock)));
                                itemExists = true;
                                break;
                            }
                        }

                        if (!itemExists) {
                            Vector<Object> vector = new Vector<>();
                            vector.add(name); //String
                            vector.add(qtyInsert); //int
                            vector.add(dPrice); //double
                            dtm.addRow(vector);

                            productIDMap.put(name, productID);

                            qtyStock -= qtyInsert;
                            jTextField8.setText(String.valueOf((int) Math.floor(qtyStock)));
                        }

                        // Update or add invoice item
                        InvoiceItem invoiceItem = new InvoiceItem();
                        invoiceItem.setitemID(itemID);
                        invoiceItem.setItemName(name);
                        invoiceItem.setQty(String.valueOf(qtyInsert)); // Set the inserted quantity initially
                        invoiceItem.setSellingPrice(price);

                        InvoiceItem found = invoiceItemMap.get(itemID);
                        if (found == null) {
                            invoiceItemMap.put(itemID, invoiceItem);
                        } else {
                            double updatedQty = Double.parseDouble(found.getQty()) + qtyInsert;
                            found.setQty(String.valueOf(updatedQty));
                        }

                        // Set cell renderer
                        for (int i = 0; i < jTable1.getColumnModel().getColumnCount(); i++) {
                            jTable1.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                        }
                    }
                }
            } else {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "No stock for this item");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.severe("Number Format Exception in MainFrame");
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Invalid numeric values entered");
        }

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jTextField1.setText("");
        InventoryManagement im = new InventoryManagement();
        im.setTabPane().setSelectedIndex(1);
        im.setTabPane().setEnabledAt(0, false);
        im.mainFrame = "main";
        im.setParent(this);
        im.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        resetOne();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jTabbedPane1.setSelectedIndex(1);
        tableSelection = true;
        jCheckBox2.setEnabled(false);
        jCheckBox2.setSelected(false);
    }//GEN-LAST:event_jButton2ActionPerformed

//Tables
    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        boolean updateTableAvailable = false;
        String color = "";

        if (jCheckBox2.isSelected()) {
            if (evt.getClickCount() == 2) {
                if (jLabel8.getBackground().equals(Color.GREEN)) {
                    jLabel8.setBackground(Color.RED);
                    color = "green";
                    updateTableAvailable = true;

                } else if (jLabel8.getBackground().equals(Color.RED)) {
                    jLabel8.setBackground(Color.GREEN);
                    color = "red";
                    updateTableAvailable = true;
                }
            }
        }

        if (updateTableAvailable == true) {
            if (color.equals("red")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '1' WHERE `id`='1'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Available");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            } else if (color.equals("green")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '2' WHERE `id`='1'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Occupied");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            }
        }

        if (tableSelection == true) {
            if (evt.getClickCount() == 2) {
                if (jLabel8.getBackground().equals(Color.GREEN)) {
                    tableSelection = false;
                    jCheckBox2.setEnabled(true);
                    jTabbedPane1.setSelectedIndex(0);
                    jTextField9.setText("1");
                } else if (jLabel8.getBackground().equals(Color.RED)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "This table is occupied");
                }
            }
        }


    }//GEN-LAST:event_jLabel8MouseClicked

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        boolean updateTableAvailable = false;
        String color = "";

        if (jCheckBox2.isSelected()) {
            if (evt.getClickCount() == 2) {
                if (jLabel9.getBackground().equals(Color.GREEN)) {
                    jLabel9.setBackground(Color.RED);
                    color = "green";
                    updateTableAvailable = true;

                } else if (jLabel9.getBackground().equals(Color.RED)) {
                    jLabel9.setBackground(Color.GREEN);
                    color = "red";
                    updateTableAvailable = true;
                }
            }
        }

        if (updateTableAvailable == true) {
            if (color.equals("red")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '1' WHERE `id`='2'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Available");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            } else if (color.equals("green")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '2' WHERE `id`='2'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Occupied");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            }
        }

        if (tableSelection == true) {
            if (evt.getClickCount() == 2) {
                if (jLabel9.getBackground().equals(Color.GREEN)) {
                    tableSelection = false;
                    jCheckBox2.setEnabled(true);
                    jTabbedPane1.setSelectedIndex(0);
                    jTextField9.setText("2");
                } else if (jLabel9.getBackground().equals(Color.RED)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "This table is occupied");
                }
            }
        }

    }//GEN-LAST:event_jLabel9MouseClicked

    private void jLabel10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseClicked
        boolean updateTableAvailable = false;
        String color = "";

        if (jCheckBox2.isSelected()) {
            if (evt.getClickCount() == 2) {
                if (jLabel10.getBackground().equals(Color.GREEN)) {
                    jLabel10.setBackground(Color.RED);
                    color = "green";
                    updateTableAvailable = true;

                } else if (jLabel10.getBackground().equals(Color.RED)) {
                    jLabel10.setBackground(Color.GREEN);
                    color = "red";
                    updateTableAvailable = true;
                }
            }
        }

        if (updateTableAvailable == true) {
            if (color.equals("red")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '1' WHERE `id`='3'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Available");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            } else if (color.equals("green")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '2' WHERE `id`='3'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Occupied");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            }
        }

        if (tableSelection == true) {
            if (evt.getClickCount() == 2) {
                if (jLabel10.getBackground().equals(Color.GREEN)) {
                    tableSelection = false;
                    jCheckBox2.setEnabled(true);
                    jTabbedPane1.setSelectedIndex(0);
                    jTextField9.setText("3");
                } else if (jLabel10.getBackground().equals(Color.RED)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "This table is occupied");
                }
            }
        }
    }//GEN-LAST:event_jLabel10MouseClicked

    private void jLabel11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel11MouseClicked
        boolean updateTableAvailable = false;
        String color = "";

        if (jCheckBox2.isSelected()) {
            if (evt.getClickCount() == 2) {
                if (jLabel11.getBackground().equals(Color.GREEN)) {
                    jLabel11.setBackground(Color.RED);
                    color = "green";
                    updateTableAvailable = true;

                } else if (jLabel11.getBackground().equals(Color.RED)) {
                    jLabel11.setBackground(Color.GREEN);
                    color = "red";
                    updateTableAvailable = true;
                }
            }
        }

        if (updateTableAvailable == true) {
            if (color.equals("red")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '1' WHERE `id`='4'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Available");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            } else if (color.equals("green")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '2' WHERE `id`='4'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Occupied");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            }
        }

        if (tableSelection == true) {
            if (evt.getClickCount() == 2) {
                if (jLabel11.getBackground().equals(Color.GREEN)) {
                    tableSelection = false;
                    jCheckBox2.setEnabled(true);
                    jTabbedPane1.setSelectedIndex(0);
                    jTextField9.setText("4");
                } else if (jLabel11.getBackground().equals(Color.RED)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "This table is occupied");
                }
            }
        }
    }//GEN-LAST:event_jLabel11MouseClicked

    private void jLabel12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel12MouseClicked
        boolean updateTableAvailable = false;
        String color = "";

        if (jCheckBox2.isSelected()) {
            if (evt.getClickCount() == 2) {
                if (jLabel12.getBackground().equals(Color.GREEN)) {
                    jLabel12.setBackground(Color.RED);
                    color = "green";
                    updateTableAvailable = true;

                } else if (jLabel12.getBackground().equals(Color.RED)) {
                    jLabel12.setBackground(Color.GREEN);
                    color = "red";
                    updateTableAvailable = true;
                }
            }
        }

        if (updateTableAvailable == true) {
            if (color.equals("red")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '1' WHERE `id`='5'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Available");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            } else if (color.equals("green")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '2' WHERE `id`='5'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Occupied");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            }
        }

        if (tableSelection == true) {
            if (evt.getClickCount() == 2) {
                if (jLabel12.getBackground().equals(Color.GREEN)) {
                    tableSelection = false;
                    jCheckBox2.setEnabled(true);
                    jTabbedPane1.setSelectedIndex(0);
                    jTextField9.setText("5");
                } else if (jLabel12.getBackground().equals(Color.RED)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "This table is occupied");
                }
            }
        }
    }//GEN-LAST:event_jLabel12MouseClicked

    private void jLabel13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel13MouseClicked
        boolean updateTableAvailable = false;
        String color = "";

        if (jCheckBox2.isSelected()) {
            if (evt.getClickCount() == 2) {
                if (jLabel13.getBackground().equals(Color.GREEN)) {
                    jLabel13.setBackground(Color.RED);
                    color = "green";
                    updateTableAvailable = true;

                } else if (jLabel13.getBackground().equals(Color.RED)) {
                    jLabel13.setBackground(Color.GREEN);
                    color = "red";
                    updateTableAvailable = true;
                }
            }
        }

        if (updateTableAvailable == true) {
            if (color.equals("red")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '1' WHERE `id`='6'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Available");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            } else if (color.equals("green")) {
                try {
                    SQL.executeIUD("UPDATE `table` SET `table_status_id` = '2' WHERE `id`='6'");
                    Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Table status changed to : Occupied");
                    loadTables();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("SQL Exception");
                }
            }
        }

        if (tableSelection == true) {
            if (evt.getClickCount() == 2) {
                if (jLabel13.getBackground().equals(Color.GREEN)) {
                    tableSelection = false;
                    jCheckBox2.setEnabled(true);
                    jTabbedPane1.setSelectedIndex(0);
                    jTextField9.setText("6");
                } else if (jLabel13.getBackground().equals(Color.RED)) {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "This table is occupied");
                }
            }
        }
    }//GEN-LAST:event_jLabel13MouseClicked
//Tables

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        resetTwo();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        resetThree();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        CustomerManagement cm = new CustomerManagement();
        cm.setTabPane().setSelectedIndex(1);
        cm.mainFrame = "main";
        cm.setParent(this);
        cm.setVisible(true);

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField13KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField13KeyReleased
        if (jTextField13.getText().equals("")) {
            jCheckBox1.setSelected(false);
            balanceUpdate();
        } else {
            jCheckBox1.setSelected(true);
            if (jTextField17.getText().equals(jTextField6.getText())) {
                balanceUpdate();
                jTextField14.setText("0");
                jTextField15.setText("0");
            } else {
                balanceUpdate();
            }

        }
    }//GEN-LAST:event_jTextField13KeyReleased

    private void jCheckBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox2ItemStateChanged
        if (!tableSelection) {
            if (jCheckBox2.isSelected()) {
                Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Tables in Update Mode");
            } else {
                Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Update mode turned off");
            }
        } else {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Unable to put update mode");
        }

    }//GEN-LAST:event_jCheckBox2ItemStateChanged

    private void jTextField14KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField14KeyReleased
        balanceUpdate();
    }//GEN-LAST:event_jTextField14KeyReleased

//    Balance Update function
    private void balanceUpdate() {
        String priceValid = "^-?\\d*(?:\\.\\d+)?$";
        String totalAmount = jTextField6.getText();
        String totalPaying = jTextField14.getText();
        String discount = jTextField17.getText();

        if (totalPaying.equals("")) {
            jTextField15.setText("");
        } else {
            if (!totalPaying.matches(priceValid)) {
//Do nothing
            } else {
                if (!totalAmount.equals("")) {

                    double balance = Double.parseDouble(totalPaying) - (Double.parseDouble(totalAmount) - Double.parseDouble(discount));
                    if (balance < 0) {
                        jTextField15.setText("");
                    } else {
                        jTextField15.setText(String.valueOf(balance));
                    }
                }
            }
        }
    }

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (evt.getClickCount() == 2) {
            int row = jTable1.getSelectedRow();
            DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();

            int confirm = JOptionPane.showConfirmDialog(this, "Confirm row deletion", "Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Employee Management Delete error");
                }
                Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Item Row Deleted");
            }
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jComboBox2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox2ItemStateChanged
        if (jComboBox2.getSelectedItem().equals("Dine In")) {
            jButton2.setEnabled(true);
        } else if (jComboBox2.getSelectedItem().equals("Takeaway") || jComboBox2.getSelectedItem().equals("Select")) {
            jTextField9.setText("");
            jButton2.setEnabled(false);
        }
    }//GEN-LAST:event_jComboBox2ItemStateChanged

//    FINAL PROCESS
//    PRINT BUTTON
//    INVOICE AAAAAAAAAAAA
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        String empName = employeeName;
        String orderID = jTextField4.getText();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String totalPrice = jTextField6.getText();
        String totalPaid = jTextField14.getText();
        String balance = jTextField15.getText();
        String customerName = jTextField10.getText();

        String discount = jTextField17.getText();
        String pointsUse = jTextField13.getText();

        String tableID = jTextField9.getText();

        String paymentMethod = (String) jComboBox1.getSelectedItem();
        String orderMethod = (String) jComboBox2.getSelectedItem();
        //view or print report
        if (totalPrice != null && !totalPrice.trim().isEmpty() && totalPaid != null && !totalPaid.trim().isEmpty()) {
            if (paymentMethod.equals("Select")) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Please select a payment method");
            } else if (orderMethod.equals("Select")) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Please select an order method");
            } else if (orderMethod.equals("Dine In") && (tableID == null || tableID.trim().isEmpty())) {
                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Please select a table for the customer");
            } else {
//                Final Code
                boolean pointsUsed = false;
                boolean orderTable = false;
                boolean orderItems = false;
                boolean employeeEarnings = false;
                boolean inventoryUpdate = false;
                boolean invoiceUpdate = false;

//Customer Points Update
                if (jCheckBox1.isSelected() == true) {
                    try {
                        double spendingPoints = Double.parseDouble(jTextField13.getText());
                        double availablePoints = Double.parseDouble(jTextField12.getText());

                        double finalPoints = availablePoints - spendingPoints;

                        SQL.executeIUD("UPDATE `customer` SET `points`='" + finalPoints + "' WHERE `id`='" + customerID + "'");

//                        True
                        pointsUsed = true;
                        logger.log(Level.INFO, "Customer ID {0} points updated", customerID);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.log(Level.SEVERE, "Customer points updating error: {0}", e.getMessage());
                    }
                } else if (jCheckBox1.isSelected() == false) {
                    try {
                        if (customerID != null && !customerID.trim().isEmpty()) {
                            double paidAmount = Double.parseDouble(jTextField14.getText());
                            double pointsForPaidAmount = paidAmount / 100;
                            double availablePoints = Double.parseDouble(jTextField12.getText());

                            double finalPoints = availablePoints + pointsForPaidAmount;

                            this.finalCusPoints = String.valueOf(finalPoints);

                            SQL.executeIUD("UPDATE `customer` SET `points`='" + finalPoints + "' WHERE `id`='" + customerID + "'");

                            pointsUsed = false;
                            logger.log(Level.INFO, "Customer ID {0} points updated", customerID);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.log(Level.SEVERE, "Error updating customer points: {0}", e.getMessage());
                    }

                }

//Order table Update
                try {
                    String sql;
                    if (tableID == null || tableID.trim().isEmpty()) {
                        if (customerID == null || customerID.trim().isEmpty()) {
                            sql = "INSERT INTO `order` (`id`, `table_id`, `customer_id`, `total_price`, `order_method_id`, `user_id`) "
                                    + "VALUES ('" + orderID + "', NULL, NULL, '" + totalPrice + "', '" + orderMethodMap.get(orderMethod) + "', '" + employeeID + "')";
                        } else {
                            sql = "INSERT INTO `order` (`id`, `table_id`, `customer_id`, `total_price`, `order_method_id`, `user_id`) "
                                    + "VALUES ('" + orderID + "', NULL, '" + customerID + "', '" + totalPrice + "', '" + orderMethodMap.get(orderMethod) + "', '" + employeeID + "')";
                        }
                    } else {
                        if (customerID == null || customerID.trim().isEmpty()) {
                            sql = "INSERT INTO `order` (`id`, `table_id`, `customer_id`, `total_price`, `order_method_id`, `user_id`) "
                                    + "VALUES ('" + orderID + "', '" + tableID + "', NULL, '" + totalPrice + "', '" + orderMethodMap.get(orderMethod) + "', '" + employeeID + "')";
                        } else {
                            sql = "INSERT INTO `order` (`id`, `table_id`, `customer_id`, `total_price`, `order_method_id`, `user_id`) "
                                    + "VALUES ('" + orderID + "', '" + tableID + "', '" + customerID + "', '" + totalPrice + "', '" + orderMethodMap.get(orderMethod) + "', '" + employeeID + "')";
                        }
                    }

                    SQL.executeIUD(sql);

//                    True
                    orderTable = true;
                    logger.log(Level.INFO, "Order ID {0} logged", orderID);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, "Order Creation error: {0}", e.getMessage());
                }

//Order Item table Update
                try {
                    int rowCount = jTable1.getRowCount();

                    for (int i = 0; i < rowCount; i++) {
                        SQL.executeIUD("INSERT INTO `order_item` (`order_id`,`menu_item_id`,`quantity`,`unit_price`) "
                                + "VALUES ('" + orderID + "','" + productIDMap.get(jTable1.getValueAt(i, 0)) + "','" + jTable1.getValueAt(i, 1) + "','" + jTable1.getValueAt(i, 2) + "')");
                    }

                    logger.info("Order item records added");

//                    True
                    orderItems = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Order Item Table Error");
                }

//Employee Earnings Update
                try {
                    ResultSet earningsResult = SQL.executeSearch("SELECT `earnings` FROM `employee_earnings` WHERE `user_id` = '" + employeeID + "'");

                    if (earningsResult != null && earningsResult.next()) {
                        double earnings = Double.parseDouble(earningsResult.getString("earnings"));
                        double finalEarnings = earnings + Double.parseDouble(totalPrice);

                        SQL.executeIUD("UPDATE `employee_earnings` SET `earnings`='" + finalEarnings + "' WHERE `user_id`='" + employeeID + "'");

                        logger.log(Level.INFO, "Employee {0} earnings updated", empName);

//                        True
                        employeeEarnings = true;
                    } else {
                        logger.log(Level.SEVERE, "No earnings record found for user: {0}", employeeID);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Employee earnings update error");
                }

//Inventory Update
                try {
                    int rowCount = jTable1.getRowCount();

                    for (int i = 0; i < rowCount; i++) {
                        String qty = String.valueOf(jTable1.getValueAt(i, 1));
                        String itemId = productIDMap.get(String.valueOf(jTable1.getValueAt(i, 0)));

                        ResultSet result = SQL.executeSearch("SELECT `quantity` FROM `inventory` WHERE `menu_item_id`='" + itemId + "'");

                        if (result != null && result.next()) {
                            String currentQty = result.getString("quantity");
                            int finalQty = (int) (Double.parseDouble(currentQty) - Double.parseDouble(qty));

                            SQL.executeIUD("UPDATE `inventory` SET `quantity` = '" + String.valueOf(finalQty) + "' WHERE `menu_item_id`='" + itemId + "'");
                            SQL.executeIUD("UPDATE `inventory` SET `last_updated` = '" + jTextField16.getText() + "' WHERE `menu_item_id`='" + itemId + "'");

                            logger.log(Level.INFO, "Item ID {0} inventory updated", itemId);
//                            True
                            inventoryUpdate = true;
                        } else {
                            logger.log(Level.SEVERE, "No inventory found for menu_item_id: {0}", itemId);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, "Inventory Updating error: {0}", e.getMessage());
                }

//Finally.. Invoice Table
                try {
                    if (customerID == null || customerID.trim().isEmpty()) {
                        SQL.executeIUD("INSERT INTO `invoice` (`date`,`user_id`,`payment_method_id`,`customer_id`,`total_paid`,`discount`,`order_id`) "
                                + "VALUES ('" + jTextField16.getText() + "','" + employeeID + "','" + paymentMethodMap.get(paymentMethod) + "', NULL, '" + totalPaid + "','" + discount + "','" + orderID + "')");
                    } else {
                        SQL.executeIUD("INSERT INTO `invoice` (`date`,`user_id`,`payment_method_id`,`customer_id`,`total_paid`,`discount`,`order_id`) "
                                + "VALUES ('" + jTextField16.getText() + "','" + employeeID + "','" + paymentMethodMap.get(paymentMethod) + "','" + customerID + "','" + totalPaid + "','" + discount + "','" + orderID + "')");
                    }

//                    True
                    invoiceUpdate = true;
                    logger.log(Level.INFO, "Invoice added");
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.severe("Invoice table insert error");
                }

//Jasper Print
                if (orderItems == true && orderTable == true && employeeEarnings == true && inventoryUpdate == true && invoiceUpdate == true) {

                    try {
                        if (customerName == null || customerName.trim().isEmpty()) {
                            customerName = "Quick Checkout";
                        }

                        String path = "src/reports/invoice.jasper";

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("Parameter1", orderMethod);
                        params.put("Parameter2", empName);
                        params.put("Parameter3", orderID);
                        params.put("Parameter4", dateTime);
                        params.put("Parameter5", totalPrice);
                        params.put("Parameter6", totalPaid);
                        params.put("Parameter7", balance);
                        params.put("Parameter8", paymentMethod);
                        params.put("Parameter9", customerName);
                        params.put("Parameter10", discount);

                        if (pointsUsed) {
                            params.put("Parameter11", "-" + pointsUse);
                        } else {
                            params.put("Parameter11", "+" + finalCusPoints);
                        }

                        JRTableModelDataSource dataSource = new JRTableModelDataSource(jTable1.getModel());

                        JasperPrint jasperPrint = JasperFillManager.fillReport(path, params, dataSource);

                        JasperViewer.viewReport(jasperPrint, false);

                        resetOne();
                        resetTwo();
                        resetThree();
                        resetFinal();
                        Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.TOP_CENTER, "Invoice Printed");
                        logger.log(Level.INFO, "Invoice Printed for Order ID ''{0}''", orderID);

                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.severe("Jasper Report Loading error :((( kes");
                    }
                }
            }
        } else {
            Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_CENTER, "Please enter the paid amount or add some items to the order");
        }
    }//GEN-LAST:event_jButton8ActionPerformed
//    FINAL PROCESS
//    PRINT BUTTON
//    INVOICE AAAAAAAAAAAA

    private void jCheckBox1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox1StateChanged
        if (jCheckBox1.isSelected() == true) {
            jTextField13.setEnabled(true);
        } else {
            jTextField13.setEnabled(false);
            jTextField13.setText("");
        }
    }//GEN-LAST:event_jCheckBox1StateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        FlatMacLightLaf.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField15;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private keeptoo.KGradientPanel kGradientPanel1;
    // End of variables declaration//GEN-END:variables
//Items
    public JTextField getItemField() {
        return jTextField2;
    }

    public JTextField getPriceField() {
        return jTextField3;
    }

    public JTextField getItemStatusField() {
        return jTextField7;
    }

    public JTextField getItemQty() {
        return jTextField8;
    }

//    Customers
    public JTextField getCustomerName() {
        return jTextField10;
    }

    public JTextField getCustomerMobile() {
        return jTextField11;
    }

    public JTextField getCustomerPoints() {
        return jTextField12;
    }
}

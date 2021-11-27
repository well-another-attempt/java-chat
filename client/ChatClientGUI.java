package com.company;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatClientGUI extends Thread {
    final JTextPane jtextFilDiscu = new JTextPane();
    final JTextPane jtextListUsers = new JTextPane();
    final JTextField jtextInputChat = new JTextField();
    private String oldMsg = "";
    private Thread read;
    private String serverName;
    private int PORT;
    private String name;
    BufferedReader input;
    PrintWriter output;
    Socket server;

    public ChatClientGUI() {
        this.serverName = "localhost";
        this.PORT = 12345;
        this.name = "nickname";

        String fontfamily = "Arial, sans-serif";
        Font font = new Font(fontfamily, Font.PLAIN, 15);

        final JFrame jfr = new JFrame("Chat");
        jfr.getContentPane().setLayout(null);
        jfr.setSize(700, 500);
        jfr.setResizable(false);
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jtextFilDiscu.setBounds(25, 25, 490, 320);
        jtextFilDiscu.setFont(font);
        jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
        jtextFilDiscu.setEditable(false);
        JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
        jtextFilDiscuSP.setBounds(25, 25, 490, 320);

        jtextFilDiscu.setContentType("text/html");
        jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        jtextListUsers.setBounds(520, 25, 156, 320);
        jtextListUsers.setEditable(true);
        jtextListUsers.setFont(font);
        jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
        jtextListUsers.setEditable(false);
        JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
        jsplistuser.setBounds(520, 25, 156, 320);

        jtextListUsers.setContentType("text/html");
        jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        jtextInputChat.setBounds(0, 350, 400, 50);
        jtextInputChat.setFont(font);
        jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
        final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
        jtextInputChatSP.setBounds(25, 350, 650, 50);

        final JButton jsbtn = new JButton("Send");
        jsbtn.setFont(font);
        jsbtn.setBounds(575, 410, 100, 35);

        final JButton jsbtndeco = new JButton("Disconnect");
        jsbtndeco.setFont(font);
        jsbtndeco.setBounds(25, 410, 130, 35);

        jtextInputChat.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }

                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    String currentMessage = jtextInputChat.getText().trim();
                    jtextInputChat.setText(oldMsg);
                    oldMsg = currentMessage;
                }

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    String currentMessage = jtextInputChat.getText().trim();
                    jtextInputChat.setText(oldMsg);
                    oldMsg = currentMessage;
                }
            }
        });

        jsbtn.addActionListener(ae -> sendMessage());

        final JTextField jtfName = new JTextField(this.name);
        final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
        final JTextField jtfAddr = new JTextField(this.serverName);
        final JButton jcbtn = new JButton("Connect");

        jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
        jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
        jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));

        jcbtn.setFont(font);
        jtfAddr.setBounds(25, 380, 135, 40);
        jtfName.setBounds(375, 380, 135, 40);
        jtfport.setBounds(200, 380, 135, 40);
        jcbtn.setBounds(575, 380, 100, 40);

        jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
        jtextListUsers.setBackground(Color.LIGHT_GRAY);

        jfr.add(jcbtn);
        jfr.add(jtextFilDiscuSP);
        jfr.add(jsplistuser);
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.setVisible(true);


        appendToPane(jtextFilDiscu, "<h4>Hello,World!</h4>");

        jcbtn.addActionListener(ae -> {
            try {
                name = jtfName.getText();
                String port = jtfport.getText();
                serverName = jtfAddr.getText();
                PORT = Integer.parseInt(port);

                appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
                server = new Socket(serverName, PORT);

                appendToPane(jtextFilDiscu, "<span>Connected to " +
                        server.getRemoteSocketAddress() + "</span>");

                input = new BufferedReader(new InputStreamReader(server.getInputStream()));
                output = new PrintWriter(server.getOutputStream(), true);

                output.println(name);

                read = new Read();
                read.start();
                jfr.remove(jtfName);
                jfr.remove(jtfport);
                jfr.remove(jtfAddr);
                jfr.remove(jcbtn);
                jfr.add(jsbtn);
                jfr.add(jtextInputChatSP);
                jfr.add(jsbtndeco);
                jfr.revalidate();
                jfr.repaint();
                jtextFilDiscu.setBackground(Color.WHITE);
                jtextListUsers.setBackground(Color.WHITE);
            } catch (Exception ex) {
                appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
                JOptionPane.showMessageDialog(jfr, ex.getMessage());
            }
        });

        jsbtndeco.addActionListener(ae -> {
            jfr.add(jtfName);
            jfr.add(jtfport);
            jfr.add(jtfAddr);
            jfr.add(jcbtn);
            jfr.remove(jsbtn);
            jfr.remove(jtextInputChatSP);
            jfr.remove(jsbtndeco);
            jfr.revalidate();
            jfr.repaint();
            read.interrupt();
            jtextListUsers.setText(null);
            jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
            jtextListUsers.setBackground(Color.LIGHT_GRAY);
            appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
            output.close();
        });

    }

    public static class TextListener implements DocumentListener {
        JTextField jtf1;
        JTextField jtf2;
        JTextField jtf3;
        JButton jcbtn;

        public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn) {
            this.jtf1 = jtf1;
            this.jtf2 = jtf2;
            this.jtf3 = jtf3;
            this.jcbtn = jcbtn;
        }

        public void changedUpdate(DocumentEvent e) {
        }

        public void removeUpdate(DocumentEvent e) {
            jcbtn.setEnabled(!jtf1.getText().trim().equals("") &&
                    !jtf2.getText().trim().equals("") &&
                    !jtf3.getText().trim().equals(""));
        }

        public void insertUpdate(DocumentEvent e) {
            jcbtn.setEnabled(!jtf1.getText().trim().equals("") &&
                    !jtf2.getText().trim().equals("") &&
                    !jtf3.getText().trim().equals(""));
        }

    }

    public void sendMessage() {
        try {
            String message = jtextInputChat.getText().trim();
            if (message.equals("")) {
                return;
            }
            this.oldMsg = message;
            output.println(message);
            jtextInputChat.requestFocus();
            jtextInputChat.setText(null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new ChatClientGUI();
    }

    class Read extends Thread {
        public void run() {
            String message;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    message = input.readLine();
                    if (message != null) {
                        if (message.charAt(0) == '[') {
                            message = message.substring(1, message.length() - 1);
                            ArrayList<String> ListUser = new ArrayList<>(
                                    Arrays.asList(message.split(", "))
                            );
                            jtextListUsers.setText(null);
                            for (String user : ListUser) {
                                appendToPane(jtextListUsers, "@" + user);
                            }
                        } else {
                            appendToPane(jtextFilDiscu, message);
                        }
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to parse incoming message");
                }
            }
        }
    }

    private void appendToPane(JTextPane tp, String msg) {
        HTMLDocument doc = (HTMLDocument) tp.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
            tp.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

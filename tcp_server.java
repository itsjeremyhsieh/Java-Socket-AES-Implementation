import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.swing.*;
import java.awt.event.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileOutputStream;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.Base64;

public class tcp_server extends JFrame {

    static JTextPane chat = new JTextPane();
    static JTextPane tosend = new JTextPane();
    static JButton pic = new JButton("圖片");
    static JButton send = new JButton("傳送");
    static String toprint = "";
    static DataInputStream input;
    static DataOutputStream output;
    static DataInputStream input1;
    static DataOutputStream output1;
    static String path;
    static File selectedFile;
    static Boolean image = false;
    static ServerSocket ss;
    static ServerSocket ss1;
    static Socket sc;
    static Socket sc1;
    static String img = "";
    static JLabel myClient = new JLabel();
    static JLabel myPort = new JLabel();
    static JLabel selectedfilelabel = new JLabel();
    static int imgcnt = 0;

    public static void main(String[] args) throws Exception {

        JFrame frm = new JFrame("TCP Server");
        frm.setLayout(null);
        Container ctp = frm.getContentPane();
        ctp.setLayout(null);

        JLabel Client_label = new JLabel("Client IP: "); // display connected client's IP address & port num.
        JLabel Client_port = new JLabel("Client Port: ");

        JScrollPane jsp = new JScrollPane(); // add scrollbar for chat and text area
        JScrollPane jsp2 = new JScrollPane();

        Client_label.setBounds(30, 30, 60, 20);
        myClient.setBounds(100, 30, 100, 20);
        Client_port.setBounds(220, 30, 80, 20);
        myPort.setBounds(300, 30, 60, 20);
        // chat.setBounds(30, 80, 400, 600);
        jsp.setBounds(30, 80, 400, 600);
        jsp.setViewportView(chat);
        jsp2.setBounds(30, 700, 400, 60);
        jsp2.setViewportView(tosend);
        // tosend.setBounds(30, 700, 400, 60);
        pic.setBounds(30, 780, 100, 30);
        send.setBounds(160, 780, 100, 30);
        pic.addActionListener(new Actlis());
        send.addActionListener(new Actlis());
        chat.setEditable(false);
        chat.setContentType("text/html"); // set to display html in chat textpane
        chat.setText("正在等待連線...");
        selectedfilelabel.setBounds(30, 810, 300, 30);

        ctp.add(Client_label);
        ctp.add(myClient);
        ctp.add(Client_port);
        ctp.add(myPort);
        // ctp.add(chat);
        // ctp.add(tosend);
        ctp.add(pic);
        ctp.add(send);
        ctp.add(jsp);
        ctp.add(jsp2);
        ctp.add(selectedfilelabel);
        frm.setSize(485, 900);
        frm.setLocation(500, 150);
        frm.setVisible(true);

        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ss = new ServerSocket(10000); // create 2 new serversockets, one for text message and one for image.
        ss1 = new ServerSocket(10001);
        sc = ss.accept(); // create 2 sockets and wait for client's connection.
        sc1 = ss1.accept();
        // System.out.println("run");

        // System.out.println("success");
        String clientIP = sc.getInetAddress().getHostAddress(); // get client's information and display
        myClient.setText(clientIP);
        String clientport = sc.getRemoteSocketAddress().toString().split(":")[1];
        myPort.setText(clientport);

        input = new DataInputStream(sc.getInputStream());
        output = new DataOutputStream(sc.getOutputStream());

        input1 = new DataInputStream(sc1.getInputStream());
        output1 = new DataOutputStream(sc1.getOutputStream());
        new rcvmsg(); // run new thread for receiving message
        new rcvimg(); // run new thread for receiving image
        chat.setText("連線成功！");
    }

    static public class rcvmsg extends Thread { // thread for receiving messages
        static String key = "12345678abcdefgh"; 
        static String iv = "iviviviviviviviv";
        static String aad = "aad"; 
        static int tagLength = 128; 

        public rcvmsg() {
            new Thread(this).start();
        }

        public static byte[] decryptGCM(byte[] data, byte[] key, byte[] iv, byte[] aad, int tagLength)
                throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
                IllegalBlockSizeException, InvalidAlgorithmParameterException {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(tagLength, iv));
            cipher.updateAAD(aad);
            byte[] result = cipher.doFinal(data);
            return result;
        }

        public void run() {
            // System.out.println("can receive");
            while (true) {

                try {
                    String str = "";
                    str = input.readUTF();
                    if (!str.equals("")) {
                        byte[] plaintext = decryptGCM(Base64.getDecoder().decode(str), key.getBytes(), iv.getBytes(),
                                aad.getBytes(), tagLength);
                        String eof = "EOF";
                        if (new String(plaintext).equals(eof)) { // if client sends EOF, meaning to end connection
                            System.out.println("end");
                            input.close(); // close everything and restart everything
                            input1.close();
                            output.close();
                            output1.close();
                            sc.close();
                            sc1.close();
                            toprint = "";
                            chat.setText(toprint);
                            myClient.setText("");
                            myPort.setText("");
                            chat.setText("正在等待連線");
                            // System.out.println("run");
                            Thread.sleep(1000);
                            ss = new ServerSocket(10000); // recreate ServerSocket
                            ss1 = new ServerSocket(10001);
                            sc = ss.accept(); // wait for next connection
                            sc1 = ss1.accept();
                            String clientIP = sc.getInetAddress().getHostAddress();
                            myClient.setText(clientIP);
                            String clientport = sc.getRemoteSocketAddress().toString().split(":")[1];
                            myPort.setText(clientport);
                            input = new DataInputStream(sc.getInputStream());
                            output = new DataOutputStream(sc.getOutputStream());
                            input1 = new DataInputStream(sc1.getInputStream());
                            output1 = new DataOutputStream(sc1.getOutputStream());
                            new rcvmsg();
                            new rcvimg();
                        }
                        // str = str.replaceAll("\n", "<br>"); // normal text message, replace enter
                        // with <br>
                       // System.out.println(str);
                        
                        //System.out.println(new String(plaintext));
                        toprint += "Server IP: 密文：" + str + "<br>明文：" + new String(plaintext) + "<br>";
                        chat.setText(toprint); // print text message on chat area
                        chat.setCaretPosition(chat.getDocument().getLength()); // scroll to bottom of chat area
                                                                               // automatically
                    }

                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }

    static public class rcvimg extends Thread { // Thread for receiving images
        static String key = "12345678abcdefgh";
        static String iv = "iviviviviviviviv";
        static String aad = "aad"; 
        static int tagLength = 128; 

        public static byte[] decryptGCM(byte[] data, byte[] key, byte[] iv, byte[] aad, int tagLength)
                throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
                IllegalBlockSizeException, InvalidAlgorithmParameterException {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(tagLength, iv));
            cipher.updateAAD(aad);
            byte[] result = cipher.doFinal(data);
            return result;
        }

        public rcvimg() {
            new Thread(this).start();
        }

        public void run() {
            while (true) {
                try {
                    // System.out.println("can rsv img");
                    BufferedInputStream in;
                    InputStream ins = sc1.getInputStream();
                    in = new BufferedInputStream(ins);
                   
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    
                    byte[] plaintext1 = decryptGCM(in.readAllBytes(), key.getBytes(), iv.getBytes(),
                            aad.getBytes(), tagLength);
                    buf.write(plaintext1); // write to buffer

                    // while ((len = in.read(b)) > 0) {

                    //     System.out.print(new String(b));
                    //     // System.out.println("receiving");
                    // }
                    System.out.println("img received");
                    OutputStream out = new FileOutputStream(new File("imgs" + imgcnt + ".jpg")); // Save image as file,
                                                                                                 // filename = imgs +
                                                                                                 // imgcnt + .jpg
                    buf.writeTo(out);
                    sc1.shutdownInput();
                    // in.close();
                    out.close();
                    in = null;
                    String img = "<img src= 'file:imgs" + imgcnt + ".jpg' width = '200'> <br>"; // display image on chat
                                                                                                // area using image
                                                                                                // saved previously
                    toprint += "Client IP: <br>" + img;
                    imgcnt++; // img counter +1
                    chat.setText(toprint); // print image
                    chat.setCaretPosition(chat.getDocument().getLength()); // scroll to bottom of chat area
                                                                           // automatically
                } catch (Exception e) {
                    // System.out.println(e.toString() + "server rcv");
                }
            }
        }
    }

    static public class sendmsg extends Thread { // Thread for sending messages (texts and images)
        static String key = "12345678abcdefgh"; 
        static String iv = "iviviviviviviviv";
        static String aad = "aad"; 
        static int tagLength = 128; 
        private String msg;

        public sendmsg(String str) {
            msg = str;
            new Thread(this).start();
        }

        public static byte[] encryptGCM(byte[] data, byte[] key, byte[] iv, byte[] aad, int tagLength)
                throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
                IllegalBlockSizeException, InvalidAlgorithmParameterException {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(tagLength, iv));
            cipher.updateAAD(aad);
            byte[] result = cipher.doFinal(data);
            return result;
        }

        public void run() {
            if (image == false) { // if sending text
                try {
                    msg = msg.replaceAll("\n", "<br>"); // replace enter with <br>
                    if (!msg.equals("")) {
                        byte[] ciphertext = encryptGCM(msg.getBytes(), key.getBytes(), iv.getBytes(), aad.getBytes(),
                                tagLength);

                        // System.out.println("GCM 模式加密结果（Base64）：" +
                        // Base64.getEncoder().encodeToString(ciphertext));
                        output.writeUTF(Base64.getEncoder().encodeToString(ciphertext)); // send message to server
                        toprint += "Client IP: 密文：" + Base64.getEncoder().encodeToString(ciphertext) + "<br>明文：" + msg
                                + "<br>";
                        chat.setText(toprint); // display message on chat area
                        chat.setCaretPosition(chat.getDocument().getLength()); // scroll to bottom of chat area
                                                                               // automatically
                    }

                } catch (Exception e) {
                    System.out.println("訊息傳送失敗");
                }
            } else { // sending image
                System.out.println("start sending");
                image = false; // restore default image value
                String img = "<img src= 'file:\\" + path + "' width = '200'> <br>"; // display image on chat area using
                                                                                    // html syntax
                // toprint += "Server IP: ";
                toprint += "Server IP: <br>" + img;
                chat.setText(toprint);
                chat.setCaretPosition(chat.getDocument().getLength()); // scroll to bottom of chat area automatically
                try {
                    FileInputStream fis = new FileInputStream(new File(path));
                    OutputStream os = sc1.getOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                 
                    System.out.println("ready to send.");
                    byte[] ciphertext = encryptGCM(fis.readAllBytes(), key.getBytes(), iv.getBytes(), aad.getBytes(),
                                tagLength);
                        //output.writeUTF(Base64.getEncoder().encodeToString(ciphertext));
                    bos.write(ciphertext);
                    bos.flush();

                    sc1.shutdownOutput(); // must shutdown output, otherwise it won't send
                    // bos.close();
                    fis.close();
                    // bos = null;
                    System.out.println("sent");
                    selectedfilelabel.setText(""); // restore default Jlabel value
                } catch (Exception e) {
                    System.out.println(e.toString() + "Server send");
                }
            }
        }
    }

    static public class Actlis extends WindowAdapter implements ActionListener // .addActionListener(new Actlis());
    {
        public void windowClosing(WindowEvent e) { // close everything if X clicked
            try {
                input.close();
                input1.close();
                output.close();
                output1.close();
                sc.close();
                sc1.close();
            } catch (Exception eee) {
                // TODO: handle exception
            }
            System.exit(0);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == send) { // if send button clicked

                String str = tosend.getText();
                new sendmsg(str); // send message
                tosend.setText(""); // restore texting area
            }
            if (e.getSource() == pic) { // if sendpic button clicked
                JFileChooser cfile = new JFileChooser();
                cfile.setFileFilter(new FileNameExtensionFilter("Image files (*.GIF,*.PNG,*.JPG, *.JPEG)", "GIF", "PNG",
                        "JPG", "JPEG")); // limit selected file type *extra function
                int returnfile = cfile.showOpenDialog(pic);
                if (returnfile == JFileChooser.APPROVE_OPTION) { // check if any file is selected
                    selectedFile = cfile.getSelectedFile(); // get selected file
                    selectedfilelabel.setText(cfile.getSelectedFile().getName()); // display the name of selected file
                    path = selectedFile.getAbsolutePath(); // get path of the file
                    image = true;
                } else {
                    selectedfilelabel.setText("檔案未選擇");
                }
            }
        }
    }
}

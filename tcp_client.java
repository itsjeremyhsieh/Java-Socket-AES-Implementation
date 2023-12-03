import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.util.Base64;

public class tcp_client {
    static JTextField myServer = new JTextField();
    static JTextField myPort = new JTextField();
    static JButton connect = new JButton("連線");
    static JTextPane chat = new JTextPane();
    static JTextPane tosend = new JTextPane();
    static JButton pic = new JButton("圖片");
    static JButton send = new JButton("傳送");
    static JButton end = new JButton("結束連線");
    static String toprint = "";
    static DataInputStream input;
    static DataInputStream input1;
    static DataOutputStream output;
    static DataOutputStream output1;
    static InetAddress server_ip;
    static String path;
    static File selectedFile;
    static Boolean image = Boolean.valueOf(false);
    static Socket sc;
    static Socket sc1;
    static JLabel selectedfilelabel = new JLabel();
    static int imgcnt = 0;

    public static void main(String[] args) throws Exception {
        JFrame frm = new JFrame("TCP Client");
        frm.setLayout(null);
        Container ctp = frm.getContentPane();
        ctp.setLayout(null);

        JLabel Server_label = new JLabel("Server IP: ");
        JLabel Server_port = new JLabel("Server Port: ");
        JScrollPane jsp = new JScrollPane(); // add scrollbar for chat and text area
        JScrollPane jsp2 = new JScrollPane();
        Server_label.setBounds(30, 30, 60, 20);
        myServer.setBounds(100, 30, 100, 20);
        Server_port.setBounds(220, 30, 80, 20);
        myPort.setBounds(300, 30, 60, 20);
        connect.setBounds(380, 30, 100, 20);
        connect.addActionListener(new Actlis());
        pic.addActionListener(new Actlis());
        // chat.setBounds(30, 80, 450, 600);
        jsp.setBounds(30, 80, 450, 600);
        jsp.setViewportView(chat);
        jsp2.setBounds(30, 700, 450, 60);
        jsp2.setViewportView(tosend);
        // tosend.setBounds(30, 700, 450, 60);
        pic.setBounds(30, 780, 100, 30);
        send.setBounds(200, 780, 100, 30);
        send.addActionListener(new Actlis());
        end.setBounds(380, 780, 100, 30);
        chat.setEditable(false);
        chat.setContentType("text/html");
        chat.setText("請輸入Server IP 與 port號（10000）"); // set default chat text
        selectedfilelabel.setBounds(30, 810, 300, 30);
        end.addActionListener(new Actlis());
        ctp.add(Server_label);
        ctp.add(Server_port);
        ctp.add(myServer);
        ctp.add(myPort);
        ctp.add(connect);
        ctp.add(jsp);
        ctp.add(jsp2);
        ctp.add(pic);
        ctp.add(send);
        ctp.add(end);
        ctp.add(selectedfilelabel);
        frm.setSize(550, 900);
        frm.setLocation(500, 150);
        frm.setVisible(true);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    static public class rcvmsg extends Thread { // Thread for receiving messages
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
                        // str = str.replaceAll("\n", "<br>"); // normal text message, replace enter
                        // with <br>
                        // System.out.println(str);
                        byte[] plaintext = decryptGCM(Base64.getDecoder().decode(str), key.getBytes(), iv.getBytes(),
                                aad.getBytes(), tagLength);
                        // System.out.println(new String(plaintext));
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

    static String img = "";

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
                    // System.out.println("can rsv");
                    BufferedInputStream in;
                    InputStream ins = sc1.getInputStream();
                    in = new BufferedInputStream(ins);

                    ByteArrayOutputStream buf = new ByteArrayOutputStream();

                    byte[] plaintext1 = decryptGCM(in.readAllBytes(), key.getBytes(), iv.getBytes(),
                            aad.getBytes(), tagLength);
                    buf.write(plaintext1); // write to buffer
                    System.out.println("received");
                    OutputStream out = new FileOutputStream(new File("imgc" + imgcnt + ".jpg")); // Save image as file,
                                                                                                 // filename = imgs +
                                                                                                 // imgcnt + .jpg
                    buf.writeTo(out);
                    out.close();
                    sc1.shutdownInput();
                    // in.close();
                    in = null;
                    String img = "<img src= 'file:imgc" + imgcnt + ".jpg' width = '200'> <br>"; // display image on chat
                                                                                                // area using image
                                                                                                // saved previously
                    toprint += "Server IP: <br>" + img;
                    imgcnt++;
                    chat.setText(toprint);
                    chat.setCaretPosition(chat.getDocument().getLength()); // scroll to bottom of chat area
                                                                           // automatically
                } catch (Exception e) {
                    // System.out.println(e.toString() + "Client rcv");
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
                    msg = msg.replaceAll("\n", "<br>");
                    if (!msg.equals("")) {
                        byte[] ciphertext = encryptGCM(msg.getBytes(), key.getBytes(), iv.getBytes(), aad.getBytes(),
                                tagLength);

                        // System.out.println("GCM 模式加密結果（Base64）：" +
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
                image = false; // restore default image value
                String img = "<img src= 'file:\\" + path + "' width = '200'> <br>"; // display image on chat area using
                                                                                    // html syntax
                // toprint += "Server IP: ";
                toprint += "Client IP: <br>" + img;
                chat.setText(toprint);
                chat.setCaretPosition(chat.getDocument().getLength()); // scroll to bottom of chat area automatically
                try {
                    FileInputStream fis = new FileInputStream(new File(path));
                    OutputStream os = sc1.getOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(os);

                    System.out.println("ready to send.");
                    byte[] ciphertext = encryptGCM(fis.readAllBytes(), key.getBytes(), iv.getBytes(), aad.getBytes(),
                            tagLength);
                    // output.writeUTF(Base64.getEncoder().encodeToString(ciphertext));
                    bos.write(ciphertext);
                    // while ((len = fis.read(b)) != -1) {

                    // //System.out.print(new String(ciphertext));
                    // }
                    bos.flush();
                    sc1.shutdownOutput(); // must shutdown output, otherwise it won't send
                    // bos.close();
                    fis.close();
                    bos = null;
                    System.out.println("sent!");
                    selectedfilelabel.setText(""); // restore default Jlabel value
                } catch (Exception e) {
                    System.out.println(e.toString() + "Cleint send");
                }
            }
        }
    }

    static InetAddress serverIPformat;
    static int serverport;

    static public class Actlis extends WindowAdapter implements ActionListener // .addActionListener(new Actlis());
    {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }

        // 192.168.50.128

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == connect) { // if connect button clicked
                // System.out.println("run");
                try {
                    String serverIP = myServer.getText();
                    InetAddress serverIPformat = InetAddress.getByName(serverIP);
                    int serverport = Integer.parseInt(myPort.getText());
                    sc = new Socket(serverIPformat, serverport); // 10000
                    sc1 = new Socket(serverIPformat, 10001); // 10001
                    // System.out.println("success");
                    input = new DataInputStream(sc.getInputStream());
                    input1 = new DataInputStream(sc1.getInputStream());
                    output = new DataOutputStream(sc.getOutputStream());
                    output1 = new DataOutputStream(sc1.getOutputStream());
                    new rcvmsg(); // run new thread for receiving message
                    new rcvimg(); // run new thread for receiving image
                    chat.setText("連線成功！");
                    connect.setEnabled(false); // disable connect button to prevent multiple connection causing errors
                } catch (Exception eee) {
                    // TODO: handle exception
                }

            }
            if (e.getSource() == send) { // if send button clicked

                String str = tosend.getText();
                new sendmsg(str); // send message
                tosend.setText(""); // restore texting area
            } else if (e.getSource() == pic) { // if sendpic button clicked
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

            else if (e.getSource() == end) { // if end button clicked
                try {
                    new sendmsg("EOF"); // send "EOF" to server
                    Thread.sleep(500); // wait for 500 millisec to process the sending process
                    input.close(); // close everything
                    input1.close();
                    output.close();
                    output1.close();
                    sc.close();
                    sc1.close();
                    System.exit(0); // close the window
                } catch (Exception ec) {

                }
            }
        }
    }

}

package chatRun;

import chatHandler.ChatDTO;
import chatHandler.Info;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient extends JFrame implements ActionListener, Runnable {
    private JTextArea output;
    private JTextField input;
    private JButton sendBtn;
    private JScrollPane scroll;

    private Socket socket;

    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;

    private String nickName;

    public ChatClient() {
        output = new JTextArea();
        output.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        output.setEditable(false);
        scroll = new JScrollPane(output);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        input = new JTextField();

        sendBtn = new JButton("보내기");

        bottom.add("Center", input);
        bottom.add("East", sendBtn);

        Container c = this.getContentPane();
        c.add("Center", scroll);
        c.add("South", bottom);

        setBounds(300, 300, 300, 300);
        setVisible(true);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try{
                    ChatDTO dto = new ChatDTO();
                    dto.setNickName(nickName);
                    dto.setCommand(Info.EXIT);
                    oos.writeObject(dto);
                    oos.flush();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        new ChatClient().service();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try{
            String msg = input.getText();
            ChatDTO dto = new ChatDTO();

            if (msg.equals("exit")){
                dto.setCommand(Info.EXIT);
            } else {
                dto.setCommand(Info.SEND);
                dto.setMessage(msg);
                dto.setNickName(nickName);
            }
            oos.writeObject(dto);
            oos.flush();
            input.setText("");

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void service() {
        String serverIP = JOptionPane.showInputDialog(this, "서버 IP를 입력하세요", "192.168.0.1");
        if (serverIP == null || serverIP.length() == 0) {
            System.out.println("서버 IP가 입력되지 않았습니다.");
            System.exit(0);
        }

        nickName = JOptionPane.showInputDialog(this, "닉네임을 입력하세요", "닉네임", JOptionPane.INFORMATION_MESSAGE);
        if (nickName == null || nickName.length() == 0) {
            nickName = "Guest";
        }

        try {
            socket = new Socket(serverIP, 8099);
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("준비 완료");

        } catch (UnknownHostException e) {
            System.out.println("서버를 찾을 수 없습니다.");
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("서버와 연결이 안 되었습니다.");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            ChatDTO dto = new ChatDTO();
            dto.setCommand(Info.JOIN);
            dto.setNickName(nickName);
            oos.writeObject(dto);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t = new Thread(this);
        t.start();
        input.addActionListener(this);
        sendBtn.addActionListener(this);
    }

    @Override
    public void run() {
        ChatDTO dto = null;
        while (true) {
            try {
                dto = (ChatDTO) ois.readObject();
                if (dto.getCommand() == Info.EXIT) {
                    ois.close();
                    oos.close();
                    socket.close();
                    System.exit(0);
                } else if (dto.getCommand() == Info.SEND) {
                    output.append(dto.getMessage() + "\n");

                    int pos = output.getText().length();
                    output.setCaretPosition(pos);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

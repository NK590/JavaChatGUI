package chatHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ChatDAO extends Thread {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Socket socket;
    private ArrayList<ChatDAO> list;

    public ChatDAO(Socket socket, ArrayList<ChatDAO> list) throws IOException {
        this.socket = socket;
        this.list = list;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        ChatDTO dto = null;
        String nickName;
        try {
            while (true) {
                dto = (ChatDTO) ois.readObject();
                nickName = dto.getNickName();

                if (dto.getCommand() == Info.EXIT) {
                    ChatDTO sendDto = new ChatDTO();
                    sendDto.setCommand(Info.EXIT);
                    oos.writeObject(sendDto);
                    oos.flush();

                    ois.close();
                    oos.close();
                    socket.close();

                    list.remove(this);

                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage(nickName + "님 퇴장하셨습니다.");
                    broadcast(sendDto);
                    break;
                } else if (dto.getCommand() == Info.JOIN) {
                    ChatDTO sendDto = new ChatDTO();
                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage(nickName + "님 입장하셨습니다.");
                    broadcast(sendDto);
                } else if (dto.getCommand() == Info.SEND) {
                    ChatDTO sendDto = new ChatDTO();
                    sendDto.setCommand(Info.SEND);
                    sendDto.setMessage("[" + nickName + "]" + dto.getMessage());
                    broadcast(sendDto);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(ChatDTO sendDto) throws IOException {
        for (ChatDAO handler : list) {
            handler.oos.writeObject(sendDto);
            handler.oos.flush();
        }
    }
}

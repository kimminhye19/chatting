package com.example.chat2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChatThread extends Thread{
    private String name;
    private BufferedReader br;
    private PrintWriter pw;
    private Socket socket;
    List<ChatThread> list;

    public ChatThread(Socket socket, List<ChatThread> list)throws Exception{
        this.socket = socket;
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.br = br;
        this.pw = pw;
        this.name = br.readLine();
        this.list = list;
        this.list.add(this);  // 자기자신을 추가함
    }

    public void sendMessage(String msg){
        pw.println(msg);
        pw.flush();
    }

    @Override
    public void run(){
        // broadcast
        // ChatThread는 사용자가 보낸 메시지를 읽어들여서
        // 접속된 모든 클라이언트에게 메시지를 보낸다

        // 나를 제외한 모든 사용자에게 "00님이 연결되었습니다."
        // 현재 ChatThread를 제외하고 보낸다
        try{
            broadcast(name + "님이 연결되었습니다", false);

            String line = null;

            while ((line = br.readLine()) != null) {
                if("/quit".equals(line)){
                    break;
                }
                // 나를 포함한 ChatThread에게 메시지를 보낸다
                broadcast(name + " : " + line, true);
            }
        }catch (Exception ex){  // ChatThread가 연결이 끊어졌다
            ex.printStackTrace();
        }finally {
            broadcast(name + "님이 연결이 끊어졌습니다", false);
            this.list.remove(this);
            try{
                br.close();
            }catch (Exception ex){}

            try{
                pw.close();
            }catch (Exception ex){}

            try{
                socket.close();
            }catch (Exception ex){}
        }
    }

    private void broadcast(String msg, boolean includMe){
        List<ChatThread> chatThreads = new ArrayList<>();
        for(int i = 0; i < this.list.size(); i++){
            chatThreads.add(list.get(i));
        }

        try{
            for(int i=0; i < chatThreads.size(); i++){
                ChatThread ct = chatThreads.get(i);
                if(!includMe){  // 나를 포함하지 말아라
                    if(ct == this){
                        continue;
                    }
                }
                ct.sendMessage(msg);
            }
        }catch (Exception ex){
            System.out.println("///");
        }

    }
}

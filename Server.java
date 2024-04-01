package com.tv;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class Server {
    private static final Properties prop = new Properties();

    public static void main(String[] args) throws IOException {
        //下面说到的实现第n步是指在client端选择登陆时控制台中出现的语句:思考平时上网在登录时会出现的情况然后一步步完善
        ServerSocket ss=new ServerSocket(10000);
        //1.获取本地文件中正确的用户名和密码
        FileInputStream fis = new FileInputStream("D:\\BaiduNetdiskDownload\\java exercise\\TVchat\\servicedir\\userinfo.txt");
        prop.load(fis);
        fis.close();

        //2.实现第一步:来了客户端,就开一条线程处理
        while(true){
            Socket socket =ss.accept();
            System.out.println("客户端已连接");
            new Thread(new MyRunnable(socket,prop)).start();
        }



    }
}

class MyRunnable implements Runnable{
    Socket socket;
    Properties prop;
    public MyRunnable(Socket socket, Properties prop){
        this.prop=prop;
        this.socket=socket;
    }
    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));



            /*1.服务端通过 BufferedReader 从客户端接收命令字符串 command。
            2.如果 command 不为空且包含 :，则将其按 : 分割为 choice 和 orderValue 两部分。choice 表示客户端的操作类型（例如 "login"、"register"、"query"），orderValue 则是与操作相关的附加信息（例如要查询的用户名）。
            3.如果 command 不包含 :，则将 command 整个作为 choice。*/

            while(true){

                String command = br.readLine();
                String orderValue = null;
                String choice = null;
                System.out.println("==============="+command);
                if(command != null && command.indexOf(":")>0){
                    String[] cmdArgs = command.split(":");
                    choice = cmdArgs[0];
                    orderValue = cmdArgs[1];
                }else{
                    choice = command;
                }
                System.out.println("order==============="+choice);
                switch(choice){
                    case "login"-> login(br);
                    case "register"-> register(br);
                    case "query" -> {
                        String result = query(orderValue,socket);
                        write(result, socket);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //write方法用于将消息写回客户端，它通过 BufferedWriter 将消息写入到客户端的输出流中。
    private void write(String message, Socket socket){

        try {
            System.out.println("写回数据:"+message);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write(message);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    //查询结果通过 write() 方法发送回客户端。
//    private String query(String userName) throws IOException {
//        System.out.println("query======="+userName);
//        String passwd = prop.getProperty(userName);
//        if(passwd != null){
//            return passwd;
//        }else{
//            return "没有找到相关用户信息";
//        }
//    }
    private String query(String userName, Socket socket) throws IOException {
        String passwd = prop.getProperty(userName);
        if (passwd != null) {
            write(" " + passwd, socket);
            BufferedReader br = null;
            String modifyChoice = br.readLine();
            if ("modify_username".equalsIgnoreCase(modifyChoice)) {
                String newUsername = br.readLine();
                if (prop.containsKey(newUsername)) {
                    write("用户名已存在，请重新输入", socket);
                } else {
                    String oldPasswd = prop.getProperty(userName);
                    prop.remove(userName);
                    prop.setProperty(newUsername, oldPasswd);
                    write("用户名修改成功", socket);
                    // 这里你可能还需要将prop持久化到文件或数据库
                }
            } else if ("modify_password".equalsIgnoreCase(modifyChoice)) {
                String newPasswd = br.readLine();
                prop.setProperty(userName, newPasswd);
                write("密码修改成功", socket);
                // 这里你可能还需要将prop持久化到文件或数据库
            } else {
                write("无效的选择，请输入'modify_username'或'modify_password'", socket);
            }
            return null; // 不需要返回密码，因为已经写回客户端了
        } else {
            return "没有找到相关用户信息";
        }
    }

    //登陆代码很多,写个方法(判断和接收信息)让前面去调用即可
    public void login(BufferedReader br) throws IOException {
        //实现第三步:增加"用户选择了登陆操作"这句话
        System.out.println("用户选择了登陆操作");
        String userinfo = br.readLine();
        //实现第四步:将username=xxx&password=xxx用户号码变成为:...密码为:...
        //格式:username=xxx&password=xxx;
        String[] userinfoArr = userinfo.split("&");
        //0索引是username=xxx 1索引是password=xxx
        String usernameInput = userinfoArr[0].split("=")[1];
        String passwordInput = userinfoArr[1].split("=")[1];
        System.out.println("账号为:"+usernameInput);
        System.out.println("密码为:"+passwordInput);
        //开始判断用户名是否正确
        if(prop.containsKey(usernameInput)){
            //用户名存在
            //继续判断密码是否正确
            String rightPassword = (String)prop.get(usernameInput);
            if(rightPassword.equals(passwordInput)){
                //第五步:出现"用户登陆成功,可以开始聊天"
//                rewriteMessage("登陆成功,请开始聊天");
                rewriteMessage("1");

                //开始写聊天,1.接收客户端的消息并打印
                //抽出方法:一个方法写一个功能
                //2nd:没写调用
                chatToAll(br,usernameInput);
            }else{
                //密码错误

                rewriteMessage("2");
            }

        }else{
            //用户名不存在
            //第五步:出现"用户名不存在,请重新输入或注册"
//            rewriteMessage("用户名不存在,请重新输入或注册");
            rewriteMessage("3");
            //当出现密码错误和或者用户名不存在时,会导致:login方法的结束->switch的结束->使用一个死循环包裹switch
        }

    }

    // 添加注册方法
    public void register(BufferedReader br) throws IOException {
        System.out.println("用户选择了注册操作");
        String userinfo = br.readLine();
        String[] userinfoArr = userinfo.split("&");
        String usernameInput = userinfoArr[0].split("=")[1];
        String passwordInput = userinfoArr[1].split("=")[1];

        // 判断用户名是否已经存在
        if (prop.containsKey(usernameInput)) {
            rewriteMessage("4"); // 发送用户名已存在的消息给客户端
        } else {
            // 将用户名和密码存储到 Properties 中
            prop.setProperty(usernameInput, passwordInput);
            FileOutputStream fos = new FileOutputStream("D:\\BaiduNetdiskDownload\\java exercise\\TVchat\\servicedir\\userinfo.txt");
            prop.store(fos, "User Information");
            fos.close();
            rewriteMessage("5"); // 发送注册成功的消息给客户端
        }
    }


    private void chatToAll(BufferedReader br, String username) throws IOException {
        while(true){
            String message = br.readLine();
            System.out.println(username+"发送了:"+message);
        }
    }

    //写一个方法:当用户不存在时,向服务器回写
    public void rewriteMessage(String message) throws IOException {

        //获取输出流
        BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bw.write(message);
        bw.newLine();
        bw.flush();

    }



}



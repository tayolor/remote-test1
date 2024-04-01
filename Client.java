package com.tv;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        //此时记得抛出异常(alt+回车)
        Socket socket =new Socket("127.0.0.1",10000);
        System.out.println("服务器连接成功");
        while(true){
            //进行用户选择:使用switch结构
            System.out.println("***********Welcome to TVchat************");
            System.out.println("choice1:login");
            System.out.println("choice2:register");
            System.out.println("choice3:query and update personal information");
            System.out.println("please enter your chioce:");
            //Scanner sc = new Scanner(System.in);
            String choice=sc.nextLine();
            switch(choice){
//              //这里的小箭头后面原本是sout("用户选择了login"),但是在后面添加单独的方法能够提高阅读性
                case"1"-> login(socket);
                case"2"-> register(socket);
                case "3"-> queryInfo(socket);
                default -> System.out.println("无此选项");
            }
        }
    }

    //登录方法
    public static void login(Socket socket) throws IOException {
        //获取输出流
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //客户端写出用户名和密码,通过socket获取流
        //因为要重复调用scanner 不想重复定义scanner的话可以把while循环中的scanner调到成员位置
        //Scanner sc=new Scanner(System.in);

        //键盘录入
        System.out.println("please enter the username");
        String username=sc.nextLine();
        System.out.println("please enter the password");
        String password=sc.nextLine();

        //拼接
        StringBuilder sb=new StringBuilder();
        sb.append("username=").append(username).append("&password=").append(password);

        //1.执行登陆操作

        bw.write("login");
        bw.newLine();
        bw.flush();
        //2.往服务器写出用户号码和密码

        bw.write(sb.toString());
        bw.newLine();
        bw.flush();

        //需要socket来接收信息,向上修改
        //获取输入流
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message = br.readLine();
        System.out.println(message);
        //用字符串来判断太麻烦,规定1表示登陆成功,2表示密码有误,3表示用户名不存在
        if("1".equals(message)){
            System.out.println("登陆成功,请开始聊天");
            //往外写,选择bw
            chatToAll(bw);


        }else if("2".equals(message)){
            System.out.println("密码错误,请重新输入");
        }else if("3".equals(message)){
            System.out.println("账号不存在,请重新输入或注册");

        }

    }
    //注册方法
    public static void register(Socket socket) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        System.out.println("请输入用户名:");
        String newUsername = sc.nextLine();
        System.out.println("请输入密码:");
        String newPassword = sc.nextLine();

        //拼接用户名和密码
        StringBuilder registerInfo = new StringBuilder();
        registerInfo.append("username=").append(newUsername).append("&password=").append(newPassword);

        //向服务器发送注册请求
        bw.write("register");
        bw.newLine();
        bw.flush();

        //发送注册信息给服务器
        bw.write(registerInfo.toString());
        bw.newLine();
        bw.flush();

        //接收服务器返回的注册结果
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String registerMessage = br.readLine();
        System.out.println(registerMessage);

        //4:用户名已存在 5:注册成功
        if("4".equals(registerMessage)){
            System.out.println("用户名已存在");
        }else if("5".equals(registerMessage)){
            System.out.println("注册成功");
        }
    }
    //聊天方法
    private static void chatToAll(BufferedWriter bw) throws IOException {
        while(true){
            System.out.println("请输入:");
            String str = sc.nextLine();
            //把聊天内容write给服务器
            bw.write(str);
            bw.newLine();
            bw.flush();
        }
    }


    //查询
    public static void queryInfo(Socket socket) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        System.out.println("请输入要查询的用户名:");
        String queryUsername = sc.nextLine();


    //执行查询操作+往服务器回写
        bw.write("query:"+queryUsername);
        bw.newLine();
        bw.flush();

        //bug1:这里与登录和注册的方法不同:在查询操作中要将把query和queryUsername组装在一起,如果分开的话是接受不到的
//        bw.write(queryUsername);
//        bw.newLine();
//        bw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String queryResult = null;


            queryResult = br.readLine();
//            帮助调试System.out.println("queryResult:"+queryResult);
            if(queryResult != null){
                System.out.println("对应的密码是:"+queryResult);
                if(queryResult.startsWith("Username:")){
                    System.out.println("对应的密码是:"+ queryResult);
               }
//                else {
//                    System.out.println(queryResult);
//                }
            }
        }

}

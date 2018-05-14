
//发送消息的线程
//在此线程中停止接收方法
private final static class ChatSendThread implements Runnable {
    private DatagramSocket ds;
    private SocketAddress socketAddress;
    private String senderID;
    private String anotherID;
    private byte nature;
    private String sendTime;
    private String messsage;
    private DatagramPacket dp = null;
    private DatagramPacket recevdp = null;
    private byte[] receibytes = new byte[1024 * 6];
    private byte[] bytes = null;
    private ChatMessage chatMessage;
    private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    public ChatSendThread(DatagramSocket messageds, SocketAddress messageSocketAddress, String senderID, String anotherID, byte nature, String sendTime, String message) {
        this.ds = messageds;
        this.socketAddress = messageSocketAddress;
        this.senderID = senderID;
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.messsage = message;
        this.chatMessage = new ChatMessage(senderID, anotherID, nature, sendTime, message);
    }
    @Override
    public void run() {
        String content = gson.toJson(chatMessage);
        bytes = ("Chat/" + content).getBytes();
        dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



private static class WaitThread implements Runnable {
    private Thread thread;

    public WaitThread(Thread thread) {
        this.thread = thread;
    }

    @Override
    public void run() {
        synchronized (thread) {
            try {
                thread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}



private static class NotifyThread implements Runnable {
    private Thread thread;

    public NotifyThread(Thread thread) {
        this.thread = thread;
    }

    @Override
    public void run() {
        synchronized (thread) {
            thread.notify();
        }
    }
}



private static class ChatLocalSendTask implements Callable<Boolean> {
    private DatagramSocket ds;
    private String senderID;
    private String anotherID;
    private SocketAddress socketAddress;
    private byte nature;
    private String sendTime;
    private String messsage;
    private DatagramPacket dp = null;
    private byte[] bytes = null;
    private ChatMessage chatMessage;
    private String address;
    private String addressList;
    private Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    public ChatLocalSendTask(DatagramSocket messageds, String senderID, String anotherID, byte nature, String sendTime, String message, String address, String addressList) {
        this.ds = messageds;
        this.senderID = senderID;
        this.anotherID = anotherID;
        this.nature = nature;
        this.sendTime = sendTime;
        this.messsage = message;
        this.address = address;
        this.addressList = addressList;
        this.chatMessage = new ChatMessage(senderID, anotherID, nature, sendTime, message);
    }

    @Override
    public Boolean call() throws Exception {
            /*Map<String,String> parameters=new HashMap<String, String>();
            parameters.put("senderID",senderID);
            parameters.put("anotherID",anotherID);
            Request request=new Request(LoginClient.URL_ADDRESS+"/GetLocalAddress",parameters,RequestProperty.APPLICATION);
            String addressList=request.doPost();
            String address=addressList.split("/")[1];*/
        String ip = address.split(",")[0];
        int port = Integer.parseInt(address.split(",")[1]);

        socketAddress = new InetSocketAddress(ip, port);
        String content = gson.toJson(chatMessage);
        bytes = ("LocalChat/" + addressList + "/" + content).getBytes();
        dp = new DatagramPacket(bytes, 0, bytes.length, socketAddress);
        try {
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] receby = new byte[1024 * 6];
        dp = new DatagramPacket(receby, 0, receby.length);
        try {
            do {
                ds.receive(dp);
            } while (!(new String(dp.getData(), 0, dp.getLength()).startsWith("CallLocalBack")));
            System.out.println("成功回收数据!局域网内信息发送成功!" + new String(dp.getData(), 0, dp.getLength()));

            /**局域网发送的数据(暂时没有设计)无法存到 数据库/历史消息 里**/
            /**contacts.get(anotherID).setTheLatestText(messsage);
             contacts.get(anotherID).setTheLatestTextTime(sendTime);**/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}



/*private static class FileListenerThread implements Runnable {
        private DatagramSocket ds;
        private SocketAddress fileSocketAddress;
        private byte[] by;
        private DatagramPacket dp;
        private String message;

        public FileListenerThread(DatagramSocket fileds, SocketAddress fileSocketAddress) {
            this.ds = fileds;
            this.fileSocketAddress = fileSocketAddress;
            this.by = new byte[1024 * 8];
        }

        @Override
        public void run() {
            System.out.println("等待接收...");
            while (true) {
                by = new byte[1024 * 8];
                dp = new DatagramPacket(by, 0, by.length);
                try {
                    ds.get(dp);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                *//**
 * ------------------------------------对收到的dp包进行各种情况的讨论--------------------------------
 * **//*

                message = new String(dp.getData(), 0, dp.getLength());
                if (message.startsWith("SendFile")) {
                    *//**接收到在线发送文件的请求**//*
                    ResponseThread responseThread = new ResponseThread(dp, ds);
                    Thread thread = new Thread(responseThread);
                    thread.start();
                }
            }
        }

        private class ResponseThread implements Runnable {
            private DatagramPacket dp;
            private DatagramSocket ds;
            private String message;
            private FileMessage fileMessage;

            public ResponseThread(DatagramPacket dp, DatagramSocket ds) {
                this.dp = dp;
                this.ds = ds;
                this.message = new String(dp.getData(), 0, dp.getLength());
                Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                Type type = new TypeToken<FileMessage>() {
                }.getType();
                this.fileMessage = gson.fromJson(message.substring(9, message.length()), type);
            }

            @Override
            public void run() {
                System.out.println("\n在线发送请求:来自" + fileMessage.getSenderID() + "\t昵称" + fileMessage.getSenderNickName()
                        + "\t文件名" + fileMessage.getFileName() + "\t文件大小" + fileMessage.getFileSize() + "\n");
                System.out.println("\n是否同意接收?(T/F)");
                Scanner scanner = new Scanner(System.in);
                String ifAgree = scanner.next();  *//**同意或拒绝**//*

 *//**可以在任何时候接收或拒绝**//*
                while (!ifAgree.equals("T") && !ifAgree.equals("F")) {
                    System.out.println("输入格式错误...");
                    System.out.println("\n是否同意接收?(T/F)");
                    ifAgree = scanner.next();  *//**同意或拒绝**//*
                }

                if (ifAgree.equals("T")) {
                    //System.out.println("\n请输入文件在您PC上的全路径及文件名(包括后缀) (PS:路径可以有中文、但上传的文件本身不能含中文! 不能发送文件夹!):");
                    System.out.println("\n请输入您的存储路径(目标文件夹全路径):");
                    String save_path = scanner.nextLine();
                    save_path += "\\" + fileMessage.getFileName();
                    FileReceiver fileReceiver = new FileReceiver(save_path, fileMessage.getSenderAddress(), fileMessage.getReceiverAddress(), ds);
                    */

/**
 * 暂停主接收?
 **//*

                    fileReceiver.get();
                }
            }
        }
    }*/
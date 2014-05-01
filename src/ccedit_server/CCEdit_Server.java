/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccedit_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Basti
 */
public class CCEdit_Server {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean ok;
        int port;
        String temp;
        Properties settings = new Properties();
        if (!new File("CCEdit.cfg").isFile()) {
            try {
                FileOutputStream fos = new FileOutputStream("CCEdit.cfg");
                try {
                    settings = new Properties();
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    System.out.println("Which port should CCEdit use?");
                    ok = false;
                    port = 25568;
                    while (!ok) {
                        try {
                            port = Integer.parseInt(br.readLine());
                            if (port >= 1024 && port <= 65535) {
                                ok = true;
                            } else {
                                System.out.println("Port cannot be a system port!");
                            }
                        } catch (NumberFormatException ex) {
                            ok = false;
                            System.out.println("Which port should CCEdit use?");
                        }
                    }
                    br.close();
                    settings.put("port", String.valueOf(port));
                    settings.store(fos, null);
                    fos.flush();
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(CCEdit_Server.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                } finally {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        Logger.getLogger(CCEdit_Server.class.getName()).log(Level.SEVERE, null, ex);
                        System.exit(1);
                    }
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        try {
            settings.clear();
            FileInputStream fis = new FileInputStream("CCEdit.cfg");
            settings.load(fis);
            if (settings.containsKey("port")) {
                port = Integer.parseInt(settings.getProperty("port"));
                System.out.println("Starting on port " + String.valueOf(port));
            } else {
                port = 25568;
                System.out.println("Starting on default port (25568)");
            }

            String mode, computer, file, password, content, ret;
            String[] files;
            int i;
            StackTraceElement[] ste;
            BufferedReader pwr, fr;
            BufferedWriter fw;
            Socket s;
            BufferedReader br;
            ServerSocket ssocket = new ServerSocket(port);
            System.out.println("Press CTRL-C to quit");
            while (true) {
                try {
                    s = ssocket.accept();
                    s.setSoTimeout(512);
                    System.out.println(s.getInetAddress());
                    ret = "Nothing happened!";
                    //TODO: Get mode, computer, file, and password from client
                    br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    mode = br.readLine();
                    computer = br.readLine();
                    file = br.readLine();
                    password = br.readLine();
                    content = "";
                    while (!"\u001A".equals(temp = br.readLine())) {
                        if (!"".equals(content)) {
                            content = content + "\n" + temp;
                        } else {
                            content = temp;
                        }
                        System.out.println(temp);
                    }

                    System.out.println("Finished reading");
                    if (!"put".equals(mode) && !"mv".equals(mode)) {
                        ok = mode != null && computer != null && file != null && password != null;
                    } else {
                        ok = mode != null && computer != null && file != null && password != null && content != null;
                    }
                    //Process input...
                    if (ok) {
                        if (new File(computer + "/passwd").isFile()) {
                            pwr = new BufferedReader(new FileReader(new File(computer + "/passwd")));
                            try {
                                if (password.equals(pwr.readLine())) {
                                    if ("get".equals(mode)) {
                                        if (new File(computer + "/" + file).isFile()) {
                                            fr = new BufferedReader(new FileReader(new File(computer + "/" + file)));
                                            ret = "";
                                            while ((temp = fr.readLine()) != null) {
                                                if ("".equals(ret)) {
                                                    ret = temp;
                                                } else {
                                                    ret = ret + "\n" + temp;
                                                }
                                            }
                                        } else {
                                            ret = "Error: File doesn't exist!";
                                        }
                                    }
                                    if ("put".equals(mode)) {
                                        if (!new File(computer + "/" + file).isDirectory()) {
                                            fw = new BufferedWriter(new FileWriter(new File(computer + "/" + file)));
                                            fw.write(content);
                                            fw.close();
                                            ret = "File created!";
                                        } else {
                                            ret = "Error: There is already a directory with the same name!";
                                        }
                                    }
                                    if ("ls".equals(mode)) {
                                        if (new File(computer + "/" + file).isDirectory()) {
                                            files = new File(computer + "/" + file).list();
                                            i = 0;
                                            ret = "";
                                            while (i < files.length) {
                                                if (new File(computer + "/" + file + "/" + files[i]).isDirectory()) {
                                                    if (i == 0) {
                                                        ret = "(dir)" + files[i];
                                                    } else {
                                                        ret = ret + "\n(dir)" + files[i];
                                                    }
                                                } else {
                                                    if (i == 0) {
                                                        ret = "(file)" + files[i];
                                                    } else {
                                                        ret = ret + "\n(file)" + files[i];
                                                    }
                                                }
                                                i++;
                                            }
                                        } else {
                                            ret = "Error: No such directory!";
                                        }
                                    }
                                    if ("rm".equals(mode)) {
                                        if (new File(computer + "/" + file).delete()) {
                                            ret = "File deleted!";
                                        } else {
                                            ret = "Error: Couldn't delete file!";
                                        }
                                    }
                                    if ("mv".equals(mode)) {
                                        if (new File(computer + "/" + file).renameTo(new File(computer + "/" + content))) {
                                            ret = "File " + file + " moved to " + content;
                                        } else {
                                            ret = "Error: Couldn't move file " + file + " to " + content + "!";
                                        }
                                    }
                                    if ("mkdir".equals(mode)) {
                                        if (new File(computer + "/" + file).mkdirs()) {
                                            ret = "Directory created!";
                                        } else {
                                            ret = "Error: Couldn't create directory!";
                                        }
                                    }
                                    if ("yenon".equals(mode)) {
                                        ret = "yenon threw a BrainException at you!";
                                    }
                                } else {
                                    ret = "Error: Passwords don't match!";
                                }
                            } catch (IOException ex) {
                                //Send StackTrace to Client
                                i = 0;
                                ste = ex.getStackTrace();
                                ret = "An Error occured!\nPlease send this to bhogm4@gmail.com:";
                                while (i < ste.length) {
                                    ret = ret + "\n" + ste[i].toString();
                                }
                            } finally {
                                try {
                                    pwr.close();
                                } catch (IOException ex) {
                                    i = 0;
                                    ste = ex.getStackTrace();
                                    ret = "An Error occured!\nPlease send this to bhogm4@gmail.com:";
                                    while (i < ste.length) {
                                        ret = ret + "\n" + ste[i].toString();
                                    }
                                }
                            }
                        } else {
                            ret = "Error: Create passwd on computer " + computer + " to access files!";
                        }
                    } else {
                        ret = "Error: Request incomplete!";
                    }
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                    System.out.println(ret);
                    bw.write(ret + "\n");
                    bw.write("\u001A\n");
                    bw.flush();
                    br.close();
                    bw.close();
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(CCEdit_Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
}

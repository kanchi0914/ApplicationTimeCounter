import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class ApplicationTimeCounter {

    public ApplicationTimeCounter() throws IOException, AWTException {

        Map<String, Integer> applicationMinutes = new HashMap<String, Integer>();
        String dataPath = System.getProperty("user.dir") + "/data/data.csv";

        //load file
        try{
            File file = new File(dataPath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null){
                String[] parsedLine = line.split(",", 0);
                applicationMinutes.put(parsedLine[0], Integer.parseInt(parsedLine[1]));
            }
            br.close();

        }catch (IOException e) {
            System.out.println(e);
        }

        String dir = System.getProperty("user.dir");
        System.out.println(dir);
        Image image = ImageIO.read(new File(dir + "/icon/timerIcon.png"));
        final TrayIcon icon = new TrayIcon(image, "ApplicationTimeCounter");
        icon.setImageAutoSize(true);

        PopupMenu menu = new PopupMenu();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menu.add(exitItem);
        icon.setPopupMenu(menu);

        SystemTray.getSystemTray().add(icon);

        Timer timer = new Timer(false);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                for (String key: applicationMinutes.keySet()) {

                    String commandFilePath = System.getProperty("user.dir") + "/batchFile/" + key + ".bat";
                    String command = "cmd.exe /c" + commandFilePath;
                    Runtime runtime = Runtime.getRuntime();
                    Process p = null;

                    //execute command
                    try {
                        p = runtime.exec(command);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    InputStream is = p.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    ArrayList<String> list = new ArrayList<String>();
                    while (true){
                        try{
                            String line = br.readLine();
                            if (line == null){
                                break;
                            }else{
                                list.add(line);
                            }
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }

                    //if application is running
                    if (list.size() > 2){
                        int preTime = applicationMinutes.get(key);
                        applicationMinutes.put(key, preTime + 1);
                    }
                }

                try{
                    FileWriter fw = new FileWriter(dataPath, false);
                    PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

                    for (String key : applicationMinutes.keySet()){
                        pw.print(key);
                        pw.print(",");
                        pw.print(applicationMinutes.get(key));
                        pw.println();
                    }

                    pw.close();

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };

        //every minute
        timer.schedule(task, 1 * 60 * 1000, 1 * 60 * 1000);

    }

    public static void main(String[] args) throws Exception {
        new ApplicationTimeCounter();
    }
}
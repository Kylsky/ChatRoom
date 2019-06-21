package NettyStudy.netty.Version4;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientFrame extends Frame {
    Button btnStart = new Button("start");
    public static ClientFrame INSTANCE = new ClientFrame();
    TextArea ta;
    private TextField tf;
    private Client client;
    private ClientFrame() {
//        Panel panel = new Panel(new GridLayout());
        ta = new TextArea();
        tf = new TextField();
        setVisible(true);
        setSize(600, 400);
        setLocation(100, 20);
        this.add(btnStart,BorderLayout.NORTH);
//        add(ta, BorderLayout.CENTER);
//        add(tf, BorderLayout.SOUTH);

        tf.addActionListener((e) -> {
            try {
                client.sendMsg(tf.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            tf.setText("");
        });
        Thread thread = new Thread(()->{
            client = new Client(this);
            client.start();
        });

        thread.setDaemon(true);
        thread.start();


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    client.closeConnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    public void updateText(String text){
        this.ta.setText(text);
    }

    public static void main(String[] args) {
        ClientFrame clientFrame = ClientFrame.INSTANCE;
//        clientFrame.setVisible(true);

    }
}

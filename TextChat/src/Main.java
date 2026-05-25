import javax.swing.SwingUtilities;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Main {
    public static void main(String[] args) {
        try {
            //Кодировка для консоли 
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            System.err.println("Ошибка установки кодировки: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            ConnectionForm1 connectForm = new ConnectionForm1();
            connectForm.setVisible(true);
        });
    }
}
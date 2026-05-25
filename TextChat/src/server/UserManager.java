package server;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

    //Менеджер пользователей: регистрация, вход, хранение данных
    public class UserManager {
    private final ConcurrentHashMap<String, String> users; // логин -> пароль
    private final String userFile = "users.dat";
    private final Object fileLock = new Object();

    public UserManager() {
        this.users = new ConcurrentHashMap<>();
        loadUsersFromFile();
    }

    //Проверка логина и пароля
    public boolean validateLogin(String login, String password) {
        String storedPassword = users.get(login);
        return storedPassword != null && storedPassword.equals(password);
    }

    //Регистрация нового пользователя
    public boolean registerUser(String login, String password) {
        if (users.containsKey(login)) {
            return false;
        }
        users.put(login, password);
        saveUsersToFile();
        return true;
    }

    //Получение всех логинов
    public List<String> getAllLogins() {
        return new ArrayList<>(users.keySet());
    }

    //Загрузка пользователей из файла
    private void loadUsersFromFile() {
        synchronized (fileLock) {
            File file = new File(userFile);
            if (!file.exists()) return;
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int pos = line.indexOf(':');
                    if (pos != -1) {
                        String login = line.substring(0, pos);
                        String password = line.substring(pos + 1);
                        users.put(login, password);
                    }
                }
            } catch (IOException e) {
                System.err.println("Ошибка загрузки пользователей: " + e.getMessage());
            }
        }
    }

    //Сохранение пользователей в файл
    private void saveUsersToFile() {
        synchronized (fileLock) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(userFile))) {
                for (Map.Entry<String, String> entry : users.entrySet()) {
                    writer.println(entry.getKey() + ":" + entry.getValue());
                }
            } catch (IOException e) {
                System.err.println("Ошибка сохранения пользователей: " + e.getMessage());
            }
        }
    }
}
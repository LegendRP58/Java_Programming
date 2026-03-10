package com.mycompany.lab4;

import java.io.*;
import java.util.LinkedList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.Reader;
import java.io.Writer;

public class FileManager { 
    // Сохранение в текстовый файл
    public static void saveToTextFile(LinkedList<RecIntegral> records, File file) throws IOException, IntegralException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // заголовок
            writer.write("Step;UpperLimit;LowerLimit;Result");
            writer.newLine();
            
            for (RecIntegral record : records) {
                // проверка(валидация перед сохранением)
                validateRecord(record);
                String line = String.format("%.6f;%.6f;%.6f;%.6f", record.getStep(), record.getUpperLimit(), record.getLowerLimit(), record.getResult()); writer.write(line); writer.newLine();
            }
        }
    }
    
    // Загрузка из текстового файла
    public static LinkedList<RecIntegral> loadFromTextFile(File file) throws IOException, IntegralException {
        LinkedList<RecIntegral> records = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;     
            // пропуск заголовока
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue; // пропуск пустых строк 
                
                String[] parts = line.split(";"); // разбив строки на массив подстрок
                if (parts.length != 4) {
                    throw new IntegralException(
                        String.format("Неверный формат строки %d: ожидается 4 значения, получено %d", lineNumber, parts.length));
                }
                
                try {
                    double step = Double.parseDouble(parts[0].replace(',', '.'));
                    double upperLimit = Double.parseDouble(parts[1].replace(',', '.'));
                    double lowerLimit = Double.parseDouble(parts[2].replace(',', '.'));
                    double result = Double.parseDouble(parts[3].replace(',', '.'));
                    
                    // Валидация данных
                    validateValues(step, upperLimit, lowerLimit);
                    
                    RecIntegral record = new RecIntegral(step, upperLimit, lowerLimit, result);
                    records.add(record);
                    
                } catch (NumberFormatException e) {
                    throw new IntegralException(String.format("Ошибка парсинга числа в строке %d: %s", lineNumber, e.getMessage()));
                }
            }
        }
        return records;
    }
    
    // сохранение в бинарный файл (сериализация)
    public static void saveToBinaryFile(LinkedList<RecIntegral> records, File file) throws IOException, IntegralException {
        // Валидация записей перед сохранением
        for (RecIntegral record : records) {
            validateRecord(record);
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {oos.writeObject(records);
        }
    }
    
    // Загрузка из бинарного файла (десериализация)
    @SuppressWarnings("unchecked")
    public static LinkedList<RecIntegral> loadFromBinaryFile(File file) throws IOException, ClassNotFoundException, IntegralException {
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            
            Object obj = ois.readObject(); // десереализация
            
            if (!(obj instanceof LinkedList)) {
                throw new IntegralException("Файл содержит неверный тип данных");
            }
            
            LinkedList<RecIntegral> records = (LinkedList<RecIntegral>) obj;
            // Валидация загруженных данных
            for (RecIntegral record : records) {
                validateRecord(record);
            }
            return records;
        }
    }
    
    // методы валидации
    public static void validateRecord(RecIntegral record) throws IntegralException {validateValues(record.getStep(), record.getUpperLimit(), record.getLowerLimit());
        // Проверка соотношения пределов
        if (record.getLowerLimit() >= record.getUpperLimit()) {
            throw new IntegralException(
                String.format("Нижний предел (%.6f) должен быть меньше верхнего (%.6f)", record.getLowerLimit(), record.getUpperLimit()));
        }
        // Проверка шага
        if (record.getStep() <= 0) {
            throw new IntegralException("Шаг должен быть положительным числом");
        }
        
        if (record.getStep() > record.getUpperLimit() - record.getLowerLimit()) {
            throw new IntegralException(
                String.format("Шаг (%.6f) больше разницы пределов (%.6f)", record.getStep(), record.getUpperLimit() - record.getLowerLimit()));
        }
    }
    
    public static void validateValues(double step, double upperLimit, double lowerLimit) 
            throws IntegralException {
        if (step < RecIntegral.MIN_VALUE || step > RecIntegral.MAX_VALUE) {
            throw new IntegralException("Шаг вне допустимого диапазона", "step", step);
        }
        if (upperLimit < RecIntegral.MIN_VALUE || upperLimit > RecIntegral.MAX_VALUE) {
            throw new IntegralException("Верхний предел вне допустимого диапазона", "upperLimit", upperLimit);
        }
        if (lowerLimit < RecIntegral.MIN_VALUE || lowerLimit > RecIntegral.MAX_VALUE) {
            throw new IntegralException("Нижний предел вне допустимого диапазона", "lowerLimit", lowerLimit);
        }
        if (lowerLimit > upperLimit) {
            throw new IntegralException("Нижний предел не может быть больше верхнего", "lowerLimit", lowerLimit);
        }
        if (step > upperLimit-lowerLimit) {
            throw new IntegralException("Шаг не может быть больше разницы верхнего и нижнего пределов", "step", step);
        }
    }

    // Сохранение в JSON файл
    public static void saveToJsonFile(LinkedList<RecIntegral> records, File file) throws IOException, IntegralException {
        // Валидация записей перед сохранением
        for (RecIntegral record : records) {
            validateRecord(record);
        }
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()  // форматирование для вида
            .setVersion(1.0)      // Версионирование
            .create();

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {gson.toJson(records, writer);}
    }
    
    // Загрузка из JSON файла
@SuppressWarnings("unchecked")
public static LinkedList<RecIntegral> loadFromJsonFile(File file) throws IOException, IntegralException {
    Gson gson = new GsonBuilder().setVersion(1.0).create();
  
    LinkedList<RecIntegral> records;
    
    try (Reader reader = new InputStreamReader(
            new FileInputStream(file), StandardCharsets.UTF_8)) {
        
        // Десериализация
        Type listType = new TypeToken<LinkedList<RecIntegral>>() {}.getType();
        records = gson.fromJson(reader, listType);
        
        if (records == null) {
            throw new IntegralException("Файл содержит пустые данные");
        }
        
        // Валидация загруженных данных
        for (RecIntegral record : records) {
            record.validate();
        }
    }
    
    return records;
}
    
}
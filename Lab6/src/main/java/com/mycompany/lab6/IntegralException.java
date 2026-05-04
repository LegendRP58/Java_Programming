package com.mycompany.lab6;

public class IntegralException extends Exception {
    private String fieldName;     // поле, вызвавшее ошибку
    private double invalidValue;  // значение, вызвавшее ошибку
    
    // конструктор
    public IntegralException(String message, String fieldName, double invalidValue) {
        // Передаем сообщение в родительский класс Exception
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }
    
    // конструктор когда необходим только текст об ошибке
    public IntegralException(String message) {super(message);}
    
    // геттеры
    public String getFieldName() {return fieldName;}
    
    public double getInvalidValue() {return invalidValue;}
    
}

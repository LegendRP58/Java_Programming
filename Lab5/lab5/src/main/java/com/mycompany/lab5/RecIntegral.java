package com.mycompany.lab5;

import java.io.Serializable;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import com.google.gson.annotations.SerializedName;

public class RecIntegral implements Externalizable  {  //implements Externalizable implements Serializable
    private static final long serialVersionUID = 1L; // для версионирования
    
   // константы минимального/максимального значений для вводимых данных
    public static final double MIN_VALUE = 0.000001;
    public static final double MAX_VALUE = 1000000.0;

    // Метод валидации значения
    private static void validateValue(double value, String fieldName) throws IntegralException {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IntegralException(String.format("Значение поля %s (%.6f) должно быть в диапазоне от %.6f до %.6f", fieldName, value, MIN_VALUE, MAX_VALUE), fieldName, value);
        }
    }
    
   // переменные таблицы и их аннотации для json
    @SerializedName("step")
    private double step;
    @SerializedName("upperLimit") 
    private double upperLimit;
    @SerializedName("lowerLimit")
    private double lowerLimit;
    @SerializedName("result")
    private double result;
    
     // конструктор по умолчанию
    public RecIntegral() {this.step = 0.0;
            this.upperLimit = 0.0;
            this.lowerLimit = 0.0;
            this.result = 0.0;
}
     // Конструктор с параметрами
    public RecIntegral(double step, double upperLimit, double lowerLimit, double result){
        this.step = step;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.result = result;
}
      // Конструктор без результата (для новых записей)
    public RecIntegral(double step, double upperLimit, double lowerLimit) {
        this(step, upperLimit, lowerLimit, 0.0);
    }
    
    // геттеры и сеттеры переменных RecIntegral + проверка значений (валидация)
    public double getStep() { return step;}
    public void setStep(double step) throws IntegralException { 
        validateValue(step, "Шаг");
        this.step = step;}
    
    public double getUpperLimit() {return upperLimit;}
    public void setUpperLimit (double upperLimit) throws IntegralException {
        validateValue(upperLimit, "Верхний предел");
        this.upperLimit = upperLimit;} 
    
    public double getLowerLimit() {return lowerLimit;} 
    public void setLowerLimit(double lowerLimit) throws IntegralException {
        validateValue(lowerLimit, "Нижнний предел");
        this.lowerLimit = lowerLimit;} 
    
    public double getResult() {return result;}
    public void setResult(double result) {this.result = result;}
    
        // Метод для преобразования в массив Object для таблицы
    public Object[] toTableRow() {
        return new Object[]{step, upperLimit, lowerLimit,result == 0.0 ? "" : String.format("%.5f", result)};
    }
    
    // Вычисление интеграла //
      public static double func(double x) {
        return Math.sin(x*x); 
    }
      
      // валидация напрямую со значениями из recintegral
      public void validate() throws IntegralException {
    FileManager.validateValues(this.step, this.upperLimit, this.lowerLimit);
    if (this.lowerLimit >= this.upperLimit) {
        throw new IntegralException(
            String.format("Нижний предел (%.6f) должен быть меньше верхнего (%.6f)", this.lowerLimit, this.upperLimit));
    }
    if (this.step <= 0) {
        throw new IntegralException("Шаг должен быть положительным числом");
    }
    if (this.step > this.upperLimit - this.lowerLimit) {
        throw new IntegralException(
            String.format("Шаг (%.6f) больше разницы пределов (%.6f)", this.step, this.upperLimit - this.lowerLimit));
    }
}
      
      public static double computeIntegral(double step, double lowlim, double uplim) {
    double sum = 0.0;
    double x = lowlim;
    
    while (x < uplim) {
        //Подсчет шага с учетом погрешности с помощью минимизации Math.min
        double currentstep = Math.min(step, uplim - x);
        double nextX = x + currentstep;
        
        double y1 = func(x);
        double y2 = func(nextX);
        // Площадь трапеции
        sum += (y1 + y2) * (nextX - x) / 2.0;
        x = nextX;
    }
    return sum;
}
//      ---Externalizable---
      
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(step);
        out.writeDouble(upperLimit);
        out.writeDouble(lowerLimit);
        out.writeDouble(result);
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        step = in.readDouble();
        upperLimit = in.readDouble();
        lowerLimit = in.readDouble();
        result = in.readDouble();
        
        // Опционально: можно выполнить валидацию после загрузки
        try {
            validate();
        } catch (IntegralException e) {
            throw new IOException("Ошибка валидации загруженных данных: " + e.getMessage(), e);
        }
    }
}
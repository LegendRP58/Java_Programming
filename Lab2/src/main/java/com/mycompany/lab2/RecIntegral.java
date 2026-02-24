package com.mycompany.lab2;

public class RecIntegral {
   // переменные таблицы
    private double step;
    private double upperLimit;
    private double lowerLimit;
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
        this.step = step;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.result = 0.0;
    }
    
    // геттеры и сеттеры переменных RecIntegral
    public double getStep() { return step;}
    public void setStep(double step) {this.step = step;}
    public double getUpperLimit() {return upperLimit;}
    public void setUpperLimit(double upperLimit) {this.upperLimit = upperLimit;} 
    public double getLowerLimit() {return lowerLimit;} 
    public void setLowerLimit(double lowerLimit) {this.lowerLimit = lowerLimit;}
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
  
      public static double computeIntegral(double step, double lowlim, double uplim) {
    if (lowlim >= uplim || step <= 0) {
        throw new IllegalArgumentException("Некорректные параметры: lowlim < uplim, step > 0");}
    
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
}
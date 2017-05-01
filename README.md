# Tarea 1: Diseño y Analisis de Algoritmos

# 1. Generacion de segmentos
Clase ```SegmentGenerator``` se instancia con los valores:
	* N: Cantidad de segmentos a generar. {2^9, ..., 2^21}
	* Distribución: De X en segmentos verticales. (Uniforme o Normal)
	* a: Balance de segmentos verticales vs horizontales. {0.25, 0.5, 0.75}

Además se pueden customizar los siguientes parametros:
    ```
	static final int YMAX = 100; // Valor maximo de coordenada X
    static final int XMAX = 100; // Valor maximo de coordenada Y
	static final double normalMean = XMAX/2; // Media de la distribución Normal
	static final double normalDesv = XMAX/7; // Desviacion estándar de la distribución Normal
	```

Al llamar al método: ```SegmentGenerator.GenerateSegments()``` se crean los segmentos dejandolos en el archivo ```<timestamp>.txt``` en el formato pedido.

	
	
# 2. Programación de Algoritmos
## 2.1. Sort
## 2.2. Distribution Sweep
# 3. Pruebas
# 4. Informe
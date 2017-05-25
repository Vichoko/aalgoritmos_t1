
# Luego de compilar

## Generar segmentos:
	java segment/SegmentGenerator(i, d, a)

- i corresponde a un numero que indica el exponente de la cantidad de segmentos, es decir si i = 10, se crear\'e1n 2^10 segmentos.\
- d es la distribution, puede ser Normal o Uniforme\
- a es la constante alfa, se entrega el numero, por ejemplo 0.25\

Esto retornará el exponente y el nombre del archivo txt donde se encuentran los segmentos, ambos datos separados por un espacio

## Probar mergesort
```Java
algorithm/sort/MergeSort(i,f)
```
- i tiene el mismo significado anteriormente
- f es el nombre del archivo, sin extensión.txt (de la misma forma que retorna SegmentGenerator)

## Probar DistributionSweep
```Java
algorithm/sort/DistributionSweep(segmentFile)
```
-segmentFile es la ruta al archivo que tiene los segmentos.

Nota: La variable TOTAL_SEGMENTS en ```utils.Constant.java``` debe ser consistente con la cantidad de segmentos en segmentFile.


### Probar con test
Esta bateria de test incluye todos los casos de prueba propuestos en el enunciado.
1. Dirigirse a clase ```test.DSTest```.
2. Ejecutar metodo main.

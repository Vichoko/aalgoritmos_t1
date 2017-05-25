
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
- i tiene el mismo significado anteriormente\
- f es el nombre del archivo, sin extensi\'f3n .txt (de la misma forma que retorna SegmentGenerator)\

## Probar DistributionSweep

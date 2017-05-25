{\rtf1\ansi\ansicpg1252\cocoartf1504\cocoasubrtf810
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
{\*\expandedcolortbl;;}
\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\pardirnatural\partightenfactor0

\f0\fs24 \cf0 Luego de compilar\
\
Generar segmentos:\
	java segment/SegmentGenerator $i $d $a\
	- i corresponde a un numero que indica el exponente de la cantidad de segmentos, es decir si i = 10, se crear\'e1n 2^10 segmentos.\
	- d es la distribution, puede ser Normal o Uniforme\
	- a es la constante alfa, se entrega el numero, por ejemplo 0.25\
Esto retornar\'e1 el exponente y el nombre del archivo txt donde se encuentran los segmentos, ambos datos separados por un espacio\
\
Probar mergesort\
	java algorithm/sort/MergeSort $i $f\
	- i tiene el mismo significado anteriormente\
	- f es el nombre del archivo, sin extensi\'f3n .txt (de la misma forma que retorna SegmentGenerator)\
\
Probar DistributionSweep}
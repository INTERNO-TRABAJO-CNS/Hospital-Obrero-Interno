CNS - HOSPITAL OBRERO | DIAGRAMAS DE ESTADOS UML

Se incluyen 7 diagramas, ED-01 a ED-07, en sus propias carpetas.
Cada carpeta contiene:
- PNG: imagen en alta resolución.
- SVG: imagen vectorial escalable.
- PUML: código original aportado, sin alterar la lógica de estados.
- DOT: archivo fuente de Graphviz que permite reproducir la representación visual.

Los archivos se representaron con el mismo estilo del ejemplo ED-05: 
fondo blanco, bordes negros, cajas redondeadas, flechas y nodos
UML de inicio y fin. El ED-05 conserva exactamente el PNG y SVG de ejemplo.

Para regenerar las imágenes con Graphviz:
  dot -Tsvg archivo.dot -o archivo.svg
  dot -Tpng -Gdpi=160 archivo.dot -o archivo.png

Observación: los comentarios y las reglas presentes en los PUML
se conservan en los originales; los títulos y las transiciones
proceden de esos mismos archivos.

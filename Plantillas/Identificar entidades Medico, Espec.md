1.- Identificar entidades Medico, Especialidad, Horario/Turno, Agenda, Consultorio y posibles cupos, justificando sus límites desde los CU.


medico. persona con una especialidad encargada de tratar a los pacientes dentro de citas. tiene acceso al historial del paciente que va a tratar dentro del marco de las citas.

especialidad: area de expertiz centrada en el area medica, los medicos puede tener una especialidad.

Horario/Turno: periodo de tiempo, este periodo de tiempo es especifico al medico, donde la persona esta disponible para tratar pacientes.

Agenda: refiere a las citas registradas previamente que es especifica al medico.

Consultorio: se refiere a un lugar especifico del hospital, donde el medico se va situar para tratar a los pacientes, el lugar de trabajo del medico en otras palabras

cupos: se refiere a un caso particular, en el que el paciente con una cita agendada no llega a confirmar la cita a tiempo, donde el procedimiento consiste en comunicar a los pacientes que ese turno se vuelve disponible para la primera persona que la reclame

2.- Definir operaciones consultarDisponibilidad, validarRecursos, consultarAgenda y obtenerInformacionPermitida en lenguaje de diseño, sin código.

consultar disponibilidad(medico,vartiempo):
--vartiempo se define como un punto en el tiempo, que en este caso define el comienzo de una cita medica previamente agendada, junto con la conclusion de la misma dentro de una matriz/array, siguiendo la siguiente estructura: [comienzocita,fincita], donde ambas variables son variables tiempo)
-- algo algo, supongo que la variable medico tendra su variable horario/agenda, ya que es una clase (asumo)
-- Tambien se asume que vartiempo esta dentro del rango disponible del horario del medico,si NO es el caso fijarse y tirar error si no es el caso si es esto esta implementado en, error: vartiempo no esta dentro del horario disponible del medico seleccionado
-- fijarse agenda si el medico tiene alguna cita agendada (if not medico.agenda.isempty())
--en este caso se asume que agenda guarda periodos de tiempo en una forma similar a vartiempo, (ej [[comienzocita1,fincita1],[comienzocita2,fincita2], [comienzocitan,fincitan]])
-- if True, then fijarse si vartiempo no sobrepone sobre ninguna de las citas agendadas EJEMPLO:vartiempo=[05/10/26 14:00, 05/10/26/16:00] compara a medico.agenda[0]=[05/10/26 15:00, 05/10/26/17:00] -- en este caso vartiempo SI se sobrepone con la primera cita agendada entre las horas 15:00 a 16:00, la función puede terminar aquí mostrando el mensaje: medico f'{medico.nombre} tiene otra cita agendada en las horas especificadas.
--si ninguna cita previamente agendada se sobrepone, enviar mensaje confirmando la disponibilidad del medico en el tiempo dado.

validarrecursos(medico,vartiempo,consultorio): -- asumo que esto se fija si consultorio y medico estan disponibles para agendarCita
--se usa consultar disponibilidad para confirmar si medico, junto con vartiempo esta disponible en el tiempo dado
--right, usar query en la base de datos para extraer si hay citas en consultorio especifico, en la horas dadas
si sale un resultado no vacio, consultorio no esta disponible, return false

return true, ya que el consultorio, en los tiempos dados deberia estar disponible 

consultaragenda(medico)
--fijarse por el rol del usuario para confirmar que esten autorizado para usar esta funcion(si el usuario es una enfermera, medico, o admin), si no esta autorizado terminar la funcion aqui.

return medico.agenda



obtenerinformacionpermitida(cita)
--fijarse si el usuario que pide esto es un medico.
--fijarse si el nombre/rol del usuario es el mismo que el medico asignado a la cita
--terminar la funcion si no es el caso.
--obtener los datos del paciente, en base a la informacion de la cita
return data.





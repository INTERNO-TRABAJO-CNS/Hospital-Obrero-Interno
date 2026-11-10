import 'package:flutter/material.dart';

import '../models/sesion_dto.dart';

class MenuPage extends StatelessWidget {
  const MenuPage({super.key, required this.sesion, required this.onLogout});

  final SesionDto sesion;
  final VoidCallback onLogout;

  static const _options = <_MenuOption>[
    _MenuOption(
      permission: 'CITAS_CONSULTAR',
      title: 'Mis citas',
      icon: Icons.event,
    ),
    _MenuOption(
      permission: 'AGENDA_CONSULTAR',
      title: 'Mi agenda',
      icon: Icons.calendar_month,
    ),
    _MenuOption(
      permission: 'PACIENTES_CONSULTAR',
      title: 'Pacientes',
      icon: Icons.people,
    ),
    _MenuOption(
      permission: 'ATENCION_REGISTRAR',
      title: 'Registrar atencion',
      icon: Icons.medical_services,
    ),
    _MenuOption(
      permission: 'USUARIOS_GESTIONAR',
      title: 'Usuarios',
      icon: Icons.manage_accounts,
    ),
    _MenuOption(
      permission: 'REPORTES_CONSULTAR',
      title: 'Reportes',
      icon: Icons.assessment,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final availableOptions = _options
        .where((option) => sesion.permisos.contains(option.permission))
        .toList();

    return Scaffold(
      appBar: AppBar(
        title: Text('Menu - ${sesion.rol}'),
        actions: [
          IconButton(
            onPressed: onLogout,
            tooltip: 'Cerrar sesion',
            icon: const Icon(Icons.logout),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          Text('Bienvenido, ${sesion.nombreUsuario}'),
          const SizedBox(height: 24),
          if (availableOptions.isEmpty)
            const Text('No tienes opciones habilitadas.')
          else
            ...availableOptions.map(
              (option) => ListTile(
                leading: Icon(option.icon),
                title: Text(option.title),
                key: Key('menu-${option.permission}'),
              ),
            ),
        ],
      ),
    );
  }
}

class _MenuOption {
  const _MenuOption({
    required this.permission,
    required this.title,
    required this.icon,
  });

  final String permission;
  final String title;
  final IconData icon;
}
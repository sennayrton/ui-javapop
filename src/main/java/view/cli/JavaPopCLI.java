/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package view.cli;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

import controller.JavaPopController;
import model.Categoria;
import model.Cliente;
import model.ClienteProfesional;
import model.Compra;
import model.EstadoProducto;
import model.Horario;
import model.Producto;
import model.Ubicacion;

public class JavaPopCLI {

    public static void main(String[] args) throws ClassNotFoundException {

        JavaPopController controller = JavaPopController.getController();

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        boolean isAuthenticated = false;
        boolean isAdmin = false;
        Cliente cliente = null;

        while (!exit) {
            while (!isAuthenticated && !exit) {

                printInitialMenu();
                int comando = Integer.valueOf(scanner.nextLine());

                switch (comando) {
                    case 1:
                        System.out.println("Introduce tu correo:");
                        String correo = scanner.nextLine();

                        System.out.println("Introduce tu clave:");
                        String clave = scanner.nextLine();

                        cliente = controller.login(correo, clave);
                        if (cliente == null) {
                            isAdmin = controller.isAdmin(correo, clave);
                            isAuthenticated = isAdmin;
                        } else {
                            isAuthenticated = true;
                        }
                        break;
                    case 2:
                        Cliente nuevoCliente = leerCliente(scanner);
                        if (controller.addCliente(nuevoCliente)) {
                            System.out.println("Cliente registrado");
                        } else {
                            System.err.println("Ese cliente ya existe");
                        }
                        break;
                    case 3:
                        exit = true;
                        try {
                            controller.guardar();
                        } catch (IOException e) {
                            System.err.println("Error al guardar los datos");
                        }
                }
            }

            if (!exit) {
                if (isAdmin) {

                    printAdminMenu();
                    int comando = Integer.valueOf(scanner.nextLine());

                    switch (comando) {
                        case 1:
                            consultarClientes(controller.getClientes());
                            break;
                        case 2:
                            consultarProductos(controller.getProductos());
                            break;
                        case 3:
                            consultarVentas(controller.getCompras());
                            break;
                        case 4:
                            darBajaCliente(scanner, controller);
                            break;
                        case 5:
                            darBajaProducto(scanner, controller);
                            break;
                        case 6:
                            isAuthenticated = false;
                            break;
                        case 7:
                            exit = true;
                            try {
                                controller.guardar();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.println("Error al guardar los datos");
                            }
                            break;
                        default:
                            break;
                    }
                } else if (controller.isProfesional(cliente)) {
                    printProfesionalMenu();
                    int comando = Integer.valueOf(scanner.nextLine());

                    switch (comando) {
                        case 1:
                            System.out.println("Se le ha hecho un cargo en su tarjeta de crédito ");
                            break;
                        case 2:
                            altaProducto(cliente, scanner, controller);
                            break;
                        case 3:
                            borrarProductos(cliente, controller);
                            break;
                        case 4:
                            buscarProductos(scanner, controller, cliente);
                            break;
                        case 5:
                            comprarProducto(scanner, cliente, controller);
                            break;
                        case 6:
                            revisarCompras(cliente, controller);
                            break;
                        case 7:
                            isAuthenticated = false;
                            break;
                        case 8:
                            exit = true;
                            try {
                                controller.guardar();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.println("Error al guardar los datos");
                            }
                            break;
                        default:
                            break;
                    }
                } else {

                    printClientMenu();
                    int comando = Integer.valueOf(scanner.nextLine());

                    switch (comando) {
                        case 1:
                            ClienteProfesional nuevoCliente = leerDatosAdicionales(cliente, scanner);
                            if (controller.transformClientToProfesional(nuevoCliente)) {
                                cliente = nuevoCliente;
                                System.out.println("Eres profesional!");
                            } else {
                                System.err.println("No hemos encontrado tus datos");
                            }

                            break;
                        case 2:
                            altaProducto(cliente, scanner, controller);
                            break;
                        case 3:
                            borrarProductos(cliente, controller);
                            break;
                        case 4:
                            buscarProductos(scanner, controller, cliente);
                            break;
                        case 5:
                            comprarProducto(scanner, cliente, controller);
                            break;
                        case 6:
                            revisarCompras(cliente, controller);
                            break;
                        case 7:
                            isAuthenticated = false;
                            break;
                        case 8:
                            exit = true;
                            try {
                                controller.guardar();
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.err.println("Error al guardar los datos");
                            }
                            break;
                        default:
                            break;
                    }

                }
            }
        }

        scanner.close();

    }

    private static void buscarProductos(Scanner scanner, JavaPopController applicationData, Cliente cliente) {

        System.out.println("Introduce la categoria:");
        for (Categoria cat : Categoria.values()) {
            System.out.println(cat.name());
        }

        Categoria categoria = Categoria.valueOf(scanner.nextLine());

        System.out.println(
                "Introducir palabras clave del producto a buscar separadas por un espacio (vacio si no quieres): ");

        String palabrasClave = scanner.nextLine();

        List<Producto> productosEncontrados = applicationData.find(categoria, palabrasClave, cliente);
        for (Producto producto : productosEncontrados) {
            System.out.println(producto);
        }

    }

    private static void revisarCompras(Cliente cliente, JavaPopController applicationData) {
        applicationData.revisarCompras(cliente);
        System.out.println("Comprar verificadas");
        System.out.println();
        System.out.println();
    }

    private static void comprarProducto(Scanner scanner, Cliente cliente, JavaPopController applicationData) {

        System.out.println("Introduce id del producto a comprar:");
        int id = Integer.valueOf(scanner.nextLine());

        if (!applicationData.comprarProducto(cliente, id)) {
            System.out.println("Producto no encontrado");
        } else {
            System.out.println("Producto marcado para comprar. Espera la confirmación del vendedor");
            System.out.println();
            System.out.println();
        }
    }

    private static void altaProducto(Cliente cliente, Scanner scanner, JavaPopController applicationData) {
        Producto producto = leerDatosProducto(cliente, scanner);
        applicationData.addProducto(producto);
        System.out.println("Producto añadido correctamente");
        System.out.println();
        System.out.println();
    }

    private static void printInitialMenu() {
        System.out.println("\033[36m¡Bienvenido a JAVAPOP!");
        System.out.println("Elige una opcion:");
        System.out.println("1 - Iniciar sesión");
        System.out.println("2 - Registrarse");
        System.out.println("3 - Exit");
    }

    private static void printAdminMenu() {
        System.out.println("\033[36m Iniciada sesión como administrador");
        System.out.println("Elige una opcion:");
        System.out.println("1 - Consultar usuarios");
        System.out.println("2 - Consultar productos");
        System.out.println("3 - Consultar ventas realizadas");
        System.out.println("4 - Dar baja cliente fraudulento");
        System.out.println("5 - Dar baja producto fraudulento");
        System.out.println("6 - Cerrar sesion");
        System.out.println("7 - Exit");
    }

    private static void printProfesionalMenu() {
        System.out.println("\033[35m Iniciada sesion como cliente profesional");
        System.out.println("Elige una opcion:");
        System.out.println("1 - Comprobar estado pago cuota 30€");
        System.out.println("2 - Alta producto");
        System.out.println("3 - Baja productos de un usuario");
        System.out.println("4 - Buscar productos");
        System.out.println("5 - Comprar productos");
        System.out.println("6 - Confirmar compra");
        System.out.println("7 - Cerrar sesion");
        System.out.println("8 - Exit");
    }

    private static void printClientMenu() {
        System.out.println("\033[34m Iniciada sesion como cliente");
        System.out.println("Elige una opcion:");
        System.out.println("1 - Ser cliente profesional");
        System.out.println("2 - Alta producto");
        System.out.println("3 - Baja productos de un usuario");
        System.out.println("4 - Buscar productos");
        System.out.println("5 - Comprar productos");
        System.out.println("6 - Confirmar compra");
        System.out.println("7 - Cerrar sesion");
        System.out.println("8 - Exit");
    }

    private static void borrarProductos(Cliente cliente, JavaPopController applicationData) {
        applicationData.removeProductos(cliente.getDni());
        System.out.println("Productos borrados");
        System.out.println();
        System.out.println();
    }

    private static Producto leerDatosProducto(Cliente cliente, Scanner scanner) {

        Producto producto = new Producto();

        System.out.println("Introduce la categoria del producto:");
        for (Categoria cat : Categoria.values()) {
            System.out.println(cat.name());
        }
        Categoria categoria = Categoria.valueOf(scanner.nextLine());
        producto.setCategoria(categoria);

        System.out.println("Introduce la descripción del producto:");
        String descripcion = scanner.nextLine();
        producto.setDescripcion(descripcion);

        System.out.println("Introduce el estado del producto:");
        for (EstadoProducto estado : EstadoProducto.values()) {
            System.out.println(estado.name());
        }
        EstadoProducto estado = EstadoProducto.valueOf(scanner.nextLine());

        producto.setEstado(estado);

        LocalDate fecha = LocalDate.now();
        producto.setFecha(fecha);

        System.out.println("Introduce el precio del producto:");
        double precio = Double.valueOf(scanner.nextLine());
        producto.setPrecio(precio);

        System.out.println("Introduce el titulo del producto:");
        String titulo = scanner.nextLine();
        producto.setTitulo(titulo);

        producto.setUbicacion(cliente.getUbicacion());

        System.out.println("Indica si el producto es urgente (0) o no lo es (1):");
        boolean urgente = scanner.nextLine().equals("0") ? true : false;
        producto.setUrgente(urgente);
        return producto;

    }

    private static Cliente leerCliente(Scanner scanner) {

        Cliente cliente = new Cliente();

        System.out.println("Introduce el dni del cliente:");
        String dni = scanner.nextLine();
        cliente.setDni(dni);

        System.out.println("Introduce el nombre del cliente:");
        String nombre = scanner.nextLine();
        cliente.setNombre(nombre);

        System.out.println("Introduce el codigo postal del cliente:");
        Ubicacion ubicacion = new Ubicacion();
        String codigoPostal = scanner.nextLine();
        ubicacion.setCodigoPostal(codigoPostal);
        System.out.println("Introduce la ciudad del cliente:");
        String ciudad = scanner.nextLine();
        ubicacion.setCiudad(ciudad);
        cliente.setUbicacion(ubicacion);

        System.out.println("Introduce la tarjeta del cliente:");
        String tarjeta = scanner.nextLine();
        cliente.setTarjeta(tarjeta);

        System.out.println("Introduce el correo del cliente:");
        String correo = scanner.nextLine();
        cliente.setCorreo(correo);

        System.out.println("Introduce la clave del cliente");
        String clave = scanner.nextLine();
        cliente.setClave(clave);

        return cliente;
    }

    private static ClienteProfesional leerDatosAdicionales(Cliente cliente, Scanner scanner) {

        ClienteProfesional nuevoCliente = new ClienteProfesional();
        nuevoCliente.setDni(cliente.getDni());
        nuevoCliente.setNombre(cliente.getNombre());
        nuevoCliente.setUbicacion(cliente.getUbicacion());
        nuevoCliente.setTarjeta(cliente.getTarjeta());
        nuevoCliente.setCorreo(cliente.getCorreo());
        nuevoCliente.setClave(cliente.getClave());

        System.out.println("Introduce la descripción de la tienda:");
        String descripcion = scanner.nextLine();
        nuevoCliente.setDescripcion(descripcion);

        System.out.println("Introduce el teléfono de la tienda:");
        String telefono = scanner.nextLine();
        nuevoCliente.setTelefono(telefono);

        System.out.println("Introduce la web de la tienda:");
        String web = scanner.nextLine();
        nuevoCliente.setWeb(web);

        Horario horario = new Horario();

        System.out.println("Introduce la hora de apertura de la tienda:");
        String horaApertura = scanner.nextLine();
        horario.setApertura(LocalTime.parse(horaApertura));

        System.out.println("Introduce la hora de cierre de la tienda:");
        String horaCierre = scanner.nextLine();
        horario.setCierre(LocalTime.parse(horaCierre));

        nuevoCliente.setHorario(horario);

        return nuevoCliente;
    }

    private static void darBajaCliente(Scanner scanner, JavaPopController applicationData) {

        System.out.println("Introduce el dni del cliente:");
        String dni = scanner.nextLine();

        if (applicationData.removeCliente(dni)) {
            System.out.println("El cliente ha sido dado de baja");
        } else {
            System.out.println("El cliente no ha sido encontrado");
        }
    }

    private static void consultarClientes(List<Cliente> clientes) {
        for (Cliente i : clientes) {
            System.out.println(i);
        }
        System.out.println();
    }

    private static void consultarProductos(List<Producto> productos) {
        for (Producto producto : productos) {
            System.out.println(producto);
        }
        System.out.println();
    }

    private static void consultarVentas(List<Compra> compras) {
        for (Compra i : compras) {
            System.out.println(i);
        }
        System.out.println();
    }

    private static void darBajaProducto(Scanner scanner, JavaPopController applicationData) {

        System.out.println("Introduce el nombre del producto:");
        String id = scanner.nextLine();

        if (applicationData.removeProducto(Integer.valueOf(id))) {
            System.out.println("El producto ha sido dado de baja");
        } else {
            System.out.println("El produto no ha sido encontrado");
        }
    }

}

package controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.Administrador;
import model.Categoria;
import model.Cliente;
import model.ClienteProfesional;
import model.Compra;
import model.Producto;

public class JavaPopController {
    // Gestión de datos de la aplicación
    private List<Cliente> clientes;
    private List<Producto> productos;
    private List<Compra> compras;
    //abrir varias pantallas a la vez
    //los datos de clientes, productos, compras
    //se compartan
    private static JavaPopController controller;
    
    // synchronized es para evitar problemas de concurrencia
    public static synchronized JavaPopController getController() {
        if (controller == null) {
            controller = new JavaPopController();
        }
        return controller;
    }
    // En el constructor hacemos la carga inicial de los datos
    private JavaPopController() {

        try {
            clientes = leerClientes();
        } catch (Exception e) {
            clientes = new ArrayList<>();
        }

        try {

            productos = leerProductos();
        } catch (Exception e) {
            productos = new ArrayList<>();
        }

        try {
            compras = leerCompras();
        } catch (Exception e) {
            compras = new ArrayList<>();
        }

    }

    /**
     * @return the clientes
     */
    public List<Cliente> getClientes() {
        return clientes;
    }

    /**
     * @return the productos
     */
    public List<Producto> getProductos() {
        return productos;
    }

    /**
     * @return the compras
     */
    public List<Compra> getCompras() {
        return compras;
    }

    public Cliente login(String correo, String clave) {

        return comprobarCredenciales(correo, clave);
    }

    private Cliente comprobarCredenciales(String correo, String clave) {

        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getCorreo().equals(correo) && clientes.get(i).getClave().equals(clave)) {
                return clientes.get(i);
            }
        }

        return null;

    }

    public boolean isAdmin(String correo, String clave) {
        return correo.equals(Administrador.CORREO) && clave.equals(Administrador.CLAVE);
    }

    public boolean isProfesional(Cliente cliente) {
        return cliente instanceof ClienteProfesional;
    }
    //registrarse
    public boolean addCliente(Cliente cliente) {

        int i;
        for (i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getDni().equals(cliente.getDni())
                    || clientes.get(i).getCorreo().equals(cliente.getCorreo())) {
                break;
            }
        }

        if (i == clientes.size()) {
            clientes.add(cliente);
            return true;
        }

        return false;

    }

    public boolean removeCliente(String dni) {
        int i;
        boolean encontrado = false;
        for (i = 0; i < getClientes().size() && !encontrado; i++) {
            encontrado = getClientes().get(i).getDni().equals(dni);
        }

        if (encontrado) {
            clientes.remove(i - 1);
        }

        return encontrado;
    }

    public boolean transformClientToProfesional(Cliente cliente) {

        int i;
        boolean encontrado = false;
        for (i = 0; i < getClientes().size() && !encontrado; i++) {
            encontrado = getClientes().get(i).getDni().equals(cliente.getDni());
        }

        if (encontrado) {
            clientes.set(i - 1, cliente);
        }

        return encontrado;
    }

    public void addProducto(Producto producto) {
        getProductos().add(producto);
    }

    public boolean removeProducto(int id) {
        int i;
        boolean encontrado = false;
        for (i = 0; i < getProductos().size() && !encontrado; i++) {
            encontrado = getProductos().get(i).getId() == id;
        }
        if (encontrado) {
            getProductos().remove(i - 1);
        }
        return encontrado;
    }

    public void removeProductos(String dni) {
        productos.removeIf(producto -> producto.getAñadidoPor().equals(dni));
    }

    public boolean comprarProducto(Cliente cliente, int id) {
        Producto productoComprar = null;

        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == id) {
                productoComprar = productos.get(i);
                break;
            }
        }

        if (productoComprar == null) {
            return false;
        }

        Compra compra = new Compra();
        compra.setFecha(LocalDate.now());
        compra.setProducto(productoComprar);
        compra.setNombreComprador(cliente.getNombre());
        compra.setDniComprador(cliente.getDni());
        compra.setDniVendedor(productoComprar.getAñadidoPor());

        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getDni().equals(productoComprar.getAñadidoPor())) {
                compra.setNombreVendedor(clientes.get(i).getNombre());
                break;
            }
        }

        getCompras().add(compra);

        return true;
    }

    public List<Producto> find(Categoria categoria, String palabrasClave, Cliente cliente) {

        List<Producto> productosEncontrados = new ArrayList<>();

        if (!palabrasClave.isEmpty()) {
            String[] palabras = palabrasClave.split(" ");

            for (Producto producto : productos) {
                if (producto.getCategoria().equals(categoria)) {
                    int k;
                    for (k = 0; k < palabras.length; k++) {
                        if (producto.getTitulo().contains(palabras[k])) {
                            productosEncontrados.add(producto);
                            break;
                        }
                    }
                }
            }
        } else {
            for (Producto producto : productos) {
                if (producto.getCategoria().equals(categoria)) {
                    productosEncontrados.add(producto);
                }
            }
        }

        List<Producto> urgentes = new ArrayList<>(); //Productos urgentes
        List<Producto> codePostal1 = new ArrayList<>(); //Productos más proximos
        List<Producto> codePostal2 = new ArrayList<>(); //Productos proximos
        List<Producto> codePostal3 = new ArrayList<>(); //Resto

        int index;
        
        // Distingo entre productos urgentes y no urgentes
        // Entre los que no son urgentes, miro la distancia:
        // Si el los tres primeros digitos del codigo postal de producto son iguales a los del cliente
        // --> muy proximo (lo metemos en codePostal1)
        // Si solo coincide en 2
        // --> proximo (lo metemos en codePostal2)
        // En otro caso, lejano (lo metemos en codePostal3)

        for (index = 0; index < productosEncontrados.size(); index++) {
            if (!productosEncontrados.get(index).isUrgente()) {
                Producto producto = productosEncontrados.get(index);
                if (producto.getUbicacion().getCodigoPostal().substring(0, 3)
                        .equals(cliente.getUbicacion().getCodigoPostal().substring(0, 3))) {
                    codePostal1.add(producto);
                } else if (producto.getUbicacion().getCodigoPostal().substring(0, 2)
                        .equals(cliente.getUbicacion().getCodigoPostal().substring(0, 2))) {
                    codePostal2.add(producto);
                } else {
                    codePostal3.add(producto);
                }
            } else {
                urgentes.add(productosEncontrados.get(index));
            }
        }

        List<Producto> result = new ArrayList<>(urgentes);
        result.addAll(codePostal1);
        result.addAll(codePostal2);
        result.addAll(codePostal3);

        return result;

    }

    public void revisarCompras(Cliente cliente) {
        for (int i = 0; i < compras.size(); i++) {
            Compra compra = compras.get(i);
            if (compra.getDniVendedor().equals(cliente.getDni())) {
                compra.setConfirmada(true);
                productos.remove(compra.getProducto());
                try {
                    generarFichero(compra);
                } catch (IOException e) {
                    System.err.println("Error al generar factura");
                }
            }
        }
    }

    private static void generarFichero(Compra compra) throws IOException {
        String fileName = "factura" + LocalDateTime.now() + ".txt";
        FileOutputStream fis = new FileOutputStream(fileName);
        fis.write(compra.toString().getBytes());
        fis.close();
    }

    public void guardar() throws IOException {

        // Guardar los clientes
        String fileName = "clientes.txt";
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(clientes);
        oos.close();

        // Guardar los productos
        fileName = "productos.txt";
        fos = new FileOutputStream(fileName);
        oos = new ObjectOutputStream(fos);
        oos.writeObject(productos);
        oos.close();

        // Guardar las compras
        fileName = "compras.txt";
        fos = new FileOutputStream(fileName);
        oos = new ObjectOutputStream(fos);
        oos.writeObject(compras);
        oos.close();
    }

    @SuppressWarnings("unchecked")
    public List<Cliente> leerClientes() throws IOException, ClassNotFoundException {

        // Leer los clientes
        String fileName = "clientes.txt";
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<Cliente> clientes = (List<Cliente>) ois.readObject();
        ois.close();
        return clientes;
    }

    @SuppressWarnings("unchecked")
    public List<Producto> leerProductos() throws ClassNotFoundException, IOException {

        // Leer los productos
        String fileName = "productos.txt";
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<Producto> productos = (List<Producto>) ois.readObject();
        ois.close();
        return productos;
    }

    @SuppressWarnings("unchecked")
    public List<Compra> leerCompras() throws ClassNotFoundException, IOException {

        // Leer las compras
        String fileName = "compras.txt";
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<Compra> compras = (List<Compra>) ois.readObject();
        ois.close();
        return compras;
    }

}

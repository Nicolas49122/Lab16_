package com.tecsup.controller;

import com.tecsup.Exception.ResourceNotFoundException;
import com.tecsup.model.Auditoria;
import com.tecsup.model.Producto;
import com.tecsup.repository.AuditoriaRepository;
import com.tecsup.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper; // Importar ObjectMapper para JSON
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000/")
@RestController
@RequestMapping("/api/v1")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository; // Inyectamos el repositorio de Auditoría

    // Listar productos
    @GetMapping("/productos")
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    // Guardar producto
    @PostMapping("/productos")
    public Producto guardarProducto(@RequestBody Producto producto) {
        // Guardamos el producto
        Producto savedProducto = productoRepository.save(producto);

        // Registrar la auditoría (action = "INSERT")
        Auditoria auditoria = new Auditoria();
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setTipo("INSERT");
        auditoria.setAccion("Se ha creado un nuevo producto");
        auditoria.setProductoId(savedProducto.getId());
        auditoria.setProductoNombre(savedProducto.getNombre());
        auditoria.setProductoDescripcion(savedProducto.getDescripcion());
        auditoria.setProductoPrecio(String.valueOf(savedProducto.getPrecio()));
        auditoriaRepository.save(auditoria); // Guardamos el registro de auditoría

        return savedProducto;
    }

    // Listar producto por ID
    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> listarProductoPorId(@PathVariable Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no existe con el id: " + id));
        return ResponseEntity.ok(producto);
    }

    // Actualizar producto
    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto productoRequest) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no existe con el id: " + id));
        producto.setNombre(productoRequest.getNombre());
        producto.setDescripcion(productoRequest.getDescripcion());
        producto.setPrecio(productoRequest.getPrecio());

        // Guardamos el producto actualizado
        Producto productoActualizado = productoRepository.save(producto);

        // Registrar la auditoría (action = "UPDATE")
        Auditoria auditoria = new Auditoria();
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setTipo("UPDATE");
        auditoria.setAccion("Se ha actualizado el producto");
        auditoria.setProductoId(productoActualizado.getId());
        auditoria.setProductoNombre(productoActualizado.getNombre());
        auditoria.setProductoDescripcion(productoActualizado.getDescripcion());
        auditoria.setProductoPrecio(String.valueOf(productoActualizado.getPrecio()));
        auditoriaRepository.save(auditoria); // Guardamos el registro de auditoría

        return ResponseEntity.ok(productoActualizado);
    }

    // Eliminar producto
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarProducto(@PathVariable Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no existe con el id: " + id));

        // Registrar la auditoría (action = "DELETE")
        Auditoria auditoria = new Auditoria();
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setTipo("DELETE");
        auditoria.setAccion("Se ha eliminado un producto");
        auditoria.setProductoId(producto.getId());
        auditoria.setProductoNombre(producto.getNombre());
        auditoria.setProductoDescripcion(producto.getDescripcion());
        auditoria.setProductoPrecio(String.valueOf(producto.getPrecio()));
        auditoriaRepository.save(auditoria); // Guardamos el registro de auditoría

        productoRepository.delete(producto); // Borramos el producto

        Map<String, Boolean> response = new HashMap<>();
        response.put("eliminado", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    // Endpoint para descargar productos en formato CSV
    @GetMapping("/productos/download/csv")
    public ResponseEntity<byte[]> descargarListaProductosCSV() throws IOException {
        List<Producto> productos = productoRepository.findAll();  // Obtenemos los productos desde el repositorio

        // Crear el archivo CSV
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(baos);

        // Escribimos los encabezados del CSV
        writer.write("ID,Nombre,Descripción,Precio\n");

        // Escribimos los datos de los productos
        for (Producto producto : productos) {
            writer.write(producto.getId() + "," + producto.getNombre() + "," + producto.getDescripcion() + "," + producto.getPrecio() + "\n");
        }

        writer.flush();
        byte[] fileContent = baos.toByteArray();

        // Devolvemos el archivo como una respuesta con el tipo de contenido adecuado
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(fileContent);
    }

    // Endpoint para descargar productos en formato PDF
    @GetMapping("/productos/download/pdf")
    public ResponseEntity<byte[]> descargarListaProductosPDF() throws IOException {
        List<Producto> productos = productoRepository.findAll();  // Obtenemos los productos desde el repositorio

        // Crear el archivo PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(baos);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        // Título del PDF
        Paragraph title = new Paragraph("Listado de Productos")
                .setFontSize(18)
                .setBold();
        document.add(title);

        // Crear tabla (4 columnas)
        Table table = new Table(4);
        table.addHeaderCell(new Cell().add(new Paragraph("ID")));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre")));
        table.addHeaderCell(new Cell().add(new Paragraph("Descripción")));
        table.addHeaderCell(new Cell().add(new Paragraph("Precio")));

        // Llenar la tabla con los productos
        for (Producto producto : productos) {
            table.addCell(String.valueOf(producto.getId()));
            table.addCell(producto.getNombre());
            table.addCell(producto.getDescripcion());
            table.addCell(String.valueOf(producto.getPrecio()));
        }

        // Añadir la tabla al documento
        document.add(table);

        // Cerrar el documento
        document.close();

        // Convertir el contenido del PDF a un array de bytes
        byte[] pdfBytes = baos.toByteArray();

        // Retornar el PDF como respuesta
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfBytes);
    }

    // Endpoint para descargar productos en formato JSON
    @GetMapping("/productos/download/json")
    public ResponseEntity<byte[]> descargarListaProductosJSON() throws IOException {
        List<Producto> productos = productoRepository.findAll();  // Obtenemos los productos desde el repositorio

        // Convertir la lista de productos a JSON
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] jsonData = objectMapper.writeValueAsBytes(productos);

        // Devolvemos el archivo JSON como respuesta
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.json")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(jsonData);
    }
}

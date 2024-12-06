package com.tecsup.controller;

import com.tecsup.Exception.ResourceNotFoundException;
import com.tecsup.model.Auditoria;
import com.tecsup.model.Categoria;
import com.tecsup.repository.AuditoriaRepository;
import com.tecsup.repository.CategoriaRepository;
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
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private AuditoriaRepository auditoriaRepository; // Inyectamos el repositorio de Auditoría

    @GetMapping("/categoria")
    public List<Categoria> listarCategoria() {
        return categoriaRepository.findAll();
    }

    @PostMapping("/categoria")
    public Categoria guardarCategoria(@RequestBody Categoria categoria) {
        // Guardamos la categoría
        Categoria savedCategoria = categoriaRepository.save(categoria);

        // Registrar la auditoría (action = "INSERT")
        Auditoria auditoria = new Auditoria();
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setTipo("INSERT");
        auditoria.setAccion("Se ha creado una nueva categoría");
        auditoria.setCategoriaId(savedCategoria.getId());
        auditoria.setCategoriaNombre(savedCategoria.getNombre());
        auditoriaRepository.save(auditoria); // Guardamos el registro de auditoría

        return savedCategoria;
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<Categoria> listarCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría no existe con el id: " + id));
        return ResponseEntity.ok(categoria);
    }

    @PutMapping("/categoria/{id}")
    public ResponseEntity<Categoria> actualizarCategoria(@PathVariable Long id, @RequestBody Categoria categoriaRequest) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría no existe con el id: " + id));
        categoria.setNombre(categoriaRequest.getNombre());

        // Guardar la categoría actualizada
        Categoria categoriaActualizada = categoriaRepository.save(categoria);

        // Registrar la auditoría (action = "UPDATE")
        Auditoria auditoria = new Auditoria();
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setTipo("UPDATE");
        auditoria.setAccion("Se ha actualizado la categoría");
        auditoria.setCategoriaId(categoriaActualizada.getId());
        auditoria.setCategoriaNombre(categoriaActualizada.getNombre());
        auditoriaRepository.save(auditoria); // Guardamos el registro de auditoría

        return ResponseEntity.ok(categoriaActualizada);
    }

    @DeleteMapping("/categoria/{id}")
    public ResponseEntity<Map<String, Boolean>> eliminarCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría no existe con el id: " + id));

        // Registrar la auditoría (action = "DELETE")
        Auditoria auditoria = new Auditoria();
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setTipo("DELETE");
        auditoria.setAccion("Se ha eliminado una categoría");
        auditoria.setCategoriaId(categoria.getId());
        auditoria.setCategoriaNombre(categoria.getNombre());
        auditoriaRepository.save(auditoria); // Guardamos el registro de auditoría

        categoriaRepository.delete(categoria); // Borramos la categoría

        Map<String, Boolean> response = new HashMap<>();
        response.put("eliminado", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    // Endpoint para descargar categorías en formato CSV
    @GetMapping("/categoria/download/csv")
    public ResponseEntity<byte[]> descargarListaCategoriasCSV() throws IOException {
        List<Categoria> categorias = categoriaRepository.findAll();  // Obtenemos las categorías desde el repositorio

        // Crear el archivo CSV
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(baos);

        // Escribimos los encabezados del CSV
        writer.write("ID,Nombre\n");

        // Escribimos los datos de las categorías
        for (Categoria categoria : categorias) {
            writer.write(categoria.getId() + "," + categoria.getNombre() + "\n");
        }

        writer.flush();
        byte[] fileContent = baos.toByteArray();

        // Devolvemos el archivo como una respuesta con el tipo de contenido adecuado
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categorias.csv")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(fileContent);
    }

    // Endpoint para descargar categorías en formato PDF
    @GetMapping("/categoria/download/pdf")
    public ResponseEntity<byte[]> descargarListaCategoriasPDF() throws IOException {
        List<Categoria> categorias = categoriaRepository.findAll();  // Obtenemos las categorías desde el repositorio

        // Crear el archivo PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = new PdfWriter(baos);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        // Título del PDF
        Paragraph title = new Paragraph("Listado de Categorías")
                .setFontSize(18)
                .setBold();
        document.add(title);

        // Crear tabla (2 columnas)
        Table table = new Table(2);
        table.addHeaderCell(new Cell().add(new Paragraph("ID")));
        table.addHeaderCell(new Cell().add(new Paragraph("Nombre")));

        // Llenar la tabla con las categorías
        for (Categoria categoria : categorias) {
            table.addCell(String.valueOf(categoria.getId()));
            table.addCell(categoria.getNombre());
        }

        // Añadir la tabla al documento
        document.add(table);

        // Cerrar el documento
        document.close();

        // Convertir el contenido del PDF a un array de bytes
        byte[] pdfBytes = baos.toByteArray();

        // Retornar el PDF como respuesta
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categorias.pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfBytes);
    }

    // Endpoint para descargar categorías en formato JSON
    @GetMapping("/categoria/download/json")
    public ResponseEntity<byte[]> descargarListaCategoriasJSON() throws IOException {
        List<Categoria> categorias = categoriaRepository.findAll();  // Obtenemos las categorías desde el repositorio

        // Convertir la lista de categorías a JSON
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] jsonData = objectMapper.writeValueAsBytes(categorias);

        // Devolvemos el archivo JSON como respuesta
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=categorias.json")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(jsonData);
    }
}
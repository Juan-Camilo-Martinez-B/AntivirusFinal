package com.antivirus.Antivirus.bst;

import java.util.ArrayList;
import java.util.List;

public class FileBST {

    // Clase interna para representar un nodo del BST
    public class Node {
        ScannedFile file;
        Node left, right;

        public Node(ScannedFile file) {
            this.file = file;
            this.left = null;
            this.right = null;
        }
    }

    private Node root;

    public FileBST() {
        root = null;
    }

    /**
     * Inserta un nuevo archivo escaneado en el árbol.
     * Se ordena primero por categoría y, en caso de empate, por la ruta del archivo.
     *
     * @param newFile el archivo escaneado a insertar
     */
    public void insert(ScannedFile newFile) {
        root = insertRec(root, newFile);
    }

    private Node insertRec(Node node, ScannedFile newFile) {
        if (node == null) {
            return new Node(newFile);
        }
        // Compara usando la categoría primero.
        int cmp = newFile.getCategory().compareTo(node.file.getCategory());
        if (cmp == 0) {
            // Si la categoría es igual, se ordena de forma secundaria por la ruta del archivo.
            cmp = newFile.getFilePath().compareTo(node.file.getFilePath());
        }
        if (cmp < 0) {
            node.left = insertRec(node.left, newFile);
        } else if (cmp > 0) {
            node.right = insertRec(node.right, newFile);
        }
        // Si es igual (duplicado), se ignora o se podría actualizar, según tu lógica.
        return node;
    }

    /**
     * Realiza un recorrido en orden y devuelve una lista de archivos escaneados ordenados.
     *
     * @return lista ordenada de ScannedFile
     */
    public List<ScannedFile> inOrderTraversal() {
        List<ScannedFile> result = new ArrayList<>();
        inOrderRec(root, result);
        return result;
    }

    private void inOrderRec(Node node, List<ScannedFile> result) {
        if (node != null) {
            inOrderRec(node.left, result);
            result.add(node.file);
            inOrderRec(node.right, result);
        }
    }

    /**
     * Permite buscar un archivo según su ruta.
     *
     * @param filePath la ruta del archivo a buscar
     * @return el objeto ScannedFile si se encuentra, o null en caso contrario
     */
    public ScannedFile search(String filePath) {
        Node node = searchRec(root, filePath);
        return (node != null) ? node.file : null;
    }

    private Node searchRec(Node node, String filePath) {
        if (node == null || node.file.getFilePath().equals(filePath)) {
            return node;
        }
        // Aquí se sigue comparando solo por ruta; en caso de necesitar buscar dentro de una categoría,
        // se podrían ampliar estas condiciones.
        int cmp = filePath.compareTo(node.file.getFilePath());
        if (cmp < 0) {
            return searchRec(node.left, filePath);
        } else {
            return searchRec(node.right, filePath);
        }
    }
}

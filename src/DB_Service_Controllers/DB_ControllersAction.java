/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DB_Service_Controllers;

import MaxDistelsa.DB_ControllerMax;
import MaxDistelsa.AgenciasWay.GetCategoriasMax;
import MaxDistelsa.HTTP_cat_categorias;
import Mysql.ConnectionDB;
import StoredProcedures.Stored;
import Utils.GetDate;
import Utils.UniversalIdentifaction;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.productosURLs;

/**
 *
 * @author agr12
 */
public class DB_ControllersAction {

    public void saveCategorias(List<productosURLs> categorias, String div, String tienda, String idTienda) {
        UniversalIdentifaction uuid = new UniversalIdentifaction();
        GetDate getDate = new GetDate();
        String dateFormat = getDate.getFormatDate();
        String PK_UUID_LOG_LECTURA = uuid.uuidScrap();

        Connection conn = null;
        CallableStatement stmtLog = null;
        try {
            conn = ConnectionDB.getConnection();
            Stored stored = new Stored();
            stmtLog = conn.prepareCall(stored.STORED_PROCEDURE_INSERT_LOG_CAT_LECTURA);
            stmtLog.setString(1, PK_UUID_LOG_LECTURA);
            stmtLog.setString(2, "1");
            stmtLog.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al ejecutar el Stored Procedure de log", e);
        } finally {
            if (stmtLog != null) {
                try {
                    stmtLog.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar el CallableStatement de log", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar la conexión a la base de datos del log inicial", e);
                }
            }
        }

        for (productosURLs categoria : categorias) {
            Connection connCat = null;
            CallableStatement stmt = null;
            try {
                connCat = ConnectionDB.getConnection();
                Stored stored = new Stored();
                DB_ControllersAction scrap = new DB_ControllersAction();
                String UUID_CATEGORIA = uuid.uuidScrap();

                // Inserción en categorias
                stmt = connCat.prepareCall(stored.STORED_PROCEDURE_INSERT_CATEGORIA);
                stmt.setString(1, tienda);
                stmt.setString(2, idTienda);
                stmt.setString(3, categoria.getCategoria());
                stmt.setString(4, categoria.getNombreProducto());
                stmt.setString(5, categoria.getUrlProducto());
                stmt.setString(6, dateFormat);
                stmt.setString(7, PK_UUID_LOG_LECTURA);
                stmt.setString(8, UUID_CATEGORIA);

                stmt.executeUpdate();
                System.out.println("Registro insertado correctamente: " + stmt.toString());

                // Llamada al metodo de lectura de pagina
                scrap.lectura_of_page(categoria.getUrlProducto(), div, UUID_CATEGORIA);

            } catch (SQLException e) {
                Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al ejecutar el Stored Procedure de categorias", e);
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar el CallableStatement de categorias", e);
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar la conexión a la base de datos de categorias", e);
                    }
                }
            }
        }

    }

    public void lectura_of_page(String urlSearch, String div, String UUID_CATEGORIA) {
        Connection conn = null;
        CallableStatement stmt = null;

        try {
            Stored stored = new Stored();
            HTTP_cat_categorias getCategoria = new HTTP_cat_categorias();
            List<productosURLs> listPages = (List<productosURLs>) getCategoria.runPages(urlSearch, div);//Cambiar
            conn = ConnectionDB.getConnection(); // Abrir conexión

            for (productosURLs page : listPages) {
                // Verificar si page es null y si getUrlProducto() no es null ni vac�o
                if (page != null && page.getUrlProducto() != null && !page.getUrlProducto().isEmpty()) {
                    try {
                        // Preparar el Stored Procedure
                        stmt = conn.prepareCall(stored.STORED_PROCEDURE_INSERT_URLS_PAGINADAS);
                        UniversalIdentifaction uuid = new UniversalIdentifaction();
                        String UUID_PAGINA = uuid.uuidScrap();

                        // Establecer el parametro del Stored Procedure
                        stmt.setString(1, page.getUrlProducto());
                        stmt.setString(2, urlSearch);
                        stmt.setString(3, UUID_CATEGORIA);
                        stmt.setString(4, UUID_PAGINA);

                        // Ejecutar el Stored Procedure
                        stmt.executeUpdate();
                        System.out.println("Registro insertado correctamente: " + stmt.toString());

                    } catch (SQLException e) {
                        // Manejar excepciones SQL
                        e.printStackTrace();
                    } finally {
                        // Asegurate de cerrar el CallableStatement despues de cada uso
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Asegurate de cerrar la conexión despues de que se hayan procesado todos los elementos
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

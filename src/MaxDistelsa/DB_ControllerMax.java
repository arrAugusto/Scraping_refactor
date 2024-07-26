/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MaxDistelsa;

import MaxDistelsa.AgenciasWay.GetCategoriasMax;
import MaxDistelsa.GetProductoMax;
import Mysql.ConnectionDB;
import StoredProcedures.Stored;
import Utils.GetDate;
import Utils.UniversalIdentifaction;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Descriptions;
import models.Especificaciones;
import models.Pagina;
import models.Producto;
import models.URL_prod;

/**
 *
 * @author agr12
 */
public class DB_ControllerMax {


    /*
        PROCESS NUMBER 1
     */
    public void leerUrlForPage(String div, String anchorHTML) {
        try {
            Stored stored = new Stored();
            Connection conn = ConnectionDB.getConnection();
            GetDate getAmerican = new GetDate();
            DB_ControllerMax runRobot = new DB_ControllerMax();
            String date = getAmerican.getFormatDateAmerican();
            try (CallableStatement stmt = conn.prepareCall(stored.STORED_PROCEDURE_GET_LOG_LECTURA)) {
                // Establecer el parametro del Stored Procedure
                stmt.setString(1, date);
                System.out.println(stmt.toString());
                // Ejecutar el procedimiento almacenado
                try (ResultSet resultSet = stmt.executeQuery()) {
                    // Procesar los resultados
                    while (resultSet.next()) {

                        String estado_productos = resultSet.getString("estado_productos");
                        String FK_UUID_LOG_LECTURA = resultSet.getString("PK_UUID_LOG_LECTURA");
                        String number_pages = resultSet.getString("number_pages");

                        if (estado_productos.equals("0")) {
                            int pagina = 0;
                            int numerPages = Integer.parseInt(number_pages);
                            int paginado = (numerPages / 100);
                            if (paginado == 0) {
                                paginado += 1;
                            }
                            for (int i = 0; i < paginado; i++) {
                                System.out.println("pagina:: " + i + 1);
                                List<Pagina> getConsulta = runRobot.getConsultaSegmentada(pagina, FK_UUID_LOG_LECTURA);
                                runRobot.saveURLForPage(getConsulta, div, anchorHTML, FK_UUID_LOG_LECTURA);
                                pagina += 100;
                            }
                        }

                    }
                }
            } catch (SQLException e) {
                // Manejar excepciones SQL
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Pagina> getConsultaSegmentada(int pagina, String FK_UUID_LOG_LECTURA) {
        List<Pagina> listPage = new ArrayList<>();
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            Stored stored = new Stored();
            conn = ConnectionDB.getConnection();
            stmt = conn.prepareCall(stored.STORED_PROCEDURE_GET_PAGINAS_LECTURA);

            // Establecer el par�metro del Stored Procedure
            stmt.setInt(1, pagina);
            stmt.setString(2, FK_UUID_LOG_LECTURA);

            // Ejecutar el procedimiento almacenado
            rs = stmt.executeQuery();

            // Procesar los resultados
            while (rs.next()) {
                Pagina page = new Pagina();
                page.setUrl_page(rs.getString("url_pagina"));
                page.setUuid_page(rs.getString("PK_UUID_PAGINA"));
                page.setUuid_categoria(rs.getString("FK_UUID_CATEGORIA"));
                page.setUuid_categoria(rs.getString("FK_UUID_LOG_LECTURA"));
                listPage.add(page);
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Asegurarse de cerrar el ResultSet
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar ResultSet", e);
                }
            }
            // Asegurarse de cerrar el CallableStatement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar CallableStatement", e);
                }
            }
            // Asegurarse de cerrar la conexión
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar la conexión a la base de datos", e);
                }
            }
        }

        return listPage;
    }

    public void saveURLForPage(List<Pagina> paginas, String div, String anchorHTML, String FK_UUID_LOG_LECTURA) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>");
        Connection conn = null;
        try {
            Stored stored = new Stored();
            GetCategoriasMax peticion = new GetCategoriasMax();
            GetDate getDate = new GetDate();
            String _fecha = getDate.getFormatDate();

            conn = ConnectionDB.getConnection(); // Obtener la conexión

            for (Pagina pagina : paginas) {
                System.out.println("pagina.getUrl_page()> " + pagina.getUrl_page());
                List<URL_prod> urls_page = peticion.urlDataProduct(pagina.getUrl_page(), div, anchorHTML);
                for (URL_prod urlProd : urls_page) {
                    CallableStatement stmt = null;
                    try {
                        stmt = conn.prepareCall(stored.STORED_PROCEDURE_INSERT_URL_FOR_PAGE);
                        UniversalIdentifaction uuid = new UniversalIdentifaction();

                        // Establecer el par�metro del Stored Procedure
                        stmt.setString(1, urlProd.getUrl());
                        stmt.setString(2, pagina.getUrl_page());
                        stmt.setString(3, _fecha);
                        stmt.setString(4, uuid.uuidScrap());
                        stmt.setString(5, pagina.getUuid_page());
                        stmt.setString(6, FK_UUID_LOG_LECTURA);

                        // Ejecutar el Stored Procedure
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        // Manejar excepciones SQL espec�ficas
                        System.err.println("Error al ejecutar el Stored Procedure: " + e.getMessage());
                    } finally {
                        // Asegurarse de cerrar el CallableStatement
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                System.err.println("Error al cerrar CallableStatement: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error en la conexión a la base de datos", ex);
        } finally {
            // Asegurarse de cerrar la conexión en el bloque finally
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar la conexión a la base de datos", e);
                }
            }
        }
    }

    public void saveProduct(String tipo) {
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet resultSet = null;

        try {
            Stored stored = new Stored();
            conn = ConnectionDB.getConnection();
            GetDate getAmerican = new GetDate();
            DB_ControllerMax runRobot = new DB_ControllerMax();
            String date = getAmerican.getFormatDateAmerican();
            int status = 0;

            stmt = conn.prepareCall(stored.STORED_PROCEDURE_GET_LOG_LECTURA);
            stmt.setString(1, date);
            System.out.println("Consulta exitosa: " + stmt.toString());
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                String PK_UUID_LOG_LECTURA = resultSet.getString("PK_UUID_LOG_LECTURA");
                String number_urls_prod = resultSet.getString("number_urls_products");

                int paginado_urls = Integer.parseInt(number_urls_prod);
                if (status == 0) {
                    try (CallableStatement stmtLog = conn.prepareCall(stored.STORED_PROCEDURE_UPDATE_LOG_CAT_LECTURA_CARGA)) {
                        stmtLog.setString(1, PK_UUID_LOG_LECTURA);
                        stmtLog.executeUpdate();
                        System.out.println("Consulta exitosa. " + stmtLog.toString());
                    } catch (SQLException e) {
                        Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al ejecutar el Stored Procedure", e);
                    }
                }
                status = 1;

                System.out.println("paginado_urls> " + paginado_urls);

                int registros_por_pagina = 100;
                int paginas_load = (int) Math.ceil((double) paginado_urls / registros_por_pagina);

                System.out.println("paginas_load> " + paginas_load + " " + tipo);
                for (int i = 0; i < paginas_load; i++) {
                    int pagina = i * registros_por_pagina; // Calcular el índice de inicio de la página
                    System.out.println("i> " + i + ", pagina> " + pagina + " " + tipo);

                    List<Pagina> listPagina = runRobot.getConsultaSegmentadaProd(pagina, PK_UUID_LOG_LECTURA);
                    System.out.println("listaPagina " + listPagina.size());
                    switch (tipo) {
                        case "WAY":
                            runRobot.saveProductionWAY(listPagina);
                            break;
                        case "MAX":
                            runRobot.saveProductionMAX(listPagina);
                            break;
                        case "PANAMERICANA":
                            runRobot.saveProductionPanamericana(listPagina);
                            break;

                    }
                    pagina += 100;
                }

            }

        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Cerrar ResultSet
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar ResultSet", e);
                }
            }
            // Cerrar CallableStatement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar CallableStatement", e);
                }
            }
            // Cerrar Connection
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar Connection", e);
                }
            }
        }
    }

    public void saveProductionWAY(List<Pagina> listPagina) {
        Stored stored = new Stored();
        GetProductoWAY getProd = new GetProductoWAY();
        GetDate getDate = new GetDate();
        String _fecha = getDate.getFormatDate();
        UniversalIdentifaction uuid = new UniversalIdentifaction();
        DB_ControllerMax save = new DB_ControllerMax();

        Connection conn = null;
        CallableStatement stmt = null;
        System.out.println(listPagina.toString());
        try {
            if (listPagina.size() < 1) {
                System.out.println("No se pueden insertar valores vacios");
                return;
            }
            conn = ConnectionDB.getConnection();

            for (Pagina pagina : listPagina) {
                Producto producto = (Producto) getProd.getAllDataWAY(pagina.getUrl_page());
                String UUID_PRODUCTO = uuid.uuidScrap();

                try {
                    stmt = conn.prepareCall(stored.STORED_PROCEDURE_INSERT_PRODUCTO_READ);
                    // Establecer el parametro del Stored Procedure
                    stmt.setString(1, producto.getUrl_img_prod());
                    stmt.setString(2, producto.getUrl_marca());
                    stmt.setString(3, pagina.getUrl_page());
                    stmt.setString(4, producto.getTag_name());
                    stmt.setString(5, producto.getSkuValue());
                    stmt.setString(6, producto.getSku());
                    stmt.setString(7, producto.getDisponibilidad());
                    stmt.setDouble(8, producto.getPrice());
                    stmt.setDouble(9, producto.getPriceOld());
                    stmt.setDouble(10, 0.00);
                    stmt.setString(11, _fecha);
                    stmt.setString(12, UUID_PRODUCTO);
                    stmt.setString(13, pagina.getUuid_page());
                    stmt.setString(14, pagina.getUuid_categoria());
                    stmt.setString(15, pagina.getUuid_log_lectura());

                    // Ejecutar el Stored Procedure
                    stmt.executeUpdate();
                    save.saveDescription(UUID_PRODUCTO, producto.getDescriptions());
                    save.saveEspecificacion(UUID_PRODUCTO, producto.getEspecificaciones());

                } catch (SQLException e) {
                    // Manejar excepciones SQL
                    e.printStackTrace();
                } finally {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Pagina> getConsultaSegmentadaProd(int pagina, String UUID_SCRAPING) {
        System.out.println("UUID_SCRAPING> " + UUID_SCRAPING + " " + pagina);
        List<Pagina> listPage = new ArrayList<>();
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        System.out.println("consulta");
        try {
            Stored stored = new Stored();
            conn = ConnectionDB.getConnection();
            stmt = conn.prepareCall(stored.STORED_PROCEDURE_GET_URLS_PRODUCTS);

            // Establecer el par�metro del Stored Procedure
            stmt.setInt(1, pagina);
            stmt.setString(2, UUID_SCRAPING);
            System.out.println("Consulta exitosa: " + stmt.toString());

            // Ejecutar el procedimiento almacenado
            rs = stmt.executeQuery();
            // Procesar los resultados
            while (rs.next()) {
                Pagina page = new Pagina();
                page.setUrl_page(rs.getString("url_for_page"));
                page.setUuid_page(rs.getString("PK_UUID_FOR_PAGE"));
                page.setUuid_categoria(rs.getString("FK_UUID_PAGINA"));
                page.setUuid_log_lectura(rs.getString("FK_UUID_LOG_LECTURA"));
                listPage.add(page);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Cerrar ResultSet
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar ResultSet", e);
                }
            }
            // Cerrar CallableStatement
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar CallableStatement", e);
                }
            }
            // Cerrar Connection
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, "Error al cerrar Connection", e);
                }
            }
        }

        return listPage;
    }

    public void saveProductionMAX(List<Pagina> listPagina) {
        // Crear un pool de 3 hilos
        Stored stored = new Stored();
        GetProductoMax getProd = new GetProductoMax();
        GetDate getDate = new GetDate();
        String _fecha = getDate.getFormatDate();
        UniversalIdentifaction uuid = new UniversalIdentifaction();
        DB_ControllerMax save = new DB_ControllerMax();

        for (Pagina pagina : listPagina) {
            Connection conn = null;
            CallableStatement stmt = null;
            try {
                conn = ConnectionDB.getConnection();
                String UUID_PRODUCTO = uuid.uuidScrap();

                Producto producto = (Producto) getProd.getAllData(pagina.getUrl_page());
                if (producto.getTag_name() == null || producto.getTag_name().isEmpty()) {
                    return;
                }
                stmt = conn.prepareCall(stored.STORED_PROCEDURE_INSERT_PRODUCTO_READ);
                // Establecer el parametro del Stored Procedure
                stmt.setString(1, producto.getUrl_img_prod());
                stmt.setString(2, producto.getUrl_marca());
                stmt.setString(3, pagina.getUrl_page());
                stmt.setString(4, producto.getTag_name());
                stmt.setString(5, producto.getSkuValue());
                stmt.setString(6, producto.getSku());
                stmt.setString(7, producto.getDisponibilidad());
                stmt.setDouble(8, producto.getPrice());
                stmt.setDouble(9, producto.getPriceOld());
                stmt.setDouble(10, 0.00);
                stmt.setString(11, _fecha);
                stmt.setString(12, UUID_PRODUCTO);
                stmt.setString(13, pagina.getUuid_page());
                stmt.setString(14, pagina.getUuid_categoria());
                stmt.setString(15, pagina.getUuid_log_lectura());
                System.out.println("Insert producto: " + stmt.toString());
                // Ejecutar el Stored Procedure
                stmt.executeUpdate();
                System.out.println("Registro insertado correctamente: " + stmt.toString());
                save.saveDescription(UUID_PRODUCTO, producto.getDescriptions());
                save.saveEspecificacion(UUID_PRODUCTO, producto.getEspecificaciones());
            } catch (SQLException e) {
                // Manejar excepciones SQL
                e.printStackTrace();
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
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

    public void saveProductionPanamericana(List<Pagina> listPagina) {
        // Crear un pool de 3 hilos
        Stored stored = new Stored();
        GetProductoPanamericana getProd = new GetProductoPanamericana();
        GetDate getDate = new GetDate();
        String _fecha = getDate.getFormatDate();
        UniversalIdentifaction uuid = new UniversalIdentifaction();
        DB_ControllerMax save = new DB_ControllerMax();

        for (Pagina pagina : listPagina) {
            Connection conn = null;
            CallableStatement stmt = null;
            try {
                conn = ConnectionDB.getConnection();
                String UUID_PRODUCTO = uuid.uuidScrap();

                Producto producto = (Producto) getProd.getAllDataPanamericana(pagina.getUrl_page());
                if (producto.getTag_name() == null || producto.getTag_name().isEmpty()) {
                    return;
                }
                stmt = conn.prepareCall(stored.STORED_PROCEDURE_INSERT_PRODUCTO_READ);
                // Establecer el parametro del Stored Procedure
                stmt.setString(1, producto.getUrl_img_prod());
                stmt.setString(2, producto.getUrl_marca());
                stmt.setString(3, pagina.getUrl_page());
                stmt.setString(4, producto.getTag_name());
                stmt.setString(5, producto.getSkuValue());
                stmt.setString(6, producto.getSku());
                stmt.setString(7, producto.getDisponibilidad());
                stmt.setDouble(8, producto.getPrice());
                stmt.setDouble(9, producto.getPriceOld());
                stmt.setDouble(10, 0.00);
                stmt.setString(11, _fecha);
                stmt.setString(12, UUID_PRODUCTO);
                stmt.setString(13, pagina.getUuid_page());
                stmt.setString(14, pagina.getUuid_categoria());
                stmt.setString(15, pagina.getUuid_log_lectura());
                System.out.println("Insert producto: " + stmt.toString());
                // Ejecutar el Stored Procedure
                stmt.executeUpdate();
                System.out.println("Registro insertado correctamente: " + stmt.toString());
                save.saveDescription(UUID_PRODUCTO, producto.getDescriptions());
                save.saveEspecificacion(UUID_PRODUCTO, producto.getEspecificaciones());
            } catch (SQLException e) {
                // Manejar excepciones SQL
                e.printStackTrace();
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
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

    public void saveDescription(String uuidProd, List<Descriptions> descriptions) {
        if (descriptions == null || uuidProd == null) {
            System.out.println("La lista de descripciones o el UUID del producto son nulos.");
            return;
        }

        Connection conn = null;

        try {
            Stored stored = new Stored();
            conn = ConnectionDB.getConnection();

            for (int i = 0; i < descriptions.size(); i++) {
                Descriptions description = descriptions.get(i);

                if (description == null || description.getDescripcion() == null) {
                    System.out.println("Descripci�n o elemento en la lista es nulo en el �ndice " + i);
                    continue;
                }

                try (CallableStatement stmt = conn.prepareCall(stored.STORED_PROCEDURE_INSERT_DESCRIPTION)) {
                    // Establecer el par�metro del Stored Procedure
                    stmt.setString(1, description.getDescripcion());
                    stmt.setString(2, uuidProd);

                    // Ejecutar el Stored Procedure
                    stmt.executeUpdate();

                } catch (SQLException e) {
                    // Manejar excepciones SQL
                    e.printStackTrace();
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Cerrar Connection
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveEspecificacion(String uuidProd, List<Especificaciones> especificaciones) {
        if (especificaciones == null || uuidProd == null) {
            System.out.println("La lista de especificaciones o el UUID del producto son nulos.");
            return;
        }

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            Stored stored = new Stored();
            conn = ConnectionDB.getConnection();

            for (int i = 0; i < especificaciones.size(); i++) {
                Especificaciones especificacion = especificaciones.get(i);

                if (especificacion == null || especificacion.getEspecificacion() == null || especificacion.getDescripcion() == null) {
                    System.out.println("Especificaci�n o Descripci�n es nula en el �ndice " + i);
                    continue;
                }

                try {
                    stmt = conn.prepareCall(stored.STORED_PROCEDURE_INSERT_ESPECIFICACIONES);
                    // Establecer el par�metro del Stored Procedure
                    stmt.setString(1, especificacion.getEspecificacion());
                    stmt.setString(2, especificacion.getDescripcion());
                    stmt.setString(3, uuidProd);

                    // Ejecutar el Stored Procedure
                    stmt.executeUpdate();
                    System.out.println("Registro insertado correctamente: " + stmt.toString());

                } catch (SQLException e) {
                    // Manejar excepciones SQL
                    e.printStackTrace();
                } finally {
                    // Cerrar CallableStatement
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB_ControllerMax.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Cerrar Connection
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

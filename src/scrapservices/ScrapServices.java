/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package scrapservices;

import DB_Service_Controllers.DB_ControllersAction;
import MaxDistelsa.AgenciasWay.GetCategoriasWay;
import MaxDistelsa.DB_ControllerMax;
import Utils.DeleteListCategorias;
import java.util.List;
import models.productosURLs;

/**
 *
 * @author agr12
 */
public class ScrapServices {

    public static final String urlBase = "https://www.max.com.gt/"; // Cambia esto a la URL que necesites
    public static final String tienda = "MAX DISTELSA";
    public static final String idTienda = "05264";
    public static final String divPaginator = ".action.next";

    public static final String urlBaseWAY = "https://agenciaswayonline.com/categorias/"; // Agencias way
    public static final String tiendaWAY = "AGENCIAS WAY";
    public static final String idTiendaWAY = "05267";

    public static final String urlPanamericana = "https://electronicapanamericana.com/"; // Panamericana
    public static final String tiendaPanamericana = "ELECTRONICA PANAMERICANA";
    public static final String idTiendaPanamericana = "05268";

    public static void main(String[] args) {
        int part = 1;
        String run = "MAX";
        
        // Inicializar la primera parte
        if (part == 1 && run.equals("MAX")) {
            GetCategoriasWay getCategoryWAY = new GetCategoriasWay();
            DB_ControllersAction db_controllerMax = new DB_ControllersAction();
            List<productosURLs> old_categoriasMax = (List<productosURLs>) getCategoryWAY.getCategoria(urlBase, "div.section-item-content");
            DeleteListCategorias new_categoriasMax = new DeleteListCategorias();

            // NUMERAR PAGINAS
            List<productosURLs> categorias = new_categoriasMax.eliminarDuplicados(old_categoriasMax);
            db_controllerMax.saveCategorias(categorias, ".action.next", tienda, idTienda); // {.paginator} agencias way {.action.next} agencias max

            // Limpieza de memoria RAM y conexiones de MySQL para la primera parte
            old_categoriasMax = null;
            categorias = null;
            new_categoriasMax = null;
            getCategoryWAY = null;
            db_controllerMax = null; // Método para cerrar conexiones de DB
            db_controllerMax = null;

            System.gc(); // Llamada explícita al recolector de basura
        }

        // Inicializar la segunda parte
        if (part == 2 && run.equals("MAX")) {
            DB_ControllerMax dbControllers = new DB_ControllerMax();
            dbControllers.leerUrlForPage(".product-items", "a.product.photo.product-item-photo[href]"); // {".product-items", "a.product.photo.product-item-photo[href]"} div y anchor html de max {".wrap-products", ".block-item a"} div y anchor agencias way
            System.gc(); // Llamada explícita al recolector de basura

        }
        if (part == 3 && run.equals("MAX")) {
            DB_ControllerMax wayProduct = new DB_ControllerMax();
            wayProduct.saveProduct("MAX");
            System.gc(); // Llamada explícita al recolector de basura
        }

        //{CATEGORIAS SCRAPING AGENCIAS WAY}
        if (part == 1 && run.equals("WAY")) {
            GetCategoriasWay getCategoryWAY = new GetCategoriasWay();
            DB_ControllersAction db_controllerWAY = new DB_ControllersAction();
            List<productosURLs> old_categoriasWAY = (List<productosURLs>) getCategoryWAY.getCategoria(urlBaseWAY, "div.wrap-categories");
            DeleteListCategorias new_categoriasWAY = new DeleteListCategorias();

            //NUMERAR PAGINAS
            List<productosURLs> categoriasWAY = (List<productosURLs>) new_categoriasWAY.eliminarDuplicados(old_categoriasWAY);
            db_controllerWAY.saveCategorias(categoriasWAY, ".paginator", tiendaWAY, idTiendaWAY);//{.paginator} agencias way {.action.next} agencias max
            System.gc(); // Llamada explícita al recolector de basura
        }
        if (part == 2 && run.equals("WAY")) {
            DB_ControllerMax dbControllersWAY = new DB_ControllerMax();

            dbControllersWAY.leerUrlForPage(".wrap-products", ".block-item__thumb");//{".product-items", "a.product.photo.product-item-photo[href]"}div y anchor html de max {".wrap-products", ".block-item a"} div y anchor agencias way
            System.gc(); // Llamada explícita al recolector de basura
        }
        if (part == 3 && run.equals("WAY")) {
            //RECORER PAGINAS
            DB_ControllerMax wayProductWAY = new DB_ControllerMax();
            wayProductWAY.saveProduct("WAY");
            System.gc(); // Llamada explícita al recolector de basura
        }
        //panamericana
        //{CATEGORIAS SCRAPING PANAMERICANA}
        if (part == 1 && run.equals("PANAMERICANA")) {
            GetCategoriasWay getCategoryWAY = new GetCategoriasWay();
            DB_ControllersAction db_controllerWAY = new DB_ControllersAction();
            List<productosURLs> old_categoriasWAY = (List<productosURLs>) getCategoryWAY.getCategoria(urlPanamericana, "ul.sub-menu");
            DeleteListCategorias new_categoriasWAY = new DeleteListCategorias();
            //NUMERAR PAGINAS
            List<productosURLs> categoriasWAY = (List<productosURLs>) new_categoriasWAY.eliminarDuplicados(old_categoriasWAY);
            db_controllerWAY.saveCategorias(categoriasWAY, ".paginator", tiendaPanamericana, idTiendaPanamericana);//{.paginator} agencias way {.action.next} agencias max
            System.gc(); // Llamada explícita al recolector de basura
        }

        if (part == 2 && run.equals("PANAMERICANA")) {
            DB_ControllerMax dbControllersWAY = new DB_ControllerMax();

            dbControllersWAY.leerUrlForPage(".products", "div.inner_product > a");//{".product-items", "a.product.photo.product-item-photo[href]"}div y anchor html de max {".wrap-products", ".block-item a"} div y anchor agencias way
            System.gc(); // Llamada explícita al recolector de basura
        }

        if (part == 3 && run.equals("PANAMERICANA")) {
            //RECORER PAGINAS
            DB_ControllerMax wayProductWAY = new DB_ControllerMax();
            wayProductWAY.saveProduct("PANAMERICANA");
            System.gc(); // Llamada explícita al recolector de basura
       }
    }

}

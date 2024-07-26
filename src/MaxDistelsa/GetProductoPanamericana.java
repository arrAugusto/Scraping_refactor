/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MaxDistelsa;

import models.Descriptions;
import models.Especificaciones;
import models.Producto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author agr12
 */
public class GetProductoPanamericana {

    private static final int TIMEOUT = 10000; // Tiempo de espera en milisegundos

    public Producto getAllDataPanamericana(String newURL) {
        Producto producto = new Producto();

        try {
            GetProductoPanamericana getProd = new GetProductoPanamericana();
            Document doc = null;

            try {
                // Intenta conectar y obtener los datos con un timeout de 10 segundos
                doc = Jsoup.connect(newURL).timeout(TIMEOUT).get();
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("Tiempo de espera agotado para la URL: " + newURL + " - " + e.getMessage());
                // Log del error y continuar el proceso, devolviendo el producto con datos incompletos
                return producto;
            } catch (IOException e) {
                System.err.println("Error al conectar con la URL: " + newURL + " - " + e.getMessage());
                // Log del error y continuar el proceso, devolviendo el producto con datos incompletos
                return producto;
            }

            // Establece los valores básicos del producto si el documento no es nulo
            if (doc != null) {
                producto.setUrl_marca(getProd.getUrlMarca(doc, "div.pwb-single-product-brands"));
                producto.setUrl_img_prod(getProd.getImageURL(doc, "div.woocommerce-product-gallery__wrapper"));
                producto.setTag_name(getProd.getTitle(doc, "h1.product_title"));
                producto.setSkuValue(getProd.getSKU(doc, "span.sku_wrapper"));

                try {
                    // Obtiene y limpia los valores de precio actual
                    String price = getProd.getPriceValue(doc, "p.price");
                    double priceValue = Double.parseDouble(price.replaceAll("[^\\d.]", ""));
                    producto.setPrice(priceValue);
                } catch (Exception e) {
                    System.out.println("No se encontrados precio actual: " + e.getMessage());
                    producto.setPrice(0.00);
                }

                try {
                    // Obtiene y limpia los valores de precio antiguo
                    String priceOld = getProd.getOldPriceValue(doc, "span.woocommerce-Price-amount");
                    double priceOldValue = Double.parseDouble(priceOld.replaceAll("[^\\d.]", ""));
                    producto.setPriceOld(priceOldValue);
                } catch (Exception e) {
                    System.out.println("No se encontrados precio antiguo: " + e.getMessage());
                    producto.setPriceOld(0.00);
                }

                try {
                    // Establece la Descripción del producto
                    producto.setEspecificaciones(getProd.getAdditionalAttributes(doc));
                } catch (Exception ex) {
                    System.out.println("Error al obtener la Descripción del producto: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GetProductoPanamericana.class.getName()).log(Level.SEVERE, null, ex);
        }
        return producto;
    }

    public String getTitle(Document doc, String div) {
        // Seleccionar el div con la clase "page-title-wrapper product"
        Element productInfoMain = doc.selectFirst(div);
        if (productInfoMain != null) {
            // Seleccionar el span dentro del div
            Element spanElement = productInfoMain.selectFirst("h1");
            if (spanElement != null) {
                // Obtener el texto del span
                String spanText = spanElement.text();
                System.out.println("Texto del h1: " + spanText);
                return spanText;
            } else {
                System.out.println("No se encontrados el span dentro del div.");
            }
        } else {
            System.out.println("No se encontrados el div con la clase '" + div + "'.");
        }
        return "";
    }

    public String getUrlMarca(Document doc, String div) {
        // Seleccionar el div con la clase específica proporcionada
        Element productInfoMain = doc.selectFirst(div);
        if (productInfoMain != null) {
            // Seleccionar todos los elementos <noscript> dentro del div
            Elements noscripts = productInfoMain.select("noscript");
            if (noscripts != null && !noscripts.isEmpty()) {
                // Obtener el último elemento <noscript>
                Element lastNoscript = noscripts.last();
                // Parsear el contenido de <noscript> como un nuevo documento
                Document noscriptDoc = Jsoup.parse(lastNoscript.html());
                // Seleccionar la imagen dentro del <noscript>
                Element img = noscriptDoc.selectFirst("img");
                if (img != null) {
                    // Obtener la URL de la imagen desde el atributo src
                    String imgUrl = img.attr("src");
                    System.out.println("URL de la imagen en el último <noscript>: " + imgUrl);
                    return imgUrl;
                } else {
                    System.out.println("No se encontró una imagen dentro del último <noscript>.");
                }
            } else {
                System.out.println("No se encontró ningún elemento <noscript> dentro del div con la clase '" + div + "'.");
            }
        } else {
            System.out.println("El div con la clase '" + div + "' no se encontró.");
        }
        return "";
    }

    public String getSKU(Document doc, String div) {
        // Seleccionar el div con la clase "page-title-wrapper product"
        Element productInfoMain = doc.selectFirst(div);
        if (productInfoMain != null) {
            // Seleccionar el div con la clase "value" dentro del div principal
            Element valueElement = productInfoMain.selectFirst("span");
            if (valueElement != null) {
                // Obtener el texto del div con la clase "value"
                String valueText = valueElement.text();
                System.out.println("Texto del div con la clase 'value': " + valueText);
                return valueText;
            } else {
                System.out.println("No se encontrados el div con la clase 'value' dentro del div.");
            }
        } else {
            System.out.println("No se encontrados el div con la clase '" + div + "'.");
        }
        return "";
    }

    public String getPriceValue(Document doc, String div) {
        // Seleccionar el div con la clase "price-box price-final_price"
        Element priceBox = doc.selectFirst(div);
        if (priceBox != null) {
            // Seleccionar el span con la clase "price" dentro del div principal
            Element priceElement = priceBox.selectFirst("ins");
            if (priceElement != null) {
                // Obtener el texto del span con la clase "price"
                String priceText = priceElement.text();
                System.out.println("Precio: " + priceText);
                return priceText;
            } else {
                System.out.println("No se encontrados el span con la clase 'price' dentro del div.");
            }
        } else {
            System.out.println("No se encontrados el div con la clase '" + div + "'.");
        }
        return "";
    }

    public String getOldPriceValue(Document doc, String div) {
        // Seleccionar el div con la clase "price-box price-final_price"
        Element priceBox = doc.selectFirst(div);

        if (priceBox != null) {
            // Seleccionar el span con la clase "price" dentro del div principal
            Element priceElement = priceBox.selectFirst("span");
            if (priceElement != null) {
                // Obtener el texto del span con la clase "price"
                String priceText = priceElement.text();
                System.out.println("old-price: " + priceText);
                return priceText;
            } else {
                System.out.println("No se encontrados el span con la clase 'price' dentro del div.");
            }
        } else {
            System.out.println("No se encontrados el div con la clase '" + div + "'.");
        }
        return "";
    }

    public List<Especificaciones> getAdditionalAttributes(Document doc) {
        List<Especificaciones> attributes = new ArrayList<>();

        // Seleccionar el div con la clase específica proporcionada
        Element attributesWrapper = doc.selectFirst("div.woocommerce-Tabs-panel--description");
        if (attributesWrapper != null) {
            // Seleccionar la tabla dentro del div
            Element table = attributesWrapper.selectFirst("table");
            if (table != null) {
                // Seleccionar todas las filas de la tabla
                Elements rows = table.select("tbody tr");
                for (Element row : rows) {
                    Especificaciones especificacion = new Especificaciones();
                    // Obtener el primer td (tipo) y el segundo td (Descripción) en cada fila
                    Elements tds = row.select("td");
                    if (tds.size() == 2) {
                        String type = tds.get(0).text();
                        String description = tds.get(1).text();
                        especificacion.setEspecificacion(type);
                        especificacion.setDescripcion(description);

                        attributes.add(especificacion);
                        System.out.println("Tipo: " + type + ", Descripción: " + description);
                    }
                }
            } else {
                System.out.println("No se encontró la tabla dentro del div.");
            }
        } else {
            System.out.println("No se encontró el div con la clase 'woocommerce-Tabs-panel--description'.");
        }

        return attributes;
    }

    public String getImageURL(Document doc, String div) {
        // Seleccionar el div con la clase específica proporcionada
        Element textCenterDiv = doc.selectFirst(div);
        if (textCenterDiv != null) {
            // Seleccionar el primer elemento <a> dentro del div objetivo
            Element anchor = textCenterDiv.selectFirst("a");
            if (anchor != null) {
                // Obtener el href del enlace
                String href = anchor.attr("href");
                System.out.println("URL del enlace: " + href);
                return href;
            } else {
                System.out.println("No se encontró un enlace dentro del div con la clase 'woocommerce-product-gallery__wrapper'.");
            }
        } else {
            System.out.println("El div con la clase 'woocommerce-product-gallery__wrapper' no se encontró.");
        }
        return "";
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author agr12
 */
public class Pagina {

    private String url_page;
    private String uuid_page;
    private String uuid_categoria;
    private String uuid_log_lectura;

    @Override
    public String toString() {
        return "Pagina{" + "url_page=" + url_page + ", uuid_page=" + uuid_page + ", uuid_categoria=" + uuid_categoria + ", uuid_log_lectura=" + uuid_log_lectura + '}';
    }

    public String getUrl_page() {
        return url_page;
    }

    public void setUrl_page(String url_page) {
        this.url_page = url_page;
    }

    public String getUuid_page() {
        return uuid_page;
    }

    public void setUuid_page(String uuid_page) {
        this.uuid_page = uuid_page;
    }

    public String getUuid_categoria() {
        return uuid_categoria;
    }

    public void setUuid_categoria(String uuid_categoria) {
        this.uuid_categoria = uuid_categoria;
    }

    public String getUuid_log_lectura() {
        return uuid_log_lectura;
    }

    public void setUuid_log_lectura(String uuid_log_lectura) {
        this.uuid_log_lectura = uuid_log_lectura;
    }

}


/*Este programa permite exportar datos desde MS Access a SQLite.
Para conectarnos a la base de datos en access usamos la librería UCanAccess. Esta
librería crea una copia de la base en la memoria. Dependiendo del tamaño de la 
base de datos y la capacidad de cálculo del equipo, puede demorarse bastante.
*/

package javaapplication3;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Usuario
 */
public class Connect {
     /**
     * Connect to a sample database
     */
    public static void main(String[] args) {
         
        try {
            //Nos conectamos a la base de Access usando la librería UCanAccess
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            String ruta = "Ruta de base de datos";//En caso de que el programa corra en Linux, debemos agregar otra barra más(/)
            String dbURL = "jdbc:ucanaccess://" + ruta;
            
            //conn será la conección a la base de Access
            Connection conn = DriverManager.getConnection(dbURL);
            Statement sentencia = conn.createStatement();
            
            //Otenemos los nombres de las tablas de la base de datos
            String consulta = "SELECT Name FROM MSysObjects "
            + "WHERE (Left([Name],4) <> 'MSys') AND [Type] = 1 ";
            //Guardamos los nombres en rs
            ResultSet rs=sentencia.executeQuery(consulta);

            String cons2;
            ResultSet rs2;
            ResultSetMetaData rsmd;
            PreparedStatement stmt;
            int columnas;
            int colCount;
            String sql;//Sentencia para crear tablas
            String sql2;//Valores a agregar a la tabla.
            String sql3;//Sentencia para agregar los datos a las tablas
            Connection conexion;//Esta será la conexión a la base SQLite. 
            String tipo;//Tipo de datos de la columna a crear en SQLite.

            Class.forName("org.sqlite.JDBC");
            conexion=DriverManager.getConnection ("jdbc:sqlite:Nueva base de datos");

            //Empezamos a recorrer rs
            while (rs.next()){
                //Obtenemos el nombre la tabla que estamos iterando
                cons2 = rs.getString("name");
                //Seleccionamos todos los registros de la tabla
                rs2 = sentencia.executeQuery("select * from ["+cons2 + "]");
                //Obtenemos los metadatos y los almacenamos en rsmd
                rsmd = rs2.getMetaData();
                //Obtenemos la cantidad de columnas que posee cada tabla
                columnas = rsmd.getColumnCount();
                
                //Reiniciamos las variables 
                colCount = 1;
                sql = "";
                sql3 = "";
                
                //Recorremos cada columna de la tabla obteniendo datos
                while (colCount <= columnas){
                    //Si no es la primera columna, agregamos una coma separar 
                    //los nombres de columna
                    if (colCount != 1){
                        sql = sql + " , ";
                        sql3 = sql3 + " , ";
                    }
                    //Para una mejor usabilidad de SQlite, seleccionamos los 
                    //tipos de datos de las nuevas columnas
                    if (rsmd.getColumnTypeName(colCount)=="INTEGER"){
                        tipo = "INTEGER";
                    }else if(rsmd.getColumnTypeName(colCount)=="DOUBLE"){
                        tipo = "NUMERIC";                                
                    } else{
                        tipo = "TEXT";
                    }

                    sql = sql + "'" + rsmd.getColumnName(colCount) + "' "+
                            tipo ;
                    sql3 = sql3 + "'" + rsmd.getColumnName(colCount) + "'";
                    colCount = colCount +1;
                }
            
                //Creamos la tabla correspondiente
                try {
                    stmt = conexion.prepareStatement("create table '" + cons2 
                            + "' (" + sql + ")");
                    stmt.executeUpdate();
                    }catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                
                //Recorremos los registros de cada tabla
                while (rs2.next()){
                    //Reiniciamos las variables
                    colCount = 1;
                    sql2 = "";
                    
                    while (colCount <= columnas){
                        if (colCount != 1){
                            sql2 = sql2 + " , ";
                        }
                        //Obtenemos los valores de cada campo del registro
                        sql2 = sql2 + "'"+ rs2.getString(colCount) + "'" ;
                        colCount = colCount +1;
                        }
                    
                    try {
                        //Agregamos los valores a la tabla iterada
                        stmt = conexion.prepareStatement("insert into '" + cons2
                          + "' (" + sql3 + ") values (" + sql2 + ");");
                        stmt.executeUpdate();
                        }catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                }//fin de iterar dentro de cada tabla
             
            } //fin de iterar tablas
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
        }

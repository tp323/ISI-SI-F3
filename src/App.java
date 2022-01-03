import java.sql.*;
import java.util.*;


public class App {

    private static final String URL = "jdbc:postgresql://10.62.73.22:5432/";
    private static final String username = "l3d43";
    private static final String password = "l3d43";
    private static Connection con = null;
    private static Statement stmt = null;
    private static PreparedStatement pstmt = null;
    private static ResultSet rs = null;
    public static Scanner input = new Scanner(System.in);


    public static void main(String[] args) throws SQLException {

        try {
            connect();
            stmt = con.createStatement();
            //connection test
            rs = stmt.executeQuery("select nome, modelo, marca, localizacao from ACTIVO where estado = '1';\n");
            printTable(rs,4);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private static void checkRestrictions() throws SQLException{
        correctDBerrors();
        //todas as equipas têm no mínimo 2 pessoas
        queries.checkEquipasMin2Elements();
        //VCOMERCIAL.dtvcomercial de VCOMERCIAL >= ACTIVO.dtaquisicao
        queries.checkRestrictionVcomercial();
        //Se INTERVENCAO.valcusto > valor comercial do activo,
        // ACTIVO.estado = “0”, INTERVENCAO.estado = “concluído” e INTERVENCAO.dtfim = data actual
        queries.checkRestrictionIntervencao();
        //Se INTERVENCAO.dtfim é não nulo, estado = “concluído”;
        queries.checkRestrictionIntervencaoDtFim();
        //os activos “filhos” são do mesmo tipo que o activo “pai”;
        queries.checkRestrictionActivohierarchy();
        //a pessoa que gere um activo não faz a manutenção desse activo.
        queries.checkRestrictionConflictGestaoMan();
    }

    private static void correctDBerrors() throws SQLException{

    }

    private static void connect() throws SQLException {
        try {
            con = DriverManager.getConnection(URL, username, password);
        } catch (SQLException sqlex) {
            System.out.println("Erro : " + sqlex.getMessage());
        }
    }

    public static int printTable(ResultSet rs, int columnsNumber) throws SQLException {
        int numRows = 1;
        while (rs.next()) {
            for(int i = 1 ; i <= columnsNumber; i++){
                if(i==1) System.out.print(numRows + " >     ");
                System.out.print(rs.getString(i) + "      "); //Print one element of a row
            }System.out.println();//Move to the next line to print the next row.
            numRows++;
        }
        if(numRows == 1) System.out.println("Não existem valores para a interrogação feitas");
        return numRows-1;
    }

    private static void closeConnection() throws SQLException {
        try {
            // free the resources of the ResultSet
            if (rs != null) rs.close();
            // free the resources of the Statement
            if (stmt != null) stmt.close();
            // close connection
            if (con != null) con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

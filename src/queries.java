import java.sql.*;

public class queries {
    private static final String URL = "jdbc:postgresql://10.62.73.22:5432/";
    private static final String username = "l3d43";
    private static final String password = "l3d43";
    private static Connection con = null;
    private static Statement stmt = null;
    private static PreparedStatement pstmt = null;
    private static ResultSet rs = null;


    private static void connect() throws SQLException {
        try {
            con = DriverManager.getConnection(URL, username, password);
        } catch (SQLException sqlex) {
            System.out.println("Erro : " + sqlex.getMessage());
        }
    }

    private static void closeConnection() throws SQLException {
        try {
            // free the resources of the ResultSet
            if (rs != null) rs.close();
            // free the resources of the Statement
            if (stmt != null) stmt.close();
            // close connection
            if (con != null) con.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    public static void exampleTest() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select nome, modelo, marca, localizacao from ACTIVO where estado = '1';");
            App.printTable(rs,4);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public static void checkRestrictionActivohierarchy() {

    }

    public static void checkRestrictionIntervencaoDtFim() {

    }

    public static void checkRestrictionIntervencao() {

    }

    public static void checkRestrictionVcomercial() {

    }

    public static void checkEquipasMin2Elements() {
    }

    public static void checkRestrictionConflictGestaoMan() {

    }

    public static void novoActivo() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            pstmt = con.prepareStatement("INSERT INTO ACTIVO (id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            rs = stmt.executeQuery("INSERT INTO ACTIVO(id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa)\n" +
                    "VALUES ('a0001','cena1','1','2021-02-02',NULL,NULL,'ali','a0001',3,1,2),");
            pstmt.setInt(1, maxIdActivo()+1);    //ident reserva

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private static int maxIdActivo() throws SQLException {
        int maxInt = -1;
        stmt = con.createStatement();
        rs = stmt.executeQuery("SELECT MAX(id) FROM ACTIVO");
        rs.next();
        maxInt = rs.getInt(1);
        return maxInt;
    }

    public static void substituirElem() {

    }

    public static void activoForaServico() {

    }

    public static void custoTotalActivo() {

    }

    public static void querrie2d() {

    }

    public static void querrie2e() {

    }

    public static void querrie3c() {

    }

    public static void querrie3d() {

    }



}
